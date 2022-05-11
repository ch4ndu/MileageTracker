package com.udnahc.locationapp.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udnahc.locationapp.App
import com.udnahc.locationapp.util.Plog
import com.udnahc.locationapp.util.Preferences
import com.udnahc.locationmanager.GpsMessage
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.*

class GeofenceReceiver : BroadcastReceiver() {
    private val TAG: String = "GeofenceReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (!Preferences.isAutoTrackEnabled())
            return
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        Plog.appendLog(context, "$TAG got geofence event")
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceErrorMessages.getErrorString(geofencingEvent.errorCode)
            Plog.appendLog(context, "$TAG fence has error message")
            Plog.e(TAG, errorMessage)
            return
        }
        handleEvent(geofencingEvent, context)
//        GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
    }

    private fun handleEvent(geofencingEvent: GeofencingEvent, context: Context) {
        val geofenceTransition = geofencingEvent.geofenceTransition
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            val geofenceTransitionDetails = getGeofenceTransitionDetails(
                geofenceTransition, triggeringGeofences, context)

            Plog.d(TAG, "geofenceDetails %s", geofenceTransitionDetails)
            Plog.appendLog(context, """$TAG: geofenceDetails $geofenceTransitionDetails""")
            TransitionRecognitionReceiver
                .saveTransition(context, SimpleDateFormat("HH:mm", Locale.US)
                    .format(Date()) + "-" + geofenceTransitionDetails)
            TransitionRecognitionReceiver
                .postNotification(TransitionRecognitionReceiver.showPreviousTransitions(App.get().applicationContext))
        } else {
            Plog.appendLog(context, "$TAG - error on transition: $geofenceTransition")
            Plog.e(TAG, "error on transition: $geofenceTransition")
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
    private fun getGeofenceTransitionDetails(geofenceTransition: Int,
        triggeringGeofences: List<Geofence>, context: Context): String {

        val geofenceTransitionString = getTransitionString(geofenceTransition)

        if (geofenceTransitionString == "Enter") {
            EventBus.getDefault().post(GpsMessage.StopBackgroundGpsUpdates("GeoFence Enter"))
        } else if (geofenceTransitionString == "Exit") {
            if (!MileageService.isGpsTrackerActive()) {
                val intent = Intent(context, MileageService::class.java)
                intent.putExtra("fromBackground", true)
                intent.putExtra("fromGeofence", true)
                if (triggeringGeofences.isNotEmpty()) {
                    for (fence in triggeringGeofences) {
                        if (!TextUtils.isEmpty(fence.requestId)) {
                            intent.putExtra("geofenceId", fence.requestId)
                            break
                        }
                    }
                }
                intent.putExtra("reason", "geofenceExit")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }
//        val status = geofenceTransitionString + ":size" + triggeringGeofences.size

        // Get the Ids of each geofence that was triggered.
        val triggeringGeofencesIdsList = ArrayList<String>()
        for (geofence in triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.requestId)
        }
        val triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList)

        return "$geofenceTransitionString: $triggeringGeofencesIdsString"
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private fun getTransitionString(transitionType: Int): String {
        return when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Enter"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "Exit"
            else -> "Unknown"
        }
    }
}
