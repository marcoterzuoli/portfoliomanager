package com.mt3.portfoliomanager.downloader;

import com.google.common.collect.ImmutableList;
import com.mt3.portfoliomanager.fund.Fund;
import com.mt3.portfoliomanager.fund.FundDefinition;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.linked.TDoubleLinkedList;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MorningstarDownloader {

    private static final Logger LOG = Logger.getLogger(MorningstarDownloader.class);

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

    public List<Fund> downloadByIsins(Collection<String> isins) {
        return DownloadHelper.downloadInParallel(isins, this::downloadByIsins, fundProcessor);
    }

    public List<Fund> downloadByFundDefinitions(Collection<FundDefinition> fundDefinitions) {
        return DownloadHelper.downloadInParallel(fundDefinitions, this::downloadMarketData, fundProcessor);
    }

    private List<Fund> downloadByIsins(String isin) {
        LOG.info("Searching for Morningstar internal ID for ISIN " + isin);
        String url = SEARCH_URL_TEMPLATE + isin;

        String document = DownloadHelper.downloadWithRetry(url);
        Matcher matcher = searchPattern.matcher(document);
        if (!matcher.find()) {
            if (onErrorContinue)
                return null;
            throw new IllegalArgumentException("Cannot find search regex for ISIN " + isin);
        }
        String morningstarId = matcher.group(1);
        String fundName = matcher.group(2);
        LOG.info("ISIN " + isin + " has Morningstar ID " + morningstarId + " and fund name " + fundName);

        return downloadMarketData(new FundDefinition(morningstarId, fundName, isin));
    }

    private List<Fund> downloadMarketData(FundDefinition fundDefinition) {
        LOG.info("Downloading market data for " + fundDefinition);
        String url = MARKET_DATA_URL_TEMPLATE.replace("{{id}}", fundDefinition.getMorningStarId());

        String document = DownloadHelper.downloadWithRetry(url);
        JSONObject root = new JSONObject(document);
        JSONObject timeSeries = root.getJSONObject("TimeSeries");
        JSONArray security = timeSeries.getJSONArray("Security");
        if (security.length() != 1)
            throw new IllegalArgumentException("Cannot parse fund " + fundDefinition.getIsinAndName() + ": Security does not have exactly one element");
        JSONArray cumulativeReturnSeries = security.getJSONObject(0).getJSONArray("CumulativeReturnSeries");
        if (cumulativeReturnSeries.length() != 1)
            throw new IllegalArgumentException("Cannot parse fund " + fundDefinition.getIsinAndName() + ": CumulativeReturnSeries does not have exactly one element");
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
        return ImmutableList.of(new Fund(fundDefinition, dates, prices, true));
    }
}
