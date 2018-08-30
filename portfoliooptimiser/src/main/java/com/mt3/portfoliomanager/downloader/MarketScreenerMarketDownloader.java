package com.mt3.portfoliomanager.downloader;

import com.google.common.collect.ImmutableSet;
import com.mt3.portfoliomanager.marketscreener.MarketScreenerInternals;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MarketScreenerMarketDownloader {

    private static final Logger LOG = Logger.getLogger(MarketScreenerMarketDownloader.class);

    private static final String MARKET_INTERNAL_ID_TOKEN = "{market}";
    private static final String PAGE_NUMBER_TOKEN = "{page}";
    private static final String MARKET_URL_TEMPLATE = "https://www.marketscreener.com/" + MARKET_INTERNAL_ID_TOKEN +
            "/components/col=&asc=0&fpage=" + PAGE_NUMBER_TOKEN;

    private static final Pattern EQUITY_PATTERN = Pattern.compile("<a href=\\\"/([a-zA-Z0-9\\-]+)/\\\" +codezb=\\\"([0-9]+)\\\" at=\\\"1\\\">(.*)</a>");

    private final Set<String> marketInternalNames = ImmutableSet.of("S-P-500-4985");

    public List<MarketScreenerInternals> download() {
        return DownloadHelper.downloadInParallel(marketInternalNames, this::downloadEquityInternalIds, null);
    }

    private List<MarketScreenerInternals> downloadEquityInternalIds(String marketInternalId) {
        LOG.info("Downloading equities for market " + marketInternalId);

        List<MarketScreenerInternals> result = new ArrayList<>();
        for (int page = 1; ; page++) {
            LOG.info("Downloading page " + page);
            String url = MARKET_URL_TEMPLATE.replace(MARKET_INTERNAL_ID_TOKEN, marketInternalId)
                    .replace(PAGE_NUMBER_TOKEN, Integer.toString(page));

            String document = DownloadHelper.downloadWithRetry(url);
            Matcher matcher = EQUITY_PATTERN.matcher(document);

            boolean anyFound = false;
            while (matcher.find()) {
                anyFound = true;
                String internalName = matcher.group(1);
                String code = matcher.group(2);
                String name = matcher.group(3);
                result.add(new MarketScreenerInternals(internalName, code, name));
            }

            if (!anyFound)
                break;
        }
        return result;
    }
}
