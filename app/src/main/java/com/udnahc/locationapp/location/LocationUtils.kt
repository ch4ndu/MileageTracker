package com.udnahc.locationapp.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udnahc.locationapp.App
import com.udnahc.locationapp.util.Preferences
import com.udnahc.locationmanager.Plog
import java.util.concurrent.TimeUnit


class LocationUtils {

    companion object {
        private const val TAG = "LocationUtils"
        private var geofencePendingIntent: PendingIntent? = null

        fun startPermissionChecker() {
            WorkManager.getInstance(App.get().applicationContext)
                .cancelAllWorkByTag("PermissionWorker")
            val constraints = Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val saveRequest = OneTimeWorkRequest.Builder(PermissionWorker::class.java)
                .setInitialDelay(6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .addTag("PermissionWorker")
                .build()
            WorkManager.getInstance(App.get().applicationContext)
                .enqueue(saveRequest)
        }

        fun startPeriodicGeoFence() {
            WorkManager.getInstance(App.get().applicationContext)
                .cancelAllWorkByTag("PeriodicGeoFenceWorker")
            val constraints = Constraints.Builder()
                .setRequiresCharging(false)
                .setRequiresBatteryNotLow(false)
                .setRequiresStorageNotLow(false)
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
            val saveRequest = OneTimeWorkRequest.Builder(PeriodicGeoFenceWorker::class.java)
                .setConstraints(constraints)
                .setInitialDelay(2, TimeUnit.HOURS)
                .addTag("PeriodicGeoFenceWorker")
                .build()
            WorkManager.getInstance(App.get().applicationContext)
                .enqueue(saveRequest)
        }

        @SuppressLint("MissingPermission")
        fun setGeoFenceAtCurrentLocation(context: Context,
            location: Location? = null,
            listener: (() -> Unit)? = null) {
            Plog.d(TAG, "setGeoFenceAtCurrentLocation")
            val geofencingClient = LocationServices.getGeofencingClient(context)
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            if (geofencingClient == null || fusedLocationClient == null) {
                listener?.invoke()
                return
            }
            if (location == null) {
                fusedLocationClient.lastLocation.addOnSuccessListener { currentLocation ->
                    if (currentLocation == null) {
                        Plog.e(TAG, "last location null!!")
                        listener?.invoke()
                        return@addOnSuccessListener
                    }
                    Plog.d(TAG, "current location: %s %s", currentLocation.latitude, currentLocation.longitude)

                    cancelAllGeoFences(context) { success ->
                        Plog.d(TAG, "cancel geofence success %s", success)
                        val geofenceList = ArrayList<Geofence>()
                        geofenceList.add(
                            Geofence.Builder()
                                .setRequestId("mileageApp-currentLocation")
                                .setCircularRegion(
                                    currentLocation.latitude,
                                    currentLocation.longitude,
                                    20f
                                )
                                .setLoiteringDelay(10000)
                                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                                .build()
                        )
                        Plog.d(TAG, "setGeoFenceAtCurrentLocation: adding fences")
                        geofencingClient.addGeofences(getGeofencingRequest(geofenceList),
                            getGeofencePendingIntent(context)).run {
                            addOnSuccessListener {
                                Plog.d(TAG, "Added geofence successfully")
                                Preferences.saveLocation(currentLocation)
                                listener?.invoke()
                            }
                            addOnFailureListener { error ->
                                Plog.e(TAG, error, "failed to add geofence")
                                listener?.invoke()
                            }
                        }
                    }
                }
            } else {
                cancelAllGeoFences(context) { success ->
                    Plog.d(TAG, "cancel geofence success %s", success)
                    val geofenceList = ArrayList<Geofence>()
                    geofenceList.add(Geofence.Builder()
                        .setRequestId("mileageApp-currentLocation")
                        .setCircularRegion(location.latitude,
                            location.longitude,
                            30f)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build())
                    Plog.d(TAG, "setGeoFenceAtCurrentLocation: adding fences")
                    geofencingClient.addGeofences(
                        getGeofencingRequest(geofenceList),
                        getGeofencePendingIntent(context)).run {
                        addOnSuccessListener {
                            Plog.d(TAG, "Added geofence successfully")
                            listener?.invoke()
                        }
                        addOnFailureListener { error ->
                            Plog.e(TAG, error, "failed to add geofence")
                            listener?.invoke()
                        }
                    }
                }
            }
        }

        fun cancelAllGeoFences(context: Context, listener: ((success: Boolean) -> Unit)? = null) {
            val geofencingClient = LocationServices.getGeofencingClient(context)
            geofencingClient.removeGeofences(getGeofencePendingIntent(context)).run {
                addOnSuccessListener {
                    Plog.d(TAG, "cancelled all geofences")
                    listener?.invoke(true)
                }
                addOnFailureListener {
                    Plog.d(TAG, "Failed to cancel geofences")
                    listener?.invoke(false)
                }
            }
        }

        private fun getGeofencingRequest(geofenceList: List<Geofence>): GeofencingRequest {
            return GeofencingRequest.Builder().apply {
                setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                addGeofences(geofenceList)
            }.build()
        }

        private fun getGeofencePendingIntent(context: Context): PendingIntent {
            // Reuse the PendingIntent if we already have it.
            geofencePendingIntent?.let {
                return it
            }
            val intent = Intent(context, GeofenceReceiver::class.java)
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
            // calling addGeofences() and removeGeofences().
            geofencePendingIntent =
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            return geofencePendingIntent!!
        }
    }
}
