package com.mt3.portfoliomanager.main;

import com.google.common.base.Joiner;
import com.mt3.portfoliomanager.Constants;
import com.mt3.portfoliomanager.downloader.FidelityDownloader;
import com.mt3.portfoliomanager.downloader.MorningstarDownloader;
import com.mt3.portfoliomanager.fund.FundDefinition;
import com.mt3.portfoliomanager.utils.FileUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class MainAllPricesDownloader {

    private static final Logger LOG = Logger.getLogger(MainClusterer.class);

    public static void main(String[] args) {
        FidelityDownloader fidelityDownloader = new FidelityDownloader();
        List<FundDefinition> definitions = fidelityDownloader.download();

        LOG.info(definitions.size() + " fund definitions found");

        MorningstarDownloader morningstarDownloader = new MorningstarDownloader(true,
                fund -> {
                    Map<LocalDate, Double> dateAndPrice = fund.getPricesAndDates();
                    String content = Joiner.on('\n').withKeyValueSeparator(',').join(dateAndPrice);
                    Path file = Constants.MARKET_DATA_FOLDER.resolve(FileUtils.convertToSafePath(fund.getDefinition().getIsinAndName()) + ".csv");
                    try {
                        Files.write(file, content.getBytes());
                    } catch (IOException e) {
                        throw new IllegalArgumentException(e);
                    }
                    return null;
                });
        morningstarDownloader.downloadByFundDefinitions(definitions);
    }
}
