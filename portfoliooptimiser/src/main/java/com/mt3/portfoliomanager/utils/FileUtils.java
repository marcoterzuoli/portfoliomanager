package com.mt3.portfoliomanager.utils;

public final class FileUtils {

    public static String convertToSafePath(String path) {
        return path.replaceAll("[\\\\/:*?\"<>|]", " ");
    }
}
