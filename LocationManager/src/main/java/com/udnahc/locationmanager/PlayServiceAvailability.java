package com.udnahc.locationmanager;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class PlayServiceAvailability {
    public static int isAvailable(Context context) {

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (ConnectionResult.SUCCESS == status) {
            return status;
        } else {
//            GooglePlayServicesUtil.getErrorDialog(status, context, 0).show();
            return status;
        }
    }
}
