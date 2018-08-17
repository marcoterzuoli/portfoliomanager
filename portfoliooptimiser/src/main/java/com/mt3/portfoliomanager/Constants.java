package com.mt3.portfoliomanager;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Constants {

    public static Path MARKET_DATA_FOLDER = Paths.get("C:\\Users\\admin\\Dropbox\\Documents\\fidelity\\ms_md");

    public static double BUSINESS_DAYS_IN_YEAR = 365.0;
    public static double BUSINESS_DAYS_IN_MONTH = BUSINESS_DAYS_IN_YEAR / 12.0;
}
