package com.mt3.portfoliomanager.downloader;

import com.mt3.portfoliomanager.fund.Fund;
import com.mt3.portfoliomanager.utils.ThreadUtils;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.linked.TDoubleLinkedList;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MorningstarDownloader {

    private static final Logger LOG = Logger.getLogger(MorningstarDownloader.class);

    private static final int MAX_DOWNLOAD_RETRIES = 4;

    private static final String SEARCH_URL_TEMPLATE = "http://www.morningstar.co.uk/uk/funds/SecuritySearchResults.aspx?type=&search=";
    private static final String MARKET_DATA_URL_TEMPLATE = "http://tools.morningstar.co.uk/api/rest.svc/timeseries_cumulativereturn/t92wz0sj7c?currencyId=GBP&idtype=Morningstar&frequency=daily&startDate=1900-01-01&performanceType=&outputType=JSON&id={{id}}]2]0]FOGBR$$ALL&decPlaces=8";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Pattern searchPattern = Pattern.compile("<td class=\"msDataText searchLink\"><a href=.*id=(.+?)\">(.+?)</a></td><td class=\"msDataText searchIsin\"><span>");

    private final boolean onErrorContinue;
    private final Function<Fund, Void> fundProcessor;

    public MorningstarDownloader() {
        this(false, null);
    }

    public MorningstarDownloader(boolean onErrorContinue, Function<Fund, Void> fundProcessor) {
        this.onErrorContinue = onErrorContinue;
        this.fundProcessor = fundProcessor;
    }

    public List<Fund> download(Collection<String> isins) {
        List<ForkJoinTask<Fund>> tasks = new ArrayList<>(isins.size());
        for (String isin : isins) {
            ForkJoinTask<Fund> task = ForkJoinPool.commonPool().submit(() -> download(isin));
            tasks.add(task);
        }

        List<Fund> result = new ArrayList<>();
        for (ForkJoinTask<Fund> task : tasks) {
            try {
                Fund fund = task.get();
                if (fund != null) {
                    if (fundProcessor != null)
                        fundProcessor.apply(fund);
                    result.add(fund);
                }
            } catch (InterruptedException e) {
                ThreadUtils.interrupted();
            } catch (ExecutionException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return result;
    }

    private Fund download(String isin) {
        LOG.info("Searching for Morningstar internal ID for ISIN " + isin);
        String url = SEARCH_URL_TEMPLATE + isin;

        String document = downloadWithRetry(url);
        Matcher matcher = searchPattern.matcher(document);
        if (!matcher.find()) {
            if (onErrorContinue)
                return null;
            throw new IllegalArgumentException("Cannot find search regex for ISIN " + isin);
        }
        String morningstarId = matcher.group(1);
        String fundName = matcher.group(2);
        LOG.info("ISIN " + isin + " has Morningstar ID " + morningstarId + " and fund name " + fundName);

        return downloadMarketData(morningstarId, isin + " - " + fundName);
    }

    private Fund downloadMarketData(String morningstarId, String fundName) {
        LOG.info("Downloading market data for Morningstar internal ID " + morningstarId);
        String url = MARKET_DATA_URL_TEMPLATE.replace("{{id}}", morningstarId);

        String document = downloadWithRetry(url);
        JSONObject root = new JSONObject(document);
        JSONObject timeSeries = root.getJSONObject("TimeSeries");
        JSONArray security = timeSeries.getJSONArray("Security");
        if (security.length() != 1)
            throw new IllegalArgumentException("Cannot parse fund " + fundName + ": Security does not have exactly one element");
        JSONArray cumulativeReturnSeries = security.getJSONObject(0).getJSONArray("CumulativeReturnSeries");
        if (cumulativeReturnSeries.length() != 1)
            throw new IllegalArgumentException("Cannot parse fund " + fundName + ": CumulativeReturnSeries does not have exactly one element");
        JSONArray historyDetails = cumulativeReturnSeries.getJSONObject(0).getJSONArray("HistoryDetail");

        List<LocalDate> dates = new LinkedList<>();
        TDoubleList prices = new TDoubleLinkedList();
        for (int i = 0; i < historyDetails.length(); i++) {
            JSONObject historyDetail = historyDetails.getJSONObject(i);
            LocalDate date = LocalDate.parse(historyDetail.getString("EndDate"), DATE_TIME_FORMATTER);
            double value = Double.parseDouble(historyDetail.getString("Value")) + 100.0;
            dates.add(date);
            prices.add(value);
        }
        return new Fund(fundName, dates, prices, true);
    }

    private String downloadWithRetry(String url) {
        for (int i = 0; i <= MAX_DOWNLOAD_RETRIES; i++) {
            try {
                return Jsoup.connect(url).ignoreContentType(true).execute().body();
            } catch (SocketTimeoutException e) {
                if (i == MAX_DOWNLOAD_RETRIES)
                    throw new IllegalArgumentException(e);
                ThreadUtils.sleep(1000);
                LOG.warn("Retrying for time number " + i);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
        throw new IllegalArgumentException("This line should never be reached, just needed for compilation");
    }
}
