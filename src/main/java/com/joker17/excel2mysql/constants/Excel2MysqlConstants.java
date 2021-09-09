package com.joker17.excel2mysql.constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Excel2MysqlConstants {

    public final static Logger LOG = LoggerFactory.getLogger("excel2mysql");

    public final static String XLS = "xls";

    public final static String XLSX = "xlsx";


    public final static String LEFT_BRACKET_TAG = "(";

    public final static String RIGHT_BRACKET_TAG = ")";

    /**
     * 旧列到新列映射的tag
     */
    public final static String COLUMN_OLD_TO_NEW_MAPPING_TAG = "->";

    public final static String NULL_TAG = "-";


}
