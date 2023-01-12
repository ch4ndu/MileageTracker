package com.udnahc.locationmanager.executors;

import android.os.Process;

import com.udnahc.locationmanager.Plog;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
 * Singleton class for default executor supplier
 */
public class PureExecutor {
    /*
     * Number of cores to decide the number of threads
     */
    public static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    /*
     * an instance of DefaultExecutorSupplier
     */
    private static PureExecutor sInstance;
    /*
     * thread pool executor for background tasks
     */
    private final PriorityThreadPoolExecutor mForBackgroundTasks;
    private final ThreadPoolExecutor mForFolderObserving;
    private final PriorityThreadPoolExecutor initExecutor;
    private final ThreadPoolExecutor coroutineExecutor;
    /*
     * thread pool executor for main thread tasks
     */
    private final Executor mMainThreadExecutor;

    /*
     * constructor for  DefaultExecutorSupplier
     */
    private PureExecutor() {
        Plog.d("PureExecutor", "cores: %s", NUMBER_OF_CORES);
        // setting the thread pool executor for mMainThreadExecutor;
        mMainThreadExecutor = new MainThreadExecutor();

        // setting the thread factory
        ThreadFactory backgroundPriorityThreadFactory = new
                PriorityThreadFactory(Process.THREAD_PRIORITY_FOREGROUND);

        // setting the thread pool executor for mForBackgroundTasks;
        mForBackgroundTasks = new PriorityThreadPoolExecutor(
                NUMBER_OF_CORES,
                NUMBER_OF_CORES,
                60L,
                TimeUnit.SECONDS,
                backgroundPriorityThreadFactory
        );

        // setting the thread factory
        ThreadFactory folderThreadFactory = new
                PriorityThreadFactory(Process.THREAD_PRIORITY_FOREGROUND);

        // setting the thread pool executor for FolderObserver;
        mForFolderObserving = new ThreadPoolExecutor(
                1,
                1,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>()
        );

        // setting the thread pool executor for initializing;
        initExecutor = new PriorityThreadPoolExecutor(
                1,
                1,
                60L,
                TimeUnit.SECONDS,
                folderThreadFactory
        );

        BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();
        coroutineExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES * 2,
                NUMBER_OF_CORES * 3,
                60L,
                TimeUnit.SECONDS,
                taskQueue,
                new BackgroundThreadFactory());
    }

    private static class BackgroundThreadFactory implements ThreadFactory {
        private static int sTag = 1;

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("CustomThread" + sTag);
            thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);

            // A exception handler is created to log the exception from threads
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    Plog.e("PureExecutor", thread.getName() + " encountered an error: " + ex.getMessage());
                }
            });
            return thread;
        }
    }

    /*
     * returns the instance of DefaultExecutorSupplier
     */
    public static PureExecutor get() {
        if (sInstance == null)
            synchronized (PureExecutor.class) {
                sInstance = new PureExecutor();
            }
        return sInstance;
    }

    public ThreadPoolExecutor forCoroutines() {
        return coroutineExecutor;
    }

    /*
     * returns the thread pool executor for background task
     */
    public PriorityThreadPoolExecutor forBackgroundTasks() {
        return mForBackgroundTasks;
    }

    /*
     * returns the thread pool executor for main thread task
     */
    public Executor forMainThreadTasks() {
        return mMainThreadExecutor;
    }

    public ThreadPoolExecutor forFolderObserving() {
        return mForFolderObserving;
    }

    public PriorityThreadPoolExecutor getInitExecutor() {
        return initExecutor;
    }
}
