package com.udnahc.locationapp.location

import android.Manifest.permission.ACTIVITY_RECOGNITION
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import com.udnahc.locationapp.App
import com.udnahc.locationapp.controller.UtilActivity
import com.udnahc.locationapp.util.Plog
import com.udnahc.locationapp.util.Preferences


class TransitionRecognition {
    private val TAG = TransitionRecognition::class.java.simpleName
    private var mPendingIntent: PendingIntent? = null

    fun startTracking(context: Context) {
//        stopTracking(context)
        launchTransitionsTracker(context)
//        saveTransition(context)
    }

    fun stopTracking(context: UtilActivity) {
        if (ContextCompat.checkSelfPermission(
                context,
                ACTIVITY_RECOGNITION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            ActivityRecognition.getClient(context)
                .removeActivityTransitionUpdates(getPendingIntent(context))
                .addOnSuccessListener {
                    getPendingIntent(context).cancel()
                    Toast.makeText(context, "Cancelled auto-track successfully!", Toast.LENGTH_LONG)
                        .show()
                }
                .addOnFailureListener { e ->
                    if (!context.isFinishing && !context.isDestroyed) {
                        Toast.makeText(
                            context,
                            "There was an error canceling auto-track. Please try again later!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Preferences.saveAutoTrackPreference(true)
                    Plog.e(TAG, e, "Transitions could not be unregistered")
                }
        }
    }

    /***********************************************************************************************
     * LAUNCH TRANSITIONS TRACKER
     **********************************************************************************************/
    @SuppressLint("MissingPermission")
    private fun launchTransitionsTracker(mContext: Context) {
        if (mContext is UtilActivity) {
            if (!mContext.hasActivityPermission())
                return
        }
        val transitions = ArrayList<ActivityTransition>()
        transitions.add(ActivityTransition.Builder()
            .setActivityType(DetectedActivity.IN_VEHICLE)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build())
        transitions.add(ActivityTransition.Builder()
            .setActivityType(DetectedActivity.IN_VEHICLE)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build())
        if (MileageService.isDebugWalking()) {
            Plog.d(TAG, "Adding walking")
            transitions.add(ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build())
            transitions.add(ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build())
        }
        val request = ActivityTransitionRequest(transitions)
        val activityRecognitionClient = ActivityRecognition.getClient(mContext)

        val task = activityRecognitionClient.requestActivityTransitionUpdates(request,
            getPendingIntent(mContext))
        App.get().isIgnoreTransitions = true
        task.addOnSuccessListener {
            Plog.d(TAG, "created transition recognizer")
        }

        task.addOnFailureListener { error ->
            if (mContext is UtilActivity && !mContext.isFinishing && !mContext.isDestroyed) {
                Toast.makeText(mContext,
                    "Failed to enable Auto-tracking. Please try again later!",
                    Toast.LENGTH_LONG).show()
            }
            Preferences.saveAutoTrackPreference(false)
            Plog.e(TAG, error, "failed to create transition recognizer")
        }
        if (mContext is UtilActivity) {
            mContext.mainHandler.postDelayed({ App.get().isIgnoreTransitions = false }, 2000)
        }
    }

    private fun getPendingIntent(mContext: Context): PendingIntent {
        if (mPendingIntent == null) {
            val intent = Intent(mContext, TransitionRecognitionReceiver::class.java)
            intent.action = "com.udnahc.locationapp.TRANSITION_RECOGNITION"
            mPendingIntent =
                PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
        return mPendingIntent!!
    }

    private fun saveTransition(mContext: Context) {
        // Save in Preferences
        val sharedPref = mContext.getSharedPreferences(
            TransitionRecognitionReceiver.SHARED_PREFERENCES_FILE_KEY_TRANSITIONS,
            Context.MODE_PRIVATE
        )

        with(sharedPref.edit()) {
            putString(TransitionRecognitionReceiver.SHARED_PREFERENCES_KEY_TRANSITIONS, "")
            apply()
        }
    }
}
