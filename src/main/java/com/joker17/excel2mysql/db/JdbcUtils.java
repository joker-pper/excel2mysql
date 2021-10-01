package com.joker17.excel2mysql.db;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class JdbcUtils {

    private JdbcUtils() {
    }

    /**
     * 加载properties
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static Properties loadProperties(InputStream is) throws IOException {
        Properties properties = new Properties();
        try {
            properties.load(is);
        } finally {
            is.close();
        }
        return properties;
    }

    /**
     * 获取data source
     *
     * @param properties
     * @return
     */
    public static DataSource getDataSource(Properties properties) {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setUsername(properties.getProperty("datasource.username"));
        hikariDataSource.setPassword(properties.getProperty("datasource.password"));
        hikariDataSource.setJdbcUrl(properties.getProperty("datasource.url"));
        hikariDataSource.setDriverClassName(properties.getProperty("datasource.driver-class-name"));

        hikariDataSource.setMinimumIdle(Optional.ofNullable(properties.getProperty("datasource.minimum-idle"))
                .map(Integer::valueOf).orElse(1));
        hikariDataSource.setMaximumPoolSize(Optional.ofNullable(properties.getProperty("datasource.maximum-pool-size"))
                .map(Integer::valueOf).orElse(20));
        Optional.ofNullable(properties.getProperty("datasource.connection-timeout"))
                .ifPresent((it) -> hikariDataSource.setConnectionTimeout(Long.valueOf(it)));
        return hikariDataSource;
    }

    /**
     * 获取jdbc template
     *
     * @param dataSource
     * @return
     */
    public static JdbcTemplate getJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }


}
