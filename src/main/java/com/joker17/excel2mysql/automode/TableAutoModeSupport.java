package com.joker17.excel2mysql.automode;

import com.joker17.excel2mysql.enums.AutoModeEnum;
import org.springframework.jdbc.core.JdbcTemplate;

public interface TableAutoModeSupport {

    AutoModeEnum autoMode();

    void execute(JdbcTemplate jdbcTemplate, TableAutoModeSupportOptions options);
}
