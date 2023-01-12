package com.udnahc.locationapp.util;


import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Constants {
    //    public static final String REFERRAL_URL = "http://awsezmoneyapp-hosting-mobilehub-1722161070.s3-website.us-east-1.amazonaws.com/#/signup/~~";
    public static final String REFERRAL_URL = "http://myloxley.com/#/welcome";
    public static final String STRIPE_KEY = "pk_test_t5paJxM0PcgsUmsJHPett7mB";
    public static final String TEST_REFERRAL = "J5mnpyl1i";


//    public static final String NOTIFICATION_CHANNEL = "ex_channel";
//    public static final String NOTIFICATION_CHANNEL_DEBUG = "ex_channel_debug";
    public static final String GPS_ACTIVITY_ACTION = "go_to_gps_activity";
//    public static final int UPDATE_GPS_TRACKING_NOTIFICATION = 1234291;
//    public static final int UPDATE_AUTO_TRACK_NOTIFICATION = 2143921;
@SuppressLint("ConstantLocale")
public static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
    public static final SimpleDateFormat offlineDateFormat = new SimpleDateFormat("MMM dd hh:mm aa", Locale.getDefault());
    public static final SimpleDateFormat offlineEndDateFormat = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
    public static final int SMS_PERMISSION_CODE = 2312;

    public static final String BROADCAST_DETECTED_ACTIVITY = "activity_intent";
    static final long DETECTION_INTERVAL_IN_MILLISECONDS = 30 * 1000;
    public static final int CONFIDENCE = 70;
//    public static final boolean USE_AUTO_TRACKING = true;
//    public static final boolean DEBUG_AUTO_TRACKING = BuildConfig.DEBUG;
//    public static final boolean DEBUG_AUTO_TRACKING = false;

    public static String BusinessGeneral = "general";
    public static String BusinessTruckOwner = "t-owner";
    public static String BusinessTruckEmployee = "t-employee";
    public static String BusinessRental = "rental";

    public static String BackStackKey = "backstackKey";
    public static String FragmentId = "FragmentId";

    public static String GetStartedVideo = "getstarted";
    public static String SetupProfile = "setupProfile";
    public static String RecordMiles = "recordMiles";
    public static String RecordExpense = "recordExpense";
    public static String ShareWithFriends = "shareWithFriends";
    public static String VisitLearningCenter = "learningCenter";
    public static String OthersMenu = "othersMenu";
    public static String ShareCount = "shareCount";
}
