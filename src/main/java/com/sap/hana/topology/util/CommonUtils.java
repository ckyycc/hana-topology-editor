package com.sap.hana.topology.util;

import java.util.Collection;

public class CommonUtils {
    /**
     * Check whether the given string is null or empty
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * Check whether the given list is null or empty
     */
    public static boolean isNullOrEmpty(Collection<?> o) {
        return o == null || o.isEmpty();
    }
}
