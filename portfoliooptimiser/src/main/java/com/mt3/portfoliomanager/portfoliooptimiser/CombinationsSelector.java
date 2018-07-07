package com.mt3.portfoliomanager.portfoliooptimiser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class CombinationsSelector<T> {

    public void select(List<T> availableItems, int numberOfItems, Predicate<List<T>> callback) {
        int[] indices = new int[numberOfItems];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }

        int maxValue = availableItems.size() - 1;
        for (;;) {
            List<T> combination = new ArrayList<>(numberOfItems);
            for (int index : indices) {
                combination.add(availableItems.get(index));
            }
            callback.test(combination);
            if (increment(indices, indices.length - 1, maxValue))
                break;
        }
    }

    // return true means that no index in indices could be incremented
    private boolean increment(int[] indices, int index, int maxValue) {
        if (indices[index] == maxValue) {
            if (index == 0)
                return true;
            if (increment(indices, index - 1, maxValue - 1))
                return true;
            indices[index] = max(indices, index) + 1;
            return false;
        } else {
            indices[index]++;
            return false;
        }
    }

    private int max(int[] indices, int maxIndex) {
        int max = -1;
        for (int i = 0; i < maxIndex; i++)
            if (max < indices[i])
                max = indices[i];
        return max;
    }
}
