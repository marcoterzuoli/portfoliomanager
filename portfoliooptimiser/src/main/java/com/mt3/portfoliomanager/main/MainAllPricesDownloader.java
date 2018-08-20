package com.mt3.portfoliomanager.main;

import com.google.common.base.Joiner;
import com.mt3.portfoliomanager.Constants;
import com.mt3.portfoliomanager.downloader.MorningstarDownloader;
import com.mt3.portfoliomanager.utils.FileUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class MainAllPricesDownloader {

    private static final Logger LOG = Logger.getLogger(MainClusterer.class);

    public static void main(String[] args) throws IOException {
        List<String> isins = Files.readAllLines(Paths.get("C:\\Users\\admin\\Dropbox\\Documents\\fidelity\\isin.txt"));
        MorningstarDownloader downloader = new MorningstarDownloader(true,
                fund -> {
                    Map<LocalDate, Double> dateAndPrice = fund.getPricesAndDates();
                    String content = Joiner.on('\n').withKeyValueSeparator(',').join(dateAndPrice);
                    Path file = Constants.MARKET_DATA_FOLDER.resolve(FileUtils.convertToSafePath(fund.getName()) + ".csv");
                    try {
                        Files.write(file, content.getBytes());
                    } catch (IOException e) {
                        throw new IllegalArgumentException(e);
                    }
                    return null;
                });
        downloader.download(isins);
    }
}
