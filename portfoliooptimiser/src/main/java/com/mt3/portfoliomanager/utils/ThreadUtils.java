package com.mt3.portfoliomanager.utils;

public final class ThreadUtils {

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            interrupted();
        }
    }

    public static void interrupted() {
        Thread.interrupted();
    }
}
