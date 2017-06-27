package com.lorne;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lorne on 2017/6/8.
 */
public class Constants {


    public static ExecutorService threadPool = null;


    static {
        threadPool = Executors.newCachedThreadPool();
    }
}
