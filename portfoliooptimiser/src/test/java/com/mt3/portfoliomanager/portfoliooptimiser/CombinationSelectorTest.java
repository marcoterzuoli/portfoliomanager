package com.mt3.portfoliomanager.portfoliooptimiser;

import com.google.common.collect.ImmutableList;
import com.mt3.portfoliomanager.CombinationsSelector;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CombinationSelectorTest {

    private List<List<String>> select(CombinationsSelector<String> selector, List<String> items, int numberToSelect) {
        List<List<String>> result = new ArrayList<>();
        selector.select(items, numberToSelect, x -> result.add(x));
        return result;
    }

    @Test
    public void ListWith2Items1Selected() {
        List<String> input = ImmutableList.of("A", "B");
        List<List<String>> output = select(new CombinationsSelector<String>(), input, 1);
        Assert.assertEquals(ImmutableList.of(ImmutableList.of("A"), ImmutableList.of("B")), output);
    }

    @Test
    public void ListWith2Items2Selected() {
        List<String> input = ImmutableList.of("A", "B");
        List<List<String>> output = select(new CombinationsSelector<String>(), input, 2);
        Assert.assertEquals(ImmutableList.of(ImmutableList.of("A", "B")), output);
    }

    @Test
    public void ListWith3Items2Selected() {
        List<String> input = ImmutableList.of("A", "B", "C");
        List<List<String>> output = select(new CombinationsSelector<String>(), input, 2);
        Assert.assertEquals(ImmutableList.of(ImmutableList.of("A", "B"), ImmutableList.of("A", "C"), ImmutableList.of("B", "C")), output);
    }

    @Test
    public void ListWith5Items3Selected() {
        List<String> input = ImmutableList.of("A", "B", "C", "D", "E");
        List<List<String>> output = select(new CombinationsSelector<String>(), input, 3);
        Assert.assertEquals(ImmutableList.of(
                ImmutableList.of("A", "B", "C"), ImmutableList.of("A", "B", "D"), ImmutableList.of("A", "B", "E"),
                ImmutableList.of("A", "C", "D"), ImmutableList.of("A", "C", "E"), ImmutableList.of("A", "D", "E"),
                ImmutableList.of("B", "C", "D"), ImmutableList.of("B", "C", "E"), ImmutableList.of("B", "D", "E"),
                ImmutableList.of("C", "D", "E")), output);
    }
}
