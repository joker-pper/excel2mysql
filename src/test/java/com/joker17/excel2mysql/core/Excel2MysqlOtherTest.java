package com.joker17.excel2mysql.core;

import com.joker17.excel2mysql.constants.Excel2MysqlConstants;
import com.joker17.excel2mysql.support.DynamicIgnoredRunner;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import java.io.IOException;

/**
 * 1.创建database: CREATE DATABASE IF NOT EXISTS excel2mysql
 * 2.修改db.properties
 */
@RunWith(DynamicIgnoredRunner.class)
@FixMethodOrder(MethodSorters.JVM)
public class Excel2MysqlOtherTest {

    private final static String targetClassPath = Excel2MysqlOtherTest.class.getClassLoader().getResource("").getPath();

    @Test
    public void executeOtherCreateXlsx() throws IOException {
        String inFilePath = targetClassPath;
        String fileName = "other/other-create.xlsx";
        String excelType = Excel2MysqlConstants.XLSX;
        String appendText = "-check-table-schema false";

        String line = String.format("-data-source %s -in-file-path %s -file-name %s -excel-type %s %s",
                targetClassPath + "db.properties", inFilePath, fileName, excelType, appendText == null ? "" : appendText);
        String[] args = line.split(" ");
        Excel2MysqlExecutor.INSTANCE.execute(args);
    }


}