package com.mt3.portfoliomanager.downloader;

import com.google.common.collect.ImmutableSet;
import com.mt3.portfoliomanager.fund.FundDefinition;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class FidelityDownloader {

    private static final Logger LOG = Logger.getLogger(FidelityDownloader.class);

    private static final String UNIVERSE_TOKEN = "{universe}";
    private static final String SUB_UNIVERSE_TOKEN = "{subuniverse}";
    private static final String URL_TEMPLATE = "https://lt.morningstar.com/api/rest.svc/9vehuxllxs/security/screener?page=1&pageSize=10000&sortOrder=&outputType=json&version=1&languageId=en-GB&currencyId=GBP&universeIds=" +
            UNIVERSE_TOKEN + "&securityDataPoints=SecId|isin|LegalName&filters=&term=&subUniverseId=" + SUB_UNIVERSE_TOKEN;

    private final Set<KeyValue<String, String>> universeAndSubUniverses = ImmutableSet.of(
            new DefaultKeyValue<>("FOGBR$$ALL_3521", "MFEI"),                   // funds
            new DefaultKeyValue<>("ETEXG$XLON_3518|ETALL$$ALL_3518", "ETFEI"),  // ETFs
            new DefaultKeyValue<>("FCGBR$$ALL_3519", "ITEI")                    // investment trusts
    );

    public List<FundDefinition> download() {
        return DownloadHelper.downloadInParallel(universeAndSubUniverses, this::download, null);
    }

    private List<FundDefinition> download(KeyValue<String, String> universeAndSubUniverse) {
        LOG.info("Downloading isins for universe " + universeAndSubUniverse.getValue()); // sub-universe is more readable

        String url = URL_TEMPLATE.replace(UNIVERSE_TOKEN, universeAndSubUniverse.getKey())
                .replace(SUB_UNIVERSE_TOKEN, universeAndSubUniverse.getValue());

        String document = DownloadHelper.downloadWithRetry(url);
        JSONObject root = new JSONObject(document);
        JSONArray rows = root.getJSONArray("rows");

        List<FundDefinition> result = new ArrayList<>();
        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.getJSONObject(i);
            String morningStarId = row.getString("SecId");
            String isin = row.getString("isin");
            String name = row.getString("LegalName");
            result.add(new FundDefinition(morningStarId, name, isin));
        }
        return result;
    }
}
