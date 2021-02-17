package com.joker17.excel2mysql.core;

import com.joker17.excel2mysql.constants.Excel2MysqlConstants;
import com.joker17.excel2mysql.db.JdbcUtils;
import com.joker17.excel2mysql.db.MysqlUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Excel2MysqlExecutorTest {

    private final static Logger logger = LoggerFactory.getLogger(Excel2MysqlExecutorTest.class);

    private final String targetClassPath = this.getClass().getClassLoader().getResource("").getPath();


    @Test
    public void executeWithHelpArg() throws IOException {
        Excel2MysqlExecutor.INSTANCE.execute(new String[]{"--help"});
    }


    /**
     * 删除数据库全部的表 (慎用)
     *
     * @throws IOException
     */
    @Test
    public void executeWithClearDatabase() throws IOException {
        boolean dropAllTables = false;
        if (dropAllTables) {
            //开启时执行清除当前数据源的所有表
            File dataSourcePropertiesFile = new File(targetClassPath + "db.properties");
            Properties properties = JdbcUtils.loadProperties(new FileInputStream(dataSourcePropertiesFile));
            DataSource dataSource = JdbcUtils.getDataSource(properties);
            JdbcTemplate jdbcTemplate = JdbcUtils.getJdbcTemplate(dataSource);
            MysqlUtils.dropTables(jdbcTemplate);
            logger.warn("drop all tables complete ...");
        }
    }


    @Test
    public void executeXls() throws IOException {
        String inFilePath = targetClassPath;
        String fileName = "test";
        String excelType = Excel2MysqlConstants.XLS;
        String appendText = "-check-table-schema false";

        String line = String.format("-data-source %s -in-file-path %s -file-name %s -excel-type %s %s",
                targetClassPath + "db.properties", inFilePath, fileName, excelType, appendText == null ? "" : appendText);
        String[] args = line.split(" ");
        Excel2MysqlExecutor.INSTANCE.execute(args);
    }

    @Test
    public void executeXlsx() throws IOException {
        String inFilePath = targetClassPath;
        String fileName = "test";
        String excelType = Excel2MysqlConstants.XLSX;
        String appendText = "-check-table-schema false";

        String line = String.format("-data-source %s -in-file-path %s -file-name %s -excel-type %s %s",
                targetClassPath + "db.properties", inFilePath, fileName, excelType, appendText == null ? "" : appendText);
        String[] args = line.split(" ");
        Excel2MysqlExecutor.INSTANCE.execute(args);
    }
}