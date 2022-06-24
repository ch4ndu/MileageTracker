package com.udnahc.locationmanager;

import android.location.Location;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.udnahc.locationmanager.maps.PolyUtil;

import java.util.ArrayList;
import java.util.List;

public class MileageImpl implements Mileage {
    private String latLongString = "";
    private double miles = 0;
    private String poly = "";
    private List<Location> path = new ArrayList<>();
    private List<LatLng> latLngs = new ArrayList<>();
    private String startLocation;
    private String endLocation;
    private String tripDetails = "";
    private double cost = 0;
    private long timeStamp = -1;
    private long endTime = -1;

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getTripDetails() {
        if (tripDetails == null) {
            tripDetails = "";
        }
        return tripDetails;
    }

    public void setTripDetails(String tripDetails) {
        this.tripDetails = tripDetails;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getLatLongString() {
        if (TextUtils.isEmpty(latLongString) && getPath().size() > 0) {
            final StringBuilder path = new StringBuilder();
            for (Location location : getPath()) {
                path.append(location.getLatitude()).append(",").append(location.getLongitude());
                path.append("|");
            }
            latLongString = path.toString();
        }
        if (TextUtils.isEmpty(latLongString))
            latLongString = "";
        return latLongString;
    }

    public void setLatLongString(String latLongString) {
        this.latLongString = latLongString;
    }

    public double getMiles() {
        return miles;
    }

    public void setMiles(double miles) {
        this.miles = miles;
    }

    public String getPoly() {
        return poly;
    }

    public void setPoly(String poly) {
        this.poly = poly;
    }

    public List<Location> getPath() {
        if (path == null) {
            path = new ArrayList<>();
        }
        return path;
    }

    public List<LatLng> getLatLngList() {
        latLngs.clear();
        for (Location location : getPath()) {
            latLngs.add(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        return latLngs;
    }

    public void setPath(List<Location> path) {
        this.path = path;
    }

    public void postProcessOfflineMileageToSave() {
        final StringBuilder path = new StringBuilder();
        for (Location location : getPath()) {
            path.append(location.getLatitude()).append(",").append(location.getLongitude());
            path.append("|");
        }
        setLatLongString(path.toString());
    }

    public void postProcessMileage(boolean fromLocations) {
        if (fromLocations) {
            setMiles(getMiles(getPath()));
            if (!TextUtils.isEmpty(startLocation) && !TextUtils.isEmpty(endLocation)) {
                String finalTitle = startLocation + " TO " + endLocation;
                setTripDetails(finalTitle);
            }
            if (getPath().size() > 0) {
                List<LatLng> latLngs = new ArrayList<>();
                Location dummy = getPath().get(0);
                for (Location location : getPath()) {
                    latLngs.add(new LatLng(location.getLatitude(), location.getLongitude()));
                }
                latLngs = PolyUtil.simplify(latLngs, 0.2);
                String poly = PolyUtil.encode(latLngs);
                List<Location> simplifiedLocations = new ArrayList<>();
                for (LatLng latLng : latLngs) {
                    final Location location = new Location(dummy.getProvider());
                    location.setLatitude(latLng.latitude);
                    location.setLongitude(latLng.longitude);
                    simplifiedLocations.add(location);
                }
                setPath(simplifiedLocations);
                setPoly(poly);
            }
            cost = (double) Math.round(getMiles() * 0.54 * 100d) / 100d;
            endTime = System.currentTimeMillis();
        } else {
            if (!TextUtils.isEmpty(getPoly())) {
                List<LatLng> temp = PolyUtil.decode(getPoly());
                List<Location> locations = new ArrayList<>();
                for (LatLng latLng : temp) {
                    Location location = new Location("fused");
                    location.setLatitude(latLng.latitude);
                    location.setLongitude(latLng.longitude);
                    locations.add(location);
                }
                setPath(locations);
            }
        }
    }

    private double getMiles(List<Location> locations) {
        double distance = 0;
        int size = locations.size();
        for (int i = 0; i < size - 1; i++) {
            distance = distance + getMiles(locations.get(i).distanceTo(locations.get(i + 1)));
        }
        distance = Math.round(distance * 10) / 10d;
        return distance;
    }

    private double getMiles(float meters) {
        double miles = meters * 0.000621371192;
//        return miles > 0.08d ? miles : 0;
        return miles;
    }
}
