package com.joker17.excel2mysql.core;

import com.joker17.excel2mysql.db.MysqlUtils;
import com.joker17.excel2mysql.enums.ColumnKeyTypeEnum;
import com.joker17.excel2mysql.helper.Excel2MysqlHelper;
import com.joker17.excel2mysql.model.MysqlColumnModel;
import com.joker17.excel2mysql.support.DbTestHelper;
import com.joker17.excel2mysql.support.DynamicIgnoredRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.util.List;

@RunWith(DynamicIgnoredRunner.class)
@FixMethodOrder(MethodSorters.JVM)
public class MysqlTableColumnKeyTest {

    private JdbcTemplate jdbcTemplate;

    @Before
    public void before() throws IOException {
        jdbcTemplate = DbTestHelper.getJdbcTemplate();
    }

    @Test
    public void assertColumnKey_PrimaryKey1() {
        String database = MysqlUtils.getDatabase(jdbcTemplate);
        String tableName = "t_company_test_m";
        MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
        MysqlUtils.execute(jdbcTemplate, String.format("CREATE TABLE `%s` (\n" +
                "  `name` varchar(64) NOT NULL COMMENT '名称',\n" +
                "  `code` varchar(32) NOT NULL COMMENT '编码',\n" +
                "  `create_date` datetime DEFAULT NULL COMMENT '创建时间',\n" +
                "  `update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',\n" +
                "  PRIMARY KEY (`name`),\n" +
                "  UNIQUE KEY `uk_name_code` (`name`,`code`)\n" +
                ") ENGINE=InnoDB;", tableName));

        List<MysqlColumnModel> columnModelList = Excel2MysqlHelper.getMysqlColumnModelList(jdbcTemplate, database, tableName);

        Assert.assertNotNull(columnModelList);
        Assert.assertNotNull(columnModelList.size() == 4);

        Assert.assertEquals("name", columnModelList.get(0).getColumnName());
        Assert.assertEquals("名称", columnModelList.get(0).getColumnComment());
        Assert.assertEquals(ColumnKeyTypeEnum.Constants.PRIMARY_KEY, columnModelList.get(0).getColumnKeyType());

        Assert.assertEquals("code", columnModelList.get(1).getColumnName());
        Assert.assertEquals("编码", columnModelList.get(1).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(1).getColumnKeyType());

        Assert.assertEquals("create_date", columnModelList.get(2).getColumnName());
        Assert.assertEquals("创建时间", columnModelList.get(2).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(2).getColumnKeyType());

        Assert.assertEquals("update_date", columnModelList.get(3).getColumnName());
        Assert.assertEquals("更新时间", columnModelList.get(3).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(3).getColumnKeyType());

        MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
    }

    @Test
    public void assertColumnKey_PrimaryKey2() {
        String database = MysqlUtils.getDatabase(jdbcTemplate);
        String tableName = "t_company_test_m";
        MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
        MysqlUtils.execute(jdbcTemplate, String.format("CREATE TABLE `%s` (\n" +
                "  `name` varchar(64) NOT NULL COMMENT '名称',\n" +
                "  `code` varchar(32) NOT NULL COMMENT '编码',\n" +
                "  `create_date` datetime DEFAULT NULL COMMENT '创建时间',\n" +
                "  `update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',\n" +
                "  PRIMARY KEY (`name`,`code`),\n" +
                "  UNIQUE KEY `uk_name_code` (`name`,`code`)\n" +
                ") ENGINE=InnoDB;", tableName));

        List<MysqlColumnModel> columnModelList = Excel2MysqlHelper.getMysqlColumnModelList(jdbcTemplate, database, tableName);

        Assert.assertNotNull(columnModelList);
        Assert.assertNotNull(columnModelList.size() == 4);

        Assert.assertEquals("name", columnModelList.get(0).getColumnName());
        Assert.assertEquals("名称", columnModelList.get(0).getColumnComment());
        Assert.assertEquals(ColumnKeyTypeEnum.Constants.PRIMARY_KEY, columnModelList.get(0).getColumnKeyType());

        Assert.assertEquals("code", columnModelList.get(1).getColumnName());
        Assert.assertEquals("编码", columnModelList.get(1).getColumnComment());
        Assert.assertEquals(ColumnKeyTypeEnum.Constants.PRIMARY_KEY, columnModelList.get(1).getColumnKeyType());

        Assert.assertEquals("create_date", columnModelList.get(2).getColumnName());
        Assert.assertEquals("创建时间", columnModelList.get(2).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(2).getColumnKeyType());

        Assert.assertEquals("update_date", columnModelList.get(3).getColumnName());
        Assert.assertEquals("更新时间", columnModelList.get(3).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(3).getColumnKeyType());

        MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
    }

