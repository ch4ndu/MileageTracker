package com.udnahc.locationapp.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.udnahc.locationapp.location.LocationUtils;
import com.udnahc.locationapp.location.MileageService;
import com.udnahc.locationapp.location.TransitionRecognition;
import com.udnahc.locationapp.util.Plog;
import com.udnahc.locationapp.util.Preferences;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Plog.d("BootUpReceiver", "onReceive");
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (MileageService.USE_AUTO_TRACKING && Preferences.isAutoTrackEnabled()) {
                Plog.d("BootUpReceiver", "enabling transition recognizing");
                new TransitionRecognition().startTracking(context);
                LocationUtils.Companion.setGeoFenceAtCurrentLocation(context, Preferences.getLastFence(), null);
//                TransitionRecognitionReceiver recognitionReceiver = new TransitionRecognitionReceiver();
//                context.registerReceiver(recognitionReceiver, new IntentFilter("com.ez.money.TRANSITION_RECOGNITION"));

                startPermissionChecker(context);


            } else {
                Plog.d("BootUpReceiver", "ignoring bootup");
            }
        }
    }

    private void startPermissionChecker(Context context) {
        LocationUtils.Companion.startPermissionChecker();
        LocationUtils.Companion.startPeriodicGeoFence();
    }
}
