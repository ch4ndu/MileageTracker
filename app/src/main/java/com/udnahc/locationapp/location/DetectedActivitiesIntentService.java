package com.udnahc.locationapp.location;//package com.ez.money.service;
//
//import android.app.IntentService;
//import android.content.Intent;
//
//import com.ez.money.R;
//import com.ez.money.utils.Constants;
//import com.ez.money.utils.Plog;
//import com.google.android.gms.location.ActivityRecognitionResult;
//import com.google.android.gms.location.DetectedActivity;
//
//import java.util.ArrayList;
//
//import androidx.core.app.NotificationCompat;
//import androidx.core.app.NotificationManagerCompat;
//import androidx.localbroadcastmanager.content.LocalBroadcastManager;
//
//public class DetectedActivitiesIntentService extends IntentService {
//
//    protected static final String TAG = DetectedActivitiesIntentService.class.getSimpleName();
//
//    public DetectedActivitiesIntentService() {
//        // Use the TAG to name the worker thread.
//        super(TAG);
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    protected void onHandleIntent(Intent intent) {
//        Plog.d(TAG, "onHandleIntent");
//        if (ActivityRecognitionResult.hasResult(intent)) {
//            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
//
//            // Get the list of the probable activities associated with the current state of the
//            // device. Each activity is associated with a confidence level, which is an int between
//            // 0 and 100.
//            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();
//
//            for (DetectedActivity activity : detectedActivities) {
//                Plog.d(TAG, "Detected activity: " + activity.getType() + ", " + activity.getConfidence());
////            broadcastActivity(activity);
////            if (activity.getType() == DetectedActivity.IN_VEHICLE) {
//                if (activity.getConfidence() > 60) {
//                    Intent temp = new Intent(this, GpsTrackerService.class);
//                    startService(temp);
//                    postNotification(activity.toString());
//                    return;
//                }
////            }
//            }
//        } else {
//            postNotification("Empty Activity");
//        }
//    }
//
//    private void broadcastActivity(DetectedActivity activity) {
//        Intent intent = new Intent(Constants.BROADCAST_DETECTED_ACTIVITY);
//        intent.putExtra("type", activity.getType());
//        intent.putExtra("confidence", activity.getConfidence());
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
//    }
//
//    private void postNotification(String message) {
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL)
//                .setSmallIcon(R.drawable.map_vector)
//                .setContentTitle("Activity Recognition")
//                .setContentText(message)
////                .setStyle(new NotificationCompat.BigTextStyle()
////                        .bigText("Much longer text that cannot fit one line..."))
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//
//// notificationId is a unique int for each notification that you must define
//        notificationManager.notify(1234, mBuilder.build());
//    }
//}