    @Test
    public void assertColumnKey_UniqueKey1() {
        String database = MysqlUtils.getDatabase(jdbcTemplate);
        String tableName = "t_company_test_m";
        MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
        MysqlUtils.execute(jdbcTemplate, String.format("CREATE TABLE `%s` (\n" +
                "  `name` varchar(64) NOT NULL COMMENT '名称',\n" +
                "  `code` varchar(32) NOT NULL COMMENT '编码',\n" +
                "  `create_date` datetime DEFAULT NULL COMMENT '创建时间',\n" +
                "  `update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',\n" +
                "  UNIQUE KEY `uk_name` (`name`)\n" +
                ") ENGINE=InnoDB;", tableName));

        List<MysqlColumnModel> columnModelList = Excel2MysqlHelper.getMysqlColumnModelList(jdbcTemplate, database, tableName);

        Assert.assertNotNull(columnModelList);
        Assert.assertNotNull(columnModelList.size() == 4);

        Assert.assertEquals("name", columnModelList.get(0).getColumnName());
        Assert.assertEquals("名称", columnModelList.get(0).getColumnComment());
        Assert.assertEquals(ColumnKeyTypeEnum.Constants.UNIQUE_KEY, columnModelList.get(0).getColumnKeyType());

        Assert.assertEquals("code", columnModelList.get(1).getColumnName());
        Assert.assertEquals("编码", columnModelList.get(1).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(1).getColumnKeyType());

        Assert.assertEquals("create_date", columnModelList.get(2).getColumnName());
        Assert.assertEquals("创建时间", columnModelList.get(2).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(2).getColumnKeyType());

        Assert.assertEquals("update_date", columnModelList.get(3).getColumnName());
        Assert.assertEquals("更新时间", columnModelList.get(3).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(3).getColumnKeyType());

        MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
    }

    @Test
    public void assertColumnKey_UniqueKey2() {
        String database = MysqlUtils.getDatabase(jdbcTemplate);
        String tableName = "t_company_test_m";
        MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
        MysqlUtils.execute(jdbcTemplate, String.format("CREATE TABLE `%s` (\n" +
                "  `name` varchar(64) NOT NULL COMMENT '名称',\n" +
                "  `code` varchar(32) NOT NULL COMMENT '编码',\n" +
                "  `create_date` datetime DEFAULT NULL COMMENT '创建时间',\n" +
                "  `update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',\n" +
                "  UNIQUE KEY `uk_name` (`name`),\n" +
                "  UNIQUE KEY `uk_code` (`code`)\n" +
                ") ENGINE=InnoDB;", tableName));

        List<MysqlColumnModel> columnModelList = Excel2MysqlHelper.getMysqlColumnModelList(jdbcTemplate, database, tableName);

        Assert.assertNotNull(columnModelList);
        Assert.assertNotNull(columnModelList.size() == 4);

        Assert.assertEquals("name", columnModelList.get(0).getColumnName());
        Assert.assertEquals("名称", columnModelList.get(0).getColumnComment());
        Assert.assertEquals(ColumnKeyTypeEnum.Constants.UNIQUE_KEY, columnModelList.get(0).getColumnKeyType());

        Assert.assertEquals("code", columnModelList.get(1).getColumnName());
        Assert.assertEquals("编码", columnModelList.get(1).getColumnComment());
        Assert.assertEquals(ColumnKeyTypeEnum.Constants.UNIQUE_KEY, columnModelList.get(1).getColumnKeyType());

        Assert.assertEquals("create_date", columnModelList.get(2).getColumnName());
        Assert.assertEquals("创建时间", columnModelList.get(2).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(2).getColumnKeyType());

        Assert.assertEquals("update_date", columnModelList.get(3).getColumnName());
        Assert.assertEquals("更新时间", columnModelList.get(3).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(3).getColumnKeyType());

        MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
    }

