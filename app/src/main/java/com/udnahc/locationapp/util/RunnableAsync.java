package com.udnahc.locationapp.util;


import android.os.AsyncTask;

public class RunnableAsync extends AsyncTask<String, String, String> {
    private Runnable backgroundRunnable, uiRunnable;
    private Response.ErrorListener errorListener;

    public RunnableAsync(Runnable backgroundRunnable, Runnable uiRunnable, Response.ErrorListener errorListener) {
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
