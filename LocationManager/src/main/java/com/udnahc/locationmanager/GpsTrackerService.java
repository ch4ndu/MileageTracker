package com.udnahc.locationmanager;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("UnnecessaryLocalVariable")
@SuppressLint("DefaultLocale")
public class GpsTrackerService extends Service implements GpsOnListener {
    private final static String TAG = GpsTrackerService.class.getSimpleName();
    private Handler mServiceHandler;
    private HandlerThread serviceThread;
    private LocationManager locationManager;
    private Mileage mileageImpl;
    public static boolean initiatedFromBackground = false;
    private Location locationAtStopTrigger;
    private final long durationMinutes = 15;
    @SuppressWarnings("FieldCanBeLocal")
    private final float minimumMovementInMeters = 10;
    //        private float testDistance = 200;
    public static boolean gpsTrackerActive = false;
    private Geocoder geocoder;
    public boolean userStarted = false;
    private DestroyRunnable destroyRunnable = new DestroyRunnable();
    private BackgroundRunnable backgroundRunnable;


    public static final String NOTIFICATION_CHANNEL = "ex_channel";
    public static final String NOTIFICATION_CHANNEL_DEBUG = "ex_channel_debug";
    public static final int UPDATE_GPS_TRACKING_NOTIFICATION = 1234291;
    public static final int UPDATE_AUTO_TRACK_NOTIFICATION = 2143921;
    //wont stop the service
    public static final boolean DEBUG_AUTO_TRACKING = BuildConfig.DEBUG;
    //        public static final boolean DEBUG_AUTO_TRACKING = false;
    public static final boolean DEBUG_WALKING = false;
    public static final boolean USE_AUTO_TRACKING = true;

    public boolean saveLogs = false;

    @Override
    public void onCreate() {
        Intent notificationIntent = getApplicationContext().getPackageManager()
                .getLaunchIntentForPackage(getApplicationContext().getPackageName())
                .setPackage(null)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        notificationIntent.putExtra("goto", "gps");
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = getGpsNotification(getApplicationContext(), pendingIntent, "");

        startForeground(UPDATE_GPS_TRACKING_NOTIFICATION, notification);
        Plog.d(TAG, "onCreate");
        Plog.appendLog(getApplicationContext(), "onCreate", mileageImpl);
    }

    public boolean shouldRunNow() {
        return true;
    }

    public void checkDebugLogs() {

    }

    public void logException(@NonNull Throwable throwable) {

    }

