package com.joker17.excel2mysql.helper;

import com.alibaba.excel.support.ExcelTypeEnum;
import com.joker17.excel2mysql.constants.Excel2MysqlConstants;
import com.joker17.excel2mysql.model.Excel2MysqlModel;
import com.joker17.excel2mysql.utils.StringUtils;

import java.util.*;

public class Excel2MysqlHelper {


    /**
     * 获取excelType
     *
     * @param excelType
     * @return
     */
    public static String getExcelType(String excelType) {
        excelType = excelType == null || excelType.trim().isEmpty() ? Excel2MysqlConstants.XLS : excelType.trim().toLowerCase(Locale.ROOT);
        return excelType;
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
     * 是否为匹配到要处理的表名
     *
     * @param filterTableNames 过滤的表名
     * @param tableName        表名
     * @param exclude          true: 排除, false: 包含
     * @return
     */
    public static boolean isMatchResolveTableName(Collection<String> filterTableNames, String tableName, boolean exclude) {
        if (filterTableNames == null || filterTableNames.isEmpty()) {
            return true;
        }
        boolean contains = filterTableNames.contains(tableName);
        return exclude ? !contains : contains;
    }


    /**
     * 获取create sql
     * CREATE TABLE `role` (
     * `id` bigint(20) NOT NULL AUTO_INCREMENT,
     * `create_date` datetime DEFAULT NULL,
     * `update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
     * `name` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
     * `remarks` varchar(512) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
     * PRIMARY KEY (`id`)
     * ) ENGINE=InnoDB AUTO_INCREMENT=328 DEFAULT CHARSET=utf8 COLLATE=utf8_bin
     *
     * @param tableName
     * @param excel2MysqlModelList
     * @return
     */
    public static String getCreateSql(String tableName, List<Excel2MysqlModel> excel2MysqlModelList) {

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE `");
        sb.append(tableName);
        sb.append("` (");

        String[] primaryKeys = new String[]{null};

        List<String> uniqueKeyList = new ArrayList<>(8);

        excel2MysqlModelList.forEach(excel2MysqlModel -> {
            // 列名 + 类型 + 值描述 + Extra + Comment

            //   `age` smallint(5) unsigned DEFAULT NULL COMMENT '年龄',
            //   `age` smallint(5) unsigned DEFAULT '0' COMMENT '年龄',
            //   `age` smallint(5) unsigned NOT NULL COMMENT '年龄',
            //  `age` smallint(5) unsigned NOT NULL DEFAULT '0' COMMENT '年龄',

            //  `update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'

            //列名 + 类型
            sb.append(String.format(" `%s` %s ", excel2MysqlModel.getColumnName(), excel2MysqlModel.getColumnType()));

            //值描述
            if (isColumnNotNull(excel2MysqlModel.getColumnNotNull())) {
                //非空时
                sb.append(" NOT NULL ");

                if (!StringUtils.isEmpty(excel2MysqlModel.getColumnDefaultValue())) {
                    sb.append(" DEFAULT ").append(excel2MysqlModel.getColumnDefaultValue())
                            .append(" ");

                }
            } else {
                if (!StringUtils.isEmpty(excel2MysqlModel.getColumnDefaultValue())) {
                    sb.append(" DEFAULT ").append(excel2MysqlModel.getColumnDefaultValue())
                            .append(" ");

                } else {
                    sb.append(" DEFAULT NULL ");
                }
            }

            //Extra
            if (!StringUtils.isEmpty(excel2MysqlModel.getColumnExtra())) {
                sb.append(StringUtils.toUpperCase(excel2MysqlModel.getColumnExtra())).append(" ");
            }

            //Comment
            if (!StringUtils.isEmpty(excel2MysqlModel.getColumnComment())) {
                sb.append(String.format(" COMMENT '%s' ", excel2MysqlModel.getColumnComment()));
            }

            sb.append(",\n");

            //解析key
            if (!StringUtils.isEmpty(excel2MysqlModel.getColumnKeyType())) {
                if (isPrimaryKey(excel2MysqlModel.getColumnKeyType())) {
                    if (primaryKeys[0] != null) {
                        throw new IllegalArgumentException(String.format("%s has already exists primary key %s : cause by column %s", tableName, primaryKeys[0], excel2MysqlModel.getColumnName()));
                    }
                    primaryKeys[0] = excel2MysqlModel.getColumnName();
                } else if (isUniqueKey(excel2MysqlModel.getColumnKeyType())) {
                    uniqueKeyList.add(excel2MysqlModel.getColumnName());
                }
            }
        });

        //处理 key
        if (primaryKeys[0] != null) {
            sb.append(String.format(" PRIMARY KEY (`%s`),\n", primaryKeys[0]));
        }

        uniqueKeyList.forEach(uniqueKey -> {
            sb.append(String.format(" UNIQUE KEY `uk_%s` (`%s`) USING BTREE,\n", uniqueKey, uniqueKey));
        });

        //删除多余 ,\n
        int len = sb.length();
        sb.delete(len - 2, len);

        //字符集默认
        sb.append(" ) ENGINE=InnoDB ");
        return sb.toString();
    }


    /**
     * 列是否不为空
     *
     * @param text
     * @return
     */
    public static boolean isColumnNotNull(String text) {

        if (text == null) {
            return false;
        }

        if (text.contains("Y") || text.contains("y")) {
            return true;
        }

        if (text.contains("N") || text.contains("n")) {
            return false;
        }

        if (text.contains("T") || text.contains("t")
                || text.equals("1") || text.contains("是") || text.contains("真") || text.contains("对")) {
            return true;
        }

        return false;
    }

    /**
     * 是否为PK
     *
     * @param text
     * @return
     */
    public static boolean isPrimaryKey(String text) {
        text = StringUtils.toUpperCase(StringUtils.trimToEmpty(text));
        return Objects.equals("PK", text) || Objects.equals("PRI", text) || Objects.equals("PRIMARY KEY", text);
    }

    /**
     * 是否为UK
     *
     * @param text
     * @return
     */
    public static boolean isUniqueKey(String text) {
        text = StringUtils.toUpperCase(StringUtils.trimToEmpty(text));
        return Objects.equals("UK", text) || Objects.equals("UNI", text) || Objects.equals("UNIQUE KEY", text);
    }


}
