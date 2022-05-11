package com.udnahc.locationapp.controller

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.afollestad.materialdialogs.MaterialDialog
import com.udnahc.locationapp.location.LocationUtils
import com.udnahc.locationapp.location.TransitionRecognition
import com.udnahc.locationapp.BuildConfig
import com.udnahc.locationapp.R
import com.udnahc.locationapp.location.TransitionRecognitionReceiver
import com.udnahc.locationapp.util.Plog
import com.udnahc.locationapp.util.Preferences
import com.udnahc.locationapp.util.Utils
import com.udnahc.locationapp.util.UtilsKt
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*


class AutoTrackActivity : UtilActivity() {

    private lateinit var startScheduleTextOne: TextView
    private lateinit var stopScheduleTextOne: TextView
    private lateinit var startScheduleButtonOne: Button
    private lateinit var stopScheduleButtonOne: Button
    private lateinit var startScheduleTextTwo: TextView
    private lateinit var stopScheduleTextTwo: TextView
    private lateinit var startScheduleButtonTwo: Button
    private lateinit var stopScheduleButtonTwo: Button
    private lateinit var scheduleCard: CardView
    private var autoTrackSwitch: SwitchCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_track)
        Utils.attachSlidr(this)
        Utils.setFont(findViewById(R.id.main_content))
        setToolbarWithBack()
        UtilsKt.shouldRunNow()


        autoTrackSwitch = findViewById(R.id.auto_track_switch)
        val disableOnWeekend = findViewById<SwitchCompat>(R.id.disable_schedule_weekend)
        autoTrackSwitch?.isChecked = false
        val isAutoTrackEnabled = Preferences.isAutoTrackEnabled()
        initScheduleCard()
        mainHandler.postDelayed({
            if (isAutoTrackEnabled) {
                autoTrackSwitch?.isChecked = true
                val disableWeekend = Preferences.isWeekendDisabled()
                disableOnWeekend.isChecked = disableWeekend
                enableViews()
            } else {
                disableViews()
            }
            autoTrackSwitch?.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                if (isChecked) {
                    enableAutoTrack()
                } else {
                    Preferences.saveAutoTrackPreference(false)
                    TransitionRecognitionReceiver.clearTransition(this)
                    LocationUtils.cancelAllGeoFences(this)
                    TransitionRecognition().stopTracking(this)
                    disableViews()
                }
            }
            disableOnWeekend.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                Preferences.saveDisableWeekend(isChecked)
            }
        }, 300)
        val logsSwitch = findViewById<SwitchCompat>(R.id.enable_logs_switch)
        logsSwitch?.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            Preferences.saveDebugLogPreference(isChecked)
        }
        logsSwitch.isChecked = Preferences.shouldSaveLogs()
    }

    override fun permissionRequestComplete() {
        enableAutoTrack()
    }

    private fun enableAutoTrack() {
        val hasLocationPermission =
            PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val isGpsTurnedOn = isHardwareGpsEnabled(true)
        if (!isGpsTurnedOn) {
            Toast.makeText(this, "Please make sure your GPS is turned on!", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (hasLocationPermission) {
            if (askPowerOptimizationIfNeeded()) {
                autoTrackSwitch?.isChecked = false
                return
            }
            if (askAlwaysLocationIfNeeded()) {
                autoTrackSwitch?.isChecked = false
                return
            }
            if (askActivityPermissionIfNeeded()) {
                autoTrackSwitch?.isChecked = false
                return
            }
            Preferences.saveAutoTrackPreference(true)
            TransitionRecognition().startTracking(this)
            showTransitionEnableInfoDialog()
            LocationUtils.setGeoFenceAtCurrentLocation(this, null)
            enableViews()
            autoTrackSwitch?.isChecked = true
            LocationUtils.startPermissionChecker()
        } else {
            requestLocationPermission()
            autoTrackSwitch?.isChecked = false
            return
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected fun askPowerOptimizationIfNeeded(): Boolean {
        if (isBatteryOptimizationMissing()) {
            MaterialDialog(this)
                .title(text = "Need permission")
                .message(text = "Before you enable auto-track, My Loxley need to be exempted from battery optimization to effectively function in the background. Please grant the permission.")
                .positiveButton(text = "Grant") {
                    @SuppressLint("BatteryLife")
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                    startActivity(intent)
                }
                .negativeButton(text = "Cancel")
                .show()
            return true
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1234) {
            if (resultCode == Activity.RESULT_OK) {
                enableAutoTrack()
//                val builder = MaterialDialog(this)
//                builder.title(text = "Location Permission received. Please try the operation again to continue")
//                        .cancelOnTouchOutside(cancelable = false)
//                        .positiveButton(text = "Ok")
//                Utils.setFont(builder.view)
//                builder.show()
            } else {
                val builder = MaterialDialog(this)
                builder.title(text = "We cannot enable this without Location Permission. Please try again.")
                    .cancelOnTouchOutside(cancelable = false)
                    .positiveButton(text = "Ok")
                Utils.setFont(builder.view)
                builder.show()
            }
        }
    }

    private fun initScheduleCard() {
        scheduleCard = findViewById(R.id.schedule_one_card)
        startScheduleTextOne = findViewById(R.id.start_time_one)
        startScheduleButtonOne = findViewById(R.id.start_one_button)
        stopScheduleTextOne = findViewById(R.id.end_time_one)
        stopScheduleButtonOne = findViewById(R.id.end_button_one)


        startScheduleTextTwo = findViewById(R.id.start_time_two)
        startScheduleButtonTwo = findViewById(R.id.start_two_button)
        stopScheduleTextTwo = findViewById(R.id.end_time_two)
        stopScheduleButtonTwo = findViewById(R.id.end_button_two)

        val helpImage = findViewById<ImageView>(R.id.schedule_one_help)
        helpImage.setOnClickListener {
            MaterialDialog(this)
                .message(text = "Auto Tracking will only initiate between the times specified here. Enable this to save battery.")
                .positiveButton(text = "Ok")
                .show()
        }
        val scheduleSwitch = findViewById<SwitchCompat>(R.id.auto_track_schedule_one_switch)

        scheduleSwitch.isChecked = Preferences.isAutoTrackScheduleEnabled()
        toggleScheduleButtons(scheduleSwitch.isChecked)
        scheduleSwitch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            Preferences.saveAutoTrackSchedule(isChecked)
            toggleScheduleButtons(isChecked)
        }
        val dtf = DateTimeFormat.forPattern("hh:mm a")

        val date = Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, Preferences.getScheduleStartHour())
        calendar.set(Calendar.MINUTE, Preferences.getScheduleStartMinute())
        calendar.set(Calendar.SECOND, 0)
        startScheduleTextOne.text = dtf.print(DateTime(calendar.time))

        startScheduleButtonOne.setOnClickListener {
            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(
                this,
                R.style.DialogTheme,
                TimePickerDialog.OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                    Plog.d(TAG, "onStartTimeSet hour: %s minute %s", selectedHour, selectedMinute)
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                    calendar.set(Calendar.MINUTE, selectedMinute)
                    val dateTime = DateTime(calendar.time)
                    startScheduleTextOne.text = dtf.print(dateTime)
                    Preferences.saveStartScheduleHour(selectedHour)
                    Preferences.saveStartScheduleMinute(selectedMinute)
                },
                calendar[Calendar.HOUR_OF_DAY],
                calendar[Calendar.MINUTE],
                false
            )
            mTimePicker.setTitle("Select Start Time")
            mTimePicker.show()
        }

        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, Preferences.getScheduleStopHour())
        calendar.set(Calendar.MINUTE, Preferences.getScheduleStopMinute())
        calendar.set(Calendar.SECOND, 0)
        stopScheduleTextOne.text = dtf.print(DateTime(calendar.time))
        stopScheduleButtonOne.setOnClickListener {
            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(
                this,
                R.style.DialogTheme,
                TimePickerDialog.OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                    Plog.d(TAG, "onEndTimeSet hour: %s minute %s", selectedHour, selectedMinute)
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                    calendar.set(Calendar.MINUTE, selectedMinute)
                    stopScheduleTextOne.text = dtf.print(DateTime(calendar.time))
                    Preferences.saveStopScheduleHour(selectedHour)
                    Preferences.saveStopScheduleMinute(selectedMinute)
                },
                calendar[Calendar.HOUR_OF_DAY],
                calendar[Calendar.MINUTE],
                false
            )
            mTimePicker.setTitle("Select Stop Time")
            mTimePicker.show()
        }

        //schedule2

        calendar.set(Calendar.HOUR_OF_DAY, Preferences.getScheduleTwoStartHour())
        calendar.set(Calendar.MINUTE, Preferences.getScheduleTwoStartMinute())
        calendar.set(Calendar.SECOND, 0)
        startScheduleTextTwo.text = dtf.print(DateTime(calendar.time))

        startScheduleButtonTwo.setOnClickListener {
            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(
                this,
                R.style.DialogTheme,
                TimePickerDialog.OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                    Plog.d(TAG, "onStartTimeSet hour: %s minute %s", selectedHour, selectedMinute)
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                    calendar.set(Calendar.MINUTE, selectedMinute)
                    val dateTime = DateTime(calendar.time)
                    startScheduleTextTwo.text = dtf.print(dateTime)
                    Preferences.saveStartScheduleTwoHour(selectedHour)
                    Preferences.saveStartScheduleTwoMinute(selectedMinute)
                },
                calendar[Calendar.HOUR_OF_DAY],
                calendar[Calendar.MINUTE],
                false
            )
            mTimePicker.setTitle("Select Start Time")
            mTimePicker.show()
        }

        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, Preferences.getScheduleTwoStopHour())
        calendar.set(Calendar.MINUTE, Preferences.getScheduleTwoStopMinute())
        calendar.set(Calendar.SECOND, 0)
        stopScheduleTextTwo.text = dtf.print(DateTime(calendar.time))
        stopScheduleButtonTwo.setOnClickListener {
            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(
                this,
                R.style.DialogTheme,
                TimePickerDialog.OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                    Plog.d(TAG, "onEndTimeSet hour: %s minute %s", selectedHour, selectedMinute)
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                    calendar.set(Calendar.MINUTE, selectedMinute)
                    stopScheduleTextTwo.text = dtf.print(DateTime(calendar.time))
                    Preferences.saveStopScheduleTwoHour(selectedHour)
                    Preferences.saveStopScheduleTwoMinute(selectedMinute)
                },
                calendar[Calendar.HOUR_OF_DAY],
                calendar[Calendar.MINUTE],
                false
            )
            mTimePicker.setTitle("Select Stop Time")
            mTimePicker.show()
        }
    }

    private fun showScheduleCard() {
        val transition = Slide(Gravity.BOTTOM)
        transition.addTarget(scheduleCard)
        transition.duration = 500
        TransitionManager.beginDelayedTransition(findViewById(R.id.main_content), transition)
        scheduleCard.visibility = View.VISIBLE
    }

    private fun hideScheduleCard() {
        val transition = Slide(Gravity.BOTTOM)
        transition.addTarget(scheduleCard)
        transition.duration = 500
        TransitionManager.beginDelayedTransition(findViewById(R.id.main_content), transition)
        scheduleCard.visibility = View.GONE
    }

    private fun toggleScheduleButtons(enabled: Boolean) {
        startScheduleButtonOne.isEnabled = enabled
        stopScheduleButtonOne.isEnabled = enabled
    }

    private fun enableViews() {
        showScheduleCard()
    }

    private fun disableViews() {
        hideScheduleCard()
    }
}
