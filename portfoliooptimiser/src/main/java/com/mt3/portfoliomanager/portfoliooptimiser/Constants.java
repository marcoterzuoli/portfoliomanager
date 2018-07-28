package com.mt3.portfoliomanager.portfoliooptimiser;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Constants {

    public static Path MARKET_DATA_FOLDER = Paths.get("C:\\Users\\admin\\Dropbox\\Documents\\fidelity\\ms_md");

    public static double BUSINESS_DAYS_IN_YEAR = 261.0;
    public static int BUSINESS_DAYS_IN_MONTH = (int)Math.round(BUSINESS_DAYS_IN_YEAR / 12.0);
}
