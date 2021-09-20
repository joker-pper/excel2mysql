package com.joker17.excel2mysql.support;

import com.joker17.excel2mysql.db.JdbcUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DbTestHelper {

    private final static String TARGET_CLASS_PATH = DbTestHelper.class.getClassLoader().getResource("").getPath();

    private final static String DATA_SOURCE_PROPERTIES = "db.properties";

    public static File getDataSourcePropertiesFile() {
        return new File(TARGET_CLASS_PATH + DATA_SOURCE_PROPERTIES);
    }

    public static DataSource getDataSource() throws IOException {
        Properties properties = JdbcUtils.loadProperties(new FileInputStream(getDataSourcePropertiesFile()));
        return JdbcUtils.getDataSource(properties);
    }

    public static JdbcTemplate getJdbcTemplate() throws IOException {
        return JdbcUtils.getJdbcTemplate(getDataSource());
    }
}
