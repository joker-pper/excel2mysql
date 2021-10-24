package com.joker17.excel2mysql.automode;

import com.joker17.excel2mysql.automode.impl.TableCreateAutoModeSupport;
import com.joker17.excel2mysql.automode.impl.TableUpdateAutoModeSupport;
import com.joker17.excel2mysql.enums.AutoModeEnum;

import java.util.HashMap;
import java.util.Map;

public class TableAutoModeSupportFactory {

    private static final Map<AutoModeEnum, TableAutoModeSupport> CACHE_MAP = new HashMap<>(8);

    static {
        initInstance();
    }

    private TableAutoModeSupportFactory() {

    }

    public static TableAutoModeSupport getTableAutoModeSupport(AutoModeEnum autoMode) {
        TableAutoModeSupport tableAutoModeSupport = CACHE_MAP.get(autoMode);
        if (tableAutoModeSupport == null) {
            throw new UnsupportedOperationException("NOT SUPPORT auto mode: " + autoMode.getName());
        }
        return tableAutoModeSupport;
    }

    private static void registerTableAutoModeSupport(TableAutoModeSupport tableAutoModeSupport) {
        AutoModeEnum autoMode = tableAutoModeSupport.autoMode();
        if (CACHE_MAP.containsKey(autoMode)) {
            throw new RuntimeException(String.format("auto mode '%s' has already registered support instance", autoMode.getName()));
        }
        CACHE_MAP.put(autoMode, tableAutoModeSupport);
    }

    private static void initInstance() {
        registerTableAutoModeSupport(TableCreateAutoModeSupport.INSTANCE);
        registerTableAutoModeSupport(TableUpdateAutoModeSupport.INSTANCE);
    }
}
