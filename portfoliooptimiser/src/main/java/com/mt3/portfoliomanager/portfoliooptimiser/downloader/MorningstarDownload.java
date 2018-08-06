package com.mt3.portfoliomanager.portfoliooptimiser.downloader;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MorningstarDownload {

    private static final Logger LOG = Logger.getLogger(MorningstarDownload.class);

    private static final String SEARCH_URL_TEMPLATE = "http://www.morningstar.co.uk/uk/funds/SecuritySearchResults.aspx?type=&search=";
    private static final String MARKET_DATA_URL_TEMPLATE = "http://tools.morningstar.co.uk/api/rest.svc/timeseries_cumulativereturn/t92wz0sj7c?currencyId=GBP&idtype=Morningstar&frequency=daily&startDate=1900-01-01&performanceType=&outputType=COMPACTJSON&id={{id}}]2]0]FOGBR$$ALL&decPlaces=8";

    private final Pattern searchPattern = Pattern.compile("<td class=\"msDataText searchLink\"><a href=.*id=(.+?)\">(.+?)</a></td><td class=\"msDataText searchIsin\"><span>");

    public void download(List<String> isins) {
        List<ForkJoinTask<?>> tasks = new ArrayList<>(isins.size());
        for (String isin : isins) {
            ForkJoinTask<?> task = ForkJoinPool.commonPool().submit(() -> download(isin));
            tasks.add(task);
        }

        for (ForkJoinTask<?> task : tasks) {
            try {
                task.get();
            } catch (InterruptedException e) {
                Thread.interrupted();
            } catch (ExecutionException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private void download(String isin) {
        LOG.info("Searching for Morningstar internal ID for ISIN " + isin);
        String url = SEARCH_URL_TEMPLATE + isin;
        try {
            String document = Jsoup.connect(url).execute().body();
            Matcher matcher = searchPattern.matcher(document);
            if (!matcher.find())
                throw new IllegalArgumentException("Cannot find search regex for ISIN " + isin);
            String morningstarId = matcher.group(1);
            String fundName = matcher.group(2);
            LOG.info("ISIN " + isin + " has Morningstar ID " + morningstarId + " and fund name " + fundName);

            downloadMarketData(morningstarId, fundName);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void downloadMarketData(String morningstarId, String fundName) {
        LOG.info("Downloading market data for Morningstar internal ID " + morningstarId);
        String url = MARKET_DATA_URL_TEMPLATE.replace("{{id}}", morningstarId);
        try {
            String document = Jsoup.connect(url).ignoreContentType(true).execute().body();
            // TODO: parse file and save it
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
