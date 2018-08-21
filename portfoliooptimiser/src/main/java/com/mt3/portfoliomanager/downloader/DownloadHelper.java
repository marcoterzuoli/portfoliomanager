package com.mt3.portfoliomanager.downloader;

import com.mt3.portfoliomanager.utils.ThreadUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Function;

public final class DownloadHelper {

    private static final Logger LOG = Logger.getLogger(DownloadHelper.class);

    private static final int MAX_DOWNLOAD_RETRIES = 4;

    public static <TDownInput, TDownOutput> List<TDownOutput> downloadInParallel(Collection<TDownInput> inputs,
                                                                                 Function<TDownInput, List<TDownOutput>> downloadFunction,
                                                                                 Function<TDownOutput, Void> outputProcessor) {
        List<ForkJoinTask<List<TDownOutput>>> tasks = new ArrayList<>(inputs.size());
        for (TDownInput input : inputs) {
            ForkJoinTask<List<TDownOutput>> task = ForkJoinPool.commonPool().submit(() -> downloadFunction.apply(input));
            tasks.add(task);
        }

        List<TDownOutput> result = new ArrayList<>();
        for (ForkJoinTask<List<TDownOutput>> task : tasks) {
            try {
                List<TDownOutput> outputList = task.get();
                for (TDownOutput output : outputList) {
                    if (output != null) {
                        if (outputProcessor != null)
                            outputProcessor.apply(output);
                        result.add(output);
                    }
                }
            } catch (InterruptedException e) {
                ThreadUtils.interrupted();
            } catch (ExecutionException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return result;
    }

    public static String downloadWithRetry(String url) {
        for (int i = 0; i <= MAX_DOWNLOAD_RETRIES; i++) {
            try {
                return Jsoup.connect(url).ignoreContentType(true).execute().body();
            } catch (SocketTimeoutException e) {
                if (i == MAX_DOWNLOAD_RETRIES)
                    throw new IllegalArgumentException(e);
                ThreadUtils.sleep(1000);
                LOG.warn("Retrying for time number " + i);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
        throw new IllegalArgumentException("This line should never be reached, just needed for compilation");
    }
}
