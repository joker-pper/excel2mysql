package com.joker17.excel2mysql.automode.impl;

import com.joker17.excel2mysql.automode.AbstractTableAutoModeSupport;
import com.joker17.excel2mysql.automode.TableAutoModeSupportOptions;
import com.joker17.excel2mysql.constants.Excel2MysqlConstants;
import com.joker17.excel2mysql.db.MysqlUtils;
import com.joker17.excel2mysql.enums.AutoModeEnum;
import com.joker17.excel2mysql.enums.ColumnKeyTypeEnum;
import com.joker17.excel2mysql.helper.Excel2MysqlHelper;
import com.joker17.excel2mysql.helper.MysqlBoostHelper;
import com.joker17.excel2mysql.model.Excel2MysqlModel;
import com.joker17.excel2mysql.model.MysqlColumnModel;
import com.joker17.excel2mysql.utils.StringUtils;
import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 更新模式支持 -> 未存在的表进行创建,已存在的表进行更新字段(新字段将进行插入,指定最后不存在的字段将进行删除,指定最后存在的字段进行变更)
 * index key处理: uk + pk(只考虑新增,不做额外移除等处理-除pk外,但不推荐使用)
 */
public class TableUpdateAutoModeSupport extends AbstractTableAutoModeSupport {

    public static final TableUpdateAutoModeSupport INSTANCE = new TableUpdateAutoModeSupport();

    protected final static Logger MAIN_LOG = Excel2MysqlConstants.LOG;

    private TableUpdateAutoModeSupport() {
        super();
    }

    @Override
    public AutoModeEnum autoMode() {
        return AutoModeEnum.UPDATE;
    }

    @Override
    public void execute(JdbcTemplate jdbcTemplate, TableAutoModeSupportOptions options) {
        String database = options.getDatabase();
        String tableName = options.getTableName();
        String engine = options.getEngine();
        boolean tableExists = Boolean.TRUE.equals(options.getTableExists());
        List<Excel2MysqlModel> tableExcel2MysqlModelList = options.getTableExcel2MysqlModelList();

        if (!tableExists) {
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
