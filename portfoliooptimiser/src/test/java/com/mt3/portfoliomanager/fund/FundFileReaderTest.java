package com.mt3.portfoliomanager.fund;

import org.junit.Assert;
import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FundFileReaderTest {

    @Test
    public void readFile() throws URISyntaxException {
        Path file = Paths.get(getClass().getResource("/GB00BVG1CF25.csv").toURI());
        Fund fund = FundFileReader.readFromCsv(file);
        Assert.assertEquals(0.9095741400963405, fund.getAnnualisedReturn(), 0.000001); // TODO: is this calculation correct? it changed recently
    }
}
