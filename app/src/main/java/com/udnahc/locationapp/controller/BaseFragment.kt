package com.udnahc.locationapp.controller

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.udnahc.locationapp.R
import com.udnahc.locationapp.util.Constants
import com.udnahc.locationapp.util.EventMessage
import com.udnahc.locationapp.util.Plog
import com.udnahc.locationapp.util.Utils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Suppress("PropertyName")
@SuppressLint("RestrictedApi")
abstract class BaseFragment : Fragment() {
    val TAG = javaClass.simpleName

    private var handler: Handler? = null

    val baseActivity: UtilActivity?
        get() = if (activity is UtilActivity) activity as UtilActivity? else null


    val mainHandler: Handler
        get() {
            if (handler == null) {
                handler = Handler(Looper.getMainLooper())
            }
            return handler!!
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Plog.v(TAG, "onCreateView")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Plog.v(TAG, "onConfigurationChanged")
        super.onConfigurationChanged(newConfig)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onMessageEvent(event: EventMessage.DummyEvent) {
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Plog.v(TAG, "onHiddenChanged hidden %s", hidden)
    }

    abstract fun getContainerId(): Int

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        Plog.v(TAG, "setMenuVisibility %s", menuVisible)
        Plog.v(
            TAG,
            "setMenuVisibility isAdded:%s, isStateSaved:%s, isDetached:%s, isHidden:%s, isInLayout:%s, isMenuVisible:%s, isRemoving:%s, isResumed:%s, isVisible:%s",
            isAdded,
            isStateSaved,
            isDetached,
            isHidden,
            isInLayout,
            isMenuVisible,
            isRemoving,
            isResumed,
            isVisible
        )
        if (menuVisible)
            onVisible()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Plog.v(TAG, "setUserVisibleHint isVisibleToUser %s", isVisibleToUser)
    }

