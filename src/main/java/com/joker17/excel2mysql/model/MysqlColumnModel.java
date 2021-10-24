package com.joker17.excel2mysql.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
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

}
