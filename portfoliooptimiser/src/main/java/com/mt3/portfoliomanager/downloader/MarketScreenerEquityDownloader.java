package com.mt3.portfoliomanager.downloader;

import com.google.common.collect.ImmutableList;
import com.mt3.portfoliomanager.marketscreener.Equity;
import com.mt3.portfoliomanager.marketscreener.EquityPriceReader;
import com.mt3.portfoliomanager.marketscreener.MarketScreenerInternals;
import org.apache.log4j.Logger;

import java.util.List;

public final class MarketScreenerEquityDownloader {

    private static final Logger LOG = Logger.getLogger(MarketScreenerEquityDownloader.class);

    private static final String CODE_ZB_TOKEN = "{code}";
    private static final String PRICES_URL_TEMPLATE = "https://www.marketscreener.com/reuters_charts/afDataFeed.php?codeZB=" +
            CODE_ZB_TOKEN + "&t=evobjmoyen&iLang=2";

    public List<Equity> download(List<MarketScreenerInternals> internals) {
        return DownloadHelper.downloadInParallel(internals, this::downloadEquityPrices, null);
    }

    private List<Equity> downloadEquityPrices(MarketScreenerInternals internalInfo) {
        LOG.info("Downloading prices for equity " + internalInfo.getEquityName());

        String url = PRICES_URL_TEMPLATE.replace(CODE_ZB_TOKEN, internalInfo.getCodeZb());
        String document = DownloadHelper.downloadWithRetry(url);
        return ImmutableList.of(EquityPriceReader.readFromString(internalInfo.getEquityName(), document));
    }
}
