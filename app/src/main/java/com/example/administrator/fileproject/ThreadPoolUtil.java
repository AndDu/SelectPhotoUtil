package com.example.administrator.fileproject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2018/4/4.
 */

public class ThreadPoolUtil {


    public static ThreadPoolUtil threadPoolUtil;

    public static ThreadPoolUtil getInstance() {

        if (threadPoolUtil == null) {
            threadPoolUtil = InstanceUtil.threadPoolUtil;
        }
        return threadPoolUtil;
    }


    final static class InstanceUtil {
        static ThreadPoolUtil threadPoolUtil = new ThreadPoolUtil();
    }

    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public void sumbit(Runnable runnable) {
        executorService.submit(runnable);
    }
}
