package com.udnahc.locationmanager.executors;


import androidx.annotation.NonNull;

import com.udnahc.locationmanager.Plog;

public class RunnableAsync {
    private static final String TAG = RunnableAsync.class.getSimpleName();

    public static void enqueueInit(final Runnable backgroundRunnable, final Runnable uiRunnable) {
        if (backgroundRunnable != null) {
            PureExecutor.get().getInitExecutor().submit(new PriorityRunnable() {
                @Override
                public void run() {
                    try {
                        backgroundRunnable.run();
                    } catch (Exception e) {
                        Plog.e("RunnableAsync", e, "background Runnable");
                    }
                    if (uiRunnable != null) {
                        PureExecutor.get().forMainThreadTasks().execute(uiRunnable);
                    }
                }
            });
        }
    }

    public static void enqueue(@NonNull final Runnable backgroundRunnable, final Runnable uiRunnable) {
        PureExecutor.get().forBackgroundTasks().submit(new PriorityRunnable() {
            @Override
            public void run() {
                try {
                    backgroundRunnable.run();
                } catch (Exception e) {
                    Plog.e(TAG, e, "NPE");
                }
                if (uiRunnable != null) {
                    PureExecutor.get().forMainThreadTasks().execute(uiRunnable);
                }
            }
        });
    }

    public static void enqueue(@NonNull final Runnable backgroundRunnable) {
        PureExecutor.get().forBackgroundTasks().submit(new PriorityRunnable() {
            @Override
            public void run() {
                try {
                    backgroundRunnable.run();
                } catch (Exception e) {
                    Plog.e(TAG, e, "NPE");
                }
            }
        });
    }

    public abstract static class Callback {
        public abstract void onSuccess();

        public void onFailure() {
        }
    }
}
