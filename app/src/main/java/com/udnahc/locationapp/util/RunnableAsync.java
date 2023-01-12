package com.udnahc.locationapp.util;


import android.os.AsyncTask;

import androidx.annotation.Nullable;

public class RunnableAsync extends AsyncTask<String, String, String> {
    @Nullable
    private final Runnable backgroundRunnable;
    @Nullable
    private final Runnable uiRunnable;
    @Nullable
    private final Response.ErrorListener errorListener;

    public RunnableAsync(@Nullable Runnable backgroundRunnable,
                         @Nullable Runnable uiRunnable,
                         @Nullable Response.ErrorListener errorListener) {
        this.backgroundRunnable = backgroundRunnable;
        this.uiRunnable = uiRunnable;
        this.errorListener = errorListener;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            if (backgroundRunnable != null) {
                backgroundRunnable.run();
            }
        } catch (Exception e) {
            Plog.e("RunnableAsync", e, "NPE");
            if (errorListener != null) {
                errorListener.onError(new EzError(e));
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        if (uiRunnable != null) {
            uiRunnable.run();
        }
    }
}
