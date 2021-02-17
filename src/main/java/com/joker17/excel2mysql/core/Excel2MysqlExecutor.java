package com.joker17.excel2mysql.core;

import com.alibaba.excel.EasyExcel;
import com.joker17.excel2mysql.constants.Excel2MysqlConstants;
import com.joker17.excel2mysql.db.JdbcUtils;
import com.joker17.excel2mysql.model.Excel2MysqlModel;
import com.joker17.excel2mysql.param.Excel2MysqlDumpParam;
import com.joker17.excel2mysql.helper.Excel2MysqlHelper;
import com.joker17.excel2mysql.utils.FileUtils;
import com.joker17.excel2mysql.db.MysqlUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
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
            throw new FileNotFoundException(String.format("in excel file %s not found", dataSourceProperties));
        }

        //获取筛选的table配置
        String filterTable = dumpParam.getFilterTable();
        Set<String> filterTables = new HashSet<>(Arrays.asList(filterTable == null || filterTable.length() == 0 ? new String[]{} : filterTable.split(" ")));

        boolean isCheckTableSchema = dumpParam.isCheckTableSchema();


        //解析excel数据
        List<Excel2MysqlModel> excel2MysqlModelList = EasyExcel.read(inExcelFile).head(Excel2MysqlModel.class).sheet().doReadSync();
        if (excel2MysqlModelList == null || excel2MysqlModelList.isEmpty()) {
            Excel2MysqlConstants.LOG.warn("read excel empty data list, return ...");
            return;
        }

        Properties properties = JdbcUtils.loadProperties(new FileInputStream(dataSourcePropertiesFile));
        DataSource dataSource = JdbcUtils.getDataSource(properties);
        JdbcTemplate jdbcTemplate = JdbcUtils.getJdbcTemplate(dataSource);

        //获取数据库名称
        String database = MysqlUtils.getDatabase(jdbcTemplate);

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
        }).collect(Collectors.groupingBy(Excel2MysqlModel::getTableName));

        if (!notSupportTableSchemas.isEmpty()) {
            //不支持的数据库
            notSupportTableSchemas.forEach(notSupportTableSchema -> {
                Excel2MysqlConstants.LOG.warn("database {} check table schema continue {} ...", database, notSupportTableSchema);
            });
        }

        if (tableExcel2MysqlModelListMap.isEmpty()) {
            //分组后的数据为空时
            Excel2MysqlConstants.LOG.warn("database {} check table schema continue all, return ...", database);
            return;
        }

        //获取当前已存在的表
        List<String> existsTableNameList = MysqlUtils.getTableNameList(jdbcTemplate);
        tableExcel2MysqlModelListMap.forEach((tableName, tableExcel2MysqlModelList) -> {
            boolean isMatchResolve = Excel2MysqlHelper.isMatchResolveTableName(filterTables, tableName, dumpParam.isExcludeMode());
            if (isMatchResolve) {
                //匹配时进行执行操作
                boolean exists = existsTableNameList.contains(tableName);
                if (!exists) {
                    //不存在表时获取创表sql
                    String createSql = Excel2MysqlHelper.getCreateSql(tableName, tableExcel2MysqlModelList);

                    Excel2MysqlConstants.LOG.info("database {} create table {} start", database, tableName);
                    Excel2MysqlConstants.LOG.info("database {} create table {} sql - {}", database, tableName, createSql);

                    //执行sql
                    MysqlUtils.execute(jdbcTemplate, createSql);

                    Excel2MysqlConstants.LOG.info("database {} create table {} end", database, tableName);

                } else {
                    Excel2MysqlConstants.LOG.warn("database {} has already exist table {} ...", database, tableName);
                }
            }
        });
    }


}
