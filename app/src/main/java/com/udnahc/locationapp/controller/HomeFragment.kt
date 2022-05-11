package com.udnahc.locationapp.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.udnahc.locationapp.R
import com.udnahc.locationapp.util.Plog


class HomeFragment : BaseFragment() {
    val TAGG = "HomeFragment"
    override fun getContainerId(): Int {
        return R.id.main_content
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Plog.d(TAGG, "onCreateView")
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.home_fragment, container, false)
    }
}
