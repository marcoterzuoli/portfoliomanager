package com.mt3.portfoliomanager.marketscreener;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EquityPriceReaderTest {

    @Test
    public void readValidFile() throws URISyntaxException, IOException {
        Path file = Paths.get(getClass().getResource("/market_screener_price.txt").toURI());
        String content = new String(Files.readAllBytes(file));
        Equity equity = EquityPriceReader.readFromString(new MarketScreenerInternals("123", "ABC","AAPL", "S&P"), content);
        Assert.assertEquals("AAPL", equity.getMarketScreenerInternals().getEquityName());
        Assert.assertEquals(19, equity.getEstimatedPrices().size());
        Assert.assertEquals(379, equity.getActualPrices().size());
        // TODO: assert dates are converted correctly
    }
}
