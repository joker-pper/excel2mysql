package com.joker17.excel2mysql.helper;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class MysqlBoostHelperTest {

    @Test
    public void isMatchResolveTableNameByExclude() {
        //测试排除: 未匹配到时将被处理
        assertSame(false, MysqlBoostHelper.isMatchResolveTableName(Arrays.asList("user", "role"), "user", true));
        assertSame(true, MysqlBoostHelper.isMatchResolveTableName(Arrays.asList("user", "role"), "user_role", true));
        assertSame(false, MysqlBoostHelper.isMatchResolveTableName(Arrays.asList("user|user_((?!00)[\\d]{1,2})", "role"), "user", true));
        assertSame(true, MysqlBoostHelper.isMatchResolveTableName(Arrays.asList("user|user_((?!00)[\\d]{1,2})", "role"), "user_role", true));
    }

    @Test
    public void isMatchResolveTableNameByInclude() {
        //测试包含: 匹配到时将被处理
        assertSame(true, MysqlBoostHelper.isMatchResolveTableName(Arrays.asList("user", "role"), "user", false));
        assertSame(false, MysqlBoostHelper.isMatchResolveTableName(Arrays.asList("user", "role"), "user_role", false));
        assertSame(true, MysqlBoostHelper.isMatchResolveTableName(Arrays.asList("user|user_((?!00)[\\d]{1,2})", "role"), "user", false));
        assertSame(false, MysqlBoostHelper.isMatchResolveTableName(Arrays.asList("user|user_((?!00)[\\d]{1,2})", "role"), "user_role", false));
    }

    @Test
    public void isMatches() {
        assertSame(true, MysqlBoostHelper.isMatches(Arrays.asList("user", "role"), "user"));
        assertSame(false, MysqlBoostHelper.isMatches(Arrays.asList("user", "role"), "user123"));

        //验证必须是user或以user_开头并且范围在01-99
        String specialUserMatchRegex = "user|user_((?!00)[\\d]{1,2})";
        assertSame(true, MysqlBoostHelper.isMatches(Arrays.asList(specialUserMatchRegex, "role"), "user"));
        assertSame(false, MysqlBoostHelper.isMatches(Arrays.asList(specialUserMatchRegex, "role"), "user_"));
        assertSame(false, MysqlBoostHelper.isMatches(Arrays.asList(specialUserMatchRegex, "role"), "user_00"));
        assertSame(true, MysqlBoostHelper.isMatches(Arrays.asList(specialUserMatchRegex, "role"), "user_01"));
        for (int i = 1; i < 100; i++) {
            assertSame(true, MysqlBoostHelper.isMatches(Arrays.asList(specialUserMatchRegex, "role"), "user_" + String.format("%02d", i)));
        }
        assertSame(false, MysqlBoostHelper.isMatches(Arrays.asList(specialUserMatchRegex, "role"), "user_100"));

    }
}