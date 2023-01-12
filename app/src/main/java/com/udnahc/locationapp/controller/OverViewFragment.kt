package com.udnahc.locationapp.controller

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.android.material.snackbar.Snackbar
import com.udnahc.locationapp.App
import com.udnahc.locationapp.R
import com.udnahc.locationapp.adapter.OfflineExpenseItem
import com.udnahc.locationapp.util.*
import com.udnahc.locationmanager.Mileage
import com.udnahc.locationmanager.executors.RunnableAsync
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.FlexibleAdapter.OnActionStateListener
import eu.davidea.flexibleadapter.FlexibleAdapter.OnItemSwipeListener
import eu.davidea.flexibleadapter.SelectableAdapter
import eu.davidea.flexibleadapter.common.FlexibleItemDecoration


class OverViewFragment : BaseFragment(), FlexibleAdapter.OnItemClickListener,
    OnActionStateListener, OnItemSwipeListener, OnMapReadyCallback {
    private var offlineRecycler: RecyclerView? = null
    private var flexibleAdapter: FlexibleAdapter<OfflineExpenseItem>? = null
    private var itemDecoration: FlexibleItemDecoration? = null
    private var mapFragment: SupportMapFragment? = null
    private var mMap: GoogleMap? = null
    private var deleteRunnable: Runnable? = null
    private var activeSnack: Snackbar? = null
    override fun getContainerId(): Int {
        return R.id.main_content
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.overview_fragment, container, false)
        offlineRecycler = view.findViewById(R.id.overview_recycler)
        val recyclerView = offlineRecycler ?: return view
        val snapHelper: SnapHelper = FirstItemSnapper()
        snapHelper.attachToRecyclerView(recyclerView)
        Utils.setFont(view.findViewById(R.id.main_content))
        val mileages = ArrayList<Mileage>()
        RunnableAsync.enqueue({
//            mileages.addAll(UtilsKt.getMileagesForToday(App.get().dbHelper))
            mileages.addAll(App.get().dbHelper.mileages)
            Plog.d("OverView", "mileages count: %s", mileages.size)
        }, {
            if (flexibleAdapter == null) {
                val manager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
                flexibleAdapter =
                    FlexibleAdapter<OfflineExpenseItem>(processResponse(mileages), this, true)
                recyclerView.layoutManager = manager
                recyclerView.adapter = flexibleAdapter
                flexibleAdapter?.setUnlinkAllItemsOnRemoveHeaders(true)
                    ?.setDisplayHeadersAtStartUp(false)
                    ?.setStickyHeaders(false)
                flexibleAdapter?.isPermanentDelete = true
                flexibleAdapter?.isSwipeEnabled = true
                flexibleAdapter?.mode = SelectableAdapter.Mode.SINGLE
                if (itemDecoration == null) {
                    itemDecoration = FlexibleItemDecoration(requireActivity())
                        .withEdge(true)
                        .withOffset(10)
                        .withTopEdge(true)
                        .withBottomEdge(true)
                        .withLeftEdge(true)
                        .withRightEdge(true)
                    recyclerView.addItemDecoration(itemDecoration!!)
                }
                flexibleAdapter?.mItemClickListener = this@OverViewFragment
                flexibleAdapter?.itemTouchHelperCallback?.setSwipeFlags(ItemTouchHelper.LEFT)
                flexibleAdapter?.isPermanentDelete = true
                flexibleAdapter?.itemTouchHelperCallback?.setSwipeFlags(ItemTouchHelper.LEFT)
                flexibleAdapter?.isPermanentDelete = true
                flexibleAdapter?.addListener(this@OverViewFragment)
                if (mileages.isNotEmpty()) {
                    onItemClick(null, 0)
                    flexibleAdapter?.notifyItemChanged(0)
                }
                val snapOnScrollListener = SnapOnScrollListener(snapHelper,
                    SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL_STATE_IDLE,
                    object : OnSnapPositionChangeListener {
                        override fun onSnapPositionChange(position: Int) {
                            if (position >= 0) {
                                mainHandler.post {
                                    onItemClick(null, position)
                                    flexibleAdapter?.notifyItemChanged(position)
                                }
                            }
                        }
                    })
                recyclerView.addOnScrollListener(snapOnScrollListener)
            }
        })


        mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        return view
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.let {
            it.isMyLocationEnabled = false
            it.uiSettings.isMyLocationButtonEnabled = false
        }
    }

    private fun showMileage(mileage: Mileage) {
        mainHandler.postDelayed({
            updateFinalPolyline(mileage)
        }, 500)
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

    private fun processResponse(expenses: List<Mileage>): List<OfflineExpenseItem> {
        val list = expenses.sortedWith { left, right ->
            right.timeStamp.compareTo(left.timeStamp)
        }
        val temp: MutableList<OfflineExpenseItem> = ArrayList()
        for (expense in list) {
            temp.add(OfflineExpenseItem(baseActivity, expense))
        }
        return temp
    }

    override fun onItemClick(view: View?, position: Int): Boolean {
        flexibleAdapter?.clearSelection()
        flexibleAdapter?.toggleSelection(position)
        val mileage = flexibleAdapter?.getItem(position)?.listExpense ?: return false
        showMileage(mileage)
        return true
    }

    override fun onActionStateChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {

    }


    override fun onItemSwipe(position: Int, direction: Int) {
        Plog.d(TAG, "onItemSwipe position %s direction %s", position, direction)
        if (flexibleAdapter != null) {
            val item = flexibleAdapter?.getItem(position) ?: return
            activeSnack?.let {
                it.dismiss()
                activeSnack = null
            }
            deleteRunnable?.let {
                mainHandler.removeCallbacks(it)
                it.run()
                deleteRunnable = null
            }
            val contextView = view
            contextView?.let {
                activeSnack = Snackbar.make(contextView, "Item deleted", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Undo") {
                        deleteRunnable?.let {
                            mainHandler.removeCallbacks(it)
                            deleteRunnable = null
                            activeSnack?.dismiss()
                            activeSnack = null
                            flexibleAdapter?.addItem(position, item)
                        }
                    }
                activeSnack?.show()
            }
            flexibleAdapter?.removeItem(position)
            if (deleteRunnable == null) {
                RunnableAsync.enqueue({
                    try {
                        App.get().dbHelper.deleteMileage(item.listExpense.timeStamp)
                        deleteRunnable = null
                        activeSnack?.dismiss()
                        activeSnack = null
                    } catch (e: Exception) {
                        Plog.e(TAG, e, "onItemSwipe")
                    }
                }, {})
            }
            deleteRunnable?.let {
                mainHandler.postDelayed(it, 4500)
            }
        }
    }
}
