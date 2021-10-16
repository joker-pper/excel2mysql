package com.joker17.excel2mysql.automode;

import com.joker17.excel2mysql.enums.AutoModeEnum;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class AbstractTableAutoModeSupport {

    public AbstractTableAutoModeSupport() {
    }

    public abstract AutoModeEnum autoMode();

    public abstract void execute(JdbcTemplate jdbcTemplate, TableAutoModeSupportOptions options);
}
