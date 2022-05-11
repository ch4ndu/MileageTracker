# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep interface com.udnahc.locationmanager.Mileage { *; }
-keep class com.udnahc.locationmanager.Mileage { *; }
-keep class com.udnahc.locationmanager.GpsMessage { *; }
-keepattributes InnerClasses
 -keep class com.udnahc.locationmanager.GpsMessage**
 -keepclassmembers class com.udnahc.locationmanager.GpsMessage** {
    *;
 }
 -keepclassmembers class com.udnahc.locationmanager.GpsTrackerService {
    public static *** isGpsTrackerActive();
 }
 -keepclassmembers class com.udnahc.locationmanager.GpsTrackerService {
    public static *** createNotificationChannel(...);
 }
#-keep class com.udnahc.locationmanager.GpsMessage.MileageUpdate { *; }
#-keep class com.udnahc.locationmanager.GpsMessage.StopGpsUpdates { *; }
#-keep class com.udnahc.locationmanager.GpsMessage.StopBackgroundGpsUpdates { *; }
#-keep class com.udnahc.locationmanager.GpsMessage.ShowGooglePlayServicesUtilError { *; }

# Preserve static fields of inner classes of R classes that might be accessed
# through introspection.
-keepclassmembers class **.R$* {
   public static <fields>;
}

# Preserve the special static methods that are required in all enumeration classes.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# EventBus Proguard start
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
# EventBus Proguard end
