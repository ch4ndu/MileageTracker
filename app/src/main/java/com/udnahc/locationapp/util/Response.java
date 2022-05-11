package com.udnahc.locationapp.util;


import androidx.annotation.Nullable;

public class Response {

    public static interface Listener<T> {
        void onResponse(@Nullable T response);
    }

    public static interface ErrorListener {
        void onError(@Nullable EzError error);
    }
}
