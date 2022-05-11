package com.udnahc.locationapp.util;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class EzError extends Exception {

    public EzError(@Nullable Throwable throwable) {
        this.throwable = throwable;
    }

    public EzError(@NonNull String fallbackMessage) {
        this.fallbackMessage = fallbackMessage;
    }

    @Nullable
    private Throwable throwable;
    @Nullable
    private String fallbackMessage;

    @NonNull
    public String getMessage() {
        if (!TextUtils.isEmpty(fallbackMessage)) {
            return fallbackMessage;
        }
        if (throwable != null) {
            return throwable.getMessage();
        }
        return "UnKnown Error";
    }

    @Nullable
    public Throwable getThrowable() {
        return throwable;
    }

    private static EzError networkUnavailable = new EzError("Network connection unavailable. Please make sure that you are connected to a network!");

    public static EzError getNetworkUnavailableError() {
        return networkUnavailable;
    }
}
