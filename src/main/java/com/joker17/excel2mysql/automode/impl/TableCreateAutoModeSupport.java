package com.joker17.excel2mysql.automode.impl;

import com.joker17.excel2mysql.automode.AbstractTableAutoModeSupport;
import com.joker17.excel2mysql.automode.TableAutoModeSupportOptions;
import com.joker17.excel2mysql.constants.Excel2MysqlConstants;
import com.joker17.excel2mysql.db.MysqlUtils;
import com.joker17.excel2mysql.enums.AutoModeEnum;
import com.joker17.excel2mysql.helper.Excel2MysqlHelper;
import com.joker17.excel2mysql.model.Excel2MysqlModel;
import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * 创建模式支持 -> 已存在的表忽略
 */
public class TableCreateAutoModeSupport extends AbstractTableAutoModeSupport {

    public static final TableCreateAutoModeSupport INSTANCE = new TableCreateAutoModeSupport();

    protected final static Logger MAIN_LOG = Excel2MysqlConstants.LOG;

    private TableCreateAutoModeSupport() {
        super();
    }

    @Override
    public AutoModeEnum autoMode() {
        return AutoModeEnum.CREATE;
    }

    @Override
    public void execute(JdbcTemplate jdbcTemplate, TableAutoModeSupportOptions options) {
        String database = options.getDatabase();
        String tableName = options.getTableName();
        String engine = options.getEngine();
        boolean tableExists = Boolean.TRUE.equals(options.getTableExists());
        List<Excel2MysqlModel> tableExcel2MysqlModelList = options.getTableExcel2MysqlModelList();
        if (!tableExists) {
            //不存在表时获取创表sql
            String createSql = Excel2MysqlHelper.getCreateSql(tableName, engine, tableExcel2MysqlModelList);

            MAIN_LOG.info("database {} create table `{}` start", database, tableName);
            MAIN_LOG.info("database {} create table `{}` sql - {}", database, tableName, createSql);

            //执行sql
            MysqlUtils.execute(jdbcTemplate, createSql);

            MAIN_LOG.info("database {} create table `{}` end", database, tableName);

        } else {
            //已存在的表忽略
            MAIN_LOG.warn("database {} has already exist table `{}` ...", database, tableName);
        }
    }
}
