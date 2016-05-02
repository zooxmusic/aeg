package com.aeg.util;

/**
 * Created by bszucs on 5/2/2016.
 */
public class StringUtil {
    public static boolean isNotEmpty(String value) {
        return ! isEmpty(value);
    }

    public static boolean isEmpty(String value) {
        if(null == value) return true;
        if("".equals(value.trim())) return true;
        if(value.length() < 1) return true;

        return false;
    }

}
