package com.joker17.excel2mysql.enums;

public enum ColumnKeyTypeEnum {

    PRIMARY_KEY,

    UNIQUE_KEY,

    /**
     * 该列既是PRIMARY也有单独的UNIQUE约束
     */
    PRIMARY_AND_UNIQUE_KEY,

    OTHER;

    public static class Constants {

        public final static String PRIMARY_AND_UNIQUE_KEY = "PK&UK";
        public final static String PRIMARY_KEY = "PK";
        public final static String UNIQUE_KEY = "UK";
    }

}
