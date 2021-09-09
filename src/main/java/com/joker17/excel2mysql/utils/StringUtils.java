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

    public static String trimToNull(String text) {
        if (text == null) {
            return null;
        }
        String result = text.trim();
        return result.isEmpty() ? null : result;
    }

    public static String toUpperCase(String text) {
        return text == null ? null : text.toUpperCase(Locale.ROOT);
    }

    public static String toLowerCase(String text) {
        return text == null ? null : text.toLowerCase(Locale.ROOT);
    }

    public static boolean equals(CharSequence left, CharSequence right) {
        if (left == right) {
            return true;
        } else {
            if (left != null && right != null) {
                return left.toString().equals(right.toString());
            } else {
                return false;
            }
        }
    }

    public static String removeStart(String text, String remove) {
        if (!isEmpty(text) && !isEmpty(remove)) {
            return text.startsWith(remove) ? text.substring(remove.length()) : text;
        } else {
            return text;
        }
    }

    public static String removeEnd(String text, String remove) {
        if (!isEmpty(text) && !isEmpty(remove)) {
            return text.endsWith(remove) ? text.substring(0, text.length() - remove.length()) : text;
        } else {
            return text;
        }
    }

    public static String replace(String text, String searchString, String replacement) {
        if (!isEmpty(text) && !isEmpty(searchString) && replacement != null) {
            return text.replace(searchString, replacement);
        } else {
            return text;
        }
    }

}
