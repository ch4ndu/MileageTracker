package com.udnahc.locationapp.location

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.udnahc.locationapp.location.LocationUtils
import com.udnahc.locationapp.location.MileageService
import com.udnahc.locationapp.util.Plog
import java.lang.StringBuilder


class PeriodicGeoFenceWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {
    private val TAG = "PermissionWorker"

    override fun doWork(): Result {
        val buffer = StringBuilder()
        buffer.append("Starting PeriodicGeoFenceWorker\n")
        if (!MileageService.isGpsTrackerActive()) {
            LocationUtils.setGeoFenceAtCurrentLocation(applicationContext)
            buffer.append("Added GeoFence\n")
        }
        Plog.appendLog(applicationContext, buffer.toString())
        LocationUtils.startPeriodicGeoFence()
        return Result.success()
    }
}
