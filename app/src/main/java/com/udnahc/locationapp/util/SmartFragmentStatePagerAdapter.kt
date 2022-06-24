package com.udnahc.locationapp.util

import android.util.SparseArray
import com.udnahc.locationapp.controller.BaseFragment
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import java.lang.NullPointerException

/*
   Extension of FragmentStatePagerAdapter which intelligently caches
   all active fragments and manages the fragment lifecycles.
   Usage involves extending from SmartFragmentStatePagerAdapter as you would any other PagerAdapter.
*/
abstract class SmartFragmentStatePagerAdapter(fragmentManager: FragmentManager?) : FragmentStatePagerAdapter(fragmentManager!!) {
    protected val TAG = javaClass.simpleName

    // Sparse array to keep track of registered fragments in memory
    protected var registeredFragments = SparseArray<BaseFragment>()

    // Register the fragment when the item is instantiated
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as BaseFragment
        registeredFragments.put(position, fragment)
        return fragment
    }

    // Unregister when the item is inactive
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        try {
            registeredFragments.remove(position)
            super.destroyItem(container, position, `object`)
        } catch (e: NullPointerException) {
            Plog.e(TAG, e, "destroyItem")
        }
    }

    // Returns the fragment for the position (if instantiated)
    fun getRegisteredFragment(position: Int): BaseFragment? {
        return registeredFragments[position]
    }
}
