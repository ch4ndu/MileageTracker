package com.udnahc.locationapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.udnahc.locationapp.controller.*
import com.udnahc.locationapp.util.*
import java.lang.String

class MainActivity : UtilActivity(), View.OnClickListener {


    @Suppress("PrivatePropertyName")
    private var activeItem: MenuItem? = null
    private var viewPager: ViewPager? = null
    private var adapter: MainAdapter? = null

    private class MainAdapter(fragmentManager: FragmentManager?) :
        SmartFragmentStatePagerAdapter(fragmentManager) {
        private val primaryItem = -1

        override fun getItem(position: Int): BaseFragment {
            return when {
                getRegisteredFragment(position) != null -> {
                    getRegisteredFragment(position)!!
                }
                position == 0 -> {
                    var fragment: BaseFragment? = getRegisteredFragment(position)
                    if (fragment == null || !fragment.isAdded) {
                        fragment = OverViewFragment()
                        fragment.setRetainInstance(true)
                        registeredFragments.put(position, fragment)
                    }
                    fragment
                }
                position == 1 -> {
                    var fragment: BaseFragment? = getRegisteredFragment(position)
                    if (fragment == null || !fragment.isAdded) {
                        fragment = GpsFragment.getInstance()
                        fragment.setRetainInstance(true)
                        registeredFragments.put(position, fragment)
                    }
                    fragment!!
                }
                else -> { // 3
                    var fragment: BaseFragment? = getRegisteredFragment(position)
                    if (fragment == null || !fragment.isAdded) {
                        fragment = GpsFragment.getInstance()
                        val bundle = Bundle()
                        fragment.setRetainInstance(true)
                        fragment.setArguments(bundle)
                    }
                    fragment!!
                }
            }
        }


        override fun getCount(): Int {
            return 3
        }
    }

    @SuppressLint("InflateParams")
    private fun showBottomSheet(item: MenuItem?) {
        val mBottomSheetDialog = BottomSheetDialog(this@MainActivity)
        val sheetView = layoutInflater.inflate(R.layout.main_bottom_sheet, null)
        sheetView.findViewById<View>(R.id.auto_tracking_nav).setOnClickListener(this@MainActivity)
        sheetView.findViewById<View>(R.id.view_logs).setOnClickListener(this@MainActivity)
        sheetView.findViewById<View>(R.id.view_logs).setOnClickListener(this@MainActivity)
        val headerEmail = sheetView.findViewById<TextView>(R.id.header_email)
        val headerName = sheetView.findViewById<TextView>(R.id.header_name)
        val header = sheetView.findViewById<TextView>(R.id.header_version)
        if (header != null) {
            header.text = String.format("App Version - %s", BuildConfig.VERSION_NAME)
        }
        if (headerEmail != null) {
            headerEmail.text = String.format("Email ")
        }
        if (headerName != null) {
            headerName.text = String.format("Hi ")
        }
        Utils.setFont(sheetView)
        mBottomSheetDialog.setContentView(sheetView)
        mBottomSheetDialog.setOnShowListener {
            try {
                mBottomSheetDialog.setCanceledOnTouchOutside(true)
                mBottomSheetDialog.setCancelable(true)
//                val bottomSheet =
//                    mBottomSheetDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
//                if (bottomSheet != null) {
//                    BottomSheetBehavior.from(
//                        bottomSheet
//                    ).state =
//                        BottomSheetBehavior.STATE_EXPANDED
//                }
            } catch (e: Exception) {
                Plog.e("temp", e, "setOnShowListener")
            }
        }
        mBottomSheetDialog.setOnDismissListener {
            if (item != null) {
                item.isChecked = false
                if (activeItem != null) activeItem?.isChecked = true
            }
        }
        mBottomSheetDialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        viewPager = findViewById(R.id.main_pager) ?: return
        viewPager?.offscreenPageLimit = 5
        adapter = MainAdapter(supportFragmentManager)
        viewPager?.adapter = adapter
//        Data.INSTANCE.getDashboardDetails(App.get().getCurrentYear(), null, null);
        //        Data.INSTANCE.getDashboardDetails(App.get().getCurrentYear(), null, null);
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            dismissKeyboardIfNecessary()
            when (item.itemId) {
                R.id.dashboard -> {
                    activeItem = item
                    viewPager?.setCurrentItem(0, false)
                    activeFragment = adapter?.getItem(0)
                    return@setOnItemSelectedListener true
                }
                R.id.track_miles -> {
                    activeItem = item
                    viewPager?.setCurrentItem(1, false)
                    activeFragment = adapter?.getItem(1)
                    return@setOnItemSelectedListener true
                }
                R.id.more_options -> {
                    showBottomSheet(item)
                    return@setOnItemSelectedListener true
                }
                else -> {
                    viewPager?.setCurrentItem(0, false)
                    activeFragment = adapter?.getItem(0)
                    activeItem = item
                    return@setOnItemSelectedListener true
                }
            }
        }
        try {
            activeItem = bottomNavigationView.menu.findItem(R.id.dashboard)
        } catch (e: java.lang.Exception) {
            Plog.e(TAG, e, "set initial bottom item")
        }
        var goToGps = false
        if ("gps".equals(intent.getStringExtra("goto"), ignoreCase = true)) {
            Plog.d(TAG, "gps intent found.")
            goToGps = true
        }
        if (Constants.GPS_ACTIVITY_ACTION == intent.action) {
            goToGps = true
        }
        if (goToGps) {
            bottomNavigationView.selectedItemId = R.id.track_miles
        }
    }

    private fun dismissKeyboardIfNecessary() {
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
            if (imm != null && currentFocus != null && imm.isAcceptingText) {
                imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            }
        } catch (e: java.lang.Exception) {
            Plog.e(TAG, e, "dismissKeyboardIfNecessary")
        }
    }

    override fun onClick(view: View?) {

        val id: Int = view?.id ?: -1
        if (id == R.id.auto_tracking_nav) {
            val intent = Intent(this, AutoTrackActivity::class.java)
            startActivity(intent)
        }  else if (id == R.id.view_logs) {
            val logIntent = Intent(this@MainActivity, ViewLogsActivity::class.java)
            startActivity(logIntent)
        }
    }
}
