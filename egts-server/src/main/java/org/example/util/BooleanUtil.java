package org.example.util;

public class BooleanUtil {

    public static String getStringFromBool(boolean value) {
        return value ? "1" : "0";
    }

    public static boolean getBoolFromChar(char value) {
        return value == '1';
    }



}