    @Test
    public void assertColumnKey_OtherKey1() {
        String database = MysqlUtils.getDatabase(jdbcTemplate);
        String tableName = "t_company_test_m";
        MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
        MysqlUtils.execute(jdbcTemplate, String.format("CREATE TABLE `%s` (\n" +
                "  `name` varchar(64) NOT NULL COMMENT '名称',\n" +
                "  `code` varchar(32) NOT NULL COMMENT '编码',\n" +
                "  `create_date` datetime DEFAULT NULL COMMENT '创建时间',\n" +
                "  `update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'\n" +
                ") ENGINE=InnoDB;", tableName));

        List<MysqlColumnModel> columnModelList = Excel2MysqlHelper.getMysqlColumnModelList(jdbcTemplate, database, tableName);

        Assert.assertNotNull(columnModelList);
        Assert.assertNotNull(columnModelList.size() == 4);

        Assert.assertEquals("name", columnModelList.get(0).getColumnName());
        Assert.assertEquals("名称", columnModelList.get(0).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(0).getColumnKeyType());

        Assert.assertEquals("code", columnModelList.get(1).getColumnName());
        Assert.assertEquals("编码", columnModelList.get(1).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(1).getColumnKeyType());

        Assert.assertEquals("create_date", columnModelList.get(2).getColumnName());
        Assert.assertEquals("创建时间", columnModelList.get(2).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(2).getColumnKeyType());

        Assert.assertEquals("update_date", columnModelList.get(3).getColumnName());
        Assert.assertEquals("更新时间", columnModelList.get(3).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(3).getColumnKeyType());

        MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
    }

    @Test
    public void assertColumnKey_OtherKey2() {
        String database = MysqlUtils.getDatabase(jdbcTemplate);
        String tableName = "t_company_test_m";
        MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
        MysqlUtils.execute(jdbcTemplate, String.format("CREATE TABLE `%s` (\n" +
                "  `name` varchar(64) NOT NULL COMMENT '名称',\n" +
                "  `code` varchar(32) NOT NULL COMMENT '编码',\n" +
                "  `create_date` datetime DEFAULT NULL COMMENT '创建时间',\n" +
                "  `update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',\n" +
                "  UNIQUE KEY `uk_name_code` (`name`,`code`)\n" +
                ") ENGINE=InnoDB;", tableName));

        List<MysqlColumnModel> columnModelList = Excel2MysqlHelper.getMysqlColumnModelList(jdbcTemplate, database, tableName);

        Assert.assertNotNull(columnModelList);
        Assert.assertNotNull(columnModelList.size() == 4);

        Assert.assertEquals("name", columnModelList.get(0).getColumnName());
        Assert.assertEquals("名称", columnModelList.get(0).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(0).getColumnKeyType());

        Assert.assertEquals("code", columnModelList.get(1).getColumnName());
        Assert.assertEquals("编码", columnModelList.get(1).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(1).getColumnKeyType());

        Assert.assertEquals("create_date", columnModelList.get(2).getColumnName());
        Assert.assertEquals("创建时间", columnModelList.get(2).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(2).getColumnKeyType());

        Assert.assertEquals("update_date", columnModelList.get(3).getColumnName());
        Assert.assertEquals("更新时间", columnModelList.get(3).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(3).getColumnKeyType());

        MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
    }

