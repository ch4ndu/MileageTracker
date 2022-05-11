package com.udnahc.locationapp.controller

import android.os.Bundle
import com.udnahc.locationapp.App
import com.udnahc.locationapp.R
import com.udnahc.locationapp.util.Constants
import com.udnahc.locationapp.util.Utils

class NewActivity : UtilActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_new_activity)
        if (intent != null) {
            val fragmentId = intent.getStringExtra(Constants.FragmentId) ?: return
            when {
                fragmentId.equals("ViewOfflineMileage", ignoreCase = true) -> {
                    activeFragment = ViewMileageFragment()
                    modifyingExpense = App.get().modifyingExpense
                    val bundle = Bundle()
                    bundle.putBoolean("fromGps", true)
                    bundle.putString(Constants.BackStackKey, "offlineMileage")
                    activeFragment?.arguments = bundle
                }
            }
            if (activeFragment != null) {
                activeFragment?.retainInstance = true
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.main_content, activeFragment!!, fragmentId)
                        .commit()
            }
        }
    }

    override fun onBackPressed() {
        var handled = false
        if (activeFragment != null) {
            handled = activeFragment!!.handleBack()
        }
        if (handled) return
        finish()
    }
}
