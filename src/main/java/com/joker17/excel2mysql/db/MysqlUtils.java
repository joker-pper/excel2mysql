package com.joker17.excel2mysql.db;

import com.joker17.excel2mysql.utils.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

public class MysqlUtils {

    /**
     * 获取当前数据库名称
     *
     * @param jdbcTemplate
     * @return
     */
    public static String getDatabase(JdbcTemplate jdbcTemplate) {
        String sql = "SELECT DATABASE()";
        List<String> resultList = jdbcTemplate.queryForList(sql, String.class);
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    /**
     * 获取表名列表
     *
     * @param jdbcTemplate
     * @return
     */
    public static List<String> getTableNameList(JdbcTemplate jdbcTemplate) {
        String sql = "SHOW TABLE STATUS WHERE 1=1";
        List<Map<String, Object>> resultMapList = jdbcTemplate.queryForList(sql);
        List<String> resultList = new ArrayList<>(32);
        resultMapList.forEach(resultMap -> {
            resultList.add(StringUtils.convertString(resultMap.get("Name")));
        });
        return resultList;
    }

    /**
     * 删除全部的表
     *
     * @param jdbcTemplate
     */
    public static void dropTables(JdbcTemplate jdbcTemplate) {
        List<String> tableNameList = getTableNameList(jdbcTemplate);
        for (String tableName : tableNameList) {
            dropTable(jdbcTemplate, tableName);
        }
    }

    /**
     * 删除table
     *
     * @param jdbcTemplate
     * @param table
     */
    public static void dropTable(JdbcTemplate jdbcTemplate, String table) {
        String sql = String.format("DROP TABLE IF EXISTS `%s`", table);
        execute(jdbcTemplate, sql);
    }


    /**
     * execute sql
     *
     * @param jdbcTemplate
     * @param sql
     */
    public static void execute(JdbcTemplate jdbcTemplate, String sql) {
        jdbcTemplate.execute(sql);
    }

}
