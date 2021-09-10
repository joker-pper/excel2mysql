package com.joker17.excel2mysql.helper;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class Excel2MysqlHelperTest {


    @Test
    public void getBeforeColumnName() {
        //      remarks->(-)             ---> remarks (表示列被移除)
        //      remarks->-               ---> remarks (表示列被移除,支持但不推荐,使用(-)更容易区分)

        //      (-)->remark              ---> null    (表示列新增,即之前列不存在)
        //      -->remark                ---> null    (表示列新增,支持但不推荐,使用(-)更容易区分)

        //      remarks->(remark)        ---> remarks
        //      (remarks)->(remark)      ---> remarks
        //      remarks->remark          ---> remarks

        //      remarks                  ---> remarks

        Assert.assertEquals("remarks", Excel2MysqlHelper.getBeforeColumnName("remarks->(-)"));
        Assert.assertEquals("remarks", Excel2MysqlHelper.getBeforeColumnName("remarks->-"));
        Assert.assertEquals(null, Excel2MysqlHelper.getBeforeColumnName("(-)->remark"));
        Assert.assertEquals(null, Excel2MysqlHelper.getBeforeColumnName("-->remark"));
        Assert.assertEquals("remarks", Excel2MysqlHelper.getBeforeColumnName("remarks->(remark)"));
        Assert.assertEquals("remarks", Excel2MysqlHelper.getBeforeColumnName("(remarks)->(remark)"));
        Assert.assertEquals("remarks", Excel2MysqlHelper.getBeforeColumnName("remarks->remark"));
        Assert.assertEquals("remarks", Excel2MysqlHelper.getBeforeColumnName("remarks"));
    }

    @Test
    public void getFinalColumnName() {

        //      remarks->(-)             ---> null       (表示列被移除)
        //      remarks->-               ---> null       (表示列被移除,支持但不推荐,使用(-)更容易区分)

        //      (-)->remark              ---> remark     (表示列新增,即之前列不存在)
        //      -->remark                ---> remark     (表示列新增,支持但不推荐,使用(-)更容易区分)

        //      remarks->(remark)        ---> remark
        //      (remarks)->(remark)      ---> remark
        //      remarks->remark          ---> remark

        //      remark                   ---> remark

        Assert.assertEquals(null, Excel2MysqlHelper.getFinalColumnName("remarks->(-)"));
        Assert.assertEquals(null, Excel2MysqlHelper.getFinalColumnName("remarks->-"));

        Assert.assertEquals("remark", Excel2MysqlHelper.getFinalColumnName("(-)->remark"));
        Assert.assertEquals("remark", Excel2MysqlHelper.getFinalColumnName("-->remark"));

        Assert.assertEquals("remark", Excel2MysqlHelper.getFinalColumnName("remarks->(remark)"));
        Assert.assertEquals("remark", Excel2MysqlHelper.getFinalColumnName("(remarks)->(remark)"));
        Assert.assertEquals("remark", Excel2MysqlHelper.getFinalColumnName("remarks->remark"));

        Assert.assertEquals("remark", Excel2MysqlHelper.getFinalColumnName("remark"));
    }

    @Test
    public void testBuildKeySql() {

        Assert.assertEquals("ALTER TABLE `user` ADD PRIMARY KEY(`id`);", Excel2MysqlHelper.getTableAddPrimaryKeyIndexSql("user", Arrays.asList("id")));
        Assert.assertEquals("ALTER TABLE `user_role` ADD PRIMARY KEY(`user_id`, `role_id`);", Excel2MysqlHelper.getTableAddPrimaryKeyIndexSql("user_role", Arrays.asList("user_id", "role_id")));

        Assert.assertEquals("ALTER TABLE `user` ADD CONSTRAINT `uk_username` UNIQUE(`username`);", Excel2MysqlHelper.getTableAddUniqueColumnIndexSql("user", "username"));

        Assert.assertEquals("ALTER TABLE `user` DROP PRIMARY KEY;", Excel2MysqlHelper.getTableDropPrimaryKeyIndexSql("user"));
    }

    @Test
    public void testBuildColumnSql() {
        Assert.assertEquals("ALTER TABLE `user` ADD COLUMN `id` bigint NOT NULL AUTO_INCREMENT;", Excel2MysqlHelper.getTableAddColumnNameSql("user", "`id` bigint NOT NULL AUTO_INCREMENT"));
        Assert.assertEquals("ALTER TABLE `user` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT;", Excel2MysqlHelper.getTableModifyColumnNameSql("user", "`id` bigint NOT NULL AUTO_INCREMENT"));
        Assert.assertEquals("ALTER TABLE `user` CHANGE COLUMN `user_id` `id` bigint NOT NULL AUTO_INCREMENT;", Excel2MysqlHelper.getTableChangeColumnNameSql("user", "user_id", "`id` bigint NOT NULL AUTO_INCREMENT"));

        Assert.assertEquals("ALTER TABLE `user` DROP `age`;", Excel2MysqlHelper.getTableDropColumnNamesSql("user", Arrays.asList("age")));
        Assert.assertEquals("ALTER TABLE `user` DROP `age`, DROP `remarks`;", Excel2MysqlHelper.getTableDropColumnNamesSql("user", Arrays.asList("age", "remarks")));
    }

    @Test
    public void testBuildColumnDefinitionSql() {
        //   `id` bigint(20) NOT NULL AUTO_INCREMENT
        Assert.assertEquals("`id` bigint(20) NOT NULL AUTO_INCREMENT", Excel2MysqlHelper.getColumnDefinition("id", "bigint(20)", "Y", null, "auto_increment", null));
        Assert.assertEquals("`id` bigint(20) NOT NULL AUTO_INCREMENT", Excel2MysqlHelper.getColumnDefinition("id", "bigint(20)", "Y", null, "AUTO_INCREMENT", null));

        //   `age` smallint(5) DEFAULT NULL


        Assert.assertEquals("`age` smallint(5) DEFAULT NULL", Excel2MysqlHelper.getColumnDefinition("age", "smallint(5)", "N", null, null, null));

        //   `age` smallint(5) unsigned DEFAULT NULL COMMENT '年龄'
        //   `age` smallint(5) unsigned DEFAULT '0' COMMENT '年龄'
        //   `age` smallint(5) unsigned NOT NULL COMMENT '年龄'
        //   `age` smallint(5) unsigned NOT NULL DEFAULT '0' COMMENT '年龄'
        Assert.assertEquals("`age` smallint(5) unsigned DEFAULT NULL COMMENT '年龄'", Excel2MysqlHelper.getColumnDefinition("age", "smallint(5) unsigned", "N", "", "", "年龄"));
        Assert.assertEquals("`age` smallint(5) unsigned DEFAULT '0' COMMENT '年龄'", Excel2MysqlHelper.getColumnDefinition("age", "smallint(5) unsigned", "N", "0", "", "年龄"));
        Assert.assertEquals("`age` smallint(5) unsigned NOT NULL COMMENT '年龄'", Excel2MysqlHelper.getColumnDefinition("age", "smallint(5) unsigned", "Y", null, "", "年龄"));
        Assert.assertEquals("`age` smallint(5) unsigned NOT NULL DEFAULT '0' COMMENT '年龄'", Excel2MysqlHelper.getColumnDefinition("age", "smallint(5) unsigned", "Y", "0", "", "年龄"));

        //   columnDefaultValue '0' 优先级最高 默认 '0'
        Assert.assertEquals("`age` smallint(5) unsigned NOT NULL DEFAULT '0' COMMENT '年龄'", Excel2MysqlHelper.getColumnDefinition("age", "smallint(5) unsigned", "Y", "'0'", "", "年龄"));


        //   `update_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
        //   `update_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'

        Assert.assertEquals("`update_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'", Excel2MysqlHelper.getColumnDefinition("update_date", "datetime", "Y", "CURRENT_TIMESTAMP", "", "更新时间"));
        Assert.assertEquals("`update_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'", Excel2MysqlHelper.getColumnDefinition("update_date", "datetime", "Y", "CURRENT_TIMESTAMP", "ON UPDATE CURRENT_TIMESTAMP", "更新时间"));

        //   `update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
        Assert.assertEquals("`update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'", Excel2MysqlHelper.getColumnDefinition("update_date", "datetime", "N", "", "ON UPDATE CURRENT_TIMESTAMP", "更新时间"));
        Assert.assertEquals("`update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'", Excel2MysqlHelper.getColumnDefinition("update_date", "datetime", "N", "", "on update CURRENT_TIMESTAMP", "更新时间"));

    }
}