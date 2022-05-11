package com.udnahc.locationapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;

import androidx.multidex.MultiDexApplication;

import com.udnahc.locationapp.database.DbHelper;
import com.udnahc.locationapp.location.MileageService;
import com.udnahc.locationapp.model.Expense;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class App extends MultiDexApplication {
    private DbHelper dbHelper;
    private Typeface regularFont, boldFont, italicFont;
    private static App INSTANCE;
    private Map<String, Double> monthlyMap = new HashMap<>();
    private Map<String, Double> categoryPieMap = new HashMap<>();
    private double yearlyDeductible = 0, monthlyEarnings = 0;
    private boolean ignoreTransitions = false;
    private int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    private double payoutAmount = 0;
    private long sessionStartTime = -1;

    private Expense modifyingExpense;

    public Expense getModifyingExpense() {
        return modifyingExpense;
    }

    public void setModifyingExpense(Expense modifyingExpense) {
        this.modifyingExpense = modifyingExpense;
    }

    public static App get() {
        return INSTANCE;
    }

    public long getSessionStartTime() {
        if(sessionStartTime == -1) {
            sessionStartTime = System.currentTimeMillis();
        }
        return sessionStartTime;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        FlexibleAdapter.enableLogs(DEBUG);
        try {
            SharedPreferences googleBug = getSharedPreferences("google_bug_154855417", Context.MODE_PRIVATE);
            if (!googleBug.contains("fixed")) {
                File corruptedZoomTables = new File(getFilesDir(), "ZoomTables.data");
                corruptedZoomTables.delete();
                googleBug.edit().putBoolean("fixed", true).apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dbHelper = new DbHelper(this);
        INSTANCE = this;
        MileageService.createNotificationChannel(getApplicationContext());
//        try {
//            if (BuildConfig.DEBUG || BuildConfig.FLAVOR.equalsIgnoreCase("dev")) {
//                ErrorReporter errorReporter = new ErrorReporter();
//                errorReporter.init(App.get());
//                Stetho.initializeWithDefaults(this);
//                java.util.logging.Logger.getLogger("com.amazonaws").setLevel(Level.ALL);
//                java.util.logging.Logger.getLogger("org.apache.http").setLevel(Level.ALL);
//            }
//        } catch (Exception e) {
//            Plog.e("MyLoxley", e, "initDefaults");
//        }
    }

    public DbHelper getDbHelper() {
        return dbHelper;
    }

    public Typeface getRegularFont() {
        if (regularFont == null) {
            regularFont = Typeface.createFromAsset(this
                    .getAssets(), "BrandonText-Regular.otf");
        }
        return regularFont;
    }

    public Typeface getBoldFont() {
        if (boldFont == null) {
            boldFont = Typeface.createFromAsset(this
                    .getAssets(), "BrandonText-Medium.otf");
        }
        return boldFont;
    }

    public Typeface getItalicFont() {
        if (italicFont == null) {
            italicFont = Typeface.createFromAsset(this
                    .getAssets(), "BrandonText-RegularItalic.otf");
        }
        return italicFont;
    }
    public boolean isIgnoreTransitions() {
        return ignoreTransitions;
    }

    public void setIgnoreTransitions(boolean ignoreTransitions) {
        this.ignoreTransitions = ignoreTransitions;
    }
}
