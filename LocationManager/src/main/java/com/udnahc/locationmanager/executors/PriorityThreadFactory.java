package com.udnahc.locationmanager.executors;

import android.os.Process;

import java.util.concurrent.ThreadFactory;

public class PriorityThreadFactory implements ThreadFactory {

    private final int mThreadPriority;
    private int count = 0;

    public PriorityThreadFactory(int threadPriority) {
        mThreadPriority = threadPriority;
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        count++;
        Runnable wrapperRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Process.setThreadPriority(mThreadPriority);
                } catch (Throwable t) {

                }
                runnable.run();
            }
        };
        final Thread thread = new Thread(wrapperRunnable);
        thread.setName(String.format("Thread-%d", count));
        return thread;
    }
}
