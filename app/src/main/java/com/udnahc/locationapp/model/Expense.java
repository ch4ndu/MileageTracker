package com.udnahc.locationapp.model;


import android.location.Location;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.udnahc.locationmanager.Mileage;
import com.udnahc.locationmanager.MileageImpl;

import java.util.List;

public class Expense {
    private long timeStamp = -1;
    private String id = "";
    private long eDate = -1;
    private Mileage mileage = new MileageImpl();

    public Expense(long timeStamp) {
        this.timeStamp = timeStamp;
        mileage.setTimeStamp(timeStamp);
    }

    public Expense(Mileage mileage) {
        this.mileage = mileage;
    }

    public Mileage getMileage() {
        return mileage;
    }

    public String getPoly() {
        if (mileage != null)
            return mileage.getPoly();
        return "";
//        return "y~`vFliajTi@iRik@h@bHiyAmc@y_@_uAsH_Dnb@ah@nY|PrIqU|k@qp@{UsaAr\\_wAiByCtrD{mRBmRfbAol@rRsiKnB}}E_ZkhAff@~Cv`D}b@|pFcdAr|@fF_Y~fAEpc@uoLxh@yj@`zCzAtlBf_@vuI_J`yAei@d]mu@zaBjUx_E{Mjf@cMzHy~EzwIyt@lVhZ~qAfInFb_@ff@ee@ja@pcAyS_x@nWao@pl@fAv`Dl}Bv~AbKbmBeyBd`AyCdk@aeB`c@je@cHdzFvdEw`AbtB|l@oDl}IyVvHszAeu@{d@}dImoGndAm}FlrBsqDks@zLsjDanAmcA";
//        return "enlhGh~ljTvZgA|Bsg@u`@pi@n\\jUcD`|K_`@fo@gOhgCfr@iqD|@cnDfJh`@bLyVgW_^L_lEy[_g@";
    }

    public void setPoly(String poly) {
        if (!TextUtils.isEmpty(poly))
            this.mileage.setPoly(poly);
    }

    public long getTimeStamp() {
            return mileage.getTimeStamp();
    }

    public long getEndTime() {
            return mileage.getEndTime();
    }

    public void setEndTime(long endTime) {
        this.mileage.setEndTime(endTime);
    }

    public String getLatLongString() {
        return mileage.getLatLongString();
    }

    public void setLatLongString(String latLongString) {
        this.mileage.setLatLongString(latLongString);
    }

    public double getMiles() {
        return mileage.getMiles();
    }

    public void setMiles(double miles) {
        this.mileage.setMiles(miles);
    }

    @NonNull
    public List<Location> getPath() {
        return mileage.getPath();
    }

    public void setPath(List<Location> path) {
        this.mileage.setPath(path);
    }

    public long geteDate() {
        return eDate;
    }

    public void seteDate(long eDate) {
        this.eDate = eDate;
    }

    public void postProcessOfflineMileageToSave() {
        mileage.postProcessOfflineMileageToSave();
    }

    public void postProcessMileage(boolean fromLocations) {
        mileage.postProcessMileage(fromLocations);
    }

    public String getCurrentLocation() {
        return mileage.getEndLocation();
    }

    public void setCurrentLocation(String currentLocation) {
        this.mileage.setEndLocation(currentLocation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Expense expense = (Expense) o;

        return getTimeStamp() == expense.getTimeStamp();
    }

    @Override
    public int hashCode() {
        return ("" + getTimeStamp()).hashCode();
    }

    @Override
    public String toString() {
        return "{\"Expense\":{"
                + "\"id\":\"" + id + "\""
                + ", \"eDate\":\"" + eDate + "\""
                + ", \"timeStamp\":\"" + timeStamp + "\""
                + ", \"miles\":\"" + getMiles() + "\""
                + ", \"poly\":\"" + getPoly() + "\""
                + ", \"latLng\":\"" + getLatLongString() + "\""
                + "}}";
    }
}
