package com.mt3.portfoliomanager;

public final class NumberUtils {

    public static double parsePercentage(String s) {
        if (!s.endsWith("%"))
            return Double.parseDouble(s);
        s = s.substring(0, s.length() - 1);
        return Double.parseDouble(s) / 100.0;
    }

    public static String getAsPercenage(double number) {
        double pc = 100.0 * (number - 1.0);
        return String.format("%.2f", pc) + "%";
    }
}
