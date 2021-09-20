package com.joker17.excel2mysql.core;

import com.joker17.excel2mysql.constants.Excel2MysqlConstants;
import com.joker17.excel2mysql.db.JdbcUtils;
import com.joker17.excel2mysql.db.MysqlUtils;
import com.joker17.excel2mysql.support.DynamicIgnoredRunner;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 1.创建database: CREATE DATABASE IF NOT EXISTS excel2mysql
 * 2.修改db.properties
 */
@RunWith(DynamicIgnoredRunner.class)
@FixMethodOrder(MethodSorters.JVM)
public class Excel2MysqlExecutorTest {

    private final static Logger logger = LoggerFactory.getLogger(Excel2MysqlExecutorTest.class);

    private final static String targetClassPath = Excel2MysqlExecutorTest.class.getClassLoader().getResource("").getPath();

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
        boolean dropAllTables = true;
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
    public void executeCreateXls() throws IOException {
        String inFilePath = targetClassPath;
        String fileName = "create-excel";
        String excelType = Excel2MysqlConstants.XLS;
        String appendText = "-check-table-schema false";

        String line = String.format("-data-source %s -in-file-path %s -file-name %s -excel-type %s %s",
                targetClassPath + "db.properties", inFilePath, fileName, excelType, appendText == null ? "" : appendText);
        String[] args = line.split(" ");
        Excel2MysqlExecutor.INSTANCE.execute(args);
    }


    @Test
    public void executeCreateXlsx() throws IOException {
        String inFilePath = targetClassPath;
        String fileName = "create-excel.xlsx";
        String excelType = Excel2MysqlConstants.XLSX;
        String appendText = "-check-table-schema false";

        String line = String.format("-data-source %s -in-file-path %s -file-name %s -excel-type %s %s",
                targetClassPath + "db.properties", inFilePath, fileName, excelType, appendText == null ? "" : appendText);
        String[] args = line.split(" ");
        Excel2MysqlExecutor.INSTANCE.execute(args);
    }

    @Test
    public void executeUpdateXlsx() throws IOException {
        String fileName = "update-excel.xlsx";
        String revertFileName = "update-excel-revert.xlsx";
        boolean hasError = false;
        try {
            logger.info("execute update update-excel (create-excel/update-excel-revert updated table -> update-excel) start");
            executeUpdateXlsx(fileName);
            logger.info("execute update update-excel (create-excel/update-excel-revert updated table -> update-excel) end.");
        } catch (Exception ex) {
            hasError = true;
        }

        if (hasError) {
            logger.error("execute update update-excel (create-excel/update-excel-revert updated table -> update-excel) end and has error: maybe updated by update-excel, try to execute revert.");

            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            logger.info("execute update update-excel-revert (update-excel updated table -> update-excel-revert) start");
            executeUpdateXlsx(revertFileName);
            logger.info("execute update update-excel-revert (update-excel updated table -> update-excel-revert) end.");
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }
    }


    @Test
    public void executeUpdateXlsxRepeat() throws IOException {
        executeUpdateXlsx();
    }


    private void executeUpdateXlsx(String fileName) throws IOException {
        String inFilePath = targetClassPath;
        String excelType = Excel2MysqlConstants.XLSX;
        String appendText = "-check-table-schema false -auto-mode update";

        String line = String.format("-data-source %s -in-file-path %s -file-name %s -excel-type %s %s",
                targetClassPath + "db.properties", inFilePath, fileName, excelType, appendText == null ? "" : appendText);
        String[] args = line.split(" ");
        Excel2MysqlExecutor.INSTANCE.execute(args);
    }
}