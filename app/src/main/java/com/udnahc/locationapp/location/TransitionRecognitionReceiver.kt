package com.udnahc.locationapp.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.GROUP_ALERT_ALL
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionEvent
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import com.udnahc.locationapp.App
import com.udnahc.locationapp.R
import com.udnahc.locationapp.controller.UtilActivity
import com.udnahc.locationapp.util.Plog
import com.udnahc.locationapp.util.Preferences
import com.udnahc.locationmanager.GpsMessage
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.*


class TransitionRecognitionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Plog.d(TAG, "onReceive")
        Plog.appendLog(context, "$TAG-onReceive")
        if (!Preferences.isAutoTrackEnabled()){
            Plog.appendLog(context, "$TAG-autoTrack disabled. Returning")
            return
        }
        if (intent != null && ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            result?.let { processTransitionResult(context!!, result) }
        } else {
            Plog.appendLog(context, "$TAG-intent null or no transitions")

        }
    }

    private fun processTransitionResult(context: Context, result: ActivityTransitionResult) {
        if (App.get().isIgnoreTransitions) {
            Plog.appendLog(context, "$TAG-processTransitionResult ignore transitions")
            return
        }
        for (event in result.transitionEvents) {
            onDetectedTransitionEvent(context, event)
        }
    }

    private fun onDetectedTransitionEvent(context: Context, activity: ActivityTransitionEvent) {
        when (activity.activityType) {
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.RUNNING,
            DetectedActivity.WALKING -> {
                if (MileageService.isDebugWalking()) {
                    saveTransition(context, activity)
                    postNotification(showPreviousTransitions(App.get().applicationContext))
                    if (activity.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                        handleEnterState(context)
                    } else if (activity.transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                        handleExitState()
                    }
                } else {
                    Plog.appendLog(context, "$TAG-debug walking not enabled")
                }
            }
            DetectedActivity.IN_VEHICLE -> {
                saveTransition(context, activity)
                postNotification(showPreviousTransitions(App.get().applicationContext))
                if (activity.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                    handleEnterState(context)
                } else if (activity.transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                    handleExitState()
                }
            }
        }
    }

    private fun handleExitState() {
        if (MileageService.isGpsTrackerActive() && MileageService.isFromBackground())
            EventBus.getDefault().post(GpsMessage.StopBackgroundGpsUpdates("TransitionExitState"))

    }

    private fun handleEnterState(context: Context) {
        if (!MileageService.isGpsTrackerActive()) {
            val intent = Intent(context, MileageService::class.java)
            intent.putExtra("fromBackground", true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (MileageService.isGpsTrackerActive()) {
                    EventBus.getDefault()
                        .post(GpsMessage.RestartBackgroundGpsUpdates("Transition:Enter"))
                } else {
                    intent.putExtra("reason", "Transition:Enter")
                    context.startForegroundService(intent)
                }
            } else {
                if (MileageService.isGpsTrackerActive()) {
                    EventBus.getDefault()
                        .post(GpsMessage.RestartBackgroundGpsUpdates("Transition:Enter"))
                } else {
                    intent.putExtra("reason", "Transition:Enter")
                    context.startService(intent)
                }
            }
        }
    }

    companion object {

        /**
         * In this example we save in preferences, but is a bad way to do that.
         * Is an Example, in a real app we have to save in database.
         */

        fun clearTransition(mContext: Context) {
            val sharedPref = mContext.getSharedPreferences(
                SHARED_PREFERENCES_FILE_KEY_TRANSITIONS, Context.MODE_PRIVATE
            )

            with(sharedPref.edit()) {
                putString(SHARED_PREFERENCES_KEY_TRANSITIONS, "")
                apply()
            }
        }

        fun saveTransition(mContext: Context, activity: ActivityTransitionEvent) {
            if (!MileageService.isDebugAutoTrack())
                return
            // Save in Preferences
            val sharedPref = mContext.getSharedPreferences(
                SHARED_PREFERENCES_FILE_KEY_TRANSITIONS, Context.MODE_PRIVATE
            )

            with(sharedPref.edit()) {
                val oldStr = sharedPref.getString(SHARED_PREFERENCES_KEY_TRANSITIONS, "")
                val transitions = createTransitionString(activity)
                putString(SHARED_PREFERENCES_KEY_TRANSITIONS, "$transitions|$oldStr")
                apply()
            }
        }

        fun saveTransition(mContext: Context, geofenceEvent: String) {
            if (!MileageService.isDebugAutoTrack())
                return
            // Save in Preferences
            val sharedPref = mContext.getSharedPreferences(
                SHARED_PREFERENCES_FILE_KEY_TRANSITIONS, Context.MODE_PRIVATE
            )

            with(sharedPref.edit()) {
                val oldStr = sharedPref.getString(SHARED_PREFERENCES_KEY_TRANSITIONS, "")
                putString(SHARED_PREFERENCES_KEY_TRANSITIONS, geofenceEvent + "|" + oldStr)
                apply()
            }
        }

        fun postNotification(message: String?) {
            if (!MileageService.isDebugAutoTrack() || !Preferences.isAutoTrackEnabled())
                return
//            clearTransition(App.get().applicationContext)
            val mBuilder = NotificationCompat.Builder(
                App.get().applicationContext,
                MileageService.getNotificationChannelDebug()
            )
                .setSmallIcon(R.drawable.map_vector)
                .setContentTitle("Activity Recognition")
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                //                .setStyle(new NotificationCompat.BigTextStyle()
                //                        .bigText("Much longer text that cannot fit one line..."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setGroupAlertBehavior(GROUP_ALERT_ALL)
                .setGroup("com.udnahc.locationapp")
            val notificationManager = NotificationManagerCompat.from(App.get().applicationContext)

            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(1234, mBuilder.build())
        }


        const val SHARED_PREFERENCES_FILE_KEY_TRANSITIONS =
            "SHARED_PREFERENCES_FILE_KEY_TRANSITIONS"
        const val SHARED_PREFERENCES_KEY_TRANSITIONS = "SHARED_PREFERENCES_KEY_TRANSITIONS"

        private fun createTransitionString(activity: ActivityTransitionEvent): String {
            val theActivity = toActivityString(activity.activityType)
            val transType = toTransitionType(activity.transitionType)

            return ("Transition: "
                    + theActivity + " (" + transType + ")" + "   "
                    + SimpleDateFormat("HH:mm", Locale.US)
                .format(Date()))
        }


        private fun toActivityString(activity: Int): String {
            return when (activity) {
                DetectedActivity.STILL -> "STILL"
                DetectedActivity.WALKING -> "WALKING"
                DetectedActivity.ON_BICYCLE -> "ON_BICYCLE"
                DetectedActivity.ON_FOOT -> "ON_FOOT"
                DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
                DetectedActivity.RUNNING -> "RUNNING"
                else -> "UNKNOWN"
            }
        }

        private fun toTransitionType(transitionType: Int): String {
            return when (transitionType) {
                ActivityTransition.ACTIVITY_TRANSITION_ENTER -> "ENTER"
                ActivityTransition.ACTIVITY_TRANSITION_EXIT -> "EXIT"
                else -> "UNKNOWN"
            }
        }

        /**
         * INIT TRANSITION RECOGNITION
         */
        fun initTransitionRecognition(context: UtilActivity) {
            val mTransitionRecognition = TransitionRecognition()
            mTransitionRecognition.startTracking(context)
        }

        /**
         * Show previous transitions. This is an example to explain how to detect user's activity. To
         * see this activity we have to relaunch the app.
         */
        fun showPreviousTransitions(context: Context): String? {
            val sharedPref = context.getSharedPreferences(
                SHARED_PREFERENCES_FILE_KEY_TRANSITIONS, Context.MODE_PRIVATE
            )

            return sharedPref.getString(SHARED_PREFERENCES_KEY_TRANSITIONS, "")
        }

        private val TAG = "TransitionRecognitionReceiver"
    }
}
