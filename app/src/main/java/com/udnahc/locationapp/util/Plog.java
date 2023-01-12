package com.udnahc.locationapp.util;

import android.content.Context;
import android.util.Log;

import com.udnahc.locationapp.App;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Plog {

//    private static final boolean ENABLE_VERBOSE_LOG = BuildConfig.DEBUG;
//    private static final boolean ENABLE_DEBUG_LOG = BuildConfig.DEBUG;
//    private static final boolean ENABLE_INFO_LOG = BuildConfig.DEBUG;
//    private static final boolean extraClientLogging = false;

    private static final boolean ENABLE_VERBOSE_LOG = true;
    private static final boolean ENABLE_DEBUG_LOG = true;
    private static final boolean ENABLE_INFO_LOG = true;
    private static final boolean ENABLE_SENSITIVE = true;
    private static final boolean APPEND_LOGS = true;
    private static final boolean extraClientLogging = true;

    public static void v(String tag, String msg, Object... args) {
        if (ENABLE_VERBOSE_LOG) {
            if (msg == null)
                return;
            if (args == null || args.length < 1) {
                Log.v(tag, msg);
                return;
            }
            try {
                Log.v(tag, String.format(msg, args));
            } catch (Exception e) {
                Log.v(tag, msg);
            }
        }
    }

    public static void d(String tag, String msg, Object... args) {
        if (ENABLE_DEBUG_LOG) {
            if (msg == null)
                return;
            if (args == null || args.length < 1) {
                Log.d(tag, msg);
                return;
            }
            try {
                Log.d(tag, String.format(msg, args));
            } catch (Exception e) {
                Log.d(tag, msg);
            }
        }
    }

    public static void i(String tag, String msg, Object... args) {
        if (ENABLE_INFO_LOG) {
            if (msg == null)
                return;
            if (args == null || args.length < 1) {
                Log.i(tag, msg);
                return;
            }
            try {
                Log.i(tag, String.format(msg, args));
            } catch (Exception e) {
                Log.i(tag, msg);
            }
        }
    }

    public static void c(String tag, String msg, Object... args) {
        if (extraClientLogging) {
            if (msg == null)
                return;
            if (args == null || args.length < 1) {
                Log.i(tag, msg);
                return;
            }
            try {
                Log.i(tag, String.format(msg, args));
            } catch (Exception e) {
                Log.i(tag, msg);
            }
        }
    }

//    public static void extra(String tag, String msg, Object... args) {
//        if (extraClientLogging) {
//            if (msg == null)
//                return;
//            if (args == null || args.length < 1) {
//                Log.i(tag, msg);
//                return;
//            }
//            try {
//                Log.i(tag, String.format(msg, args));
//            } catch (Exception e) {
//                Log.i(tag, msg);
//            }
//        }
//    }

    public static void w(String tag, String msg, Object... args) {
        if (ENABLE_DEBUG_LOG) {
            if (msg == null)
                return;
            if (args == null || args.length < 1) {
                Log.w(tag, msg);
                return;
            }
            try {
                Log.w(tag, String.format(msg, args));
            } catch (Exception e) {
                Log.w(tag, msg);
            }
        }
    }

    public static void e(String tag, String msg, Object... args) {
        if (ENABLE_DEBUG_LOG) {
            if (msg == null)
                return;
            if (args == null || args.length < 1) {
                Log.e(tag, msg);
                return;
            }
            try {
                Log.e(tag, String.format(msg, args));
            } catch (Exception e) {
                Log.e(tag, msg);
            }
        }
    }

    public static void e(String tag, Throwable throwable, String msg, Object... args) {
        try {
        } catch (Exception e) {
            Log.d(tag, "wtf!!");
        }
        if (ENABLE_DEBUG_LOG) {
            if (args == null || args.length < 1) {
                Log.e(tag, msg, throwable);
                return;
            }
            try {
                Log.e(tag, String.format(msg, args), throwable);
            } catch (Exception e) {
                Log.e(tag, msg, throwable);
            }
        }
    }

    public static void sensitive(String tag, String msg, Object... args) {
        if (ENABLE_SENSITIVE) {
            if (args == null || args.length < 1) {
                Log.i(tag, msg);
                return;
            }
            try {
                Log.i(tag, String.format(msg, args));
            } catch (Exception e) {
                Log.i(tag, msg);
            }
        }
    }

    public static void appendTransition(final Context context, final String text) {
        if (!APPEND_LOGS || !Preferences.shouldSaveLogs())
            return;
        try {
            String fileName = "Transitions";
            d("Plog", "fileName is %s", fileName);
            File path = new File(context.getFilesDir() + "/logs/");
            if (!path.exists()) {
                path.mkdirs();
            }
            File logFile = new File(path, fileName);
            d("Plog", "filePath is %s", logFile);
            SimpleDateFormat formatter = new SimpleDateFormat("HH_mm_ss", Locale.US);
            Date date = new Date();
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try {
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(formatter.format(date)).append(": ").append(text);
                buf.newLine();
                buf.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (Exception e) {
            Plog.e("ErrorReporter", e, "appendTransition");
        }

    }

    public static void appendLog(final Context context, final String text) {
        if (!APPEND_LOGS || !Preferences.shouldSaveLogs())
            return;
        try {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd", Locale.getDefault());
            String fileName = dateFormatter.format(new Date(App.get().getSessionStartTime()));
            d("Plog", "fileName is %s", fileName);
            File path = new File(context.getFilesDir() + "/logs/");
            if (!path.exists()) {
                path.mkdirs();
            }
            File logFile = new File(path, fileName);
            d("Plog", "filePath is %s", logFile);
            SimpleDateFormat formatter = new SimpleDateFormat("HH_mm_ss", Locale.US);
            Date date = new Date();
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try {
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(formatter.format(date)).append(": ").append(text);
                buf.newLine();
                buf.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (Exception e) {
            Plog.e("ErrorReporter", e, "appendLog");
        }
    }
}
