package com.joker17.excel2mysql.helper;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.joker17.excel2mysql.constants.Excel2MysqlConstants;
import com.joker17.excel2mysql.enums.ColumnKeyTypeEnum;
import com.joker17.excel2mysql.model.Excel2MysqlModel;
import com.joker17.excel2mysql.model.MysqlColumnModel;
import com.joker17.excel2mysql.utils.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Excel2MysqlHelper {

    private Excel2MysqlHelper() {
    }

    /**
     * 获取excelType
     *
     * @param excelType
     * @return
     */
    public static String getExcelType(String excelType) {
        excelType = StringUtils.trimToNull(excelType);
        if (excelType == null) {
            return Excel2MysqlConstants.XLS;
        }
        return StringUtils.toLowerCase(excelType);
    }

    /**
     * 获取excelTypeEnum
     *
     * @param excelType
     * @return
     */
    public static ExcelTypeEnum getExcelTypeEnum(String excelType) {
        return Objects.equals(Excel2MysqlConstants.XLS, excelType) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX;
    }


    /**
     * <p>
     * 获取最终的column name
     * </p>
     *
     * <pre>
     * remarks->(-)             ---> null       (表示列被移除)
     * (-)->remark              ---> remark     (表示列新增,即之前列不存在)
     * -->remark                ---> remark     (表示列新增,支持但不推荐,使用(-)更容易区分)
     * remarks->(remark)        ---> remark
     * (remarks)->(remark)      ---> remark
     * remarks->remark          ---> remark
     * remark                   ---> remark
     * </pre>
     *
     * @param columnName
     * @return
     */
    public static String getFinalColumnName(String columnName) {
        columnName = StringUtils.trimToNull(columnName);
        if (columnName == null) {
            return null;
        }

        String result;

        //获取右边列内容
        int startIndex = columnName.indexOf(Excel2MysqlConstants.COLUMN_OLD_TO_NEW_MAPPING_TAG);
        if (startIndex != -1) {
            result = StringUtils.trimToNull(columnName.substring(startIndex + Excel2MysqlConstants.COLUMN_OLD_TO_NEW_MAPPING_TAG.length()));
        } else {
            result = columnName;
        }

        //去除括号
        if (result != null && result.startsWith(Excel2MysqlConstants.LEFT_BRACKET_TAG) && result.endsWith(Excel2MysqlConstants.RIGHT_BRACKET_TAG)) {
            result = StringUtils.trimToNull(result.substring(1, result.length() - 1));
        }

        //判断是否为空tag
        if (result != null && result.equals(Excel2MysqlConstants.NULL_TAG)) {
            result = null;
        }

        return result;
    }


    /**
     * <p>
     * 获取之前的column name
     * </p>
     *
     * <pre>
     * remarks->(-)             ---> remarks (表示列被移除)
     * (-)->remark              ---> null    (表示列新增,即之前列不存在)
     * -->remark                ---> null    (表示列新增,支持但不推荐,使用(-)更容易区分)
     * remarks->(remark)        ---> remarks
     * (remarks)->(remark)      ---> remarks
     * remarks->remark          ---> remarks
     * remarks                  ---> remarks
     * </pre>
     *
     * @param columnName
     * @return
     */
    public static String getBeforeColumnName(String columnName) {
        columnName = StringUtils.trimToNull(columnName);
        if (columnName == null) {
            return null;
        }

        String result;

        //获取左边列内容
        int startIndex = columnName.indexOf(Excel2MysqlConstants.COLUMN_OLD_TO_NEW_MAPPING_TAG);
        if (startIndex != -1) {
            result = StringUtils.trimToNull(columnName.substring(0, startIndex));
        } else {
            result = columnName;
        }

        //去除括号
        if (result != null && result.startsWith(Excel2MysqlConstants.LEFT_BRACKET_TAG) && result.endsWith(Excel2MysqlConstants.RIGHT_BRACKET_TAG)) {
            result = StringUtils.trimToNull(result.substring(1, result.length() - 1));
        }

        //判断是否为空tag
        if (result != null && result.equals(Excel2MysqlConstants.NULL_TAG)) {
            result = null;
        }

        return result;
    }


    /**
     * 获取create sql
     *
     * <p>
     * CREATE TABLE `role` (
     * `id` bigint(20) NOT NULL AUTO_INCREMENT,
     * `create_date` datetime DEFAULT NULL,
     * `update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
     * `name` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
     * `remarks` varchar(512) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
     * PRIMARY KEY (`id`)
     * ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin
     * <p>
     *
     * @param tableName
     * @param engine
     * @param excel2MysqlModelList
     * @return
     */
    public static String getCreateSql(String tableName, String engine, List<Excel2MysqlModel> excel2MysqlModelList) {

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE `");
        sb.append(tableName);
        sb.append("` (\n");

        List<String> primaryKeyList = new ArrayList<>(4);
        List<String> uniqueKeyList = new ArrayList<>(8);

        excel2MysqlModelList.forEach(excel2MysqlModel -> {

            String columnName = getFinalColumnName(excel2MysqlModel.getColumnName());
            if (columnName == null) {
                throw new IllegalArgumentException(String.format("table `%s` build create sql has error: column exist empty.", tableName));
            }

            //拼接column字段定义内容, 列名 + 类型 + 值描述 + Extra + Comment
            sb.append(getColumnDefinition(columnName, excel2MysqlModel));

            sb.append(",\n");

            //解析key
            ColumnKeyTypeEnum columnKeyTypeEnum = MysqlBoostHelper.getColumnKeyType(excel2MysqlModel.getColumnKeyType());
            switch (columnKeyTypeEnum) {
                case PRIMARY_KEY:
                    primaryKeyList.add(columnName);
                    break;
                case UNIQUE_KEY:
                    uniqueKeyList.add(columnName);
                    break;
                case PRIMARY_AND_UNIQUE_KEY:
                    primaryKeyList.add(columnName);
                    uniqueKeyList.add(columnName);
                    break;
                case OTHER:
                default:
                    //其他类型忽略..
                    break;
            }

        });

        //处理 key (支持复合主键)
        if (!primaryKeyList.isEmpty()) {
            sb.append(String.format(" PRIMARY KEY (%s),\n", MysqlBoostHelper.getColumnFields(primaryKeyList)));
        }

        uniqueKeyList.forEach(uniqueKey -> sb.append(String.format(" UNIQUE KEY `%s` (`%s`) USING BTREE,\n", MysqlBoostHelper.getUniqueKeyName(uniqueKey), uniqueKey)));

        //删除多余 ,\n
        int len = sb.length();
        sb.delete(len - 2, len);

        //字符集默认
        sb.append("\n) ENGINE=").append(engine).append(";");

        return MysqlBoostHelper.getPrettifySql(sb.toString());
    }

    /**
     * 获取column字段定义内容
     *
     * @param columnName       列名字段
     * @param excel2MysqlModel
     * @return
     */
    public static String getColumnDefinition(String columnName, Excel2MysqlModel excel2MysqlModel) {
        return getColumnDefinition(columnName, excel2MysqlModel.getColumnType(), excel2MysqlModel.getColumnNotNull(), excel2MysqlModel.getColumnDefaultValue(), excel2MysqlModel.getColumnExtra(), excel2MysqlModel.getColumnComment());
    }

    /**
     * 获取column字段定义内容
     *
     * @param columnName       列名字段
     * @param mysqlColumnModel
     * @return
     */
    public static String getColumnDefinition(String columnName, MysqlColumnModel mysqlColumnModel) {
        return getColumnDefinition(columnName, mysqlColumnModel.getColumnType(), mysqlColumnModel.getColumnNotNull(), mysqlColumnModel.getColumnDefaultValue(), mysqlColumnModel.getColumnExtra(), mysqlColumnModel.getColumnComment());
    }

    /**
     * 获取column字段定义内容
     *
     * @param columnName         列名字段
     * @param columnType         列类型
     *                           e.g:
     *                           smallint(5) unsigned
     *                           bigint(20)
     *                           datetime
     * @param columnNotNull      列是否不为空
     * @param columnDefaultValue 列默认值
     * @param columnExtra        columnExtra,
     *                           e.g:
     *                           auto_increment
     *                           AUTO_INCREMENT
     *                           on update CURRENT_TIMESTAMP
     *                           ON UPDATE CURRENT_TIMESTAMP
     * @param columnComment      列备注,可选
     * @return
     */
    public static String getColumnDefinition(String columnName, String columnType, String columnNotNull, String columnDefaultValue, String columnExtra, String columnComment) {
        // 列名 + 类型 + 值描述 + Extra + Comment

        //   `id` bigint(20) NOT NULL AUTO_INCREMENT

        //   `age` smallint(5) DEFAULT NULL
        //   `age` smallint(5) unsigned DEFAULT NULL COMMENT '年龄'
        //   `age` smallint(5) unsigned DEFAULT '0' COMMENT '年龄'
        //   `age` smallint(5) unsigned NOT NULL COMMENT '年龄'
        //   `age` smallint(5) unsigned NOT NULL DEFAULT '0' COMMENT '年龄'

        //   `create_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
        //   `update_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
        //   `update_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'

        //   `update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'

        StringBuilder sb = new StringBuilder();

        //列名 + 类型
        sb.append(String.format("`%s` %s", columnName, columnType));

        //值描述
        if (MysqlBoostHelper.isColumnNotNull(columnNotNull)) {
            //非空时
            sb.append(" NOT NULL");

            if (!StringUtils.isEmpty(columnDefaultValue)) {
                sb.append(String.format(" DEFAULT %s", MysqlBoostHelper.getResolvedColumnDefaultValue(columnType, columnDefaultValue)));
            }
        } else {
            if (!StringUtils.isEmpty(columnDefaultValue)) {
                sb.append(String.format(" DEFAULT %s", MysqlBoostHelper.getResolvedColumnDefaultValue(columnType, columnDefaultValue)));
            } else {
                sb.append(" DEFAULT NULL");
            }
        }

        //Extra
        if (!StringUtils.isEmpty(columnExtra)) {
            String filteredColumnExtra = MysqlBoostHelper.getFilteredColumnExtra(columnExtra, true);
            if (!StringUtils.isEmpty(filteredColumnExtra)) {
                sb.append(" ").append(filteredColumnExtra);
            }
        }

        //Comment
        if (!StringUtils.isEmpty(columnComment)) {
            sb.append(String.format(" COMMENT '%s'", columnComment));
        }

        return sb.toString();
    }

    /**
     * 获取表要添加pk的sql
     *
     * @param tableName
     * @param columnNameList
     * @return
     */
    public static String getTableAddPrimaryKeyIndexSql(String tableName, List<String> columnNameList) {
        return String.format("ALTER TABLE `%s` ADD PRIMARY KEY(%s);", tableName, MysqlBoostHelper.getColumnFields(columnNameList));
    }

    /**
     * 获取表要添加唯一索引列的sql
     *
     * @param tableName
     * @param uniqueKeyColumn
     * @return
     */
    public static String getTableAddUniqueColumnIndexSql(String tableName, String uniqueKeyColumn) {
        return String.format("ALTER TABLE `%s` ADD CONSTRAINT `%s` UNIQUE(`%s`);", tableName, MysqlBoostHelper.getUniqueKeyName(uniqueKeyColumn), uniqueKeyColumn);
    }

    /**
     * 获取表要删除pk索引的sql
     *
     * @param tableName
     * @return
     */
    public static String getTableDropPrimaryKeyIndexSql(String tableName) {
        return String.format("ALTER TABLE `%s` DROP PRIMARY KEY;", tableName);
    }

    /**
     * 获取表新增的column sql
     *
     * @param tableName
     * @param columnDefinition
     * @return
     */
    public static String getTableAddColumnNameSql(String tableName, String columnDefinition) {
        return String.format("ALTER TABLE `%s` ADD COLUMN %s;", tableName, columnDefinition);
    }

    /**
     * 获取表修改的column sql
     *
     * @param tableName
     * @param columnDefinition
     * @return
     */
    public static String getTableModifyColumnNameSql(String tableName, String columnDefinition) {
        return String.format("ALTER TABLE `%s` MODIFY COLUMN %s;", tableName, columnDefinition);
    }

    /**
     * 获取表更新的column sql
     *
     * @param tableName
     * @param beforeColumnName
     * @param columnDefinition
     * @return
     */
    public static String getTableChangeColumnNameSql(String tableName, String beforeColumnName, String columnDefinition) {
        return String.format("ALTER TABLE `%s` CHANGE COLUMN `%s` %s;", tableName, beforeColumnName, columnDefinition);
    }

    /**
     * 获取表要删除的column sql
     *
     * @param tableName
     * @param columnNames
     * @return
     */
    public static String getTableDropColumnNamesSql(String tableName, List<String> columnNames) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("ALTER TABLE `%s`", tableName));
        int size = columnNames.size();
        sb.append(" ");
        for (int i = 0; i < size; i++) {
            sb.append(String.format("DROP COLUMN `%s`", columnNames.get(i)));
            if (i != size - 1) {
                sb.append(", ");
            }
        }
        sb.append(";");

        return sb.toString();
    }


    /**
     * 获取table中column list
     *
     * @param jdbcTemplate
     * @param database
     * @param tableName
     * @return
     */
    public static List<MysqlColumnModel> getMysqlColumnModelList(JdbcTemplate jdbcTemplate, String database, String tableName) {
        String sql = String.format("SHOW FULL FIELDS FROM `%s`", tableName);
        List<Map<String, Object>> resultMapList = jdbcTemplate.queryForList(sql);
        List<MysqlColumnModel> resultList = new ArrayList<>(64);
        //待处理column key的map
        Map<String, MysqlColumnModel> toResolveColumnKeyTypeMap = new HashMap<>(8);
        resultMapList.forEach(resultMap -> {
            MysqlColumnModel mysqlColumnModel = new MysqlColumnModel();
            mysqlColumnModel.setTableSchema(database);
            mysqlColumnModel.setTableName(tableName);
            mysqlColumnModel.setColumnName(StringUtils.convertString(resultMap.get("Field")));
            mysqlColumnModel.setColumnType(StringUtils.convertString(resultMap.get("Type")));
            mysqlColumnModel.setColumnDefaultValue(StringUtils.convertString(resultMap.get("Default")));
            mysqlColumnModel.setColumnNotNull(MysqlBoostHelper.getConvertColumnNotNull(StringUtils.convertString(resultMap.get("Null"))));
            mysqlColumnModel.setColumnComment(StringUtils.convertString(resultMap.get("Comment")));
            mysqlColumnModel.setColumnExtra(MysqlBoostHelper.getFilteredColumnExtra(StringUtils.convertString(resultMap.get("Extra"))));

            String columnKeyType = MysqlBoostHelper.getConvertColumnKeyType(StringUtils.convertString(resultMap.get("Key")));
            ColumnKeyTypeEnum columnKeyTypeEnum = MysqlBoostHelper.getColumnKeyType(columnKeyType);
            if (columnKeyTypeEnum == ColumnKeyTypeEnum.PRIMARY_KEY) {
                toResolveColumnKeyTypeMap.put(mysqlColumnModel.getColumnName(), mysqlColumnModel);
            }
            mysqlColumnModel.setColumnKeyType(columnKeyType);
            resultList.add(mysqlColumnModel);
        });

        if (!toResolveColumnKeyTypeMap.isEmpty()) {
            //处理PRI返回的字段实际类型是UK/PK/PK且UK?
            // (表中只存在唯一索引(包含复合唯一索引)不存在主键时通过 desc table / SHOW FULL FIELDS FROM table 返回的 Key -> PRI)
            List<Map<String, Object>> indexResultMapList = jdbcTemplate.queryForList(String.format("SHOW INDEX FROM `%s`", tableName));
            Map<String, List<Map<String, Object>>> columnName2IndexResultMapListMap = indexResultMapList.stream().collect(Collectors.groupingBy(it -> StringUtils.convertString(it.get("Column_name"))));
            Map<String, List<Map<String, Object>>> keyName2IndexResultMapListMap = indexResultMapList.stream().collect(Collectors.groupingBy(it -> StringUtils.convertString(it.get("Key_name"))));

            toResolveColumnKeyTypeMap.forEach((columnName, mysqlColumnModel) -> {
                List<Map<String, Object>> columnName2IndexResultMapList = columnName2IndexResultMapListMap.get(columnName);
                if (columnName2IndexResultMapList != null) {
                    boolean hasPrimaryKey = false;
                    boolean hasUniqueKey = false;
                    for (Map<String, Object> columnName2IndexResultMap : columnName2IndexResultMapList) {
                        if (StringUtils.equals((String) columnName2IndexResultMap.get("Key_name"), "PRIMARY")) {
                            hasPrimaryKey = true;
                        } else {
                            if (StringUtils.equals(StringUtils.convertString(columnName2IndexResultMap.get("Non_unique")), "0")) {
                                //是Unique约束时
                                if (keyName2IndexResultMapListMap.get(StringUtils.convertString(columnName2IndexResultMap.get("Key_name"))).size() == 1) {
                                    //该列存在单独的Unique约束时
                                    hasUniqueKey = true;
                                }
                            }
                        }

                        if (hasPrimaryKey && hasUniqueKey) {
                            break;
                        }
                    }

                    if (hasPrimaryKey && hasUniqueKey) {
                        mysqlColumnModel.setColumnKeyType(ColumnKeyTypeEnum.Constants.PRIMARY_AND_UNIQUE_KEY);
                    } else {
                        if (hasPrimaryKey) {
                            mysqlColumnModel.setColumnKeyType(ColumnKeyTypeEnum.Constants.PRIMARY_KEY);
                        } else if (hasUniqueKey) {
                            mysqlColumnModel.setColumnKeyType(ColumnKeyTypeEnum.Constants.UNIQUE_KEY);
                        } else {
                            //e.g: 表中只存在一组  UNIQUE KEY `uk_name_code` (`name`,`code`)
                            mysqlColumnModel.setColumnKeyType(null);
                        }
                    }
                }
            });
        }

        return resultList;
    }

    /**
     * 解析excel数据
     *
     * @param inExcelFile
     * @param excelType
     * @return
     */
    public static List<Excel2MysqlModel> getExcel2MysqlModelList(File inExcelFile, String excelType) {
        return EasyExcel.read(inExcelFile).excelType(getExcelTypeEnum(excelType))
                .head(Excel2MysqlModel.class).sheet().doReadSync();
    }

    /**
     * 获取列定义内容是否发生改变
     *
     * @param excel2MysqlModel
     * @param mysqlColumnModel
     * @return
     */
    public static boolean isColumnDefinitionChanged(Excel2MysqlModel excel2MysqlModel, MysqlColumnModel mysqlColumnModel) {

        if (!StringUtils.equals(StringUtils.trimToEmpty(excel2MysqlModel.getColumnType()), StringUtils.trimToEmpty(mysqlColumnModel.getColumnType()))) {
            return true;
        }

        if (!StringUtils.equals(excel2MysqlModel.getColumnComment(), mysqlColumnModel.getColumnComment())) {
            return true;
        }

        if (!StringUtils.equals(MysqlBoostHelper.getFilteredColumnExtra(excel2MysqlModel.getColumnExtra(), true), MysqlBoostHelper.getFilteredColumnExtra(mysqlColumnModel.getColumnExtra(), true))) {
            return true;
        }

        if (!Objects.equals(MysqlBoostHelper.isColumnNotNull(excel2MysqlModel.getColumnNotNull()), MysqlBoostHelper.isColumnNotNull(mysqlColumnModel.getColumnNotNull()))) {
            return true;
        }

        if (!StringUtils.equals(excel2MysqlModel.getColumnDefaultValue(), mysqlColumnModel.getColumnDefaultValue())) {
            return true;
        }

        return false;
    }

    /**
     * 获取最后字段的列排序位置关系
     *
     * @param beforeColumnNameOrderList
     * @param finalColumnNameOrderList
     * @param beforeColumnName2FinalColumnNameMap
     * @return
     */
    public static Map<String, String> getColumnOrderPositionSqlTextMap(List<String> beforeColumnNameOrderList, List<String> finalColumnNameOrderList, Map<String, String> beforeColumnName2FinalColumnNameMap) {
        if (beforeColumnNameOrderList.isEmpty()) {
            throw new UnsupportedOperationException("beforeColumnNameOrderList must be not empty");
        }
        if (finalColumnNameOrderList.isEmpty()) {
            throw new UnsupportedOperationException("finalColumnNameOrderList must be not empty");
        }

        Map<String, String> finalColumnName2BeforeColumnNameMap = beforeColumnName2FinalColumnNameMap.entrySet().stream()
                .filter(it -> it.getValue() != null).collect(Collectors.collectingAndThen(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey),
                        Function.identity()));

        int finalColumnNameOrderListSize = finalColumnNameOrderList.size();

        int wholeNewAddedColumnStartIndex = getWholeNewAddedColumnStartIndex(finalColumnNameOrderList, finalColumnName2BeforeColumnNameMap);

        //获取按最终列顺序的结果
        Map<String, String> resultMap = new LinkedHashMap<>(finalColumnNameOrderListSize);
        for (int i = 0; i < finalColumnNameOrderListSize; i++) {
            String finalOrderColumnName = finalColumnNameOrderList.get(i);
            String result = getColumnOrderPositionSqlText(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap, finalColumnName2BeforeColumnNameMap, i, wholeNewAddedColumnStartIndex);
            resultMap.put(finalOrderColumnName, result);
        }
        return resultMap;
    }

    /**
     * 通过表的最后字段列表和最后字段对应之前字段映射获取最后完全新增列的开始索引位置(从该位置开始时则为顺序新增),未找到时则返回-1
     *
     * @param finalColumnNameOrderList            最后字段列表
     * @param finalColumnName2BeforeColumnNameMap 最后字段对应之前字段映射
     * @return
     */
    private static int getWholeNewAddedColumnStartIndex(List<String> finalColumnNameOrderList,
                                                        Map<String, String> finalColumnName2BeforeColumnNameMap) {
        int finalColumnNameOrderListSize = finalColumnNameOrderList.size();
        //默认为-1
        int position = -1;
        //倒序查找
        for (int i = finalColumnNameOrderListSize - 1; i >= 0; i--) {
            String finalColumnNameOrder = finalColumnNameOrderList.get(i);
            String beforeColumnName = finalColumnName2BeforeColumnNameMap.get(finalColumnNameOrder);
            if (beforeColumnName != null) {
                //若该列之前便存在时停止
                break;
            }
            position = i;
        }
        return position;
    }

    /**
     * 获取最后字段列表的指定索引位置的列排序位置的配置
     *
     * @param beforeColumnNameOrderList
     * @param finalColumnNameOrderList
     * @param beforeColumnName2FinalColumnNameMap
     * @param finalColumnName2BeforeColumnNameMap
     * @param index
     * @param wholeNewAddedColumnStartIndex
     * @return
     */
    private static String getColumnOrderPositionSqlText(List<String> beforeColumnNameOrderList, List<String> finalColumnNameOrderList, Map<String, String> beforeColumnName2FinalColumnNameMap,
                                                        Map<String, String> finalColumnName2BeforeColumnNameMap, int index, int wholeNewAddedColumnStartIndex) {
        if (wholeNewAddedColumnStartIndex != -1 && index >= wholeNewAddedColumnStartIndex) {
            //存在当前字段在之前字段之后完全新增的开始索引位置且当前索引大于等于该值时则为后续完全新增字段(从该位置开始时为顺序新增),默认返回null
            return null;
        }

        String finalOrderColumnName = finalColumnNameOrderList.get(index);

        //获取同位置的之前列
        String beforeOrderColumnName = index < beforeColumnNameOrderList.size() ? beforeColumnNameOrderList.get(index) : null;
        if (index == 0) {
            //第一个时
            if (StringUtils.equals(beforeOrderColumnName, finalOrderColumnName)) {
                //字段名称未变化
                return null;
            }

            //获取之前字段在当前的名称,当一样时未变化返回null
            String beforeOrderColumnNameInAfterName = beforeColumnName2FinalColumnNameMap.get(beforeOrderColumnName);
            if (StringUtils.equals(beforeOrderColumnNameInAfterName, finalOrderColumnName)) {
                //只是变更名称,属于同一列并未变化位置
                return null;
            }

            //当前列在之前表中存在且在之后只剩下该列时(其他列被移除后该列便处于第一位),返回null
            if (finalColumnName2BeforeColumnNameMap.get(finalOrderColumnName) != null && beforeColumnName2FinalColumnNameMap.values()
                    .stream().filter(Objects::nonNull).count() == 1) {
                //beforeColumnName2FinalColumnNameMap中只找到一个值不为空的说明只剩下一列
                return null;
            }

            //最后返回FIRST
            return "FIRST";
        }

        if (beforeOrderColumnName != null) {
            //获取之前字段在当前的名称
            String beforeOrderColumnNameInAfterName = beforeColumnName2FinalColumnNameMap.get(beforeOrderColumnName);
            if (StringUtils.equals(beforeOrderColumnNameInAfterName, finalOrderColumnName)) {
                //当前位置未变化
                return null;
            }
        }

        //如果当前字段在之前存在且前一列的字段一致时,则认为(相对)位置未发生变化(注: 以AFTER 前一列 定位)
        String finalOrderColumnNameInBeforeName = finalColumnName2BeforeColumnNameMap.get(finalOrderColumnName);
        if (finalOrderColumnNameInBeforeName != null) {
            int beforeIndex = beforeColumnNameOrderList.indexOf(finalOrderColumnNameInBeforeName);
            //当之前同名称列处于第一个或者找不到时不处理
            if (beforeIndex > 0) {
                //获取之前同名称列的前一列
                String finalOrderColumnNameInBeforeNamePrevious = beforeColumnNameOrderList.get(beforeIndex - 1);
                //获取当前列的前一列
                String finalOrderColumnNamePrevious = finalColumnNameOrderList.get(index - 1);
                if (StringUtils.equals(finalOrderColumnNameInBeforeNamePrevious, finalOrderColumnNamePrevious)
                        || StringUtils.equals(beforeColumnName2FinalColumnNameMap.get(finalOrderColumnNameInBeforeNamePrevious), finalOrderColumnNamePrevious)) {
                    return null;
                }
            }
        }

        //默认处于当前排序字段的前一个之后
        return String.format("AFTER `%s`", finalColumnNameOrderList.get(index - 1));
    }

}