    override fun onStart() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        Plog.v(TAG, "onStart")
        super.onStart()
        Plog.v(
            TAG,
            "onStart isAdded:%s, isStateSaved:%s, isDetached:%s, isHidden:%s, isInLayout:%s, isMenuVisible:%s, isRemoving:%s, isResumed:%s, isVisible:%s",
            isAdded,
            isStateSaved,
            isDetached,
            isHidden,
            isInLayout,
            isMenuVisible,
            isRemoving,
            isResumed,
            isVisible
        )
    }

    override fun onResume() {
        Plog.v(TAG, "onResume")
        super.onResume()
        Plog.v(
            TAG,
            "onResume isAdded:%s, isStateSaved:%s, isDetached:%s, isHidden:%s, isInLayout:%s, isMenuVisible:%s, isRemoving:%s, isResumed:%s, isVisible:%s",
            isAdded,
            isStateSaved,
            isDetached,
            isHidden,
            isInLayout,
            isMenuVisible,
            isRemoving,
            isResumed,
            isVisible
        )
    }

    override fun onPause() {
        Plog.v(TAG, "onPause")
        super.onPause()
        Plog.v(
            TAG,
            "onPause isAdded:%s, isStateSaved:%s, isDetached:%s, isHidden:%s, isInLayout:%s, isMenuVisible:%s, isRemoving:%s, isResumed:%s, isVisible:%s",
            isAdded,
            isStateSaved,
            isDetached,
            isHidden,
            isInLayout,
            isMenuVisible,
            isRemoving,
            isResumed,
            isVisible
        )
    }

    override fun onStop() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        Plog.v(TAG, "onStop")
        super.onStop()
        Plog.v(
            TAG,
            "onStop isAdded:%s, isStateSaved:%s, isDetached:%s, isHidden:%s, isInLayout:%s, isMenuVisible:%s, isRemoving:%s, isResumed:%s, isVisible:%s",
            isAdded,
            isStateSaved,
            isDetached,
            isHidden,
            isInLayout,
            isMenuVisible,
            isRemoving,
            isResumed,
            isVisible
        )
    }

    override fun onDestroy() {
        Plog.v(TAG, "onDestroy")
        super.onDestroy()
        Plog.v(
            TAG,
            "onDestroy isAdded:%s, isStateSaved:%s, isDetached:%s, isHidden:%s, isInLayout:%s, isMenuVisible:%s, isRemoving:%s, isResumed:%s, isVisible:%s",
            isAdded,
            isStateSaved,
            isDetached,
            isHidden,
            isInLayout,
            isMenuVisible,
            isRemoving,
            isResumed,
            isVisible
        )
    }

    open fun handleBack(): Boolean {
        Plog.d(TAG, "handleBack")
        val arguments = arguments
        val backStackKey = arguments?.getString(Constants.BackStackKey) ?: TAG
        val manager = childFragmentManager
        if (manager.backStackEntryCount > 0) {
            val fragment = manager.findFragmentById(getContainerId())
            var handled = false
            if (fragment != null && fragment is BaseFragment) {
                Plog.d(TAG, "handleBack-passing back to %s", fragment.TAG)
                handled = fragment.handleBack()
                Plog.d(TAG, "%s handleBack-handled back with result:%s", fragment.TAG, handled)

                if (!handled) {
                    val tag = fragment.arguments?.getString(Constants.BackStackKey)
                        ?: fragment.TAG
                    handled = manager.popBackStackImmediate(tag, 0)
                    Plog.d(
                        TAG,
                        "%s handleBack-popBackStackImmediate with backstackKey:%s result:%s",
                        fragment.TAG,
                        tag,
                        handled
                    )
                    if (handled) {
                        onVisible()
                    }
                }
            }
            if (!handled && !TextUtils.isEmpty(backStackKey)) {
                handled = manager.popBackStackImmediate(backStackKey, 0)
                Plog.d(
                    TAG,
                    "handleBack-popBackStackImmediate backStackKey: %s result %s",
                    backStackKey,
                    handled
                )
                if (handled) {
                    onVisible()
                }
            }
            if (!handled) {
                handled = manager.popBackStackImmediate()
                Plog.d(TAG, "handleBack-nothing worked...so popped here with result %s", handled)
                if (handled) {
                    onVisible()
                }
            }
            Plog.v(
                TAG,
                "onVisible isAdded:%s, isStateSaved:%s, isDetached:%s, isHidden:%s, isInLayout:%s, isMenuVisible:%s, isRemoving:%s, isResumed:%s, isVisible:%s",
                isAdded,
                isStateSaved,
                isDetached,
                isHidden,
                isInLayout,
                isMenuVisible,
                isRemoving,
                isResumed,
                isVisible
            )
            return handled
        }
        Plog.d(TAG, "handleBack no backstack entries")
        return false
    }

    open fun onVisible() {
        Plog.v(
            TAG,
            "onVisible isAdded:%s, isStateSaved:%s, isDetached:%s, isHidden:%s, isInLayout:%s, isMenuVisible:%s, isRemoving:%s, isResumed:%s, isVisible:%s",
            isAdded,
            isStateSaved,
            isDetached,
            isHidden,
            isInLayout,
            isMenuVisible,
            isRemoving,
            isResumed,
            isVisible
        )
    }

    open fun popStackTillLastRoot() {
        Plog.d(TAG, "popStackTillLastRoot")
        if (isObsolete())
            return
        val manager = childFragmentManager
        if (manager.backStackEntryCount > 0) {
            val fragment = manager.findFragmentById(getContainerId())
            if (fragment != null && fragment is BaseFragment) {
                if (fragment.childFragmentManager.backStackEntryCount > 0) {
                    val tag = fragment.arguments?.getString(Constants.BackStackKey) ?: fragment.TAG
                    fragment.childFragmentManager.popBackStackImmediate(
                        tag,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                } else {
                    val tag = fragment.arguments?.getString(Constants.BackStackKey) ?: fragment.TAG
                    manager.popBackStackImmediate(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                }
                fragment.onVisible()
            }
        } else {
            resetViews()
        }
    }

    open fun resetViews() {

    }

    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
        Plog.v(TAG, "onDestroyOptionsMenu")
    }

    fun setBackOnToolbar(toolbar: androidx.appcompat.widget.Toolbar?, title: String) {
        if (toolbar != null) {
            toolbar.title = title
            toolbar.findViewById<TextView>(R.id.toolbar_text)?.text = ""
            toolbar.setNavigationIcon(R.drawable.back_icon_vector)
            toolbar.setNavigationOnClickListener(View.OnClickListener { activity?.onBackPressed() })
        }
    }

    fun showErrorDialog(title: String, content: String) {
        val activity = baseActivity ?: return
        mainHandler.postDelayed({

            if (activity.activeDialog != null && activity.activeDialog!!.isShowing) {
                activity.activeDialog?.dismiss()
            }
            activity.activeDialog = MaterialDialog(activity)
                .title(text = title)
                .message(text = content)
                .positiveButton(text = "Ok")
            activity.activeDialog?.show()
        }, 300)
    }

    fun showLoadingDialog() {
        Plog.d(TAG, "showLoadingDialog")
        val activity = baseActivity ?: return
        mainHandler.post {
            if (activity.activeDialog != null && activity.activeDialog!!.isShowing)
                activity.activeDialog?.dismiss()
            activity.activeDialog = MaterialDialog(activity)
                .title(text = "Loading...")
                .customView(viewRes = R.layout.ripple_loading_layout, scrollable = true)
                .cancelOnTouchOutside(cancelable = false)
                .cancelable(cancelable = false)
            activity.activeDialog?.show {
                noAutoDismiss()
            }
        }
    }

    open fun dismissActiveDialog() {
        Plog.d(TAG, "dismissActiveDialog")
        val activity = baseActivity ?: return
        mainHandler.post {
            try {
                if (activity.activeDialog != null && activity.activeDialog!!.isShowing) {
                    activity.activeDialog!!.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    protected fun isLocationPermissionGranted(): Boolean {
        val activity = baseActivity ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Plog.d(TAG, "permission granted for location")
                return true
            }
        }
        return false
    }

    fun requestLocationPermission() {
        val activity = baseActivity ?: return
        Plog.d(TAG, "requestLocationPermission")
        if (PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Plog.d(TAG, "permission granted for location")
            activity.permissionRequestComplete()
        } else if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Plog.d(TAG, "asking permission")
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                1234
            )
        } else {
            Plog.d(TAG, "permission denied somehow!!")
            val dialog = MaterialDialog(activity)
                .title(text = "We need location permission to track miles. Please grant it in the next screen by going to the Permissions section")
                .cancelOnTouchOutside(cancelable = false)
                .cancelable(false)
                .positiveButton(text = "Ok") {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", activity.packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
            Utils.setFont(dialog.view)
            dialog.show()
        }
    }

    public fun isHardwareGpsEnabled(): Boolean {
        val activity = baseActivity ?: return false
        val manager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
            return false
        }
        return true
    }

    protected fun buildAlertMessageNoGps() {
        val activity = baseActivity ?: return
        val dialog = MaterialDialog(activity)
        dialog.title(text = "Your GPS seems to be disabled, do you want to enable it?")
            .cancelOnTouchOutside(cancelable = false)
            .positiveButton(text = "Yes") {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .negativeButton(text = "No") {
                if (this is GpsFragment) {
                    Toast.makeText(
                        activity,
                        "Tracking miles is not possible without GPS turned on!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        activity,
                        "This setting cannot be toggled without GPS turned on!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        Utils.setFont(dialog.view)
        dialog.show()
    }

//    fun askAlwaysLocationIfNeeded(): Boolean {
//        val activity = baseActivity ?: return false
//        if (hasAlwaysLocation()) {
//            return false
//        }
//
//        val dialog = MaterialDialog(activity)
//        dialog.title(text = "My Loxley needs Allow all the time location permission in order to track miles in the background")
//                .cancelOnTouchOutside(cancelable = false)
//                .positiveButton(text = "Grant") {
//                    ActivityCompat.requestPermissions(activity,
//                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
//                            1234
//                    )
//                }.negativeButton(text = "Cancel") {
//                    if (this is GpsFragment) {
//                        Toast.makeText(activity, "Tracking miles is not possible without the permission!", Toast.LENGTH_LONG).show()
////                        finish()
//                    } else {
//                        Toast.makeText(activity, "This setting cannot be toggled without the permission!", Toast.LENGTH_LONG).show()
//                    }
//                }
//        Utils.setFont(dialog.view)
//        dialog.show()
//        return true
//    }

    @SuppressLint("InlinedApi")
    fun askActivityPermissionIfNeeded(): Boolean {
        val activity = baseActivity ?: return false
        if (hasActivityPermission()) {
            return false
        }
        val dialog = MaterialDialog(activity)
        dialog.title(text = "My Loxley needs activity recognition permission in order to track miles in the background")
            .cancelOnTouchOutside(cancelable = false)
            .positiveButton(text = "Grant") {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    1234
                )
            }.negativeButton(text = "Cancel") {
                if (this is GpsFragment) {
                    Toast.makeText(
                        activity,
                        "Tracking miles is not possible without the permission!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        activity,
                        "This setting cannot be toggled without the permission!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        Utils.setFont(dialog.view)
        dialog.show()


        return true
    }

    public fun hasActivityPermission(): Boolean {
        val activity = baseActivity ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat
                .checkSelfPermission(activity, Manifest.permission.ACTIVITY_RECOGNITION) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun hasAlwaysLocation(): Boolean {
        val activity = baseActivity ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat
                .checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isObsolete(): Boolean {
        val activity = baseActivity ?: return true
        return activity.isFinishing || !isAdded
                || isDetached
                || activity.isDestroyed
                || activity.isChangingConfigurations
    }

    open fun clearViews() {
        Plog.d(TAG, "clearViews")
    }
}
