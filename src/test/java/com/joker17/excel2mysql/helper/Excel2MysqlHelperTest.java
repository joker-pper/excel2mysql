package com.joker17.excel2mysql.helper;

import com.joker17.excel2mysql.db.MysqlUtils;
import com.joker17.excel2mysql.model.MysqlColumnModel;
import com.joker17.excel2mysql.support.DbTestHelper;
import com.joker17.excel2mysql.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private void checkGetColumnOrderPositionSqlTextMapResult(List<String> beforeColumnNameOrderList, List<String> finalColumnNameOrderList, Map<String, String> beforeColumnName2FinalColumnNameMap, Map<String, String> resultMap, Map<String, String> expectedResultMap) {

        //检查结果一致
        Assert.assertEquals(expectedResultMap.toString(), resultMap.toString());

        String tableName = "t_column_order_position_test_m";
        JdbcTemplate jdbcTemplate = null;
        try {
            jdbcTemplate = DbTestHelper.getJdbcTemplate();
            MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));

            String columnDescribe = "varchar(64) NOT NULL";
            StringBuilder stringBuilder = new StringBuilder();
            beforeColumnNameOrderList.forEach(it -> stringBuilder.append(String.format("`%s` %s,\n", it, columnDescribe)));

            String tableContent = StringUtils.removeEnd(stringBuilder.toString(), ",\n");
            MysqlUtils.execute(jdbcTemplate, String.format("CREATE TABLE `%s` (\n" +
                    "%s" +
                    ") ENGINE=InnoDB;", tableName, tableContent));

            Map<String, String> finalColumnName2BeforeColumnNameMap = beforeColumnName2FinalColumnNameMap.entrySet().stream()
                    .filter(it -> it.getValue() != null).collect(Collectors.collectingAndThen(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey),
                            Function.identity()));

            //先删除列
            List<String> dropColumnList = new ArrayList<>(8);
            beforeColumnName2FinalColumnNameMap.forEach((beforeColumn, afterColumn) -> {
                if (afterColumn == null) {
                    //该字段被删除
                    dropColumnList.add(beforeColumn);
                }
            });

            boolean isDropAllColumn = false;
            if (!dropColumnList.isEmpty()) {
                isDropAllColumn = dropColumnList.size() == beforeColumnNameOrderList.size();
                if (!isDropAllColumn) {
                    //非删除全列时直接删除
                    MysqlUtils.execute(jdbcTemplate, Excel2MysqlHelper.getTableDropColumnNamesSql(tableName, dropColumnList));
                }
            }

            //处理列变更
            JdbcTemplate finalJdbcTemplate = jdbcTemplate;
            finalColumnNameOrderList.forEach(finalColumnNameOrder -> {
                String sql;
                String beforeColumnName = finalColumnName2BeforeColumnNameMap.get(finalColumnNameOrder);
                String columnDefinition = String.format("`%s` %s", finalColumnNameOrder, columnDescribe);
                String columnOrderPositionSqlText = resultMap.get(finalColumnNameOrder);
                if (columnOrderPositionSqlText != null) {
                    columnDefinition = String.format("%s %s", columnDefinition, columnOrderPositionSqlText);
                }
                if (beforeColumnName == null) {
                    //新增
                    sql = Excel2MysqlHelper.getTableAddColumnNameSql(tableName, columnDefinition);
                } else {
                    //变更
                    sql = Excel2MysqlHelper.getTableChangeColumnNameSql(tableName, beforeColumnName, columnDefinition);
                }
                MysqlUtils.execute(finalJdbcTemplate, sql);
            });

            if (isDropAllColumn) {
                //删除全列时最后再删除
                MysqlUtils.execute(jdbcTemplate, Excel2MysqlHelper.getTableDropColumnNamesSql(tableName, dropColumnList));
            }

            //验证查询出的列顺序与当前的列顺序一致
            List<MysqlColumnModel> mysqlColumnModelList = Excel2MysqlHelper.getMysqlColumnModelList(jdbcTemplate, MysqlUtils.getDatabase(jdbcTemplate), tableName);
            Assert.assertEquals(finalColumnNameOrderList, mysqlColumnModelList.stream().map(MysqlColumnModel::getColumnName).collect(Collectors.toList()));
        } catch (IOException e) {
            System.err.println("get jdbc template error: " + e.getMessage());
        } finally {
            if (jdbcTemplate != null) {
                MysqlUtils.execute(jdbcTemplate, String.format("DROP TABLE IF EXISTS `%s`;", tableName));
            }
        }

    }

    @Test
    public void testGetColumnOrderPositionSqlTextMap() {
        //新增列在最前面
        {
            List<String> beforeColumnNameOrderList = Arrays.asList("a", "b");
            List<String> finalColumnNameOrderList = Arrays.asList("c", "a", "b");
            Map<String, String> beforeColumnName2FinalColumnNameMap = new HashMap<>(16);
            beforeColumnNameOrderList.forEach(it -> beforeColumnName2FinalColumnNameMap.put(it, it));
            Map<String, String> resultMap = Excel2MysqlHelper.getColumnOrderPositionSqlTextMap(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap);
            Map<String, String> expectedResultMap = new LinkedHashMap<>(16);
            expectedResultMap.put("c", "FIRST");
            expectedResultMap.put("a", "AFTER `c`");
            expectedResultMap.put("b", null);

            checkGetColumnOrderPositionSqlTextMapResult(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap, resultMap, expectedResultMap);
        }

        //新增列在最前面(之前列名变更为新的)

        {
            List<String> beforeColumnNameOrderList = Arrays.asList("a", "b");
            List<String> finalColumnNameOrderList = Arrays.asList("C", "A", "B");

            Map<String, String> beforeColumnName2FinalColumnNameMap = new HashMap<>(16);
            beforeColumnName2FinalColumnNameMap.put("a", "A");
            beforeColumnName2FinalColumnNameMap.put("b", "B");

            Map<String, String> resultMap = Excel2MysqlHelper.getColumnOrderPositionSqlTextMap(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap);
            Map<String, String> expectedResultMap = new LinkedHashMap<>(16);
            expectedResultMap.put("C", "FIRST");
            expectedResultMap.put("A", "AFTER `C`");
            expectedResultMap.put("B", null);
            checkGetColumnOrderPositionSqlTextMapResult(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap, resultMap, expectedResultMap);
        }

        //调整列位置顺序(之前列名变更为新的)

        {
            List<String> beforeColumnNameOrderList = Arrays.asList("a", "b", "c");
            List<String> finalColumnNameOrderList = Arrays.asList("C", "A", "B");

            Map<String, String> beforeColumnName2FinalColumnNameMap = new HashMap<>(16);
            beforeColumnName2FinalColumnNameMap.put("a", "A");
            beforeColumnName2FinalColumnNameMap.put("b", "B");
            beforeColumnName2FinalColumnNameMap.put("c", "C");

            Map<String, String> resultMap = Excel2MysqlHelper.getColumnOrderPositionSqlTextMap(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap);
            Map<String, String> expectedResultMap = new LinkedHashMap<>(16);
            expectedResultMap.put("C", "FIRST");
            expectedResultMap.put("A", "AFTER `C`");
            expectedResultMap.put("B", null);
            checkGetColumnOrderPositionSqlTextMapResult(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap, resultMap, expectedResultMap);
        }

        //新增列在中间位置(之前列名变更为新的)

        {
            List<String> beforeColumnNameOrderList = Arrays.asList("a", "b", "c");
            List<String> finalColumnNameOrderList = Arrays.asList("A", "B", "AA", "BB", "C");

            Map<String, String> beforeColumnName2FinalColumnNameMap = new HashMap<>(16);
            beforeColumnName2FinalColumnNameMap.put("a", "A");
            beforeColumnName2FinalColumnNameMap.put("b", "B");
            beforeColumnName2FinalColumnNameMap.put("c", "C");

            Map<String, String> resultMap = Excel2MysqlHelper.getColumnOrderPositionSqlTextMap(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap);
            Map<String, String> expectedResultMap = new LinkedHashMap<>(16);
            expectedResultMap.put("A", null);
            expectedResultMap.put("B", null);
            expectedResultMap.put("AA", "AFTER `B`");

            expectedResultMap.put("BB", "AFTER `AA`");
            expectedResultMap.put("C", "AFTER `BB`");

            checkGetColumnOrderPositionSqlTextMapResult(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap, resultMap, expectedResultMap);
        }

        //新增列在后面位置(之前列名变更为新的)

        {
            List<String> beforeColumnNameOrderList = Arrays.asList("a", "b", "c");
            List<String> finalColumnNameOrderList = Arrays.asList("A", "B", "C", "AA", "BB");

            Map<String, String> beforeColumnName2FinalColumnNameMap = new HashMap<>(16);
            beforeColumnName2FinalColumnNameMap.put("a", "A");
            beforeColumnName2FinalColumnNameMap.put("b", "B");
            beforeColumnName2FinalColumnNameMap.put("c", "C");

            Map<String, String> resultMap = Excel2MysqlHelper.getColumnOrderPositionSqlTextMap(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap);
            Map<String, String> expectedResultMap = new LinkedHashMap<>(16);
            expectedResultMap.put("A", null);
            expectedResultMap.put("B", null);
            expectedResultMap.put("C", null);
/*            expectedResultMap.put("AA", "AFTER `C`");
            expectedResultMap.put("BB", "AFTER `AA`");*/
            expectedResultMap.put("AA", null);
            expectedResultMap.put("BB", null);
            checkGetColumnOrderPositionSqlTextMapResult(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap, resultMap, expectedResultMap);
        }

        //新增列在后面位置(之前列名变更为新的,且顺序变更)

        {
            List<String> beforeColumnNameOrderList = Arrays.asList("a", "c", "b");
            List<String> finalColumnNameOrderList = Arrays.asList("A", "B", "C", "AA", "BB");

            Map<String, String> beforeColumnName2FinalColumnNameMap = new HashMap<>(16);
            beforeColumnName2FinalColumnNameMap.put("a", "A");
            beforeColumnName2FinalColumnNameMap.put("b", "B");
            beforeColumnName2FinalColumnNameMap.put("c", "C");

            Map<String, String> resultMap = Excel2MysqlHelper.getColumnOrderPositionSqlTextMap(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap);
            Map<String, String> expectedResultMap = new LinkedHashMap<>(16);
            expectedResultMap.put("A", null);
            expectedResultMap.put("B", "AFTER `A`");
            expectedResultMap.put("C", "AFTER `B`");
/*            expectedResultMap.put("AA", "AFTER `C`");
            expectedResultMap.put("BB", "AFTER `AA`");*/
            expectedResultMap.put("AA", null);
            expectedResultMap.put("BB", null);
            checkGetColumnOrderPositionSqlTextMapResult(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap, resultMap, expectedResultMap);
        }

        //新增列在后面位置(之前列名变更为新的,且顺序变更及列移除)

        {
            List<String> beforeColumnNameOrderList = Arrays.asList("a", "c", "b", "d");
            List<String> finalColumnNameOrderList = Arrays.asList("A", "B", "C", "AA", "BB");

            Map<String, String> beforeColumnName2FinalColumnNameMap = new HashMap<>(16);
            beforeColumnName2FinalColumnNameMap.put("a", "A");
            beforeColumnName2FinalColumnNameMap.put("b", "B");
            beforeColumnName2FinalColumnNameMap.put("c", "C");
            beforeColumnName2FinalColumnNameMap.put("d", null);

            Map<String, String> resultMap = Excel2MysqlHelper.getColumnOrderPositionSqlTextMap(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap);
            Map<String, String> expectedResultMap = new LinkedHashMap<>(16);
            expectedResultMap.put("A", null);
            expectedResultMap.put("B", "AFTER `A`");
            expectedResultMap.put("C", "AFTER `B`");
/*            expectedResultMap.put("AA", "AFTER `C`");
            expectedResultMap.put("BB", "AFTER `AA`");*/
            expectedResultMap.put("AA", null);
            expectedResultMap.put("BB", null);

            checkGetColumnOrderPositionSqlTextMapResult(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap, resultMap, expectedResultMap);
        }

        //新增列在前面和后面位置(之前列名变更为新的)

        {
            List<String> beforeColumnNameOrderList = Arrays.asList("a", "c", "b");
            List<String> finalColumnNameOrderList = Arrays.asList("CC", "A", "B", "C", "AA", "BB");

            Map<String, String> beforeColumnName2FinalColumnNameMap = new HashMap<>(16);
            beforeColumnName2FinalColumnNameMap.put("a", "A");
            beforeColumnName2FinalColumnNameMap.put("b", "B");
            beforeColumnName2FinalColumnNameMap.put("c", "C");

            Map<String, String> resultMap = Excel2MysqlHelper.getColumnOrderPositionSqlTextMap(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap);
            Map<String, String> expectedResultMap = new LinkedHashMap<>(16);
            expectedResultMap.put("CC", "FIRST");
            expectedResultMap.put("A", "AFTER `CC`");

            //当前位置未变化,都处于第三个
            expectedResultMap.put("B", null);

            expectedResultMap.put("C", "AFTER `B`");
            expectedResultMap.put("AA", null);
            expectedResultMap.put("BB", null);

            checkGetColumnOrderPositionSqlTextMapResult(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap, resultMap, expectedResultMap);
        }

        //新增列在前面和后面位置(之前列名变更为新的)

        {
            List<String> beforeColumnNameOrderList = Arrays.asList("a", "b", "c");
            List<String> finalColumnNameOrderList = Arrays.asList("CC", "A", "B", "C", "AA", "BB");

            Map<String, String> beforeColumnName2FinalColumnNameMap = new HashMap<>(16);
            beforeColumnName2FinalColumnNameMap.put("a", "A");
            beforeColumnName2FinalColumnNameMap.put("b", "B");
            beforeColumnName2FinalColumnNameMap.put("c", "C");

            Map<String, String> resultMap = Excel2MysqlHelper.getColumnOrderPositionSqlTextMap(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap);
            Map<String, String> expectedResultMap = new LinkedHashMap<>(16);
            expectedResultMap.put("CC", "FIRST");
            expectedResultMap.put("A", "AFTER `CC`");
            expectedResultMap.put("B", null);
            expectedResultMap.put("C", null);
            expectedResultMap.put("AA", null);
            expectedResultMap.put("BB", null);
            checkGetColumnOrderPositionSqlTextMapResult(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap, resultMap, expectedResultMap);
        }

        //新增列在前面和中间位置(之前列名变更为新的)

        {
            List<String> beforeColumnNameOrderList = Arrays.asList("a", "b", "c");
            List<String> finalColumnNameOrderList = Arrays.asList("CC", "A", "B", "AA", "BB", "C");

            Map<String, String> beforeColumnName2FinalColumnNameMap = new HashMap<>(16);
            beforeColumnName2FinalColumnNameMap.put("a", "A");
            beforeColumnName2FinalColumnNameMap.put("b", "B");
            beforeColumnName2FinalColumnNameMap.put("c", "C");

            Map<String, String> resultMap = Excel2MysqlHelper.getColumnOrderPositionSqlTextMap(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap);
            Map<String, String> expectedResultMap = new LinkedHashMap<>(16);
            expectedResultMap.put("CC", "FIRST");
            expectedResultMap.put("A", "AFTER `CC`");
            expectedResultMap.put("B", null);
            expectedResultMap.put("AA", "AFTER `B`");
            expectedResultMap.put("BB", "AFTER `AA`");
            expectedResultMap.put("C", "AFTER `BB`");
            checkGetColumnOrderPositionSqlTextMapResult(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap, resultMap, expectedResultMap);
        }


        //倒数第二列一致
        {
            List<String> beforeColumnNameOrderList = Arrays.asList("a", "b", "c", "d", "e");
            List<String> finalColumnNameOrderList = Arrays.asList("CC", "e", "B", "AA", "BB", "A", "d", "C");

            Map<String, String> beforeColumnName2FinalColumnNameMap = new HashMap<>(16);
            beforeColumnName2FinalColumnNameMap.put("a", "A");
            beforeColumnName2FinalColumnNameMap.put("b", "B");
            beforeColumnName2FinalColumnNameMap.put("c", "C");
            beforeColumnName2FinalColumnNameMap.put("d", "d");
            beforeColumnName2FinalColumnNameMap.put("e", "e");

            Map<String, String> resultMap = Excel2MysqlHelper.getColumnOrderPositionSqlTextMap(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap);
            Map<String, String> expectedResultMap = new LinkedHashMap<>(16);
            expectedResultMap.put("CC", "FIRST");
            expectedResultMap.put("e", "AFTER `CC`");
            expectedResultMap.put("B", "AFTER `e`");
            expectedResultMap.put("AA", "AFTER `B`");
            expectedResultMap.put("BB", "AFTER `AA`");

            expectedResultMap.put("A", "AFTER `BB`");
            expectedResultMap.put("d", "AFTER `A`");

            expectedResultMap.put("C", "AFTER `d`");
            checkGetColumnOrderPositionSqlTextMapResult(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap, resultMap, expectedResultMap);
        }


        //之前列全部被删除

        {
            List<String> beforeColumnNameOrderList = Arrays.asList("a", "b", "c");
            List<String> finalColumnNameOrderList = Arrays.asList("AA", "BB", "CC");

            Map<String, String> beforeColumnName2FinalColumnNameMap = new HashMap<>(16);
            beforeColumnName2FinalColumnNameMap.put("a", null);
            beforeColumnName2FinalColumnNameMap.put("b", null);
            beforeColumnName2FinalColumnNameMap.put("c", null);

            Map<String, String> resultMap = Excel2MysqlHelper.getColumnOrderPositionSqlTextMap(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap);
            Map<String, String> expectedResultMap = new LinkedHashMap<>(16);
            expectedResultMap.put("AA", null);
            expectedResultMap.put("BB", null);
            expectedResultMap.put("CC", null);

            checkGetColumnOrderPositionSqlTextMapResult(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap, resultMap, expectedResultMap);
        }


        //之前列只留下一个(在最前面)

        {
            List<String> beforeColumnNameOrderList = Arrays.asList("a", "b", "c");
            List<String> finalColumnNameOrderList = Arrays.asList("b", "AA", "BB", "CC");

            Map<String, String> beforeColumnName2FinalColumnNameMap = new HashMap<>(16);
            beforeColumnName2FinalColumnNameMap.put("a", null);
            beforeColumnName2FinalColumnNameMap.put("b", "b");
            beforeColumnName2FinalColumnNameMap.put("c", null);

            Map<String, String> resultMap = Excel2MysqlHelper.getColumnOrderPositionSqlTextMap(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap);
            Map<String, String> expectedResultMap = new LinkedHashMap<>(16);
            expectedResultMap.put("b", null);
            expectedResultMap.put("AA", null);
            expectedResultMap.put("BB", null);
            expectedResultMap.put("CC", null);

            checkGetColumnOrderPositionSqlTextMapResult(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap, resultMap, expectedResultMap);
        }

        //之前列只留下一个(在最后面)
        {
            List<String> beforeColumnNameOrderList = Arrays.asList("a", "b", "c");
            List<String> finalColumnNameOrderList = Arrays.asList("AA", "BB", "CC", "b");

            Map<String, String> beforeColumnName2FinalColumnNameMap = new HashMap<>(16);
            beforeColumnName2FinalColumnNameMap.put("a", null);
            beforeColumnName2FinalColumnNameMap.put("b", "b");
            beforeColumnName2FinalColumnNameMap.put("c", null);

            Map<String, String> resultMap = Excel2MysqlHelper.getColumnOrderPositionSqlTextMap(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap);
            Map<String, String> expectedResultMap = new LinkedHashMap<>(16);
            expectedResultMap.put("AA", "FIRST");
            expectedResultMap.put("BB", "AFTER `AA`");
            expectedResultMap.put("CC", "AFTER `BB`");
            expectedResultMap.put("b", "AFTER `CC`");

            checkGetColumnOrderPositionSqlTextMapResult(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap, resultMap, expectedResultMap);
        }

        //之前列只留下一个(在中间): 字段b处于第二个位置未变化,后两个字段为之后[顺序]新增

        {
            List<String> beforeColumnNameOrderList = Arrays.asList("a", "b", "c");
            List<String> finalColumnNameOrderList = Arrays.asList("AA", "b", "BB", "CC");

            Map<String, String> beforeColumnName2FinalColumnNameMap = new HashMap<>(16);
            beforeColumnName2FinalColumnNameMap.put("a", null);
            beforeColumnName2FinalColumnNameMap.put("b", "b");
            beforeColumnName2FinalColumnNameMap.put("c", null);

            Map<String, String> resultMap = Excel2MysqlHelper.getColumnOrderPositionSqlTextMap(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap);
            Map<String, String> expectedResultMap = new LinkedHashMap<>(16);
            expectedResultMap.put("AA", "FIRST");
            expectedResultMap.put("b", null);
            expectedResultMap.put("BB", null);
            expectedResultMap.put("CC", null);
            checkGetColumnOrderPositionSqlTextMapResult(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap, resultMap, expectedResultMap);
        }

        //之前列只留下一个(在中间): 字段b处于倒数第二个位置

        {
            List<String> beforeColumnNameOrderList = Arrays.asList("a", "b", "c");
            List<String> finalColumnNameOrderList = Arrays.asList("AA", "BB", "b", "CC");

            Map<String, String> beforeColumnName2FinalColumnNameMap = new HashMap<>(16);
            beforeColumnName2FinalColumnNameMap.put("a", null);
            beforeColumnName2FinalColumnNameMap.put("b", "b");
            beforeColumnName2FinalColumnNameMap.put("c", null);

            Map<String, String> resultMap = Excel2MysqlHelper.getColumnOrderPositionSqlTextMap(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap);
            Map<String, String> expectedResultMap = new LinkedHashMap<>(16);
            expectedResultMap.put("AA", "FIRST");
            expectedResultMap.put("BB", "AFTER `AA`");
            expectedResultMap.put("b", "AFTER `BB`");

            //CC为之后[顺序]新增
            expectedResultMap.put("CC", null);
            checkGetColumnOrderPositionSqlTextMapResult(beforeColumnNameOrderList, finalColumnNameOrderList, beforeColumnName2FinalColumnNameMap, resultMap, expectedResultMap);
        }
    }

}