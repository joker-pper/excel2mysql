package com.joker17.excel2mysql.helper;

import com.joker17.excel2mysql.enums.ColumnKeyTypeEnum;
import com.joker17.excel2mysql.utils.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class MysqlBoostHelper {


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
     * 判断列是否不为空
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
     * 获取列是否不为空
     *
     * @param nullText
     * @return
     */
    public static String getConvertColumnNotNull(String nullText) {
        if (nullText == null || nullText.contains("Y") || nullText.contains("y")) {
            return "N";
        }
        return "Y";
    }

    /**
     * 获取列类型
     *
     * @param text
     * @return
     */
    public static String getConvertColumnKeyType(String text) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }

        if (Objects.equals("PRI", text)) {
            //PRIMARY KEY
            return "PK";
        }
        if (Objects.equals("UNI", text)) {
            //UNIQUE KEY
            return "UK";
        }
        return null;
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


    /**
     * 获取ColumnKeyTypeEnum
     *
     * @param columnKeyType
     * @return
     */
    public static ColumnKeyTypeEnum getColumnKeyType(String columnKeyType) {
        if (StringUtils.isEmpty(columnKeyType)) {
            return ColumnKeyTypeEnum.OTHER;
        }

        if (isPrimaryKey(columnKeyType)) {
            return ColumnKeyTypeEnum.PRIMARY_KEY;
        }

        if (isUniqueKey(columnKeyType)) {
            return ColumnKeyTypeEnum.UNIQUE_KEY;
        }

        return ColumnKeyTypeEnum.OTHER;
    }


    /**
     * 获取过滤后的columnExtra
     *
     * @param columnExtra
     * @return
     */
    public static String getFilteredColumnExtra(String columnExtra) {
        return StringUtils.trimToNull(StringUtils.removeStart(StringUtils.toUpperCase(columnExtra), "DEFAULT_GENERATED"));
    }

    /**
     * 是否为自增
     *
     * @param sql
     * @return
     */
    public static boolean hasAutoIncrement(String sql) {
        return sql != null ? sql.contains("AUTO_INCREMENT") : false;
    }

    /**
     * 移除自增标识的sql
     *
     * @param sql
     * @return
     */
    public static String getRemovedAutoIncrementSql(String sql) {
        if (sql != null) {
            String result = sql.replace(" AUTO_INCREMENT", "");
            result = result.replace("AUTO_INCREMENT", "");
            return result;
        }
        return sql;
    }


    /**
     * 获取多列的拼接结果
     * <p>
     * e.g [id, name] -> `id`, `name`
     *
     * @param columnList
     * @return
     */
    public static String getColumnFields(List<String> columnList) {
        StringBuilder sb = new StringBuilder();
        String symbol = ", ";
        columnList.forEach(it -> {
            sb.append("`").append(it).append("`");
            sb.append(symbol);
        });
        return StringUtils.removeEnd(sb.toString(), symbol);
    }

    /**
     * 获取uniqueKey名称
     *
     * @param uniqueKey
     * @return
     */
    public static String getUniqueKeyName(String uniqueKey) {
        return String.format("uk_%s", uniqueKey);
    }


    /**
     * 美化sql
     *
     * @param sql
     * @return
     */
    public static String getPrettifySql(String sql) {
        //简单处理多余的空格
        return StringUtils.replace(sql, "  ", " ");
    }

}
