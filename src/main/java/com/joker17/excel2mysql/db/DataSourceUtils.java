package com.joker17.excel2mysql.db;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class DataSourceUtils {

    private static volatile Map<String, SoftReference<DataSource>> DATA_SOURCE_CACHE_MAP = new ConcurrentHashMap<>(16);

    private DataSourceUtils() {
    }

    /**
     * 通过propertiesFile获取dataSource
     *
     * @param propertiesFile
     * @return
     * @throws IOException
     */
    public static DataSource getDataSource(File propertiesFile) throws IOException {
        Properties properties = JdbcUtils.loadProperties(new FileInputStream(propertiesFile));
        return JdbcUtils.getDataSource(properties);
    }

    /**
     * 通过propertiesFile及缓存获取dataSource
     *
     * @param propertiesFile
     * @return
     * @throws IOException
     */
    public static DataSource getDataSourceByCache(File propertiesFile) throws IOException {
        String key = propertiesFile.getAbsolutePath();
        DataSource dataSource = getDataSourceByCache(key);
        if (dataSource == null) {
            synchronized (DataSourceUtils.class) {
                dataSource = getDataSourceByCache(key);
                if (dataSource == null) {
                    dataSource = initDataSourceByCache(key, propertiesFile);
                }
            }
        }
        return dataSource;
    }

    /**
     * 通过key获取缓存中的dataSource
     *
     * @param key
     * @return
     */
    private static DataSource getDataSourceByCache(String key) {
        SoftReference<DataSource> dataSourceSoftReference = DATA_SOURCE_CACHE_MAP.get(key);
        if (dataSourceSoftReference == null) {
            return null;
        }
        return dataSourceSoftReference.get();
    }

    /**
     * 初始化缓存中的dataSource
     *
     * @param key
     * @param propertiesFile
     * @return
     * @throws IOException
     */
    private static DataSource initDataSourceByCache(String key, File propertiesFile) throws IOException {
        DataSource dataSource = getDataSource(propertiesFile);
        SoftReference<DataSource> dataSourceSoftReference = new SoftReference<>(dataSource);
        DATA_SOURCE_CACHE_MAP.put(key, dataSourceSoftReference);
        return dataSource;
    }
}
