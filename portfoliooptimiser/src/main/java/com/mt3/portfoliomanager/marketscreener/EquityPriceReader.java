package com.mt3.portfoliomanager.marketscreener;

import com.google.common.base.Strings;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class EquityPriceReader {

    private static final LocalDate EPOCH_START = LocalDate.of(1970, 1, 1);
    private static final long DAY_DIVISOR = 24 * 60 * 60 * 1000;

    public static Equity readFromString(MarketScreenerInternals marketScreenerInternals, String content) {
        char[] chars = content.toCharArray();

        List<TObjectDoubleMap<LocalDate>> maps = new ArrayList<>();
        TObjectDoubleMap<LocalDate> currentMap = new TObjectDoubleHashMap<>();
        StringBuilder builder = new StringBuilder();
        for (char c : chars) {
            if (c == '[') {
                builder.delete(0, builder.length());
            } else if (c == ']') {
                String lastSection = builder.toString();
                if (Strings.isNullOrEmpty(lastSection)) {
                    maps.add(currentMap);
                    if (maps.size() > 2)
                        throw new IllegalArgumentException("Only 2 maps are allowed");
                    currentMap = new TObjectDoubleHashMap<LocalDate>();
                } else {
                    String[] parts = lastSection.split(",");
                    if (parts.length != 2)
                        break;

                    LocalDate date = convertFromEpoch(Long.parseLong(parts[0]));
                    double price = Double.parseDouble(parts[1]);
                    currentMap.put(date, price);
                }
                builder.delete(0, builder.length());
            } else {
                builder.append(c);
            }
        }
        return new Equity(marketScreenerInternals, maps.get(0), maps.get(1));
    }

    private static LocalDate convertFromEpoch(long millisPastEpoch) {
        long daysToAdd = millisPastEpoch / DAY_DIVISOR;
        return EPOCH_START.plusDays(daysToAdd);
    }
}
