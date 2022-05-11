package com.udnahc.locationmanager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Plog {

    private static final boolean ENABLE_VERBOSE_LOG = BuildConfig.DEBUG;
    private static final boolean ENABLE_DEBUG_LOG = BuildConfig.DEBUG;
    private static final boolean ENABLE_INFO_LOG = BuildConfig.DEBUG;
    private static final boolean extraClientLogging = false;
    public static boolean saveDebugLogs = false;

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
//        else {
//            v(tag, msg, args);
//        }
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
        if (BuildConfig.DEBUG) {
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

    static void appendLog(final Context context, final String text, final Mileage mileage) {
        if (!saveDebugLogs || mileage == null)
            return;
        new AsyncTask<String, Void, Void>() {

            @Override
            protected Void doInBackground(String... strings) {
                try {
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd HH:mm aa", Locale.getDefault());
                    String eDate = dateFormatter.format(new Date(mileage.getTimeStamp()));
                    d("Plog", "eDate is %s", eDate);
                    File path = new File(context.getFilesDir() + "/logs/");
                    if (!path.exists()) {
                        path.mkdirs();
                    }
                    File logFile = new File(path, eDate);
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
                return null;
            }
        }.execute("go");
    }
}
