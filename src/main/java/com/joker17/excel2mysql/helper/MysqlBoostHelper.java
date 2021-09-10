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
        text = StringUtils.trimToNull(text);
        if (text == null) {
            return false;
        }

        if (text.equals("Y") || text.equals("y") || text.equalsIgnoreCase("Yes")) {
            return true;
        }

        if (text.equals("N") || text.equals("n") || text.equalsIgnoreCase("No")) {
            return false;
        }

        if (text.equals("T") || text.equals("t") || text.equalsIgnoreCase("True")) {
            return true;
        }

        if (text.equals("F") || text.equals("f") || text.equalsIgnoreCase("False")) {
            return false;
        }

        if (text.equals("1")) {
            return true;
        }

        if (text.equals("0")) {
            return false;
        }

        if (text.contains("是") || text.contains("真") || text.contains("对")) {
            return true;
        }

        return false;
    }


    /**
     * 获取列是否不为空结果
     *
     * @param nullText
     * @return
     */
    public static String getConvertColumnNotNull(String nullText) {
        if (nullText == null || nullText.contains("Y") || nullText.contains("y")) {
            //当null text包含y时为 Null
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
     * 获取处理过的列默认值
     *
     * @param columnType
     * @param columnDefaultValue
     * @return
     */
    public static String getResolvedColumnDefaultValue(final String columnType, final String columnDefaultValue) {
        if (columnDefaultValue == null) {
            return null;
        }

        if (columnDefaultValue.contains("(") || columnDefaultValue.contains("_")
                || columnDefaultValue.contains("'")  || columnDefaultValue.contains("\"")
                || columnDefaultValue.contains("+") || columnDefaultValue.contains("*")
                || columnDefaultValue.contains(",") || columnDefaultValue.contains("-")
        ) {

            //  a DATETIME    DEFAULT (CURRENT_TIMESTAMP + INTERVAL 1 YEAR),
            //  f FLOAT       DEFAULT (RAND() * RAND()),
            //  b BINARY(16)  DEFAULT (UUID_TO_BIN(UUID())),
            //  d DATE        DEFAULT (CURRENT_DATE + INTERVAL 1 YEAR),
            //  c DATE        DEFAULT (CURRENT_DATE - INTERVAL 1 YEAR),
            //  p POINT       DEFAULT (Point(0,0)),
            //  j JSON        DEFAULT (JSON_ARRAY())
            return columnDefaultValue;
        }

        String columnDefaultValueUpperCase = StringUtils.toUpperCase(columnDefaultValue);
        if (columnDefaultValueUpperCase.contains("CURRENT")) {
            // CURRENT_TIMESTAMP CURRENT_DATE
            return columnDefaultValue;
        }

        String columnTypeLowerCase = StringUtils.toLowerCase(columnType);
        if (columnTypeLowerCase.contains("int") || columnTypeLowerCase.equals("char")
                || columnTypeLowerCase.equals("decimal") || columnTypeLowerCase.equals("double")
                || columnTypeLowerCase.equals("float") || columnTypeLowerCase.equals("varchar")
        ) {
            return String.format("'%s'", columnDefaultValue);
        }

        return columnDefaultValue;
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
