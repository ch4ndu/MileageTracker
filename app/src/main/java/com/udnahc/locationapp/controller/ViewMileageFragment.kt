package com.udnahc.locationapp.controller

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.robinhood.ticker.TickerView
import com.udnahc.locationapp.App
import com.udnahc.locationapp.R
import com.udnahc.locationapp.model.Expense
import com.udnahc.locationapp.util.Plog
import com.udnahc.locationapp.util.Utils
import com.udnahc.locationmanager.Mileage
import java.util.*
import kotlin.math.roundToInt

class ViewMileageFragment : BaseFragment(), View.OnClickListener, OnMapReadyCallback {
    private var viewDetailsButton: Button? = null
    private var tripDetails: TextView? = null
    private var distanceDecimal: TickerView? = null
    private var distanceSingle: TickerView? = null
    private var distanceTenth: TickerView? = null
    private var distanceHundred: TickerView? = null
    private var distanceThousand: TickerView? = null
    private var distanceTenThousand: TickerView? = null
    private var distanceHundredThousand: TickerView? = null
    private var distanceArray: MutableList<TickerView> = ArrayList()
    private var mileage: Mileage? = null
    private var mMap: GoogleMap? = null
    private var mapFragment: SupportMapFragment? = null
    private var expense: Expense? = null
    private var offlineMileage = false

    override fun getContainerId(): Int {
        return R.id.view_mileage_main_content
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.activity_view_mileage, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        setBackOnToolbar(toolbar, "Mileage")
        Utils.setFont(view.findViewById(R.id.main_content))
        viewDetailsButton = view.findViewById(R.id.view_mileage_details)
        viewDetailsButton?.setOnClickListener(this)
        offlineMileage = arguments?.getBoolean("fromGps", false) ?: false
        if (offlineMileage) {
            viewDetailsButton?.text = "Save"
        }

        tripDetails = view.findViewById(R.id.trip_details)

        distanceDecimal = view.findViewById(R.id.distance_travelled_decimal)
        distanceSingle = view.findViewById(R.id.distance_travelled_single)
        distanceTenth = view.findViewById(R.id.distance_travelled_tenth)
        distanceHundred = view.findViewById(R.id.distance_travelled_hundred)
        distanceThousand = view.findViewById(R.id.distance_travelled_thousand)
        distanceTenThousand = view.findViewById(R.id.distance_travelled_ten_thousand)
        distanceHundredThousand = view.findViewById(R.id.distance_travelled_hundred_thousand)
        distanceSingle?.let { distanceArray.add(it) }
        distanceTenth?.let { distanceArray.add(it) }
        distanceHundred?.let { distanceArray.add(it) }
        distanceThousand?.let { distanceArray.add(it) }
        distanceTenThousand?.let { distanceArray.add(it) }
        distanceHundredThousand?.let { distanceArray.add(it) }

        distanceDecimal?.animationDuration = 500
        distanceDecimal?.animationInterpolator = AccelerateDecelerateInterpolator()
        distanceDecimal?.gravity = Gravity.CENTER
        distanceDecimal?.typeface = App.get().boldFont
        for (tickerView in distanceArray) {
            tickerView.animationDuration = 500
            tickerView.animationInterpolator = AccelerateDecelerateInterpolator()
            tickerView.gravity = Gravity.CENTER
            tickerView.typeface = App.get().boldFont
        }

        if (isHardwareGpsEnabled()) {
            requestLocationPermission()
        } else {
            return view
        }

        mapFragment = childFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        val activity = activity
        if (activity is UtilActivity) {
            expense = App.get().modifyingExpense
            mileage = expense?.mileage
        }
        expense?.let {
            tripDetails?.text = "Trip Details"
        }
        toolbar?.title = "Trip"
        return view
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.let {
            it.isMyLocationEnabled = false
            it.uiSettings.isMyLocationButtonEnabled = false
            mainHandler.postDelayed({
                mileage?.let { mileage ->
                    updateFinalMileage()
                    updateFinalPolyline(mileage)
                }
            }, 500)
        }
    }

