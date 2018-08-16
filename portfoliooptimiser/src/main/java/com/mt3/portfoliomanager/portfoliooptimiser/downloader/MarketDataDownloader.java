package com.mt3.portfoliomanager.portfoliooptimiser.downloader;

import com.mt3.portfoliomanager.portfoliooptimiser.fund.Fund;

import java.util.List;

public interface MarketDataDownloader {
    List<Fund> download(List<String> isins);
}
