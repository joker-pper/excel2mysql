package com.joker17.excel2mysql.core;

import com.joker17.excel2mysql.constants.Excel2MysqlConstants;
import com.joker17.excel2mysql.db.DataSourceUtils;
import com.joker17.excel2mysql.db.JdbcUtils;
import com.joker17.excel2mysql.enums.AutoModeEnum;
import com.joker17.excel2mysql.enums.ColumnKeyTypeEnum;
import com.joker17.excel2mysql.helper.MysqlBoostHelper;
import com.joker17.excel2mysql.model.Excel2MysqlModel;
import com.joker17.excel2mysql.model.MysqlColumnModel;
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
import java.util.function.Function;
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
        if (database == null) {
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

        //获取当前已存在的表
        List<String> existsTableNameList = MysqlUtils.getTableNameList(jdbcTemplate);
        tableExcel2MysqlModelListMap.forEach((tableName, tableExcel2MysqlModelList) -> {
            boolean isMatchResolve = MysqlBoostHelper.isMatchResolveTableName(filterTables, tableName, dumpParam.isExcludeTableMode());
            if (isMatchResolve) {
                //匹配时进行执行操作
                boolean exists = existsTableNameList.contains(tableName);
                switch (autoModeEnum) {
                    case CREATE:
                        createAutoMode(jdbcTemplate, database, tableName, engine, exists, tableExcel2MysqlModelList);
                        break;
                    case UPDATE:
                        updateAutoMode(jdbcTemplate, database, tableName, engine, exists, tableExcel2MysqlModelList);
                        break;
                    default:
                        throw new RuntimeException("not support auto mode: " + autoModeEnum.getName());
                }

            }
        });
    }

    /**
     * 创建模式 -> 已存在的表忽略
     *
     * @param jdbcTemplate
     * @param database
     * @param tableName
     * @param engine
     * @param exists
     * @param tableExcel2MysqlModelList
     */
    private void createAutoMode(JdbcTemplate jdbcTemplate, String database, String tableName, String engine, boolean exists,
                                List<Excel2MysqlModel> tableExcel2MysqlModelList) {
        if (!exists) {
            //不存在表时获取创表sql
            String createSql = Excel2MysqlHelper.getCreateSql(tableName, engine, tableExcel2MysqlModelList);

            MAIN_LOG.info("database {} create table `{}` start", database, tableName);
            MAIN_LOG.info("database {} create table `{}` sql - {}", database, tableName, createSql);

            //执行sql
            MysqlUtils.execute(jdbcTemplate, createSql);

            MAIN_LOG.info("database {} create table `{}` end", database, tableName);

        } else {
            MAIN_LOG.warn("database {} has already exist table `{}` ...", database, tableName);
        }
    }


    /**
     * 更新模式 -> 未存在的表进行创建,已存在的表进行更新字段(新字段将进行插入,指定最后不存在的字段将进行删除,指定最后存在的字段进行变更)
     * index key处理: uk + pk(只考虑新增,不做额外移除等处理-除pk外,但不推荐使用)
     *
     * @param jdbcTemplate
     * @param database
     * @param tableName
     * @param engine
     * @param exists
     * @param tableExcel2MysqlModelList
     */
    private void updateAutoMode(JdbcTemplate jdbcTemplate, String database, String tableName, String engine, boolean exists,
                                List<Excel2MysqlModel> tableExcel2MysqlModelList) {
        if (!exists) {
            //不存在表时获取创表sql
            String createSql = Excel2MysqlHelper.getCreateSql(tableName, engine, tableExcel2MysqlModelList);

            MAIN_LOG.info("database {} create table `{}` start", database, tableName);
            MAIN_LOG.info("database {} create table `{}` sql - {}", database, tableName, createSql);

            //执行sql
            MysqlUtils.execute(jdbcTemplate, createSql);
            MAIN_LOG.info("database {} create table `{}` end", database, tableName);
        } else {
            //获取当前表的列信息
            List<MysqlColumnModel> mysqlColumnModelList = Excel2MysqlHelper.getMysqlColumnModelList(jdbcTemplate, database, tableName);

            Map<String, MysqlColumnModel> beforeColumnName2MysqlColumnModelMap = mysqlColumnModelList.stream().collect(Collectors.toMap(MysqlColumnModel::getColumnName, Function.identity()));
            Map<String, Excel2MysqlModel> beforeColumnName2Excel2MysqlModelMap = new HashMap<>(16);
            Map<Excel2MysqlModel, MysqlColumnModel> finalPrimaryKeyColumnModel2MysqlColumnModelMap = new LinkedHashMap<>(4);

            List<String> newUniqueKeyList = new ArrayList<>(8);
            List<String> dropColumnNames = new ArrayList<>(8);
            List<String> addColumnNameSqlList = new ArrayList<>(16);
            List<String> modifyColumnNameSqlList = new ArrayList<>(16);
            List<String> changeColumnNameSqlList = new ArrayList<>(16);
            List<String> lastExecuteSqlList = new ArrayList<>(32);

            tableExcel2MysqlModelList.forEach(tableExcel2MysqlModel -> {
                //获取之前的字段名称
                String beforeColumnName = Excel2MysqlHelper.getBeforeColumnName(tableExcel2MysqlModel.getColumnName());
                //获取最终的字段名称
                String finalColumnName = Excel2MysqlHelper.getFinalColumnName(tableExcel2MysqlModel.getColumnName());

                if (beforeColumnName == null && finalColumnName == null) {
                    throw new IllegalArgumentException(String.format("update table `%s` has error config: before and after column all null.", tableName));
                }

                MysqlColumnModel mysqlColumnModel = beforeColumnName2MysqlColumnModelMap.get(beforeColumnName);

                if (beforeColumnName != null) {
                    //检查column name必须存在
                    if (mysqlColumnModel == null) {
                        throw new IllegalArgumentException(String.format("update table `%s` has error: table not exist column `%s`.", tableName, beforeColumnName));
                    }
                    //记录之前列对应的当前列配置
                    beforeColumnName2Excel2MysqlModelMap.put(beforeColumnName, tableExcel2MysqlModel);
                }

                if (finalColumnName == null) {
                    //最后column不存在则为删除
                    dropColumnNames.add(beforeColumnName);
                } else {
                    ColumnKeyTypeEnum columnKeyTypeEnum = MysqlBoostHelper.getColumnKeyType(tableExcel2MysqlModel.getColumnKeyType());
                    ColumnKeyTypeEnum beforeColumnKeyTypeEnum = mysqlColumnModel == null ? null : MysqlBoostHelper.getColumnKeyType(mysqlColumnModel.getColumnKeyType());
                    switch (columnKeyTypeEnum) {
                        case UNIQUE_KEY:
                            if (beforeColumnKeyTypeEnum != ColumnKeyTypeEnum.UNIQUE_KEY && beforeColumnKeyTypeEnum != ColumnKeyTypeEnum.PRIMARY_AND_UNIQUE_KEY) {
                                newUniqueKeyList.add(finalColumnName);
                            }
                            break;
                        default:
                            //其他类型忽略..
                            break;
                    }

                    if (columnKeyTypeEnum == ColumnKeyTypeEnum.PRIMARY_KEY || columnKeyTypeEnum == ColumnKeyTypeEnum.PRIMARY_KEY) {
                        //为PK列时(后面进行处理)
                        finalPrimaryKeyColumnModel2MysqlColumnModelMap.put(tableExcel2MysqlModel, mysqlColumnModel);
                        return;
                    }

                    if (beforeColumnName == null) {
                        //插入新字段时验证该字段不存在
                        if (beforeColumnName2MysqlColumnModelMap.get(finalColumnName) != null) {
                            throw new IllegalArgumentException(String.format("update table `%s` has error config: table has already exist column `%s`.", tableName, finalColumnName));
                        }
                        addColumnNameSqlList.add(Excel2MysqlHelper.getTableAddColumnNameSql(tableName, Excel2MysqlHelper.getColumnDefinition(finalColumnName, tableExcel2MysqlModel)));
                    } else {
                        //字段更新时
                        if (StringUtils.equals(beforeColumnName, finalColumnName)) {
                            if (Excel2MysqlHelper.isColumnDefinitionChanged(tableExcel2MysqlModel, mysqlColumnModel)) {
                                modifyColumnNameSqlList.add(Excel2MysqlHelper.getTableModifyColumnNameSql(tableName, Excel2MysqlHelper.getColumnDefinition(finalColumnName, tableExcel2MysqlModel)));
                            }
                        } else {
                            changeColumnNameSqlList.add(Excel2MysqlHelper.getTableChangeColumnNameSql(tableName, beforeColumnName, Excel2MysqlHelper.getColumnDefinition(finalColumnName, tableExcel2MysqlModel)));
                        }
                    }
                }
            });

            //处理pk
            lastExecuteSqlList.addAll(getUpdateAutoModePrimaryKeySqlList(tableName, dropColumnNames, mysqlColumnModelList, finalPrimaryKeyColumnModel2MysqlColumnModelMap, beforeColumnName2Excel2MysqlModelMap));

            //处理唯一key索引
            if (!newUniqueKeyList.isEmpty()) {
                newUniqueKeyList.forEach(uniqueKey -> lastExecuteSqlList.add(Excel2MysqlHelper.getTableAddUniqueColumnIndexSql(tableName, uniqueKey)));
            }

            List<String> toExecuteSqlList = new ArrayList<>(64);
            //删除table指定列
            if (!dropColumnNames.isEmpty()) {
                String dropColumnNamesSql = Excel2MysqlHelper.getTableDropColumnNamesSql(tableName, dropColumnNames);
                toExecuteSqlList.add(dropColumnNamesSql);
            }

            toExecuteSqlList.addAll(addColumnNameSqlList);
            toExecuteSqlList.addAll(modifyColumnNameSqlList);
            toExecuteSqlList.addAll(changeColumnNameSqlList);
            toExecuteSqlList.addAll(lastExecuteSqlList);

            if (toExecuteSqlList.isEmpty()) {
                MAIN_LOG.info("database {} update table `{}` not changed.", database, tableName);
            } else {
                MAIN_LOG.info("database {} update table `{}` start", database, tableName);
                //执行sql
                toExecuteSqlList.forEach(it -> {
                    String toExecuteSqlSql = MysqlBoostHelper.getPrettifySql(it);
                    MAIN_LOG.info("database {} update table `{}` sql - {}", database, tableName, toExecuteSqlSql);
                    MysqlUtils.execute(jdbcTemplate, toExecuteSqlSql);
                });
                MAIN_LOG.info("database {} update table `{}` end", database, tableName);
            }


        }
    }

    /**
     * 获取处理pk的sql列表
     *
     * @param tableName
     * @param dropColumnNames
     * @param mysqlColumnModelList                           之前列的信息
     * @param finalPrimaryKeyColumnModel2MysqlColumnModelMap 最终PK列所对应之前列的信息Map
     * @param beforeColumnName2Excel2MysqlModelMap
     * @return
     */
    private List<String> getUpdateAutoModePrimaryKeySqlList(String tableName, List<String> dropColumnNames, List<MysqlColumnModel> mysqlColumnModelList, Map<Excel2MysqlModel, MysqlColumnModel> finalPrimaryKeyColumnModel2MysqlColumnModelMap, Map<String, Excel2MysqlModel> beforeColumnName2Excel2MysqlModelMap) {
        if (finalPrimaryKeyColumnModel2MysqlColumnModelMap.isEmpty()) {
            //不存在时PK变更的配置时忽略
            return Collections.emptyList();
        }

        //处理pk

        //获取之前的主键列的列表
        List<MysqlColumnModel> beforePrimaryKeyMysqlColumnModelList = mysqlColumnModelList.stream().filter(mysqlColumnModel -> {
            ColumnKeyTypeEnum columnKeyTypeEnum = MysqlBoostHelper.getColumnKeyType(mysqlColumnModel.getColumnKeyType());
            return columnKeyTypeEnum == ColumnKeyTypeEnum.PRIMARY_KEY || columnKeyTypeEnum == ColumnKeyTypeEnum.PRIMARY_AND_UNIQUE_KEY;
        }).collect(Collectors.toList());

        //若要处理PK时 检查之前的主键列必须存在于当前要更新列的配置中
        beforePrimaryKeyMysqlColumnModelList.forEach(mysqlColumnModel -> {
            String columnName = mysqlColumnModel.getColumnName();
            if (beforeColumnName2Excel2MysqlModelMap.get(columnName) == null) {
                throw new IllegalArgumentException(String.format("table `%s` has error config: not found primary key `%s` config in excel.", tableName, columnName));
            }
        });

        List<String> sqlList = new ArrayList<>(16);
        List<String> primaryKeyList = new ArrayList<>(4);
        Set<String> changedAutoIncrementColumns = new HashSet<>(4);
        Set<String> changedPkColumns = new HashSet<>(4);

        List<String> beforeSqlList = new ArrayList<>(8);
        List<String> afterSqlList = new ArrayList<>(8);

        finalPrimaryKeyColumnModel2MysqlColumnModelMap.forEach((primaryKeyExcel2MysqlModel, mysqlColumnModel) -> {
            //获取最终的字段名称
            String finalColumnName = Excel2MysqlHelper.getFinalColumnName(primaryKeyExcel2MysqlModel.getColumnName());
            //获取当前字段定义的描述
            String columnDefinition = Excel2MysqlHelper.getColumnDefinition(finalColumnName, primaryKeyExcel2MysqlModel);

            primaryKeyList.add(finalColumnName);

            boolean hasAutoIncrement = MysqlBoostHelper.hasAutoIncrement(columnDefinition);

            String beforeSql;
            String afterSql;

            if (mysqlColumnModel == null) {
                //新增字段时
                if (hasAutoIncrement) {
                    beforeSql = Excel2MysqlHelper.getTableAddColumnNameSql(tableName, MysqlBoostHelper.getRemovedAutoIncrementSql(columnDefinition));
                    afterSql = Excel2MysqlHelper.getTableModifyColumnNameSql(tableName, columnDefinition);
                    changedAutoIncrementColumns.add(finalColumnName);
                } else {
                    beforeSql = Excel2MysqlHelper.getTableAddColumnNameSql(tableName, columnDefinition);
                    afterSql = null;
                }
                changedPkColumns.add(finalColumnName);
            } else {
                //字段更新时

                //获取之前的字段名称
                String beforeColumnName = mysqlColumnModel.getColumnName();
                String beforeColumnDefinition = Excel2MysqlHelper.getColumnDefinition(beforeColumnName, mysqlColumnModel);
                boolean beforeHasAutoIncrement = MysqlBoostHelper.hasAutoIncrement(beforeColumnDefinition);
                boolean isColumnDefinitionChanged = Excel2MysqlHelper.isColumnDefinitionChanged(primaryKeyExcel2MysqlModel, mysqlColumnModel);
                ColumnKeyTypeEnum beforeColumnKeyTypeEnum = MysqlBoostHelper.getColumnKeyType(mysqlColumnModel.getColumnKeyType());
                if (ColumnKeyTypeEnum.PRIMARY_KEY != beforeColumnKeyTypeEnum && ColumnKeyTypeEnum.PRIMARY_AND_UNIQUE_KEY != beforeColumnKeyTypeEnum) {
                    //当前列之前不是PK时
                    changedPkColumns.add(finalColumnName);
                }

                if (StringUtils.equals(beforeColumnName, finalColumnName)) {
                    //列名未变更时
                    if (isColumnDefinitionChanged) {
                        //列发生变化时
                        if (hasAutoIncrement) {
                            if (beforeHasAutoIncrement) {
                                //之前也是自增列
                                beforeSql = Excel2MysqlHelper.getTableModifyColumnNameSql(tableName, columnDefinition);
                                afterSql = null;
                            } else {
                                beforeSql = Excel2MysqlHelper.getTableModifyColumnNameSql(tableName, MysqlBoostHelper.getRemovedAutoIncrementSql(columnDefinition));
                                afterSql = Excel2MysqlHelper.getTableModifyColumnNameSql(tableName, columnDefinition);
                                changedAutoIncrementColumns.add(finalColumnName);
                            }
                        } else {
                            //非自增列时
                            beforeSql = Excel2MysqlHelper.getTableModifyColumnNameSql(tableName, columnDefinition);
                            afterSql = null;
                        }
                    } else {
                        //列未发生变化时
                        beforeSql = null;
                        afterSql = null;
                    }
                } else {
                    //列名变更时
                    if (hasAutoIncrement) {
                        if (beforeHasAutoIncrement) {
                            //之前也是自增列
                            beforeSql = Excel2MysqlHelper.getTableChangeColumnNameSql(tableName, beforeColumnName, columnDefinition);
                            afterSql = null;
                        } else {
                            beforeSql = Excel2MysqlHelper.getTableChangeColumnNameSql(tableName, beforeColumnName, MysqlBoostHelper.getRemovedAutoIncrementSql(columnDefinition));
                            afterSql = Excel2MysqlHelper.getTableModifyColumnNameSql(tableName, columnDefinition);
                            changedAutoIncrementColumns.add(finalColumnName);
                        }
                    } else {
                        //非自增列时
                        beforeSql = Excel2MysqlHelper.getTableChangeColumnNameSql(tableName, beforeColumnName, columnDefinition);
                        afterSql = null;
                    }
                }
            }

            if (beforeSql != null) {
                beforeSqlList.add(beforeSql);
            }

            if (afterSql != null) {
                afterSqlList.add(afterSql);
            }

        });

        //添加pk索引sql
        String addPrimaryKeyIndexSql = Excel2MysqlHelper.getTableAddPrimaryKeyIndexSql(tableName, primaryKeyList);

        if (beforePrimaryKeyMysqlColumnModelList.isEmpty() || dropColumnNames.containsAll(beforePrimaryKeyMysqlColumnModelList.stream().map(MysqlColumnModel::getColumnName).collect(Collectors.toList()))) {
            //之前不存在pk索引 / 之前pk列全被移除时

            //先处理列变更
            sqlList.addAll(beforeSqlList);

            //进行添加pk索引
            sqlList.add(addPrimaryKeyIndexSql);

            //再处理后续sql(e.g: 修改列为自增)
            sqlList.addAll(afterSqlList);


        } else {
            //先处理列变更
            sqlList.addAll(beforeSqlList);

            if (!changedAutoIncrementColumns.isEmpty() || primaryKeyList.size() != beforePrimaryKeyMysqlColumnModelList.size() || !changedPkColumns.isEmpty()) {
                //进行删除之前的pk索引 (自增列变更 / 主键列个数变更 / 主键列新增、更换)
                sqlList.add(Excel2MysqlHelper.getTableDropPrimaryKeyIndexSql(tableName));

                //进行重新添加pk索引
                sqlList.add(addPrimaryKeyIndexSql);
            }

            //再处理后续sql(若存在修改为自增等)
            sqlList.addAll(afterSqlList);

        }
        return sqlList;
    }

}
