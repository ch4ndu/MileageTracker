package com.udnahc.locationapp.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.material.tabs.TabLayout;
import com.robinhood.ticker.TickerView;
import com.udnahc.locationapp.App;
import com.udnahc.locationapp.R;
import com.udnahc.locationapp.adapter.OfflineExpenseItem;
import com.udnahc.locationapp.location.MileageService;
import com.udnahc.locationapp.util.Constants;
import com.udnahc.locationapp.util.EventMessage;
import com.udnahc.locationapp.util.Plog;
import com.udnahc.locationapp.util.Preferences;
import com.udnahc.locationapp.util.RunnableAsync;
import com.udnahc.locationapp.util.Utils;
import com.udnahc.locationmanager.GpsMessage;
import com.udnahc.locationmanager.Mileage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.FlexibleItemDecoration;

@SuppressWarnings({"FieldCanBeLocal", "SetTextI18n"})
public class GpsFragment extends BaseFragment implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnCameraMoveListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnCameraMoveStartedListener,
        FlexibleAdapter.OnItemClickListener, FlexibleAdapter.OnActionStateListener, FlexibleAdapter.OnItemSwipeListener {
    private static final String TAG = "GpsFragment";
    private Button startTrackButton, stopTrackButton, saveButton, discardButton;
    //    private TextView tripDetails;
    private TickerView distanceDecimal, distanceSingle, distanceTenth, distanceHundred, distanceThousand, distanceTenThousand, distanceHundredThousand;
    private List<TickerView> distanceArray;
    private ViewGroup tickerCard, saveLayout;
    private List<Mileage> offlineMileages;
    @Nullable
    private Mileage mileage = null;
    @Nullable
    private GoogleMap mMap;
    @Nullable
    private SupportMapFragment mapFragment;
    private boolean moveCameraOnUpdate = true;
    private boolean finishMapInitialization = false;
    private Handler mServiceHandler;
    private HandlerThread serviceThread;
    private TextView deductionText;
    private TextView durationText;
    private PeriodFormatter periodFormatter;
    private RecyclerView offlineRecycler;
    private FlexibleAdapter flexibleAdapter;
    private TabLayout tabLayout;
    private LinearLayout mapContainer;
    private Runnable deleteRunnable = null;
    //    private Snackbar activeSnack = null;
    private FlexibleItemDecoration itemDecoration;

    private final Runnable updateTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if (mileage != null && durationText != null) {
                long difference = System.currentTimeMillis() - mileage.getTimeStamp();
                final Duration duration = new Duration(difference);
                if (periodFormatter == null) {
                    periodFormatter = new PeriodFormatterBuilder()
                            .printZeroAlways()
                            .minimumPrintedDigits(2)
                            .appendHours()
                            .appendSuffix(":")
                            .appendMinutes()
                            .appendSuffix(":")
                            .appendSeconds()
                            .toFormatter();
                }
                getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        durationText.setText("Time: " + periodFormatter.print(duration.toPeriod()));
                    }
                });
            }
            if (mServiceHandler != null) {
                mServiceHandler.postDelayed(updateTimerRunnable, 1000);
            }
        }
    };

    private static List<Integer> getDigits(int num) {
        List<Integer> digits = new ArrayList<>();
        collectDigits(num, digits);
        Collections.reverse(digits);
        return digits;
    }

    private static void collectDigits(int num, List<Integer> digits) {
        if (num / 10 > 0) {
            collectDigits(num / 10, digits);
        }
        digits.add(num % 10);
    }

    public static GpsFragment getInstance() {
        GpsFragment fragment = new GpsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.BackStackKey, TAG);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void clearViews() {
        super.clearViews();
        resetViews();
    }

    @Override
    public int getContainerId() {
        return R.id.main_content;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Plog.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.activity_gps, container, false);
        durationText = view.findViewById(R.id.miles_duration_text);
        deductionText = view.findViewById(R.id.miles_deduction_text);
        mapContainer = view.findViewById(R.id.tracking_container);
        offlineRecycler = view.findViewById(R.id.offline_recycler);
        tabLayout = view.findViewById(R.id.tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    showTrackMilesContainer();
                } else {
                    showOfflineRecycler();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
//        toolbar.setTitle("Track Miles");
        Utils.setFont(view.findViewById(R.id.main_content));

        if (!MileageService.isGpsTrackerActive()) {
            EventBus.getDefault().removeAllStickyEvents();
        }
        tickerCard = view.findViewById(R.id.ticker_card);
        startTrackButton = view.findViewById(R.id.start_tracking);
        startTrackButton.setOnClickListener(this);
        stopTrackButton = view.findViewById(R.id.stop_tracking);
        stopTrackButton.setOnClickListener(this);

        distanceDecimal = view.findViewById(R.id.distance_travelled_decimal);
        distanceSingle = view.findViewById(R.id.distance_travelled_single);
        distanceTenth = view.findViewById(R.id.distance_travelled_tenth);
        distanceHundred = view.findViewById(R.id.distance_travelled_hundred);
        distanceThousand = view.findViewById(R.id.distance_travelled_thousand);
        distanceTenThousand = view.findViewById(R.id.distance_travelled_ten_thousand);
        distanceHundredThousand = view.findViewById(R.id.distance_travelled_hundred_thousand);
        distanceArray = new ArrayList<>();
        distanceArray.add(distanceSingle);
        distanceArray.add(distanceTenth);
        distanceArray.add(distanceHundred);
        distanceArray.add(distanceThousand);
        distanceArray.add(distanceTenThousand);
        distanceArray.add(distanceHundredThousand);

        distanceDecimal.setAnimationDuration(500);
        distanceDecimal.setAnimationInterpolator(new AccelerateDecelerateInterpolator());
        distanceDecimal.setGravity(Gravity.CENTER);
        distanceDecimal.setTypeface(App.get().getBoldFont());
        for (TickerView tickerView : distanceArray) {
            tickerView.setAnimationDuration(500);
            tickerView.setAnimationInterpolator(new AccelerateDecelerateInterpolator());
            tickerView.setGravity(Gravity.CENTER);
            tickerView.setTypeface(App.get().getBoldFont());
        }

        saveLayout = view.findViewById(R.id.save_layout);
        saveButton = view.findViewById(R.id.save_expense);
        discardButton = view.findViewById(R.id.discard_expense);

        saveButton.setOnClickListener(this);
        discardButton.setOnClickListener(this);
        updateButtonText();
        checkOfflineMileages();

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
        if (isVisible() && getBaseActivity() != null
                && getBaseActivity().isHardwareGpsEnabled(true)
                && !getBaseActivity().isLocationPermissionGranted()) {
            getBaseActivity().requestLocationPermission();
        }
        return view;
    }

    private void showTrackMilesContainer() {

        if (offlineRecycler != null) {
            offlineRecycler.setVisibility(View.GONE);
            offlineRecycler.setAdapter(null);
            if (flexibleAdapter != null) {
                flexibleAdapter.clear();
                flexibleAdapter = null;
            }
            if (mapContainer != null) {
                mapContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraMoveListener(this);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        if (getBaseActivity() == null
                || getBaseActivity().isObsolete()
                || !getBaseActivity().isLocationPermissionGranted()
                || !getBaseActivity().isHardwareGpsEnabled(false))
            return;
        if (isMenuVisible())
            moveCameratoCurrent();
        else
            Plog.d(TAG, "menu not visible. not moving map");
    }

    @SuppressLint("MissingPermission")
    private void moveCameratoCurrent() {
        if (finishMapInitialization
                || mMap == null
                || getBaseActivity() == null
                || getBaseActivity().isObsolete()
                || !isLocationPermissionGranted()
                || !getBaseActivity().isHardwareGpsEnabled(true))
            return;
        if (isHardwareGpsEnabled() && isLocationPermissionGranted()) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            try {
                LocationManager locationManager = (LocationManager) getBaseActivity().getSystemService(Context.LOCATION_SERVICE);
                if (locationManager == null)
                    return;
                Criteria criteria = new Criteria();
                String bestProvider = locationManager.getBestProvider(criteria, false);
                if (bestProvider == null)
                    bestProvider = "unknown";

                Location location = locationManager.getLastKnownLocation(bestProvider);
                if (location != null) {
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17));

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(), location.getLongitude()))
                            .zoom(17)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    finishMapInitialization = true;
                    if (mileage != null && mileage.getPath().size() > 0) {
                        getMainHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isObsolete())
                                    return;
                                addPolyline(mileage);
                            }
                        }, 1000);
                    }
                }
            } catch (SecurityException e) {
                Plog.e(TAG, e, "moveCamera");
            }
        }
    }

    private void checkOfflineMileages() {
        getMainHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isObsolete())
                    return;
                RunnableAsync.execute(new Runnable() {
                    @Override
                    public void run() {
                        offlineMileages = App.get().getDbHelper().getMileages();
                        Plog.d(TAG, "offline mileageImpl count %s", offlineMileages.size());
                        getMainHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                if (offlineMileages.size() > 0) {
                                    tabLayout.setVisibility(View.VISIBLE);
                                } else {
                                    tabLayout.setVisibility(View.GONE);
                                    showTrackMilesContainer();
                                }
                            }
                        });
                    }
                });
            }
        }, 500);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_tracking:
                if (getBaseActivity() != null && getBaseActivity().isHardwareGpsEnabled(true) && !getBaseActivity().isLocationPermissionGranted()) {
                    getBaseActivity().requestLocationPermission();
                    return;
                }
                startTrackButton.setVisibility(View.GONE);
                stopTrackButton.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getBaseActivity(), MileageService.class);
                intent.putExtra("userStarted", true);
                getBaseActivity().startService(intent);
                Preferences.putBoolean(Constants.RecordMiles, true);
                break;
            case R.id.stop_tracking:
                if (MileageService.isGpsTrackerActive()) {
                    EventBus.getDefault().post(new GpsMessage.StopGpsUpdates("User Canceled"));
                    startTrackButton.setVisibility(View.VISIBLE);
                    stopTrackButton.setVisibility(View.GONE);
                }
                break;
