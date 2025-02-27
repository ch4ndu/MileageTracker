package com.udnahc.locationapp.util

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView

class FirstItemSnapper : LinearSnapHelper() {
    private var _verticalHelper: OrientationHelper? = null
    private var _horizontalHelper: OrientationHelper? = null

    //By default, this looks for the child's center to be snapped to, so we have to override
    //to change to the start of the view.  If we didn't do this, it would scroll over and over
    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        if (layoutManager.canScrollVertically()) {
            return findFirstView(layoutManager, getVerticalHelper(layoutManager))
        } else if (layoutManager.canScrollHorizontally()) {
            return findFirstView(layoutManager, getHorizontalHelper(layoutManager))
        }
        return null
    }

    //Override to use the start of the child view instead of the center
    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray {
        val out = IntArray(2) { 0 }
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToStart(targetView, getHorizontalHelper(layoutManager))
        } else {
            out[0] = 0
        }

        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToStart(targetView, getVerticalHelper(layoutManager))
        } else {
            out[1] = 0
        }
        return out
    }

    private fun findFirstView(
        layoutManager: RecyclerView.LayoutManager,
        helper: OrientationHelper
    ): View? {
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return null
        }

        //If the last item is completely visible, don't snap to any view
        if (layoutManager is LinearLayoutManager) {
            val lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()
            if (lastVisibleItemPosition == layoutManager.itemCount - 1) return null
        }

        var closestChild: View? = null
        var start: Int = 0

        if (layoutManager.clipToPadding) {
            start = helper.startAfterPadding
        }

        var absClosest = Integer.MAX_VALUE

        for (i in 0..childCount - 1) {
            val child = layoutManager.getChildAt(i)
            val childStart = helper.getDecoratedStart(child)
            val absDistance = Math.abs(childStart - start)

            /** if child start is closer than previous closest, set it as closest   */
            if (absDistance < absClosest) {
                absClosest = absDistance
                closestChild = child
            }
        }
        return closestChild
    }

    protected fun distanceToStart(targetView: View, helper: OrientationHelper): Int {
        return helper.getDecoratedStart(targetView) - helper.startAfterPadding
    }

    //We have to re-define these because they are private to the [LinearSnapHelper]
    protected fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (_verticalHelper == null) {
            _verticalHelper = OrientationHelper.createVerticalHelper(layoutManager)
        }
        return _verticalHelper!!
    }

    protected fun getHorizontalHelper(
        layoutManager: RecyclerView.LayoutManager
    ): OrientationHelper {
        if (_horizontalHelper == null) {
            _horizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return _horizontalHelper!!
    }
}