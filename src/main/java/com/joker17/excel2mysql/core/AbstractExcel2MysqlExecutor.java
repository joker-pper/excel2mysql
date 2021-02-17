package com.joker17.excel2mysql.core;

import com.beust.jcommander.JCommander;
import com.joker17.excel2mysql.constants.Excel2MysqlConstants;
import com.joker17.excel2mysql.param.Excel2MysqlDumpParam;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class AbstractExcel2MysqlExecutor {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 业务逻辑
     *
     * @param dumpParam
     * @throws IOException
     */
    protected abstract void doWork(Excel2MysqlDumpParam dumpParam) throws IOException;

    public void execute(String[] args) throws IOException {
        if (args == null || args.length == 0) {
            args = new String[]{"--help"};
        }

        //解析param
        Excel2MysqlDumpParam dumpParam = new Excel2MysqlDumpParam();
        JCommander jCommander = JCommander.newBuilder()
                .addObject(dumpParam)
                .build();
        jCommander.parse(args);

        if (dumpParam.isHelp()) {
            jCommander.usage();
            return;
        }

        LocalDateTime start = LocalDateTime.now();
        Excel2MysqlConstants.LOG.info("start: {}", start.format(DATE_TIME_FORMATTER));

        //执行业务逻辑
        doWork(dumpParam);

        LocalDateTime end = LocalDateTime.now();
        Duration duration = Duration.between(start, end);
        BigDecimal result = BigDecimal.valueOf(duration.toMillis()).divide(BigDecimal.valueOf(1000)).setScale(2, RoundingMode.HALF_UP);

        Excel2MysqlConstants.LOG.info("end: {}", end.format(DATE_TIME_FORMATTER));
        Excel2MysqlConstants.LOG.info("time consuming: {} s", result);
    }


}
