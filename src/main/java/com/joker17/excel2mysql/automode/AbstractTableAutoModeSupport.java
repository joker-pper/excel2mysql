package com.joker17.excel2mysql.automode;

import com.joker17.excel2mysql.enums.AutoModeEnum;

public abstract class AbstractTableAutoModeSupport {

    public AbstractTableAutoModeSupport() {
    }

    public abstract AutoModeEnum autoMode();

    public abstract void execute(TableAutoModeSupportOptions options);
}
