package com.joker17.excel2mysql.helper;

import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertEquals("remark", Excel2MysqlHelper. getFinalColumnName("-->remark"));

        Assert.assertEquals("remark", Excel2MysqlHelper. getFinalColumnName("remarks->(remark)"));
        Assert.assertEquals("remark", Excel2MysqlHelper. getFinalColumnName("(remarks)->(remark)"));
        Assert.assertEquals("remark", Excel2MysqlHelper. getFinalColumnName("remarks->remark"));

        Assert.assertEquals("remark", Excel2MysqlHelper. getFinalColumnName("remark"));
    }
}