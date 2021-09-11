package com.joker17.excel2mysql.helper;

import com.alibaba.excel.support.ExcelTypeEnum;
import com.joker17.excel2mysql.constants.Excel2MysqlConstants;
import com.joker17.excel2mysql.enums.ColumnKeyTypeEnum;
import com.joker17.excel2mysql.model.Excel2MysqlModel;
import com.joker17.excel2mysql.model.MysqlColumnModel;
import com.joker17.excel2mysql.utils.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Excel2MysqlHelper {

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
        sb.append("\n) ENGINE=").append(engine);

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
            String filteredColumnExtra = MysqlBoostHelper.getFilteredColumnExtra(columnExtra);
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
            sb.append(String.format("DROP `%s`", columnNames.get(i)));
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
        resultMapList.forEach(resultMap -> {
            MysqlColumnModel mysql2ExcelModel = new MysqlColumnModel();
            mysql2ExcelModel.setTableSchema(database);
            mysql2ExcelModel.setTableName(tableName);
            mysql2ExcelModel.setColumnName(StringUtils.convertString(resultMap.get("Field")));
            mysql2ExcelModel.setColumnType(StringUtils.convertString(resultMap.get("Type")));
            mysql2ExcelModel.setColumnDefaultValue(StringUtils.convertString(resultMap.get("Default")));
            mysql2ExcelModel.setColumnNotNull(MysqlBoostHelper.getConvertColumnNotNull(StringUtils.convertString(resultMap.get("Null"))));
            mysql2ExcelModel.setColumnComment(StringUtils.convertString(resultMap.get("Comment")));
            mysql2ExcelModel.setColumnExtra(MysqlBoostHelper.getFilteredColumnExtra(StringUtils.convertString(resultMap.get("Extra"))));
            mysql2ExcelModel.setColumnKeyType(MysqlBoostHelper.getConvertColumnKeyType(StringUtils.convertString(resultMap.get("Key"))));
            resultList.add(mysql2ExcelModel);
        });
        return resultList;
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

        if (!StringUtils.equals(MysqlBoostHelper.getFilteredColumnExtra(excel2MysqlModel.getColumnExtra()), MysqlBoostHelper.getFilteredColumnExtra(mysqlColumnModel.getColumnExtra()))) {
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
}