//            case R.id.save_expense:
//                Fragment fragment = new AddExpenseFragment();
//                fragment.setRetainInstance(true);
//                Bundle bundle = new Bundle();
//                bundle.putBoolean("saved", false);
//                bundle.putBoolean("fromGps", true);
//                bundle.putString(Constants.BackStackKey, "gpsFragment");
//                fragment.setArguments(bundle);
//                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//                transaction.add(R.id.main_content, fragment, "AddMileage")
//                        .addToBackStack("gpsFragment")
//                        .commit();
//                break;
            case R.id.discard_expense:
                resetViews();
                break;
        }
    }

    public void resetViews() {
        if (mMap != null)
            mMap.clear();
        mileage = null;
        updateDistance(0.0);
        stopTrackButton.setVisibility(View.GONE);
        startTrackButton.setVisibility(View.VISIBLE);
        saveLayout.setVisibility(View.GONE);
        durationText.setText("Time: 00:00");
        deductionText.setText("$0.00");
    }

    private void updateFinalMileage() {
        if (mileage == null || isObsolete())
            return;
        Log.d(TAG, "updateFinalMileage");
        updateDistance(mileage.getMiles());
        stopTrackButton.setVisibility(View.GONE);
        startTrackButton.setVisibility(View.GONE);
        saveLayout.setVisibility(View.VISIBLE);
        updateFinalPolyline(mileage);
        restTimers();
    }

    private void getStickyEventAndUpdate(GpsMessage.MileageUpdate event) {
        Plog.d(TAG, "getStickyEventAndUpdate");
        if (isObsolete())
            return;
        if (event == null)
            event = EventBus.getDefault().getStickyEvent(GpsMessage.MileageUpdate.class);
        if (event != null && event.getMileage() != null) {
            this.mileage = event.getMileage();
            if (mileage == null)
                return;
            if (!MileageService.isGpsTrackerActive()) {
                EventBus.getDefault().removeStickyEvent(event);
                updateFinalMileage();
                return;
            }
            if (serviceThread == null || !serviceThread.isAlive()) {
                serviceThread = new HandlerThread("TrackMiles");
                serviceThread.start();

                mServiceHandler = new Handler(serviceThread.getLooper());
            }
            mServiceHandler.postDelayed(updateTimerRunnable, 1000);
            tickerCard.setVisibility(View.VISIBLE);
            final int size = mileage.getPath().size();
            if (size > 1) {
                double distance = Utils.getMiles(mileage.getPath());
                updateDistance(distance);
            }
            if (mMap == null)
                return;
            if (moveCameraOnUpdate) {
//                Plog.d(TAG, "maps:moving camera");
                Location lastLocation = mileage.getPath().get(mileage.getPath().size() - 1);

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(lastLocation.getLatitude(),
                                lastLocation.getLongitude()), mMap.getCameraPosition().zoom));
            } else {
//                Plog.d(TAG, "maps: not moving camera");
            }
            addPolyline(mileage);
        }
    }

    private void addPolyline(@NonNull Mileage mileage) {
        if (mMap == null)
            return;
        try {
            PolylineOptions options = new PolylineOptions().geodesic(true).clickable(false);
            options.addAll(mileage.getLatLngList());
            Polyline polyline = mMap.addPolyline(options);
            polyline.setEndCap(new RoundCap());
            polyline.setWidth(12);
            polyline.setColor(Utils.getColorPrimary(getBaseActivity()));
            polyline.setJointType(JointType.ROUND);
        } catch (Exception e) {
            Plog.e(TAG, e, "maps");
        }
    }

    private void updateFinalPolyline(@NonNull Mileage mileage) {
        if (mMap == null || isObsolete())
            return;
        PolylineOptions polyOptions = new PolylineOptions().geodesic(true).clickable(false);
        List<LatLng> latLngs = mileage.getLatLngList();
        polyOptions.addAll(latLngs);

        mMap.clear();
        Polyline polyline = mMap.addPolyline(polyOptions);
        polyline.setEndCap(new RoundCap());
        polyline.setWidth(12);
        polyline.setColor(Utils.getColorPrimary(getBaseActivity()));
        polyline.setJointType(JointType.ROUND);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : latLngs) {
            builder.include(latLng);
        }

        final LatLngBounds bounds = builder.build();

        //BOUND_PADDING is an int to specify padding of bound.. try 100.
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 100);
        mMap.animateCamera(cu);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onMessageEvent(EventMessage.PermissionRequestComplete event) {
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onMessageEvent(GpsMessage.ShowGooglePlayServicesUtilError event) {
        GooglePlayServicesUtil.showErrorDialogFragment(event.getStatus(), getBaseActivity(), null, 0, null);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onMessageEvent(GpsMessage.MileageUpdate event) {
        getStickyEventAndUpdate(event);
    }

    @Override
    public void onVisible() {
        super.onVisible();
        moveCameratoCurrent();
        getStickyEventAndUpdate(null);
        updateButtonText();
        checkOfflineMileages();
    }

    private void updateButtonText() {
        if (isObsolete())
            return;
        if (mileage != null) {
            if (MileageService.isGpsTrackerActive()) {
                startTrackButton.setVisibility(View.GONE);
                stopTrackButton.setVisibility(View.VISIBLE);
                saveLayout.setVisibility(View.GONE);
            } else {
                updateFinalMileage();
            }
        } else {
            startTrackButton.setVisibility(View.VISIBLE);
            stopTrackButton.setVisibility(View.GONE);
            saveLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        Plog.d(TAG, "onResume");
        super.onResume();
        onVisible();
    }

    private void restTimers() {
        if (mServiceHandler != null) {
            mServiceHandler.removeCallbacks(updateTimerRunnable);
            mServiceHandler = null;
        }
        if (serviceThread != null) {
            serviceThread.quit();
            serviceThread = null;
        }
    }

    @Override
    public void onStop() {
        restTimers();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (mapFragment != null && mMap != null) {
            mMap.clear();
        }
        restTimers();
        super.onDestroy();
    }

    private void updateDistance(double newMileage) {
        if (isObsolete())
            return;
        if (newMileage == 0.0) {
            for (TickerView tickerView : distanceArray) {
                tickerView.setText("" + 0);
            }
            distanceDecimal.setText("" + 0);
            return;
        }
        newMileage = (double) Math.round(newMileage * 10d) / 10d;
        String[] decimal = ("" + newMileage).split("\\.");
        try {
            int dec = Integer.parseInt(decimal[1]);
            distanceDecimal.setText("" + dec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            int value = Integer.parseInt(decimal[0]);
            final List<Integer> values = getDigits(value);
            for (int i = 0; i < values.size(); i++) {
                final TickerView tempText = distanceArray.get(i);
                final int finalI = i;
                getMainHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            tempText.setAnimationDuration(500);
                            tempText.setText("" + values.get(finalI));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, (i + 2) * 200);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, String.format("updateDistance:%s", newMileage));
        if (mileage != null)
            updateFinalPolyline(mileage);
        getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                deductionText.setText("$" + mileage.getCost());
            }
        });
    }

    @Override
    public void onCameraMove() {
//        Plog.d(TAG, "maps:onCameraMove");
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Plog.d(TAG, "maps:onMyLocationButtonClick");
        if (isObsolete())
            return false;
        getMainHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isObsolete())
                    moveCameraOnUpdate = true;
            }
        }, 1500);
        return false;
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (isObsolete())
            return;
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            moveCameraOnUpdate = false;
            Plog.d(TAG, "maps: The user gestured on the map");
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_API_ANIMATION) {
            Plog.d(TAG, "maps: The user tapped something on the map.");
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_DEVELOPER_ANIMATION) {
            Plog.d(TAG, "maps: The app moved the camera.");
        }
    }

    private void showOfflineRecycler() {
        if (mapContainer != null) {
            mapContainer.setVisibility(View.GONE);
            if (offlineRecycler != null) {
                offlineRecycler.setVisibility(View.VISIBLE);
                loadData();
            }
        }
    }

    protected void loadData() {
        final List<Mileage> mileageList = new ArrayList<>();
        final RunnableAsync runnableAsync = new RunnableAsync(new Runnable() {
            @Override
            public void run() {
                mileageList.addAll(App.get().getDbHelper().getMileages());
                for (Mileage expense : mileageList) {
//                    expense.seteDate(expense.getTimeStamp());
                    Plog.d(TAG, "mileageImpl: %s", expense);
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                if (flexibleAdapter == null) {
                    LinearLayoutManager manager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
                    flexibleAdapter = new FlexibleAdapter(processResponse(mileageList), this, true);
                    offlineRecycler.setLayoutManager(manager);
                    offlineRecycler.setAdapter(flexibleAdapter);
                    flexibleAdapter.setUnlinkAllItemsOnRemoveHeaders(true)
                            .setDisplayHeadersAtStartUp(false)
                            .setStickyHeaders(false);
                    flexibleAdapter.setPermanentDelete(true);
                    flexibleAdapter.setSwipeEnabled(true);
                    if (itemDecoration == null) {
                        itemDecoration = new FlexibleItemDecoration(getActivity());
                        itemDecoration.withEdge(true)
                                .withOffset(10)
                                .withTopEdge(true)
                                .withBottomEdge(true)
                                .withLeftEdge(true)
                                .withRightEdge(true);
                        offlineRecycler.addItemDecoration(itemDecoration);
                    }
                    flexibleAdapter.mItemClickListener = GpsFragment.this;
                    flexibleAdapter.getItemTouchHelperCallback().setSwipeFlags(ItemTouchHelper.LEFT);
                    flexibleAdapter.setPermanentDelete(true);
                    flexibleAdapter.addListener(GpsFragment.this);
                }
            }
        }, null);
        runnableAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected List<OfflineExpenseItem> processResponse(List<Mileage> expenses) {
        Collections.sort(expenses, new Comparator<Mileage>() {
            @Override
            public int compare(Mileage left, Mileage right) {
                return Long.compare(right.getTimeStamp(), left.getTimeStamp());
            }
        });
        List<OfflineExpenseItem> temp = new ArrayList<>();
        for (Mileage expense : expenses) {
            temp.add(new OfflineExpenseItem(getBaseActivity(), expense));
        }
        return temp;
    }

    @Override
    public boolean onItemClick(View view, int position) {
        App.get().setModifyingExpense(((OfflineExpenseItem) flexibleAdapter.getItem(position)).getListExpense());
        Intent intent = new Intent(getActivity(), NewActivity.class);
        intent.putExtra(Constants.FragmentId, "ViewOfflineMileage");
        startActivity(intent);
        return true;
    }

    @Override
    public void onItemSwipe(final int position, int direction) {
        Plog.d(TAG, "onItemSwipe position %s direction %s", position, direction);
        if (flexibleAdapter != null) {
            final OfflineExpenseItem item = (OfflineExpenseItem) flexibleAdapter.getItem(position);
            if (item == null)
                return;
//            if (activeSnack != null) {
//                activeSnack.dismiss();
//                activeSnack = null;
//            }
            if (deleteRunnable != null) {
                getMainHandler().removeCallbacks(deleteRunnable);
                deleteRunnable.run();
                deleteRunnable = null;
            }

//            activeSnack = ChocoBar.builder().setBackgroundColor(Utils.getColorPrimaryDark(getActivity()))
//                    .setTextSize(12)
//                    .setTextColor(Color.parseColor("#FFFFFF"))
//                    .setTextTypefaceStyle(Typeface.NORMAL)
//                    .setText("Item deleted")
//                    .setMaxLines(5)
//                    .centerText()
//                    .setActionText("Undo")
//                    .setActionClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (deleteRunnable != null) {
//                                getMainHandler().removeCallbacks(deleteRunnable);
//                                deleteRunnable = null;
//                                if (activeSnack != null) {
//                                    activeSnack.dismiss();
//                                    activeSnack = null;
//                                }
//                                if (flexibleAdapter != null)
//                                    flexibleAdapter.addItem(position, item);
//                            }
//                        }
//                    })
//                    .setActionTextColor(Utils.getColorAccent(getActivity()))
//                    .setActionTextSize(16)
//                    .setActionTextTypefaceStyle(Typeface.BOLD)
//                    .setActivity(getActivity())
//                    .setDuration(4000)
//                    .build();
//            activeSnack.show();


            flexibleAdapter.removeItem(position);
            if (deleteRunnable == null) {
                deleteRunnable = new Runnable() {
                    @Override
                    public void run() {
                        new RunnableAsync(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    App.get().getDbHelper().deleteMileage(item.getListExpense().getTimeStamp());
                                    deleteRunnable = null;
                                } catch (Exception e) {
                                    Plog.e(TAG, e, "onItemSwipe");
                                }
                            }
                        }, null, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                };
            }
            getMainHandler().postDelayed(deleteRunnable, 4500);
        }
    }

    @Override
    public void onActionStateChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        Plog.d(TAG, "state %s", actionState);
    }
}
