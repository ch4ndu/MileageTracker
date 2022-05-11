package com.udnahc.locationmanager;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public interface Mileage {
    long getTimeStamp();

    void setTimeStamp(long timeStamp);

    public long getEndTime();

    public void setEndTime(long endTime);

    String getTripDetails();

    void setTripDetails(String tripDetails);

    String getStartLocation();

    void setStartLocation(String startLocation);

    String getEndLocation();

    void setEndLocation(String endLocation);

    double getCost();

    void setCost(double cost);

    String getLatLongString();

    void setLatLongString(String latLongString);

    double getMiles();

    void setMiles(double miles);

    String getPoly();

    void setPoly(String poly);

    @NonNull
    List<Location> getPath();

    List<LatLng> getLatLngList();

    void setPath(List<Location> path);

    void postProcessOfflineMileageToSave();

    void postProcessMileage(boolean fromLocations);
}
