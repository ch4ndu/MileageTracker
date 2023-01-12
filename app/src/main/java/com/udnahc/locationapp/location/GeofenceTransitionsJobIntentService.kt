package com.udnahc.locationapp.location

import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udnahc.locationapp.App
import com.udnahc.locationapp.util.Plog
import com.udnahc.locationmanager.GpsMessage
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.*

class GeofenceTransitionsJobIntentService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceErrorMessages.getErrorString(geofencingEvent.errorCode)
            Plog.appendLog(applicationContext, "onHandleWork: $errorMessage")
            Plog.e(TAG, errorMessage)
            return
        }
        handleEvent(geofencingEvent)
    }

    private fun handleEvent(geofencingEvent: GeofencingEvent) {
        val geofenceTransition = geofencingEvent.geofenceTransition
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return
            val geofenceTransitionDetails = getGeofenceTransitionDetails(
                geofenceTransition, triggeringGeofences
            )

            Plog.d(TAG, "geofenceDetails %s", geofenceTransitionDetails)
            Plog.appendLog(applicationContext, """${TAG}: geofenceDetails $geofenceTransitionDetails""")
            TransitionRecognitionReceiver.saveTransition(
                applicationContext, SimpleDateFormat("HH:mm", Locale.US)
                    .format(Date()) + "-" + geofenceTransitionDetails
            )
            TransitionRecognitionReceiver.postNotification(
                TransitionRecognitionReceiver.showPreviousTransitions(App.get().applicationContext)
            )
        } else {
            Plog.appendLog(applicationContext, "${TAG} - error on transition: $geofenceTransition")
            Plog.e(TAG, "error on transition")
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
    private fun getGeofenceTransitionDetails(
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>): String {

        val geofenceTransitionString = getTransitionString(geofenceTransition)

        if (geofenceTransitionString == "Enter") {
            EventBus.getDefault().post(GpsMessage.StopBackgroundGpsUpdates("GeoFence Enter"))
        } else if (geofenceTransitionString == "Exit") {
            if (!MileageService.isGpsTrackerActive()) {
                val intent = Intent(this, MileageService::class.java)
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
                    applicationContext.startForegroundService(intent)
                } else {
                    startService(intent)
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

    companion object {
        private val TAG = "GeofenceJobService"

        private const val JOB_ID = 453

        fun enqueueWork(context: Context, intent: Intent) {
            Plog.appendLog(context, "$TAG: got geofence event")
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }


}
