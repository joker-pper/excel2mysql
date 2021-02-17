package com.joker17.excel2mysql.utils;

import java.util.Locale;

public class StringUtils {

    public static String convertString(Object object) {
        return object == null ? null : String.valueOf(object);
    }

    public static boolean isEmpty(String text) {
        return text == null || text.isEmpty();
    }

    public static String trimToEmpty(String text) {
        return text == null ? "" : text.trim();
    }

    public static String toUpperCase(String text) {
        return text == null ? null : text.toUpperCase(Locale.ROOT);
    }

}
