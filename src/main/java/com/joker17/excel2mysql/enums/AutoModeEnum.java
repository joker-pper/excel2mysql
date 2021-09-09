package com.joker17.excel2mysql.enums;

public enum AutoModeEnum {

    CREATE("create"),

    UPDATE("update"),

    ;

    private final String name;

    AutoModeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static AutoModeEnum getInstance(String name) {
        for (AutoModeEnum autoModeEnum : AutoModeEnum.values()) {
            if (autoModeEnum.getName().equalsIgnoreCase(name)) {
                return autoModeEnum;
            }
        }
        return null;
    }

}
