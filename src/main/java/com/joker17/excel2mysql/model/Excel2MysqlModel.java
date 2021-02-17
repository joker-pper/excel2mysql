package com.joker17.excel2mysql.model;

import com.alibaba.excel.annotation.ExcelProperty;

public class Excel2MysqlModel {

    /**
     * 数据库名称
     */
    @ExcelProperty(value = "TABLE_SCHEMA", index = 0)
    private String tableSchema;

    /**
     * 表名
     */
    @ExcelProperty(value = "TABLE_NAME", index = 1)
    private String tableName;


    /**
     * 列名
     */
    @ExcelProperty(value = "COLUMN_NAME", index = 2)
    private String columnName;


    /**
     * 列备注
     */
    @ExcelProperty(value = "COLUMN_COMMENT", index = 3)
    private String columnComment;

    /**
     * 列类型
     */
    @ExcelProperty(value = "COLUMN_TYPE", index = 4)
    private String columnType;


    /**
     * 列是否非空
     */
    @ExcelProperty(value = "COLUMN_NOT_NULL", index = 5)
    private String columnNotNull;


    /**
     * 列初始值
     */
    @ExcelProperty(value = "COLUMN_DEFAULT_VALUE", index = 6)
    private String columnDefaultValue;

    /**
     * 列extra
     */
    @ExcelProperty(value = "COLUMN_EXTRA", index = 7)
    private String columnExtra;

    /**
     * 列 key type
     */
    @ExcelProperty(value = "COLUMN_KEY_TYPE", index = 8)
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
