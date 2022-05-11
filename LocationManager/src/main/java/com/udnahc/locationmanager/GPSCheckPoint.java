package com.udnahc.locationmanager;

import android.content.Context;
import android.location.LocationManager;

import static android.content.Context.LOCATION_SERVICE;

public class GPSCheckPoint {
    private static LocationManager locationManager;

    public static boolean gpsProviderEnable(Context context) {
        if (locationManager == null)
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean networkProviderEnable(Context context) {
        if (locationManager == null)
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}
