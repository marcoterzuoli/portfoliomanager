package com.mt3.portfoliomanager.downloader;

import com.mt3.portfoliomanager.fund.Fund;

import java.util.Collection;
import java.util.List;

public interface MarketDataDownloader {
    List<Fund> download(Collection<String> isins);
}
