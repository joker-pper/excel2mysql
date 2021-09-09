package com.joker17.excel2mysql.core;

import com.joker17.excel2mysql.constants.Excel2MysqlConstants;
import com.joker17.excel2mysql.db.JdbcUtils;
import com.joker17.excel2mysql.db.MysqlUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 1.创建database: CREATE DATABASE IF NOT EXISTS excel2mysql
 * 2.修改db.properties
 */
@RunWith(Excel2MysqlExecutorTest.DynamicIgnoredRunner.class)
public class Excel2MysqlExecutorTest {

    private final static Logger logger = LoggerFactory.getLogger(Excel2MysqlExecutorTest.class);

    private final static String targetClassPath = Excel2MysqlExecutorTest.class.getClassLoader().getResource("").getPath();

    /**
     * 是否忽略tests
     */
    private static volatile Boolean isIgnoredTests = null;

    public static class DynamicIgnoredRunner extends BlockJUnit4ClassRunner {

        public DynamicIgnoredRunner(Class<?> testClass) throws InitializationError {
            super(testClass);
        }

        protected boolean loadIsIgnoredTests() {
            File dataSourcePropertiesFile = new File(targetClassPath + "db.properties");
            Properties properties = null;
            try {
                properties = JdbcUtils.loadProperties(new FileInputStream(dataSourcePropertiesFile));
            } catch (IOException e) {
            }

            if (properties == null) {
                logger.error("ignored tests: can't find properties {}", dataSourcePropertiesFile.getPath());
                return true;
            }

            Connection connection = null;
            try {
                DataSource dataSource = JdbcUtils.getDataSource(properties);
                connection = dataSource.getConnection();
            } catch (SQLException ex) {
            }

            if (connection == null) {
                logger.error("ignored tests: can't get connection");
                return true;
            }

            return false;
        }

        @Override
        protected boolean isIgnored(FrameworkMethod frameworkMethod) {
            if (isIgnoredTests == null) {
                synchronized (Excel2MysqlExecutorTest.class) {
                    if (isIgnoredTests == null) {
                        isIgnoredTests = loadIsIgnoredTests();
                    }
                }
            }
            return isIgnoredTests;
        }
    }


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
            executeUpdateXlsx(fileName);
        } catch (Exception ex) {
            logger.error("execute update update-excel has error: maybe updated, try to revert.");
            hasError = true;
            logger.info("execute update update-excel-revert start");
            executeUpdateXlsx(revertFileName);
            logger.info("execute update update-excel-revert end.");
        } finally {
            if (hasError) {
                logger.error("execute update update-excel retry to execute start");
                executeUpdateXlsx(fileName);
                logger.error("execute update update-excel retry to execute end.");
            }
        }

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