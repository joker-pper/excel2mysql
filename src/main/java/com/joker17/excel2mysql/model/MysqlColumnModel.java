package com.joker17.excel2mysql.model;


public class MysqlColumnModel {

    /**
     * 数据库名称
     */
    private String tableSchema;

    /**
     * 表名
     */
    private String tableName;


    /**
     * 列名
     */
    private String columnName;


    /**
     * 列备注
     */
    private String columnComment;

    /**
     * 列类型
     */
    private String columnType;


    /**
     * 列是否非空
     */
    private String columnNotNull;


    /**
     * 列初始值
     */
    private String columnDefaultValue;

    /**
     * 列extra
     */
    private String columnExtra;

    /**
     * 列 key type
     */
    private String columnKeyType;

    public String getTableSchema() {
        return tableSchema;
    }

    public void setTableSchema(String tableSchema) {
        this.tableSchema = tableSchema;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getColumnNotNull() {
        return columnNotNull;
    }

    public void setColumnNotNull(String columnNotNull) {
        this.columnNotNull = columnNotNull;
    }

    public String getColumnDefaultValue() {
        return columnDefaultValue;
    }

    public void setColumnDefaultValue(String columnDefaultValue) {
        this.columnDefaultValue = columnDefaultValue;
    }

    public String getColumnExtra() {
        return columnExtra;
    }

    public void setColumnExtra(String columnExtra) {
        this.columnExtra = columnExtra;
    }

    public String getColumnKeyType() {
        return columnKeyType;
    }

    public void setColumnKeyType(String columnKeyType) {
        this.columnKeyType = columnKeyType;
    }

}