    @Test
    public void assertColumnKey_PrimaryAndUniqueKey1() {
        String database = MysqlUtils.getDatabase(jdbcTemplate);
        String tableName = "t_company_test_m";
        MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
        MysqlUtils.execute(jdbcTemplate, String.format("CREATE TABLE `%s` (\n" +
                "  `name` varchar(64) NOT NULL COMMENT '名称',\n" +
                "  `code` varchar(32) NOT NULL COMMENT '编码',\n" +
                "  `create_date` datetime DEFAULT NULL COMMENT '创建时间',\n" +
                "  `update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',\n" +
                "  PRIMARY KEY (`name`),\n" +
                "  UNIQUE KEY `uk_name` (`name`)\n" +
                ") ENGINE=InnoDB;", tableName));

        List<MysqlColumnModel> columnModelList = Excel2MysqlHelper.getMysqlColumnModelList(jdbcTemplate, database, tableName);

        Assert.assertNotNull(columnModelList);
        Assert.assertNotNull(columnModelList.size() == 4);

        Assert.assertEquals("name", columnModelList.get(0).getColumnName());
        Assert.assertEquals("名称", columnModelList.get(0).getColumnComment());
        Assert.assertEquals(ColumnKeyTypeEnum.Constants.PRIMARY_AND_UNIQUE_KEY, columnModelList.get(0).getColumnKeyType());

        Assert.assertEquals("code", columnModelList.get(1).getColumnName());
        Assert.assertEquals("编码", columnModelList.get(1).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(1).getColumnKeyType());

        Assert.assertEquals("create_date", columnModelList.get(2).getColumnName());
        Assert.assertEquals("创建时间", columnModelList.get(2).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(2).getColumnKeyType());

        Assert.assertEquals("update_date", columnModelList.get(3).getColumnName());
        Assert.assertEquals("更新时间", columnModelList.get(3).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(3).getColumnKeyType());

        MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
    }

    @Test
    public void assertColumnKey_PrimaryAndUniqueKey2() {
        String database = MysqlUtils.getDatabase(jdbcTemplate);
        String tableName = "t_company_test_m";
        MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
        MysqlUtils.execute(jdbcTemplate, String.format("CREATE TABLE `%s` (\n" +
                "  `name` varchar(64) NOT NULL COMMENT '名称',\n" +
                "  `code` varchar(32) NOT NULL COMMENT '编码',\n" +
                "  `create_date` datetime DEFAULT NULL COMMENT '创建时间',\n" +
                "  `update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',\n" +
                "  PRIMARY KEY (`name`),\n" +
                "  UNIQUE KEY `uk_name` (`name`),\n" +
                "  UNIQUE KEY `uk_name_code` (`name`,`code`)\n" +
                ") ENGINE=InnoDB;", tableName));

        List<MysqlColumnModel> columnModelList = Excel2MysqlHelper.getMysqlColumnModelList(jdbcTemplate, database, tableName);

        Assert.assertNotNull(columnModelList);
        Assert.assertNotNull(columnModelList.size() == 4);

        Assert.assertEquals("name", columnModelList.get(0).getColumnName());
        Assert.assertEquals("名称", columnModelList.get(0).getColumnComment());
        Assert.assertEquals(ColumnKeyTypeEnum.Constants.PRIMARY_AND_UNIQUE_KEY, columnModelList.get(0).getColumnKeyType());

        Assert.assertEquals("code", columnModelList.get(1).getColumnName());
        Assert.assertEquals("编码", columnModelList.get(1).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(1).getColumnKeyType());

        Assert.assertEquals("create_date", columnModelList.get(2).getColumnName());
        Assert.assertEquals("创建时间", columnModelList.get(2).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(2).getColumnKeyType());

        Assert.assertEquals("update_date", columnModelList.get(3).getColumnName());
        Assert.assertEquals("更新时间", columnModelList.get(3).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(3).getColumnKeyType());

        MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
    }

