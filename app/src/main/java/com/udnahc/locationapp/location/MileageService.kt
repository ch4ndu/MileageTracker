package com.udnahc.locationapp.location

import android.content.Intent
import android.text.TextUtils
import com.udnahc.locationapp.App
import com.udnahc.locationapp.model.Expense
import com.udnahc.locationapp.util.Preferences
import com.udnahc.locationapp.util.UtilsKt
import com.udnahc.locationmanager.GpsTrackerService
import com.udnahc.locationmanager.Mileage

@Suppress("ConstantConditionIf")
open class MileageService : GpsTrackerService() {

    override fun handleOfflineCallback(mileage: Mileage, listener: OfflineComplete?) {
        if (USE_AUTO_TRACKING) {
            if (mileage.miles > 0.2)
                App.get().dbHelper.addMileage(Expense(mileage))
        }
        if (listener != null) {
            LocationUtils.setGeoFenceAtCurrentLocation(applicationContext, null) {
                listener.onComplete()
            }
        }
    }

    override fun checkFenceInfo(intent: Intent): String {
        if (intent.getBooleanExtra("fromGeofence", false)) {
            val fenceId = intent.getStringExtra("geofenceId")
            if (TextUtils.isEmpty(fenceId)) {
                return "empty fence name"
            }
            if (isFromBackground() && !TextUtils.isEmpty(fenceId) && fenceId == "currentLocation") {
                val location = Preferences.getLastFence()
                location?.let {
                    it.accuracy = 20f
                    addInitialLocation(it)
                    return "Passed Fence"
                }
            } else {
                return "Failed to pass location - $fenceId"
            }
        }
        return "Not from fence"
    }

    override fun logException(throwable: Throwable) {
        try {
//            FirebaseCrashlytics.getInstance().recordException(throwable)
        } catch (ex: Exception) {
        }
    }

    override fun shouldRunNow(): Boolean {
        if (userStarted)
            return true
        if (Preferences.isAutoTrackScheduleEnabled()) {
            return UtilsKt.shouldRunNow()
        }
        return true
    }

    override fun checkDebugLogs() {
        saveLogs = Preferences.shouldSaveLogs()
    }

    companion object {
        fun isFromBackground(): Boolean {
            return initiatedFromBackground
        }

        fun isAutoTrackEnabled(): Boolean {
            return USE_AUTO_TRACKING
        }

        fun isDebugAutoTrack(): Boolean {
            return DEBUG_AUTO_TRACKING
        }

        fun isDebugWalking(): Boolean {
            return DEBUG_WALKING
        }

        fun isGpsTrackerActive(): Boolean {
            return gpsTrackerActive
        }

        fun getNotificationChannelDebug(): String {
            return NOTIFICATION_CHANNEL_DEBUG
        }
    }
}
