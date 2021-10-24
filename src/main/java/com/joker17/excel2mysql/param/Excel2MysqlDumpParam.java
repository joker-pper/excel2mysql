package com.joker17.excel2mysql.param;

import com.beust.jcommander.Parameter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Excel2MysqlDumpParam {

    /**
     * 数据源配置文件
     */
    @Parameter(names = {"-data-source"}, required = true, description = "data source properties")
    private String dataSourceProperties;

    /**
     * 输入路径
     */
    @Parameter(names = {"-in-file-path"}, description = "in excel file path")
    private String inFilePath;

    /**
     * 文件名称,后缀可选
     */
    @Parameter(names = {"-file-name"}, required = true, description = "in excel file name")
    private String fileName;

    /**
     * excel type
     */
    @Parameter(names = {"-excel-type"},  description = "excel type, values: xls / xlsx, default value xls")
    private String excelType;

    /**
     * 要筛选的table 以空格分隔 可为空(全部)
     */
    @Parameter(names = "-filter-table", description = "filter table config, multiple values should be separated by space")
    private String filterTable;

    /**
     * 是否为排除table模式,默认false
     * 通过命令行指定为true: -exclude-table
     */
    @Parameter(names = "-exclude-table", description = "exclude table mode")
    private boolean excludeTableMode;

    /**
     * 是否检查tableSchema,默认true
     * 通过命令行时必须携带参数值
     * e.g:
     * -check-table-schema true
     * -check-table-schema false
     */
    @Parameter(names = "-check-table-schema", description = "check table schema, default value true", arity = 1)
    private boolean checkTableSchema = true;

    /**
     * 建表模式
     */
    @Parameter(names = {"-auto-mode"}, description = "auto mode, values: create / update, default value create")
    private String autoMode;

    /**
     * 建表引擎
     */
    @Parameter(names = {"-engine"}, description = "table engine, default value InnoDB")
    private String engine;

    @Parameter(names = {"--help", "--h"}, help = true, order = 5)
    private boolean help;

}