    @Test
    public void assertColumnKey_PrimaryAndUniqueKey3() {
        String database = MysqlUtils.getDatabase(jdbcTemplate);
        String tableName = "t_company_test_m";
        MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
        MysqlUtils.execute(jdbcTemplate, String.format("CREATE TABLE `%s` (\n" +
                "  `name` varchar(64) NOT NULL COMMENT '名称',\n" +
                "  `code` varchar(32) NOT NULL COMMENT '编码',\n" +
                "  `create_date` datetime DEFAULT NULL COMMENT '创建时间',\n" +
                "  `update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',\n" +
                "  PRIMARY KEY (`name`),\n" +
                "  UNIQUE KEY `uk_name` (`name`),\n" +
                "  UNIQUE KEY `uk_code` (`code`),\n" +
                "  UNIQUE KEY `uk_name_code` (`name`,`code`)\n" +
                ") ENGINE=InnoDB;", tableName));

        List<MysqlColumnModel> columnModelList = Excel2MysqlHelper.getMysqlColumnModelList(jdbcTemplate, database, tableName);

        Assert.assertNotNull(columnModelList);
        Assert.assertNotNull(columnModelList.size() == 4);

        Assert.assertEquals("name", columnModelList.get(0).getColumnName());
        Assert.assertEquals("名称", columnModelList.get(0).getColumnComment());
        Assert.assertEquals(ColumnKeyTypeEnum.Constants.PRIMARY_AND_UNIQUE_KEY, columnModelList.get(0).getColumnKeyType());

        Assert.assertEquals("code", columnModelList.get(1).getColumnName());
        Assert.assertEquals("编码", columnModelList.get(1).getColumnComment());
        Assert.assertEquals(ColumnKeyTypeEnum.Constants.UNIQUE_KEY, columnModelList.get(1).getColumnKeyType());

        Assert.assertEquals("create_date", columnModelList.get(2).getColumnName());
        Assert.assertEquals("创建时间", columnModelList.get(2).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(2).getColumnKeyType());

        Assert.assertEquals("update_date", columnModelList.get(3).getColumnName());
        Assert.assertEquals("更新时间", columnModelList.get(3).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(3).getColumnKeyType());

        MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
    }

    @Test
    public void assertColumnKey_PrimaryAndUniqueKey4() {
        String database = MysqlUtils.getDatabase(jdbcTemplate);
        String tableName = "t_company_test_m";
        MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
        MysqlUtils.execute(jdbcTemplate, String.format("CREATE TABLE `%s` (\n" +
                "  `name` varchar(64) NOT NULL COMMENT '名称',\n" +
                "  `code` varchar(32) NOT NULL COMMENT '编码',\n" +
                "  `create_date` datetime DEFAULT NULL COMMENT '创建时间',\n" +
                "  `update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',\n" +
                "  PRIMARY KEY (`name`, `code`),\n" +
                "  UNIQUE KEY `uk_code` (`code`)\n" +
                ") ENGINE=InnoDB;", tableName));

        List<MysqlColumnModel> columnModelList = Excel2MysqlHelper.getMysqlColumnModelList(jdbcTemplate, database, tableName);

        Assert.assertNotNull(columnModelList);
        Assert.assertNotNull(columnModelList.size() == 4);

        Assert.assertEquals("name", columnModelList.get(0).getColumnName());
        Assert.assertEquals("名称", columnModelList.get(0).getColumnComment());
        Assert.assertEquals(ColumnKeyTypeEnum.Constants.PRIMARY_KEY, columnModelList.get(0).getColumnKeyType());

        Assert.assertEquals("code", columnModelList.get(1).getColumnName());
        Assert.assertEquals("编码", columnModelList.get(1).getColumnComment());
        Assert.assertEquals(ColumnKeyTypeEnum.Constants.PRIMARY_AND_UNIQUE_KEY, columnModelList.get(1).getColumnKeyType());

        Assert.assertEquals("create_date", columnModelList.get(2).getColumnName());
        Assert.assertEquals("创建时间", columnModelList.get(2).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(2).getColumnKeyType());

        Assert.assertEquals("update_date", columnModelList.get(3).getColumnName());
        Assert.assertEquals("更新时间", columnModelList.get(3).getColumnComment());
        Assert.assertEquals(null, columnModelList.get(3).getColumnKeyType());

        MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
    }
}
