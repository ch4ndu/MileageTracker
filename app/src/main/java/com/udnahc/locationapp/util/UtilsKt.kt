package com.udnahc.locationapp.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.afollestad.materialdialogs.MaterialDialog
import com.udnahc.locationapp.location.MileageService
import com.udnahc.locationapp.MainActivity
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.io.File
import java.util.*


class UtilsKt {
    companion object {
        const val TAG = "UtilsKt"

        fun shouldRunNow(): Boolean {
            val dateTime = DateTime(Calendar.getInstance(), DateTimeZone.getDefault())
            val disabledOnWeekends = Preferences.isWeekendDisabled()
            if (disabledOnWeekends) {
                val dayOfWeek = dateTime.dayOfWeek().asText
                if (dayOfWeek.equals("saturday", true) || dayOfWeek.equals("sunday", true)) {
//                    Plog.d("murali", "schedule disabed for weekend")
                    return true
                }
            }

            val startHourOne = Preferences.getScheduleStartHour()
            val startMinuteOne = Preferences.getScheduleStartMinute()
            val calendarStartOne = dateTime.minuteOfHour().setCopy(startMinuteOne)
                .hourOfDay().setCopy(startHourOne)

            val stopHourOne = Preferences.getScheduleStopHour()
            val stopMinuteOne = Preferences.getScheduleStopMinute()
            val calendarStopOne = dateTime.hourOfDay().setCopy(stopHourOne)
                .minuteOfHour().setCopy(stopMinuteOne)

            val startHourTwo = Preferences.getScheduleTwoStartHour()
            val startMinuteTwo = Preferences.getScheduleTwoStartMinute()
            val calendarStartTwo = dateTime.hourOfDay().setCopy(startHourTwo)
                .minuteOfHour().setCopy(startMinuteTwo)

            val stopHourTwo = Preferences.getScheduleTwoStopHour()
            val stopMinuteTwo = Preferences.getScheduleTwoStopMinute()
            val calendarStopTwo = dateTime.hourOfDay().setCopy(stopHourTwo)
                .minuteOfHour().setCopy(stopMinuteTwo)

            if (dateTime.isAfter(calendarStartOne) && dateTime.isBefore(calendarStopOne)) {
//                Plog.d("murali", "matched schedule one")
                return true
            }
            if (dateTime.isAfter(calendarStartTwo) && dateTime.isBefore(calendarStopTwo)) {
//                Plog.d("murali", "matched schedule two")
                return true
            }
//            Plog.d("murali", "not scheduled to start")
            return false
        }

        fun showPaymentReceivedDialog(activity: Activity) {
            MaterialDialog(activity)
                .title(text = "Please visit the website and update your payment information")
                .positiveButton(text = "Visit Website") {
                    val url = "http://www.myloxley.com"
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    activity.startActivity(i)
                }.show {
                    noAutoDismiss()
                }
        }

        fun askBeforeExit(activity: Activity) {
            MaterialDialog(activity)
                .title(text = "Do you want to exit the application?")
                .positiveButton(text = "Yes") {
                    if (activity is MainActivity) {
                        activity.onBackPressed()
                    }
                }
                .negativeButton(text = "No")
                .show()
        }

        fun showInternetNotAvailable(activity: Activity) {
            val dialog: MaterialDialog = MaterialDialog(activity)
                .title(text = "Internet connection unavailable!")
                .positiveButton(text = "Ok") {
                    activity.finish()
                }
                .cancelable(false)
            if (MileageService.isGpsTrackerActive()) {
                dialog.message(text = "Gps tracking will continue to run in the background. Please retry after you are connected to internet")
            }
            dialog.show()
        }

        private fun showDialogMessage(activity: Activity, title: String, body: String) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(title).setMessage(body)
                .setNeutralButton("OK") { dialog, which -> dialog.dismiss() }
                .setCancelable(false)
            val userDialog = builder.create()
            userDialog.show()
        }

        public fun deleteLogFiles(context: Context) {
            val path = File("$context.filesDir/logs/")
            val files = path.listFiles()
            if (files != null) {
                for (file1 in files) {
                    file1.delete()
                }
            }
        }
    }
}