    override fun onClick(v: View) {
//        if (v.id == R.id.view_mileage_details) {
//            val activity = activity
//            if (activity is UtilActivity) {
//                activity.setModifyingExpense(expense)
//            }
//            var fragment = AddExpenseFragment()
//            val bundle = Bundle()
//            bundle.putBoolean("saved", true)
//            if (offlineMileage) {
//                fragment = AddOfflineMileageFragment()
//                bundle.putBoolean("fromGps", true)
//            }
//            val backstackKey = arguments?.getString(Constants.BackStackKey) ?: "ViewOfflineMileage"
//            bundle.putString(Constants.BackStackKey, backstackKey)
//            fragment.retainInstance = true
//            fragment.arguments = bundle
//            val fragmentManager = childFragmentManager
//            val transaction = fragmentManager.beginTransaction()
//            transaction.add(getContainerId(), fragment, "AddOfflineMileage")
//                    .addToBackStack(backstackKey)
//                    .commit()
//        }
    }

    private fun updateFinalMileage() {
        val mileage = mileage ?: return
        Log.d(TAG, "updateFinalMileage")
        updateDistance(mileage.miles)
        val split = mileage.tripDetails.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (split.size == 2) {
            tripDetails!!.text = mileage.tripDetails
        }
        updateFinalPolyline(mileage)
    }

    private fun updateFinalPolyline(mileage: Mileage) {
        try {
            val mMap = mMap ?: return
            val polyOptions = PolylineOptions().geodesic(true).clickable(false)
            val latLngs = mileage.latLngList
            polyOptions.addAll(latLngs)

            mMap.clear()
            val polyline = mMap.addPolyline(polyOptions)
            polyline.endCap = RoundCap()
            polyline.width = 12f
            polyline.color = Utils.getColorPrimary(baseActivity)
            polyline.jointType = JointType.ROUND

            val builder = LatLngBounds.Builder()
            for (latLng in latLngs) {
                builder.include(latLng)
            }

            val bounds = builder.build()

            //BOUND_PADDING is an int to specify padding of bound.. try 100.
            val cu = CameraUpdateFactory.newLatLngBounds(bounds, 150)
            mMap.animateCamera(cu)
        } catch (e: Exception) {
            Plog.e(TAG, e, "updateFinalPolyline")
        }
    }

    public override fun onDestroy() {
        if (mMap != null) {
            mMap!!.clear()
        }
        super.onDestroy()
    }

    private fun updateDistance(newMileage: Double) {
        val distanceDecimal = distanceDecimal ?: return
        val mileage = mileage ?: return
        var newMileage = newMileage
        newMileage = (newMileage * 10.0).roundToInt().toDouble() / 10.0
        //        String temp = distanceTenth.getText().toString();
        val decimal = ("" + newMileage).split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        try {
            val dec = Integer.parseInt(decimal[1])
            distanceDecimal.text = "" + dec
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val value = Integer.parseInt(decimal[0])
            val values = getDigits(value)
            for (i in values.indices) {
                val tempText = distanceArray[i]
                mainHandler.postDelayed({
                    try {
                        tempText.animationDuration = 500
                        tempText.text = "" + values[i]
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ((i + 2) * 200).toLong())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Log.d(TAG, String.format("updateDistance:%s", newMileage))
        updateFinalPolyline(mileage)
    }

    companion object {

        fun getDigits(num: Int): List<Int> {
            val digits = ArrayList<Int>()
            collectDigits(num, digits)
            digits.reverse()
            return digits
        }

        private fun collectDigits(num: Int, digits: MutableList<Int>) {
            if (num / 10 > 0) {
                collectDigits(num / 10, digits)
            }
            digits.add(num % 10)
        }
    }
}
