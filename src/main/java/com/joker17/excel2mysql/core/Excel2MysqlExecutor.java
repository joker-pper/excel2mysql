package com.joker17.excel2mysql.core;

import com.joker17.excel2mysql.automode.AbstractTableAutoModeSupport;
import com.joker17.excel2mysql.automode.TableAutoModeSupportFactory;
import com.joker17.excel2mysql.automode.TableAutoModeSupportOptions;
import com.joker17.excel2mysql.constants.Excel2MysqlConstants;
import com.joker17.excel2mysql.db.DataSourceUtils;
import com.joker17.excel2mysql.db.JdbcUtils;
import com.joker17.excel2mysql.enums.AutoModeEnum;
import com.joker17.excel2mysql.helper.MysqlBoostHelper;
import com.joker17.excel2mysql.model.Excel2MysqlModel;
import com.joker17.excel2mysql.param.Excel2MysqlDumpParam;
import com.joker17.excel2mysql.helper.Excel2MysqlHelper;
import com.joker17.excel2mysql.utils.FileUtils;
import com.joker17.excel2mysql.db.MysqlUtils;
import com.joker17.excel2mysql.utils.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Excel2MysqlExecutor extends AbstractExcel2MysqlExecutor {

    public static final Excel2MysqlExecutor INSTANCE = new Excel2MysqlExecutor();


    @Override
    protected void doWork(Excel2MysqlDumpParam dumpParam) throws IOException {
        Objects.requireNonNull(dumpParam, "param must be not null");
        String dataSourceProperties = dumpParam.getDataSourceProperties();
        Objects.requireNonNull(dataSourceProperties, "data source properties must be not null");

        File dataSourcePropertiesFile = new File(dataSourceProperties);
        if (!FileUtils.isFileAndExists(dataSourcePropertiesFile)) {
            throw new FileNotFoundException(String.format("data source properties %s not found", dataSourceProperties));
        }

        String inFilePath = dumpParam.getInFilePath();

        String fileName = dumpParam.getFileName();
        Objects.requireNonNull(fileName, "file name must be not null");

        String excelType = Excel2MysqlHelper.getExcelType(dumpParam.getExcelType());
        if (!Arrays.asList(Excel2MysqlConstants.XLS, Excel2MysqlConstants.XLSX).contains(excelType)) {
            throw new IllegalArgumentException("invalid excel type");
        }

        File inExcelFile = FileUtils.getInExcelFile(inFilePath, fileName, excelType);
        if (!FileUtils.isFileAndExists(inExcelFile)) {
            throw new FileNotFoundException(String.format("in excel file %s not found", inExcelFile.getPath()));
        }

        //获取筛选的table配置
        String filterTable = dumpParam.getFilterTable();
        Set<String> filterTables = new HashSet<>(Arrays.asList(filterTable == null || filterTable.length() == 0 ? new String[]{} : filterTable.split(" ")));

        boolean isCheckTableSchema = dumpParam.isCheckTableSchema();

        AutoModeEnum autoModeEnum;
        if (StringUtils.isEmpty(dumpParam.getAutoMode())) {
            autoModeEnum = AutoModeEnum.CREATE;
        } else {
            autoModeEnum = AutoModeEnum.getInstance(dumpParam.getAutoMode());
        }
        if (autoModeEnum == null) {
            throw new IllegalArgumentException("invalid auto mode");
        }

        String engine = StringUtils.defaultIfEmpty(StringUtils.trimToNull(dumpParam.getEngine()), Excel2MysqlConstants.INNODB_ENGINE);

        //解析excel数据
        List<Excel2MysqlModel> excel2MysqlModelList = Excel2MysqlHelper.getExcel2MysqlModelList(inExcelFile, excelType);
        if (excel2MysqlModelList == null || excel2MysqlModelList.isEmpty()) {
            MAIN_LOG.warn("read excel empty data list, return ...");
            return;
        }

        //获取data source
        DataSource dataSource = DataSourceUtils.getDataSourceByCache(dataSourcePropertiesFile);
        JdbcTemplate jdbcTemplate = JdbcUtils.getJdbcTemplate(dataSource);

        //获取当前数据库名称
        String database = MysqlUtils.getDatabase(jdbcTemplate);
        if (StringUtils.isEmpty(database)) {
            throw new IllegalArgumentException("invalid database");
        }

        //获取按table分组的数据列表Map
        Set<String> notSupportTableSchemas = new HashSet<>(8);
        Map<String, List<Excel2MysqlModel>> tableExcel2MysqlModelListMap = excel2MysqlModelList.stream().filter(it -> {
            if (!isCheckTableSchema) {
                //不检查时
                return true;
            }
            boolean same = Objects.equals(it.getTableSchema(), database);
            if (!same) {
                notSupportTableSchemas.add(it.getTableSchema());
            }
            return same;
        }).collect(Collectors.groupingBy(Excel2MysqlModel::getTableName, LinkedHashMap::new, Collectors.toList()));

        if (!notSupportTableSchemas.isEmpty()) {
            //不支持的数据库
            notSupportTableSchemas.forEach(notSupportTableSchema -> {
                MAIN_LOG.warn("database {} check table schema continue {} ...", database, notSupportTableSchema);
            });
        }

        if (tableExcel2MysqlModelListMap.isEmpty()) {
            //分组后的数据为空时
            MAIN_LOG.warn("database {} check table schema continue all, return ...", database);
            return;
        }

        //获取tableAutoModeSupport
        AbstractTableAutoModeSupport tableAutoModeSupport = TableAutoModeSupportFactory.getTableAutoModeSupport(autoModeEnum);

        //获取当前已存在的表
        List<String> existsTableNameList = MysqlUtils.getTableNameList(jdbcTemplate);
        tableExcel2MysqlModelListMap.forEach((tableName, tableExcel2MysqlModelList) -> {
            boolean isMatchResolve = MysqlBoostHelper.isMatchResolveTableName(filterTables, tableName, dumpParam.isExcludeTableMode());
            if (isMatchResolve) {
                //匹配时进行执行操作
                boolean tableExists = existsTableNameList.contains(tableName);
                TableAutoModeSupportOptions autoModeOptions = TableAutoModeSupportOptions.builder().database(database).tableName(tableName).engine(engine).tableExists(tableExists)
                        .tableExcel2MysqlModelList(tableExcel2MysqlModelList).build();
                tableAutoModeSupport.execute(jdbcTemplate, autoModeOptions);
            }
        });
    }
}
