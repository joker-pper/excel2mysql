package com.joker17.excel2mysql;

import com.joker17.excel2mysql.core.Excel2MysqlExecutor;
import java.io.IOException;

public class Excel2MysqlMain {

    public static void main(String[] args) throws IOException {
        Excel2MysqlExecutor.INSTANCE.execute(args);
    }
}
