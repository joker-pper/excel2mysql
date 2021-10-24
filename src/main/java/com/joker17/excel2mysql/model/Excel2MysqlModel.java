package com.joker17.excel2mysql.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
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

}
