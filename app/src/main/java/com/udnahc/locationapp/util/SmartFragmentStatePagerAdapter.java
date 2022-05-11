package com.udnahc.locationapp.util;

import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.udnahc.locationapp.controller.BaseFragment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
   Extension of FragmentStatePagerAdapter which intelligently caches
   all active fragments and manages the fragment lifecycles.
   Usage involves extending from SmartFragmentStatePagerAdapter as you would any other PagerAdapter.
*/
public abstract class SmartFragmentStatePagerAdapter extends FragmentStatePagerAdapter {
    protected final String TAG = getClass().getSimpleName();
    // Sparse array to keep track of registered fragments in memory
    protected SparseArray<BaseFragment> registeredFragments = new SparseArray<>();

    public SmartFragmentStatePagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    // Register the fragment when the item is instantiated
    @NotNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        BaseFragment fragment = (BaseFragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    // Unregister when the item is inactive
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        try {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        } catch (NullPointerException e) {
            Plog.e(TAG, e, "destroyItem");
        }
    }

    // Returns the fragment for the position (if instantiated)
    public @Nullable BaseFragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}
