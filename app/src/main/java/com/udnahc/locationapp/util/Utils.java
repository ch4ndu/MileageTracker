package com.udnahc.locationapp.util;


import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;
import com.udnahc.locationapp.App;
import com.udnahc.locationapp.R;
import com.udnahc.locationapp.location.GeofenceReceiver;
import com.udnahc.locationapp.location.MileageService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    public static void hideKeyBoard(AppCompatActivity activity) {
        if (activity == null)
            return;
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void setFont(final View v) {
        if (v == null || v.isInEditMode()) {
            return;
        }
        if (v instanceof ViewGroup) {
            final ViewGroup viewGroup = (ViewGroup) v;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                final View child = viewGroup.getChildAt(i);
                setFont(child);
            }
        } else if (v instanceof TextView) {
            final TextView textView = (TextView) v;
            Typeface typeface = App.get().getRegularFont();
            if (textView.getTypeface() != null) {
                if (textView.getTypeface().isBold()) {
                    typeface = App.get().getBoldFont();
                } else if (textView.getTypeface().isItalic()) {
                    typeface = App.get().getItalicFont();
                }
            }
            textView.setTypeface(typeface);
        }
    }

    public static void createNotificationChannel() {
        NotificationManager mNotificationManager =
                (NotificationManager) App.get().getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        String id = MileageService.NOTIFICATION_CHANNEL;
        CharSequence name = "MyLoxley";
        String description = "MyLoxley Notifications";
        NotificationChannel mChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

        String debugId = MileageService.NOTIFICATION_CHANNEL_DEBUG;
        CharSequence debugName = "MyLoxley";
        String debugDescription = "MyLoxley Notifications";
        NotificationChannel debugChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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


    public static void postUpdateTagsNotification(int colorAccent) {
        try {
            NotificationCompat.Builder mBuilder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBuilder = new NotificationCompat.Builder(App.get().getApplicationContext(), MileageService.NOTIFICATION_CHANNEL);
            } else {
                mBuilder = new NotificationCompat.Builder(App.get().getApplicationContext());
            }
            mBuilder.setColorized(true)
                    .setSmallIcon(com.udnahc.locationapp.R.drawable.ic_launcher_foreground)
                    .setVibrate(new long[]{0L})
                    .setOngoing(true)
                    .setColor(colorAccent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBuilder.setContentTitle("Recording location");
            } else {
                mBuilder.setContentText("Recording location")
                        .setContentTitle("MyLoxley");
            }
            NotificationManager mNotifyMgr =
                    (NotificationManager) App.get().getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.notify(MileageService.UPDATE_GPS_TRACKING_NOTIFICATION, mBuilder.build());
        } catch (NullPointerException e) {
            Plog.e(TAG, e, "postMediaScanNotification");
        }
    }

    public static Notification getGpsNotification(PendingIntent intent) {
        try {
            NotificationCompat.Builder mBuilder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBuilder = new NotificationCompat.Builder(App.get().getApplicationContext(), MileageService.NOTIFICATION_CHANNEL);
            } else {
                mBuilder = new NotificationCompat.Builder(App.get().getApplicationContext());
            }
            mBuilder.setColorized(true)
                    .setSmallIcon(com.udnahc.locationapp.R.drawable.map_vector)
                    .setVibrate(new long[]{0L})
                    .setContentIntent(intent)
                    .setOngoing(true)
                    .setGroup("com.udnahc.locationapp")
                    .setGroupSummary(true)
                    .setColor(ResourcesCompat.getColor(App.get().getResources(), R.color.colorAccent, null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBuilder.setContentTitle("Recording location");
            } else {
                mBuilder.setContentText("Recording location")
                        .setContentTitle("MyLoxley");
            }
            return mBuilder.build();
        } catch (NullPointerException e) {
            Plog.e(TAG, e, "postMediaScanNotification");
        }
        return null;
    }

    public static double getMiles(List<Location> locations) {
        double distance = 0;
        int size = locations.size();
        for (int i = 0; i < size - 1; i++) {
            distance = distance + getMiles(locations.get(i).distanceTo(locations.get(i + 1)));
        }
        distance = Math.round(distance * 10) / 10d;
        return distance;
    }

    public static double getMiles(float meters) {
        double miles = meters * 0.000621371192;
//        return miles > 0.08d ? miles : 0;
        return miles;
    }

    public static int getColorPrimaryDark(Context context) {
        return context.getResources().getColor(R.color.colorPrimaryDark);
    }

    public static int getRedColor(Context context) {
        return context.getResources().getColor(R.color.stopColor);
    }

    public static int getGreenColor(Context context) {
        return context.getResources().getColor(R.color.greenColor);
    }

    public static int getColorPrimary(Context context) {
        return context.getResources().getColor(R.color.colorPrimary);
    }

    public static int getColorAccent(@NonNull Context context) {
        return context.getResources().getColor(R.color.colorAccent);
    }

    public static int getAlphaColorAccent(Context context) {
        int color = context.getResources().getColor(R.color.colorAccent);
        return ColorUtils.setAlphaComponent(color, 220);
    }

    public static double getRoundedMiles(double size) {
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);
        return Double.parseDouble(df.format(size));
    }

    public static double getRoundedCost(double size) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        return Double.parseDouble(df.format(size));
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static void attachSlidr(final Activity activity) {
        SlidrConfig config = new SlidrConfig.Builder()
                .primaryColor(Utils.getColorPrimary(activity))
                .secondaryColor(Utils.getColorPrimaryDark(activity))
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .scrimColor(Color.BLACK)
                .scrimStartAlpha(0.8f)
                .scrimEndAlpha(0f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .edge(false)
                .edgeSize(0.40f) // The % of the screen that counts as the edge, default 18%
                .build();
        Slidr.attach(activity, config);
    }

    public static float convertPixelsToDp(float px) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return Math.round(dp);
    }

    public static float convertDpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }
}
