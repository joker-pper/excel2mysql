package com.joker17.excel2mysql.param;

import com.beust.jcommander.Parameter;

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
     * 文件名称,不包含后缀
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
    @Parameter(names = "-filter-table", description = "optional, more should be separated by a space")
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

    @Parameter(names = {"--help", "--h"}, help = true, order = 5)
    private boolean help;

    public String getDataSourceProperties() {
        return dataSourceProperties;
    }

    public void setDataSourceProperties(String dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    public String getInFilePath() {
        return inFilePath;
    }

    public void setInFilePath(String inFilePath) {
        this.inFilePath = inFilePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExcelType() {
        return excelType;
    }

    public void setExcelType(String excelType) {
        this.excelType = excelType;
    }

    public String getFilterTable() {
        return filterTable;
    }

    public void setFilterTable(String filterTable) {
        this.filterTable = filterTable;
    }

    public boolean isExcludeTableMode() {
        return excludeTableMode;
    }

    public void setExcludeTableMode(boolean excludeTableMode) {
        this.excludeTableMode = excludeTableMode;
    }

    public boolean isCheckTableSchema() {
        return checkTableSchema;
    }

    public void setCheckTableSchema(boolean checkTableSchema) {
        this.checkTableSchema = checkTableSchema;
    }

    public String getAutoMode() {
        return autoMode;
    }

    public void setAutoMode(String autoMode) {
        this.autoMode = autoMode;
    }

    public boolean isHelp() {
        return help;
    }

    public void setHelp(boolean help) {
        this.help = help;
    }
}
