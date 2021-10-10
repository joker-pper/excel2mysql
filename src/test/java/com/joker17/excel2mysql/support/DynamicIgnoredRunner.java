package com.joker17.excel2mysql.support;

import com.joker17.excel2mysql.core.Excel2MysqlExecutorTest;
import com.joker17.excel2mysql.utils.FileUtils;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class DynamicIgnoredRunner extends BlockJUnit4ClassRunner {

    private final static Logger logger = LoggerFactory.getLogger(DynamicIgnoredRunner.class);

    /**
     * 是否忽略tests
     */
    private static volatile Boolean isIgnoredTests = null;

    public DynamicIgnoredRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    protected boolean loadIsIgnoredTests() {
        File dataSourcePropertiesFile = DbTestHelper.getDataSourcePropertiesFile();
        if (!FileUtils.isFileAndExists(dataSourcePropertiesFile)) {
            logger.error("ignored tests: can't find properties {}", dataSourcePropertiesFile.getPath());
            return true;
        }

        Connection connection = null;
        try {
            DataSource dataSource = DbTestHelper.getDataSource();
            connection = dataSource.getConnection();
        } catch (SQLException | IOException ex) {
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

