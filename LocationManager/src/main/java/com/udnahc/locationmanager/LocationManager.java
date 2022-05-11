package com.udnahc.locationmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

@SuppressWarnings({"FieldCanBeLocal", "unused", "ConstantConditions"})
public class LocationManager extends LocationCallback {
    private static final String TAG = "LocationManager";
    private static final long INTERVAL = 1000 * 20;
    private static final float MINIMUM_DISPLACEMENT = 0;
    private static LocationRequest mLocationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean isGPSEnabled = false;
    private Context context;
    private GpsOnListener gpsOnListener;
    private Location mCurrentLocation;
    private boolean isNetworkEnabled = false;
    private boolean continuousLocationLocation = true;

    public LocationManager(Context context) {
        this.context = context;
        this.gpsOnListener = (GpsOnListener) this.context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    private boolean isGpsOn() {
        isGPSEnabled = GPSCheckPoint.gpsProviderEnable(context);
        isNetworkEnabled = GPSCheckPoint.networkProviderEnable(context);
        return isGPSEnabled || isNetworkEnabled;
    }

    @SuppressLint("RestrictedApi")
    private LocationRequest getLocationRequest() {
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setMaxWaitTime(INTERVAL);
            mLocationRequest.setInterval(INTERVAL);
            if (MINIMUM_DISPLACEMENT > 0)
                mLocationRequest.setSmallestDisplacement(MINIMUM_DISPLACEMENT);
            mLocationRequest.setFastestInterval(INTERVAL / 2);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        return mLocationRequest;
    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(getLocationRequest(), this, Looper.myLooper());
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        Plog.d(TAG, "onLocationResult");
        if (locationResult == null) {
            return;
        }
        Plog.d(TAG, "onLocationResult: size " + locationResult.getLocations().size());
        for (Location location : locationResult.getLocations()) {
            mCurrentLocation = location;
            if (location.getLatitude() != 0.0 && fusedLocationClient != null) {
                if (continuousLocationLocation) {
                    gpsOnListener.onLocation(location);
                } else {
                    gpsOnListener.onLocation(location);
                    stopLocationUpdate();
                }
            }
        }
    }

    @Override
    public void onLocationAvailability(LocationAvailability locationAvailability) {
        Plog.d(TAG, "onLocationAvailability: " + locationAvailability);
        super.onLocationAvailability(locationAvailability);
    }

    public void stopLocationUpdate() {
        //noinspection CatchMayIgnoreException
        try {
            if (fusedLocationClient != null) {
                fusedLocationClient.removeLocationUpdates(LocationManager.this);
                fusedLocationClient = null;
            }
        } catch (Exception e) {
        }
    }

}
