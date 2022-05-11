package com.udnahc.locationapp.location

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Context.USAGE_STATS_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.udnahc.locationapp.App
import com.udnahc.locationapp.BuildConfig
import com.udnahc.locationapp.R
import com.udnahc.locationapp.util.Plog
import com.udnahc.locationapp.util.Preferences
import com.udnahc.locationmanager.GpsTrackerService


class PermissionWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {
    private val TAG = "PermissionWorker"

    override fun doWork(): Result {
        val buffer = StringBuilder()
        buffer.append("Starting PermissionWorker\n")
        val autoTrackEnabled = Preferences.isAutoTrackEnabled()
        if (autoTrackEnabled) {
            val hasAllPermissions = hasAllPermissions()
            if (!hasAllPermissions) {
                createNotificationChannel(applicationContext)
                updateNotification(applicationContext)
            } else {
                if(BuildConfig.DEBUG) {
                    createNotificationChannel(applicationContext)
                    updateDebugNotification(applicationContext)
                }
            }
        }
        buffer.append("checked Permissions\n")
        LocationUtils.startPermissionChecker()
        Plog.appendLog(applicationContext, buffer.toString())
        return Result.success()
    }

    private fun hasAllPermissions(): Boolean {
        return when {
            !isHardwareGpsEnabled() -> false
            isBatteryOptimizationMissing() -> false
            !hasAlwaysLocation() -> false
            !hasActivityPermission() -> false
            else -> true
        }
    }

    private fun isHardwareGpsEnabled(): Boolean {
        val manager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Plog.d(TAG, "isHardwareGpsEnabled missing")
            return false
        }
        return true
    }

    private fun isBatteryOptimizationMissing(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
                if (powerManager != null && powerManager.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)) {
                    return false
                }
            } catch (e: Exception) {
                Plog.e(TAG, e, "chekBatteryOptimization")
            }

        }
        Plog.d(TAG, "isBatteryOptimizationMissing missing")
        return true
    }

    private fun hasAlwaysLocation(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val missing = ActivityCompat
                    .checkSelfPermission(applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
            if (!missing)
                Plog.d(TAG, "hasAlwaysLocation missing")
            missing
        } else {
            true
        }
    }

    private fun isInStandByBucket(): Boolean {
        if (Build.VERSION.SDK_INT >= 28) {
            val usageStatsManager = App.get().applicationContext.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager?
            if (usageStatsManager != null) {
                val bucket = usageStatsManager.appStandbyBucket
                if (bucket == UsageStatsManager.STANDBY_BUCKET_RESTRICTED) {
                    return false
                }
                Plog.d(TAG, "getAppStandbyBucket():" + usageStatsManager.appStandbyBucket)
            }
        }
        return true
    }

    private fun hasActivityPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val missing = ActivityCompat
                    .checkSelfPermission(applicationContext, Manifest.permission.ACTIVITY_RECOGNITION) ==
                    PackageManager.PERMISSION_GRANTED
            if (!missing)
                Plog.d(TAG, "hasAlwaysLocation missing")
            missing
        } else {
            true
        }
    }

    private fun createNotificationChannel(context: Context) {
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        val id = "Missing Permissions"
        val name: CharSequence = "Loxley"
        val description = "$name Notifications"
        val mChannel: NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                mChannel = NotificationChannel(id, name, importance)
                mChannel.description = description
                mChannel.setSound(null, null)
                mChannel.enableVibration(false)
                mChannel.enableLights(false)
                mNotificationManager?.createNotificationChannel(mChannel)
            } catch (e: NullPointerException) {
                Plog.e(TAG, e, "NotificationChannel")
            }
        }
    }

    private fun getPermissionNotification(context: Context, intent: PendingIntent?): Notification? {
        try {
            val mBuilder: NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationCompat.Builder(context, GpsTrackerService.NOTIFICATION_CHANNEL)
            } else {
                NotificationCompat.Builder(context)
            }
            val appName = context.getString(R.string.app_name)
            mBuilder.setColorized(true)
                    .setSmallIcon(R.drawable.map_vector)
                    .setVibrate(longArrayOf(0L))
                    .setContentIntent(intent)
                    .setOngoing(false)
                    .setOnlyAlertOnce(true)
                    .setGroup("MissingPermission")
                    .setGroupSummary(true).color = ResourcesCompat.getColor(context.resources, R.color.notificationColor, null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBuilder.setContentTitle("Loxley needs missing permissions")
                mBuilder.setContentText("Loxley app is missing a few permissions needed to Auto Track miles in the background")
            } else {
                mBuilder.setContentText("Loxley needs missing permissions")
                        .setContentTitle(appName)
                mBuilder.setContentInfo("Loxley app is missing a few permissions needed to Auto Track miles in the background")
            }
            return mBuilder.build()
        } catch (e: java.lang.NullPointerException) {
            Plog.e(TAG, e, "postMediaScanNotification")
        }
        return null
    }

    private fun updateNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        if (manager != null) {
            val notificationIntent = context.packageManager
                    .getLaunchIntentForPackage(context.packageName)
                    ?.setPackage(null)
                    ?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            notificationIntent?.let {
                val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                manager.notify(GpsTrackerService.UPDATE_GPS_TRACKING_NOTIFICATION, getPermissionNotification(applicationContext, pendingIntent))
            }
        }
    }

    private fun getDebugPermissionNotification(context: Context, intent: PendingIntent?): Notification? {
        try {
            val mBuilder: NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationCompat.Builder(context, GpsTrackerService.NOTIFICATION_CHANNEL)
            } else {
                NotificationCompat.Builder(context)
            }
            val appName = context.getString(R.string.app_name)
            mBuilder.setColorized(true)
                    .setSmallIcon(R.drawable.map_vector)
                    .setVibrate(longArrayOf(0L))
                    .setContentIntent(intent)
                    .setOngoing(false)
                    .setOnlyAlertOnce(true)
                    .setGroup("MissingPermission")
                    .setGroupSummary(true).color = ResourcesCompat.getColor(context.resources, R.color.notificationColor, null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBuilder.setContentTitle("Loxley has all Permissions for auto-tracking")
                mBuilder.setContentText("")
            } else {
                mBuilder.setContentText("Loxley has all Permissions for auto-tracking")
                        .setContentTitle(appName)
                mBuilder.setContentInfo("")
            }
            return mBuilder.build()
        } catch (e: java.lang.NullPointerException) {
            Plog.e(TAG, e, "postMediaScanNotification")
        }
        return null
    }

    private fun updateDebugNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        if (manager != null) {
            val notificationIntent = context.packageManager
                    .getLaunchIntentForPackage(context.packageName)
                    ?.setPackage(null)
                    ?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            notificationIntent?.let {
                val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                manager.notify(GpsTrackerService.UPDATE_GPS_TRACKING_NOTIFICATION, getDebugPermissionNotification(applicationContext, pendingIntent))
            }
        }
    }
}
