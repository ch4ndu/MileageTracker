package com.udnahc.locationmanager.executors;

import android.os.Process;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
 * Singleton class for default executor supplier
 */
public class PriorityExecutorSupplier {
    /*
     * Number of cores to decide the number of threads
     */
    public static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    /*
     * an instance of DefaultExecutorSupplier
     */
    private static PriorityExecutorSupplier sInstance;
    /*
     * thread pool executor for background tasks
     */
    private final PriorityThreadPoolExecutor mForBackgroundTasks;
    /*
     * thread pool executor for main thread tasks
     */
    private final Executor mMainThreadExecutor;

    /*
     * constructor for  DefaultExecutorSupplier
     */
    private PriorityExecutorSupplier() {

        // setting the thread factory
        ThreadFactory backgroundPriorityThreadFactory = new
                PriorityThreadFactory(Process.THREAD_PRIORITY_BACKGROUND);

        // setting the thread pool executor for mForBackgroundTasks;
        mForBackgroundTasks = new PriorityThreadPoolExecutor(
                NUMBER_OF_CORES * 2,
                NUMBER_OF_CORES * 2,
                60L,
                TimeUnit.SECONDS,
                backgroundPriorityThreadFactory
        );

        // setting the thread pool executor for mMainThreadExecutor;
        mMainThreadExecutor = new MainThreadExecutor();
    }

    /*
     * returns the instance of DefaultExecutorSupplier
     */
    public static PriorityExecutorSupplier getInstance() {
        if (sInstance == null)
            synchronized (PriorityExecutorSupplier.class) {
                sInstance = new PriorityExecutorSupplier();
            }
        return sInstance;
    }

    /*
     * returns the thread pool executor for background task
     */
    public ThreadPoolExecutor forBackgroundTasks() {
        return mForBackgroundTasks;
    }

    /*
     * returns the thread pool executor for main thread task
     */
    public Executor forMainThreadTasks() {
        return mMainThreadExecutor;
    }
}