    public String checkFenceInfo(Intent intent) {
        return "";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Plog.d(TAG, "onStartCommand");
        Intent notificationIntent = getApplicationContext().getPackageManager()
                .getLaunchIntentForPackage(getApplicationContext().getPackageName())
                .setPackage(null)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        notificationIntent.putExtra("goto", "gps");
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = getGpsNotification(getApplicationContext(), pendingIntent, "");

        startForeground(UPDATE_GPS_TRACKING_NOTIFICATION, notification);


        if (mileageImpl == null) {
            mileageImpl = new Mileage();
            mileageImpl.setTimeStamp(System.currentTimeMillis());
            mileageImpl.setPath(new ArrayList<Location>());
        }
        checkDebugLogs();
        Plog.saveDebugLogs = saveLogs;
        Plog.appendLog(getApplicationContext(), "onStartCommand", mileageImpl);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        userStarted = userStarted || intent != null && intent.getBooleanExtra("userStarted", false);
        initiatedFromBackground = intent != null && intent.getBooleanExtra("fromBackground", false);
        if (!initiatedFromBackground) // service is restarted when app crashes
            initiatedFromBackground = !userStarted;
        if (isGpsTrackerActive()) {
            Plog.d(TAG, "onStartCommand: already active!! wtf!!");
            Plog.appendLog(getApplicationContext(), "onStartCommand: already active!! wtf!!", mileageImpl);
            return START_STICKY;
        }

        if (!shouldRunNow()) {
            Plog.d(TAG, "onStartCommand: should not run now!!");
            Plog.appendLog(getApplicationContext(), "onStartCommand: should not run now!!", mileageImpl);
            stopSelf();
            return START_STICKY;
        }
        if (serviceThread == null || !serviceThread.isAlive()) {
            serviceThread = new HandlerThread("GpsTracker");
            serviceThread.start();

            mServiceHandler = new Handler(serviceThread.getLooper());
        }
        if (geocoder == null)
            geocoder = new Geocoder(GpsTrackerService.this.getApplicationContext(), Locale.getDefault());
        setGpsTrackerActive(true);
        if (intent != null) {
            String reason = intent.getStringExtra("reason");
            if (!TextUtils.isEmpty(reason)) {
                Plog.appendLog(getApplicationContext(), "reason:" + reason, mileageImpl);
            }
            String message = checkFenceInfo(intent);
            Plog.appendLog(getApplicationContext(), "checkFenceInfo:" + message, mileageImpl);
        }
        Plog.d(TAG, "background %s userInitiated %s", initiatedFromBackground, userStarted);
        Plog.appendLog(getApplicationContext(), String.format("background %s userInitiated %s", initiatedFromBackground, userStarted), mileageImpl);

        mServiceHandler.post(new Runnable() {
            @Override
            public void run() {
                if (locationManager == null)
                    locationManager = new LocationManager(GpsTrackerService.this);
                locationManager.startLocationUpdates();
                if (initiatedFromBackground) {
                    mServiceHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onMessageEvent(new GpsMessage.StopBackgroundGpsUpdates("Service:InitiatedFromBackground"));
                        }
                    }, 30 * 1000);
                }
            }
        });
        if (DEBUG_AUTO_TRACKING && !isFromBackground()) {
            mServiceHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initiatedFromBackground = true;
                    onMessageEvent(new GpsMessage.StopBackgroundGpsUpdates("Service:DebugAutoTrack"));
                }
            }, 25 * 1000);
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class DestroyRunnable implements Runnable {
        private boolean isInProgress = false;

        boolean isInProgress() {
            return isInProgress;
        }

        @Override
        public void run() {
            Plog.d(TAG, "DestroyRunnable: run");
            Plog.appendLog(getApplicationContext(), "DestroyRunnable: run", mileageImpl);
            isInProgress = true;
            setGpsTrackerActive(false);
            initiatedFromBackground = false;
            if (mServiceHandler != null) {
                mServiceHandler.removeCallbacks(backgroundRunnable);
                mServiceHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (locationManager != null)
                            locationManager.stopLocationUpdate();
                        if (mileageImpl != null && mileageImpl.getPath().size() > 1) {
                            Plog.d(TAG, "DestroyRunnable posting");
                            Plog.appendLog(getApplicationContext(), "DestroyRunnable posting", mileageImpl);
                            mileageImpl.postProcessMileage(true);
                            mileageImpl.postProcessOfflineMileageToSave();
                            EventBus.getDefault().postSticky(new GpsMessage.MileageUpdate(mileageImpl));
                            Plog.d(TAG, "DestroyRunnable offline callback");
                            Plog.appendLog(getApplicationContext(), "DestroyRunnable offline callback", mileageImpl);
                            handleOfflineCallback(mileageImpl, new OfflineComplete() {
                                @Override
                                public void onComplete() {
                                    Plog.d(TAG, "DestroyRunnable offline callback done");
                                    Plog.appendLog(getApplicationContext(), "DestroyRunnable offline callback done", mileageImpl);
                                    isInProgress = false;
                                    serviceThread.quit();
                                }
                            });
                        }
                    }
                });
            } else {
                Plog.d(TAG, "DestroyRunnable:handler null wtf!!");
                Plog.appendLog(getApplicationContext(), "DestroyRunnable:handler null wtf!!", mileageImpl);
            }
        }
    }

    @SuppressWarnings("IfStatementWithIdenticalBranches")
    @Override
    public void onDestroy() {
        Plog.d(TAG, "onDestroy");
        Plog.appendLog(getApplicationContext(), "onDestroy - ending service", mileageImpl);
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (destroyRunnable != null && !destroyRunnable.isInProgress()) {
            destroyRunnable.run();
        } else {
            destroyRunnable = new DestroyRunnable();
            destroyRunnable.run();
        }
    }

    public void handleOfflineCallback(Mileage mileage, OfflineComplete listener) {}

    public void addInitialLocation(@NonNull Location location) {
        if (mileageImpl == null) {
            mileageImpl = new Mileage();
            mileageImpl.setTimeStamp(System.currentTimeMillis());
            mileageImpl.setPath(new ArrayList<Location>());
        }
        mileageImpl.getPath().add(0, location);
        mileageImpl.postProcessMileage(true);
    }

    @Override
    public void onLocation(@Nullable final Location location) {
        mServiceHandler.post(new Runnable() {
            @Override
            public void run() {
                if (location != null) {
                    if (mileageImpl == null) {
                        mileageImpl = new Mileage();
                        mileageImpl.setTimeStamp(System.currentTimeMillis());
                        mileageImpl.setPath(new ArrayList<Location>());
                    }
                    Plog.d(TAG, "accuracy: %s", location.getAccuracy());
                    Plog.appendLog(getApplicationContext(), String.format("accuracy: %s", location.getAccuracy()), mileageImpl);
                    //background updates will get spotty
                    // and need to be counted to effectively stop updates
                    if (location.getAccuracy() > 40) {
                        Plog.d(TAG, "bad accuracy!!");
                        Plog.appendLog(getApplicationContext(), "bad accuracy!!", mileageImpl);
                        return;
                    }
                    mileageImpl.getPath().add(location);
                    mileageImpl.postProcessMileage(true);
                    if (geocoder == null) {
                        geocoder = new Geocoder(GpsTrackerService.this.getApplicationContext(), Locale.getDefault());
                    }
                    List<Address> addresses = null;
                    try {
                        addresses = geocoder.getFromLocation(
                                location.getLatitude(),
                                location.getLongitude(),
                                1);
                    } catch (IOException | IllegalArgumentException ioException) {
                        logException(ioException);
                        // Catch network or other I/O problems.
                        Plog.e(TAG, ioException, "error getting geocoder");
//                        Plog.appendLog(getApplicationContext(), "error getting geocoder!", mileageImpl);
                    }

                    // Handle case where no address was found.
                    if (addresses == null || addresses.size() == 0) {
                        Plog.e(TAG, "failed to get address");
//                        Plog.appendLog(getApplicationContext(), "failed to get address", mileageImpl);
                        updateNotification("");
                    } else {
                        Address address = addresses.get(0);
                        String locality = address.getLocality() == null ? "NA" : address.getLocality();
                        String state = address.getAdminArea() == null ? "NA" : address.getAdminArea();
                        Plog.i(TAG, "address found: " + locality + "," + state);
                        final String currentAddress = locality + "," + state;
//                        Plog.appendLog(getApplicationContext(), "address found: " + currentAddress, mileageImpl);
                        if (mileageImpl.getPath().size() == 1) {
                            if (address.getAddressLine(0) != null) {
                                mileageImpl.setStartLocation(address.getAddressLine(0));
                                mileageImpl.setEndLocation(address.getAddressLine(0));
                            }
                            if (TextUtils.isEmpty(mileageImpl.getStartLocation()))
                                mileageImpl.setStartLocation(currentAddress);
                        } else {
                            if (address.getAddressLine(0) != null)
                                mileageImpl.setEndLocation(address.getAddressLine(0));
                            if (TextUtils.isEmpty(mileageImpl.getEndLocation()))
                                mileageImpl.setEndLocation(currentAddress);
                        }
                        updateNotification(currentAddress);
                    }
                    EventBus.getDefault().postSticky(new GpsMessage.MileageUpdate(mileageImpl));
                    mileageImpl.postProcessMileage(true);
                    mileageImpl.postProcessOfflineMileageToSave();
                    handleOfflineCallback(mileageImpl, null);
                    if (backgroundRunnable != null && backgroundRunnable.shouldRunNow()) {
                        Plog.d(TAG, "forcing backgroundRunnable");
                        Plog.appendLog(getApplicationContext(), "forcing backgroundRunnable", mileageImpl);
                        backgroundRunnable.run();
                    }
                } else {
                    updateNotification("");
                    Plog.appendLog(getApplicationContext(), "Unable to find location", mileageImpl);
                }
            }
        });
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onMessageEvent(final GpsMessage.StopGpsUpdates event) {
        mServiceHandler.post(new Runnable() {
            @Override
            public void run() {
                Plog.d(TAG, "StopGpsUpdates");
                Plog.appendLog(getApplicationContext(), String.format("StopGpsUpdates-%s", event.getReason()), mileageImpl);
            }
        });
        stopSelf();
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onMessageEvent(final GpsMessage.StopBackgroundGpsUpdates event) {
        mServiceHandler.post(new Runnable() {
            @Override
            public void run() {
                Plog.d(TAG, "StopBackgroundGpsUpdates: %s", event.getReason());
                Plog.appendLog(getApplicationContext(), String.format("StopBackgroundGpsUpdates-%s", event.getReason()), mileageImpl);
                if (!isGpsTrackerActive()) {
                    Plog.d(TAG, "StopBackgroundGpsUpdates: not active");
                    Plog.appendLog(getApplicationContext(), "StopBackgroundGpsUpdates: not active", mileageImpl);
                    return;
                }
                if (destroyRunnable.isInProgress()) {
                    Plog.d(TAG, "StopBackgroundGpsUpdates: destroy in progress");
                    Plog.appendLog(getApplicationContext(), "StopBackgroundGpsUpdates: destroy in progress", mileageImpl);
                    return;
                }
                if (initiatedFromBackground) {
                    if (backgroundRunnable == null) {
                        backgroundRunnable = new BackgroundRunnable(System.currentTimeMillis());
                    }
                    if (mileageImpl != null && mileageImpl.getPath().size() > 0 && locationAtStopTrigger == null) {
                        Plog.d(TAG, "StopBackgroundGpsUpdates: setting initializer");
                        Plog.appendLog(getApplicationContext(), "StopBackgroundGpsUpdates: setting initializer", mileageImpl);
                        locationAtStopTrigger = mileageImpl.getPath().get(mileageImpl.getPath().size() - 1);
                    } else {
                        Plog.d(TAG, "StopBackgroundGpsUpdates: not setting initializer");
                        Plog.appendLog(getApplicationContext(), "StopBackgroundGpsUpdates: not setting initializer", mileageImpl);
                    }
                    Plog.d(TAG, "StopBackgroundGpsUpdates - posting on delay:%d", durationMinutes);
                    Plog.appendLog(getApplicationContext(), String.format(Locale.US, "StopBackgroundGpsUpdates - posting on delay:%d", durationMinutes), mileageImpl);
                    mServiceHandler.removeCallbacks(backgroundRunnable);
                    mServiceHandler.postDelayed(backgroundRunnable, durationMinutes * 60 * 1000);
                }
            }
        });
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onMessageEvent(final GpsMessage.RestartBackgroundGpsUpdates event) {
        mServiceHandler.post(new Runnable() {
            @Override
            public void run() {
                Plog.d(TAG, "RestartBackgroundGpsUpdates");
                Plog.appendLog(getApplicationContext(), String.format("RestartBackgroundGpsUpdates-%s", event.getReason()), mileageImpl);
                if (initiatedFromBackground) {
                    mServiceHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onMessageEvent(new GpsMessage.StopBackgroundGpsUpdates("Service:RestartBackgroundGpsUpdates"));
                        }
                    }, 30 * 1000);
                }
            }
        });
    }

    private class BackgroundRunnable implements Runnable {

        private long lastRun;

        BackgroundRunnable(long lastRun) {
            this.lastRun = lastRun;
        }

        boolean shouldRunNow() {
            final boolean shouldRunNow = isFromBackground() && (System.currentTimeMillis() - lastRun) > durationMinutes * 60 * 1000;
            Plog.d(TAG, "BackgroundRunnable, shouldRunNow %s", shouldRunNow);
            Plog.appendLog(getApplicationContext(), String.format("BackgroundRunnable, shouldRunNow %s", shouldRunNow), mileageImpl);
            return shouldRunNow;
        }

        @Override
        public void run() {
            Plog.d(TAG, "BackgroundRunnable:run");
            Plog.appendLog(getApplicationContext(), "BackgroundRunnable:run", mileageImpl);
            if (!shouldRunNow()) {
                Plog.d(TAG, "BackgroundRunnable:should not run");
                Plog.appendLog(getApplicationContext(), "BackgroundRunnable:should not run", mileageImpl);
                return;
            }
            lastRun = System.currentTimeMillis();
            if (initiatedFromBackground) {
                if (locationAtStopTrigger != null && mileageImpl != null) {
                    if (mileageImpl.getPath().size() > 0) {
                        final Location latestTracked = mileageImpl.getPath().get(mileageImpl.getPath().size() - 1);
                        final float distance = latestTracked.distanceTo(locationAtStopTrigger);
                        Plog.d(TAG, "BackgroundRunnable: distance %s", distance);
                        Plog.appendLog(getApplicationContext(), String.format("BackgroundRunnable: distance %s", distance), mileageImpl);
//                        if (testDistance < minimumMovementInMeters) {
                        if (distance < minimumMovementInMeters) {
                            Plog.d(TAG, "BackgroundRunnable distance travelled is less than threshold - ending");
                            Plog.appendLog(getApplicationContext(), "BackgroundRunnable distance travelled is less than threshold - ending", mileageImpl);
                            GpsTrackerService.this.stopSelf();
                        } else {
//                            testDistance = testDistance - minimumMovementInMeters;
//                            Plog.d(TAG, "testDistance is %s", testDistance);
                            Plog.d(TAG, "BackgroundRunnable, distance travelled is greater, let this run");
                            Plog.appendLog(getApplicationContext(), "BackgroundRunnable, distance travelled is greater, let this run", mileageImpl);
                            locationAtStopTrigger = latestTracked;
                            Plog.d(TAG, "BackgroundRunnable - posting on delay:%d", durationMinutes);
                            Plog.appendLog(getApplicationContext(), String.format(Locale.US, "BackgroundRunnable - posting on delay:%d", durationMinutes), mileageImpl);
                            mServiceHandler.removeCallbacks(backgroundRunnable);
                            mServiceHandler.postDelayed(backgroundRunnable, durationMinutes * 60 * 1000);
                        }
                    } else {
                        long timeElapsed = System.currentTimeMillis() - mileageImpl.getTimeStamp();
                        Plog.d(TAG, "BackgroundRunnable, path == 0");
                        Plog.appendLog(getApplicationContext(), String.format("BackgroundRunnable, path == 0 time elapsed = %d", timeElapsed / 1000), mileageImpl);
                        if (timeElapsed >= durationMinutes) {
                            Plog.d(TAG, "BackgroundRunnable, durationMinutes has elapsed. killing service");
                            Plog.appendLog(getApplicationContext(), "BackgroundRunnable, durationMinutes has elapsed. killing service", mileageImpl);
                            GpsTrackerService.this.stopSelf();
                        } else {
                            Plog.d(TAG, "BackgroundRunnable, not enough time was elapsed to stop the service");
                            Plog.appendLog(getApplicationContext(), "BackgroundRunnable, not enough time was elapsed to stop the service", mileageImpl);
                            Plog.d(TAG, "BackgroundRunnable - posting on delay:%d", durationMinutes);
                            Plog.appendLog(getApplicationContext(), String.format(Locale.US, "stopUpdatesRunnable - posting on delay:%d", durationMinutes), mileageImpl);
                            mServiceHandler.removeCallbacks(backgroundRunnable);
                            mServiceHandler.postDelayed(backgroundRunnable, durationMinutes * 60 * 1000);
                        }
                    }
                } else {
                    Plog.d(TAG, "BackgroundRunnable, initializer null");
                    Plog.appendLog(getApplicationContext(), "BackgroundRunnable, initializer null", mileageImpl);
                    if (mileageImpl.getPath().size() > 0) {
                        Plog.d(TAG, "BackgroundRunnable, initializer null, setting it now");
                        Plog.appendLog(getApplicationContext(), "BackgroundRunnable, initializer null, setting it now", mileageImpl);
                        locationAtStopTrigger = mileageImpl.getPath().get(0);
                        Plog.d(TAG, "BackgroundRunnable - posting on delay:%d", durationMinutes);
                        Plog.appendLog(getApplicationContext(), String.format(Locale.US, "BackgroundRunnable - posting on delay:%d", durationMinutes), mileageImpl);
                        mServiceHandler.removeCallbacks(backgroundRunnable);
                        mServiceHandler.postDelayed(backgroundRunnable, durationMinutes * 60 * 1000);
                    } else {
                        Plog.d(TAG, "BackgroundRunnable, no locations available. ending service");
                        Plog.appendLog(getApplicationContext(), "BackgroundRunnable, no locations available. ending service", mileageImpl);
                        GpsTrackerService.this.stopSelf();
                    }
                }
            }
        }
    }

    public static boolean isFromBackground() {
        return initiatedFromBackground;
    }

    public static boolean isGpsTrackerActive() {
        return gpsTrackerActive;
    }

    public static void setGpsTrackerActive(boolean gpsTrackerActive) {
        GpsTrackerService.gpsTrackerActive = gpsTrackerActive;
    }

    private void updateNotification(String address) {
//        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        if (manager != null) {
//            Intent notificationIntent = getApplicationContext().getPackageManager()
//                    .getLaunchIntentForPackage(getApplicationContext().getPackageName())
//                    .setPackage(null)
//                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//            PendingIntent pendingIntent =
//                    PendingIntent.getActivity(this, 0, notificationIntent, 0);
//            manager.notify(UPDATE_GPS_TRACKING_NOTIFICATION, getGpsNotification(getApplicationContext(), pendingIntent, address));
//        }
    }

    public static void createNotificationChannel(Context context) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        String id = NOTIFICATION_CHANNEL;
        CharSequence name = context.getString(R.string.app_name);
        String description = name + " Notifications";
        NotificationChannel mChannel;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                mChannel = new NotificationChannel(id, name, importance);
                mChannel.setDescription(description);
                mChannel.setSound(null, null);
                mChannel.enableVibration(false);
                mChannel.enableLights(false);
                if (mNotificationManager != null)
                    mNotificationManager.createNotificationChannel(mChannel);
            } catch (NullPointerException e) {
                Plog.e(TAG, e, "NotificationChannel");
            }
        }

        if (DEBUG_AUTO_TRACKING) {
            String debugId = NOTIFICATION_CHANNEL_DEBUG;
            CharSequence debugName = context.getString(R.string.app_name);
            String debugDescription = name + " Notifications";
            NotificationChannel debugChannel;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                try {
                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                    debugChannel = new NotificationChannel(debugId, debugName, importance);
                    debugChannel.setDescription(debugDescription);
                    debugChannel.setSound(null, null);
                    debugChannel.enableVibration(false);
                    debugChannel.enableLights(false);
                    if (mNotificationManager != null)
                        mNotificationManager.createNotificationChannel(debugChannel);
                } catch (NullPointerException e) {
                    Plog.e(TAG, e, "NotificationChannel");
                }
            }
        }
    }

    public static Notification getGpsNotification(Context context, PendingIntent intent, String address) {
        try {
            NotificationCompat.Builder mBuilder;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                mBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL);
            } else {
                mBuilder = new NotificationCompat.Builder(context);
            }
            String appName = context.getString(R.string.app_name);
            mBuilder.setColorized(true)
                    .setSmallIcon(R.drawable.map_vector)
                    .setVibrate(new long[]{0L})
                    .setContentIntent(intent)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setGroup("LocationTracker")
                    .setGroupSummary(true)
                    .setColor(ResourcesCompat.getColor(context.getResources(), R.color.notificationColor, null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBuilder.setContentTitle("Recording location");
                if (!address.isEmpty())
                    mBuilder.setContentText("Current Location: " + address);
            } else {
                mBuilder.setContentText("Recording location")
                        .setContentTitle(appName);
                if (!address.isEmpty())
                    mBuilder.setContentInfo("Current Location: " + address);
            }
            return mBuilder.build();
        } catch (NullPointerException e) {
            Plog.e(TAG, e, "postMediaScanNotification");
        }
        return null;
    }

    public interface OfflineComplete {
        void onComplete();
    }
}
