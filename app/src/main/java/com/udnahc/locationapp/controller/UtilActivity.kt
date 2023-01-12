package com.udnahc.locationapp.controller

import android.Manifest
import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.location.LocationManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.udnahc.locationapp.location.LocationUtils
import com.udnahc.locationapp.BuildConfig
import com.udnahc.locationapp.R
import com.udnahc.locationapp.util.EventMessage
import com.udnahc.locationapp.util.Plog
import com.udnahc.locationapp.util.Preferences
import com.udnahc.locationapp.util.Utils
import com.udnahc.locationmanager.Mileage
import org.greenrobot.eventbus.EventBus

@SuppressLint("Registered")
abstract class UtilActivity : AppCompatActivity() {
    protected val TAG = javaClass.simpleName
    open var activeDialog: MaterialDialog? = null
    private var handler: Handler? = null
    private var anim: AnimationDrawable? = null
    open var activeFragment: BaseFragment? = null
    var modifyingExpense: Mileage? = null

    val mainHandler: Handler
        get() {
            if (handler == null) {
                handler = Handler(Looper.getMainLooper())
            }
            return handler!!
        }

    protected fun setToolbarWithBack() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            if (supportActionBar != null) {
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.bottom_top, R.anim.slide_out_right)
        setWindowProps()
        Plog.d(TAG, "onCreate")
    }

    private fun setWindowProps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }
    }

    public override fun onResume() {
        super.onResume()
        if (anim != null && !anim!!.isRunning)
            anim!!.start()
        Plog.d(TAG, "onResume")
    }

    public override fun onPause() {
        super.onPause()
        if (anim != null && anim!!.isRunning)
            anim!!.stop()
        Plog.d(TAG, "onPause")
    }

    protected fun unRegisterEventBus() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
    }

    protected fun registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    fun showErrorDialog(title: String, content: String) {
        mainHandler.postDelayed({

            if (activeDialog != null && activeDialog!!.isShowing) {
                activeDialog!!.dismiss()
            }
            activeDialog = MaterialDialog(this@UtilActivity)
                .title(text = title)
                .message(text = content)
                .positiveButton(text = "Ok")
            activeDialog!!.show()
        }, 300)
    }

    fun showLoadingDialog() {
        Plog.d(TAG, "showLoadingDialog")
        mainHandler.post {
            if (activeDialog != null && activeDialog!!.isShowing)
                activeDialog!!.dismiss()
            activeDialog = MaterialDialog(this@UtilActivity)
                .title(text = "Loading...")
                .customView(viewRes = R.layout.ripple_loading_layout, scrollable = true)
                .cancelOnTouchOutside(cancelable = false)
                .cancelable(cancelable = false)
            activeDialog!!.show {
                noAutoDismiss()
            }
        }
    }

    fun dismissActiveDialog() {
        Plog.d(TAG, "dismissActiveDialog")
        mainHandler.post {
            try {
                if (activeDialog != null && activeDialog!!.isShowing) {
                    activeDialog!!.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun finish() {
        super.finish()
    }

    open fun permissionRequestComplete() {
        EventBus.getDefault().post(EventMessage.PermissionRequestComplete())
    }

    fun isLocationPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_GRANTED == checkSelfPermission(permission.ACCESS_FINE_LOCATION)) {
                Plog.d(TAG, "permission granted for location")
                return true
            }
        }
        return false
    }

    fun requestLocationPermission() {
        Plog.d(TAG, "requestLocationPermission")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_GRANTED == checkSelfPermission(permission.ACCESS_FINE_LOCATION)) {
                Plog.d(TAG, "permission granted for location")
                permissionRequestComplete()
            } else if (!shouldShowRequestPermissionRationale(permission.ACCESS_FINE_LOCATION)) {
                Plog.d(TAG, "asking permission")
                val dialog = MaterialDialog(this)
                    .title(text = "Loxley needs location permission before tracking miles. Please request the system to grant it.")
                    .cancelOnTouchOutside(cancelable = false)
                    .cancelable(false)
                    .positiveButton(text = "Request") {
                        ActivityCompat.requestPermissions(
                            this@UtilActivity,
                            arrayOf(
                                permission.ACCESS_FINE_LOCATION,
                                permission.ACCESS_BACKGROUND_LOCATION
                            ),
                            1234
                        )
                    }
                    .negativeButton(text = "Cancel")
                Utils.setFont(dialog.view)
                dialog.show()
            } else {
                Plog.d(TAG, "permission denied somehow!!")
                val dialog = MaterialDialog(this)
                    .title(text = "We need location permission to track miles. Please grant it in the next screen by going to the Permissions section")
                    .cancelOnTouchOutside(cancelable = false)
                    .cancelable(false)
                    .positiveButton(text = "Ok") {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                Utils.setFont(dialog.view)
                dialog.show()
            }
        } else {
            val dialog = MaterialDialog(this)
                .title(text = "Loxley needs location permission before tracking miles. Please request the system to grant it.")
                .cancelOnTouchOutside(cancelable = false)
                .cancelable(false)
                .positiveButton(text = "Request") {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(permission.ACCESS_FINE_LOCATION),
                        1234
                    )
                }
            Utils.setFont(dialog.view)
            dialog.show()
        }
    }

    public fun isHardwareGpsEnabled(showDialog: Boolean): Boolean {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (showDialog)
                buildAlertMessageNoGps()
            return false
        }
        return true
    }

    protected fun buildAlertMessageNoGps() {
        val builder = MaterialDialog(this)
        builder.title(text = "Your GPS seems to be disabled, do you want to enable it?")
            .cancelOnTouchOutside(cancelable = false)
            .positiveButton(text = "Yes") {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                finish()
            }.negativeButton(text = "No")
            .negativeButton {
                Toast.makeText(
                    this@UtilActivity,
                    "This setting cannot be toggled without GPS turned on!",
                    Toast.LENGTH_LONG
                ).show()
            }
        Utils.setFont(builder.view)
        builder.show()
    }

    fun askAlwaysLocationIfNeeded(): Boolean {
        if (hasAlwaysLocation()) {
            return false
        }

        val dialog = MaterialDialog(this)
        dialog.title(text = "My Loxley needs Allow all the time location permission in order to track miles automatically in the background")
            .cancelOnTouchOutside(cancelable = false)
            .positiveButton(text = "Grant") {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission.ACCESS_BACKGROUND_LOCATION),
                    1234
                )
            }.negativeButton(text = "Cancel")
            .negativeButton {
                Toast.makeText(
                    this@UtilActivity,
                    "Auto-Tracking cannot be enabled without the permission!",
                    Toast.LENGTH_LONG
                ).show()
            }
        Utils.setFont(dialog.view)
        dialog.show()
        return true
    }

    @SuppressLint("InlinedApi")
    fun askActivityPermissionIfNeeded(): Boolean {
        if (hasActivityPermission()) {
            return false
        }
        val dialog = MaterialDialog(this)
        dialog.title(text = "Android categorizes driving as part of 'Physical Activity'. My Loxley needs Activity Recognition permission in order to track miles automatically in the background.")
            .cancelOnTouchOutside(cancelable = false)
            .positiveButton(text = "Grant") {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission.ACTIVITY_RECOGNITION),
                    1234
                )
            }.negativeButton(text = "Cancel")
            .negativeButton {
                Toast.makeText(
                    this@UtilActivity,
                    "Auto-Tracking cannot be enabled without the permission!",
                    Toast.LENGTH_LONG
                ).show()
            }
        Utils.setFont(dialog.view)
        dialog.show()


        return true
    }

    public fun hasActivityPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat
                .checkSelfPermission(this, permission.ACTIVITY_RECOGNITION) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun hasAlwaysLocation(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat
                .checkSelfPermission(this, permission.ACCESS_BACKGROUND_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Plog.d(TAG, "onRequestPermissionsResult")
        when (requestCode) {
            1234 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Plog.d(TAG, "permission granted")
                    permissionRequestComplete()
                } else {
                    Plog.d(TAG, "permission denied")

                    val hasLocationPermission =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        } else {
                            false
                        }
                    val title = if (hasLocationPermission)
                        "My Loxley needs Allow all the time location permission in order to track miles automatically in the background. Please grant it in the next screen by going to the Permissions section"
                    else
                        "We need location permission to track miles. Please grant it in the next screen by going to the Permissions section"
                    val dialog = MaterialDialog(this)
                    dialog.title(text = title)
                        .cancelOnTouchOutside(cancelable = false)
                        .positiveButton(text = "Grant") {
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }.negativeButton(text = "Cancel")
                    Utils.setFont(dialog.view)
                    dialog.show()
                }
            }
        }
    }

    protected fun isBatteryOptimizationMissing(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
                if (powerManager != null && powerManager.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)) {
                    return false
                }
            } catch (e: Exception) {
                Plog.e(TAG, e, "chekBatteryOptimization")
            }

        }
        return true
    }

    protected fun showTransitionEnableInfoDialog() {
        mainHandler.post {
            activeDialog?.dismiss()
            activeDialog = MaterialDialog(this)
                .title(text = "Auto tracking enabled!")
                .message(text = "My Loxley will track miles automatically!")
                .positiveButton(text = "Got it")
            activeDialog?.show()
        }
    }

    fun isObsolete(): Boolean {
        return isFinishing || isDestroyed || isChangingConfigurations
    }

    open fun popStackTillLastRoot() {
        if (activeFragment != null)
            activeFragment!!.popStackTillLastRoot()
    }

    fun checkAutoTrackPermissions() {
        val autoTrackEnabled = Preferences.isAutoTrackEnabled()
        if (autoTrackEnabled) {
            val hasAllPermissions = hasAllPermissions()
            if (!hasAllPermissions) {
                val builder = MaterialDialog(this)
                builder.title(text = "Loxley is missing permissions to continue auto-tracking. Please visit the Auto-track page and reenable the setting.")
                    .cancelOnTouchOutside(cancelable = false)
                    .positiveButton(text = "Got it")
                Utils.setFont(builder.view)
                builder.show()
            } else {
                LocationUtils.setGeoFenceAtCurrentLocation(this, null)
                LocationUtils.startPeriodicGeoFence()
            }
        }
    }

    private fun hasAllPermissions(): Boolean {
        return when {
            !isHardwareGpsEnabled(false) -> false
            isBatteryOptimizationMissing() -> false
            !hasAlwaysLocation() -> false
            !hasActivityPermission() -> false
            else -> true
        }
    }
}
