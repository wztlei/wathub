package io.github.wztlei.wathub.ui.modules.poi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.deange.uwaterlooapi.UWaterlooApi;
import com.deange.uwaterlooapi.annotations.ModuleFragment;
import com.deange.uwaterlooapi.model.Metadata;
import com.deange.uwaterlooapi.model.poi.ATM;
import com.deange.uwaterlooapi.model.poi.BasicPointOfInterest;
import com.deange.uwaterlooapi.model.poi.Defibrillator;
import com.deange.uwaterlooapi.model.poi.GreyhoundStop;
import com.deange.uwaterlooapi.model.poi.Helpline;
import com.deange.uwaterlooapi.model.poi.Library;
import com.deange.uwaterlooapi.model.poi.Photosphere;

import io.github.wztlei.wathub.Constants;
import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.model.CombinedPointsOfInterestInfo;
import io.github.wztlei.wathub.model.responses.CombinedPointsOfInterestInfoResponse;
import io.github.wztlei.wathub.net.Calls;
import io.github.wztlei.wathub.ui.modules.ModuleType;
import io.github.wztlei.wathub.ui.modules.base.BaseApiMapFragment;
import io.github.wztlei.wathub.utils.IntentUtils;
import io.github.wztlei.wathub.utils.LocationUtils;
import io.github.wztlei.wathub.utils.MapUtils;
import io.github.wztlei.wathub.utils.Px;
import io.github.wztlei.wathub.utils.ViewUtils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;

@ModuleFragment(
    path = "/poi",
    layout = R.layout.module_poi
)
public class PointsOfInterestFragment
        extends BaseApiMapFragment<CombinedPointsOfInterestInfoResponse, CombinedPointsOfInterestInfo>
        implements
        GoogleMap.OnMarkerClickListener,
        LayersDialog.OnLayersSelectedListener,
        OnMapReadyCallback {

    private static final String TAG = "WL/PointsOfInterest";

    private static final int BEST_SIZE = Runtime.getRuntime().availableProcessors() * 2 - 1;
    private static final Executor EXECUTOR = Executors.newFixedThreadPool(BEST_SIZE);

    @BindView(R.id.points_of_interest_info)
    ViewGroup mInfoRoot;
    @BindView(R.id.points_of_interest_info_icon)
    ImageView mViewInBrowserBton;
    @BindView(android.R.id.text1)
    TextView mTitle;
    @BindView(android.R.id.text2)
    TextView mDescription;

    private CombinedPointsOfInterestInfo mResponse;
    private SharedPreferences mSharedPreferences;
    private int mFlags;

    @Override
    protected View getContentView(final LayoutInflater inflater, final ViewGroup parent) {
        final View view = inflater.inflate(R.layout.fragment_points_of_interest, parent, false);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSharedPreferences = getContext().getSharedPreferences(
                Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        mFlags = mSharedPreferences.getInt(MapUtils.POI_FLAGS_KEY, MapUtils.DEFAULT_POI_FLAGS);

        LayersDialog.showDialog(getContext(), mFlags, this);
        mMapView.getMapAsync(this);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_poi_layers, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.menu_layers) {
            LayersDialog.showDialog(getContext(), mFlags, this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLayersSelected(final int flags) {
        // Store the flags in shared preferences
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(MapUtils.POI_FLAGS_KEY, flags);
        editor.apply();

        mFlags = flags;
        mMapView.getMapAsync(this::showPointsOfInterestInfo);
    }

    @Override
    public Call<CombinedPointsOfInterestInfoResponse> onLoadData(final UWaterlooApi api) {
        final CombinedPointsOfInterestInfo info = new CombinedPointsOfInterestInfo();
        final CombinedPointsOfInterestInfoResponse response = new CombinedPointsOfInterestInfoResponse(
                info);
        final Semaphore semaphore = new Semaphore(1 - LayersDialog.LAYERS_COUNT);

        try {
            // ATMs
            fetchPointOfInterestInfo(semaphore, () ->
                    info.setATMs(Objects.requireNonNull(
                            Calls.unwrap(api.PointsOfInterest.getATMs())).getData()));

            // Greyhound stops
            fetchPointOfInterestInfo(semaphore, () ->
                    info.setGreyhounds(Objects.requireNonNull(
                            Calls.unwrap(api.PointsOfInterest.getGreyhoundStops())).getData()));

            // Photospheres
            fetchPointOfInterestInfo(semaphore, () -> {
                // Disable Photospheres API due to bug
                info.setPhotospheres(new ArrayList<>()
                        /*Calls.unwrap(api.PointsOfInterest.getPhotospheres()).getData()*/);
            });

            // Helplines
            fetchPointOfInterestInfo(semaphore, () ->
                    info.setHelplines(Objects.requireNonNull(
                            Calls.unwrap(api.PointsOfInterest.getHelplines())).getData()));

            // Libraries
            fetchPointOfInterestInfo(semaphore, () ->
                    info.setLibraries(Objects.requireNonNull(
                            Calls.unwrap(api.PointsOfInterest.getLibraries())).getData()));

            // Defibrillators
            fetchPointOfInterestInfo(semaphore, () ->
                    info.setDefibrillators(Objects.requireNonNull(
                            Calls.unwrap(api.PointsOfInterest.getDefibrillators())).getData()));
        } catch (RuntimeException e) {
            Log.w(TAG, e.getMessage());
            return null;
        }

        try {
            // Wait until all data is loaded
            semaphore.acquire();
        } catch (InterruptedException e) {
            Log.w(TAG, e.getMessage());
            return null;
        }

        return Calls.wrap(response);
    }

    @Override
    public void onBindData(final Metadata metadata, final CombinedPointsOfInterestInfo data) {
        mResponse = data;

        mMapView.getMapAsync(this::showPointsOfInterestInfo);
    }

    private void showPointsOfInterestInfo(final GoogleMap map) {
        map.clear();
        map.setMapType(MapUtils.googleMapType(getContext()));
        if (mResponse != null) {
            addMarkersIfEnabled(map, mResponse.getATMs(), LayersDialog.FLAG_ATM);
            addMarkersIfEnabled(map, mResponse.getGreyhounds(), LayersDialog.FLAG_GREYHOUND);
            addMarkersIfEnabled(map, mResponse.getPhotospheres(), LayersDialog.FLAG_PHOTOSPHERE);
            addMarkersIfEnabled(map, mResponse.getHelplines(), LayersDialog.FLAG_HELPLINE);
            addMarkersIfEnabled(map, mResponse.getLibraries(), LayersDialog.FLAG_LIBRARY);
            addMarkersIfEnabled(map, mResponse.getDefibrillators(), LayersDialog.FLAG_DEFIBRILLATOR);
        }
    }

    private void addMarkersIfEnabled(
            final GoogleMap map,
            final List<? extends BasicPointOfInterest> items,
            final int flag) {

        if ((mFlags & flag) != 0) {
            for (final BasicPointOfInterest item : items) {
                map.addMarker(new MarkerOptions()
                        .position(LocationUtils.getLatLng(item.getLocation()))
                        .icon(BitmapDescriptorFactory.fromResource(getIcon(item)))
                        .title(item.getName()));
            }
        }
    }

    @Override
    protected void onRefreshRequested() {
        hideInfoView();
    }

    @Override
    public void onMapLongClick(final LatLng latLng) {
        onMapClick(latLng);
    }

    @Override
    public void onMapClick(final LatLng latLng) {
        hideInfoView();
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        marker.showInfoWindow();

        final LatLng latLng = marker.getPosition();
        final BasicPointOfInterest poi = matchPointByLocation(mResponse.getAllPointsOfInterest(),
                latLng);
        final String url = getUrl(poi);

        if (url != null) {
            mViewInBrowserBton.setVisibility(View.VISIBLE);
            mViewInBrowserBton.setOnClickListener(v -> IntentUtils.openBrowser(getContext(), url));

        } else {
            mViewInBrowserBton.setVisibility(View.GONE);
            mViewInBrowserBton.setOnClickListener(null);
        }

        return (poi != null);
    }

    @Override
    public String getToolbarTitle() {
        return getString(R.string.title_poi);
    }

    private <T extends BasicPointOfInterest> T matchPointByLocation(
            final Iterable<T> items,
            final LatLng latLng) {
        for (final T poi : items) {
            if (LocationUtils.getLatLng(poi.getLocation()).equals(latLng)) {
                onPointOfInterestInfoRequested(poi);
                return poi;
            }
        }

        return null;
    }

    private void hideInfoView() {
        if (mInfoRoot.getVisibility() == View.VISIBLE) {
            final Animation animOut = AnimationUtils.loadAnimation(getContext(), R.anim.top_out);
            animOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(final Animation animation) {
                }

                @Override
                public void onAnimationRepeat(final Animation animation) {
                }

                @Override
                public void onAnimationEnd(final Animation animation) {
                    if (mInfoRoot != null) {
                        mInfoRoot.setVisibility(View.GONE);
                    }
                    if (getHostActivity() != null) {
                        getHostActivity().getToolbar().setElevation(getToolbarElevationPx());
                    }
                }
            });
            mInfoRoot.startAnimation(animOut);
        }
    }

    private void onPointOfInterestInfoRequested(final BasicPointOfInterest poi) {
        if (mInfoRoot.getVisibility() == View.GONE) {
            final Animation animIn = AnimationUtils.loadAnimation(getContext(), R.anim.top_in);
            mInfoRoot.startAnimation(animIn);
            mInfoRoot.setVisibility(View.VISIBLE);

            getHostActivity().getToolbar().setElevation(0);
        }

        final String title = poi.getName();
        String description = poi.getDescription();
        if (TextUtils.isEmpty(description)) {
            description = poi.getNote();
        }

        ViewUtils.setText(mTitle, title);
        ViewUtils.setText(mDescription, description);
    }

    private void fetchPointOfInterestInfo(
            final Semaphore semaphore,
            final InfoFetcher fetcher) {

        EXECUTOR.execute(() -> {
            try {
                fetcher.fetch();
            } finally {
                semaphore.release();
            }
        });

    }

    private int getIcon(final BasicPointOfInterest poi) {
        if (poi instanceof ATM) {
            final ATM atm = (ATM) poi;
            final String name = atm.getName();
            if ("CIBC".equalsIgnoreCase(name)) {
                return R.drawable.ic_bank_cibc;
            } else if ("BMO".equalsIgnoreCase(name)) {
                return R.drawable.ic_bank_bmo;
            } else if ("TD".equalsIgnoreCase(name)) {
                return R.drawable.ic_bank_td;
            } else if ("RBC".equalsIgnoreCase(name)) {
                return R.drawable.ic_bank_rbc;
            } else if ("Scotiabank".equalsIgnoreCase(name)) {
                return R.drawable.ic_bank_scotiabank;
            } else {
                return R.drawable.ic_local_atm_png;
            }

        } else if (poi instanceof GreyhoundStop) {
            return R.drawable.ic_poi_greyhound;

        } else if (poi instanceof Photosphere) {
            return R.drawable.ic_poi_photosphere;

        } else if (poi instanceof Helpline) {
            return R.drawable.ic_poi_alert;

        } else if (poi instanceof Library) {
            return R.drawable.ic_poi_library_badge;

        } else if (poi instanceof Defibrillator) {
            return R.drawable.ic_poi_defibrillator;

        } else {
            throw new IllegalStateException("Unknown poi instance class: " + poi.getClass().getName());
        }
    }

    private String getUrl(final BasicPointOfInterest poi) {
        if (poi instanceof Photosphere) {
            return ((Photosphere) poi).getUrl();

        } else if (poi instanceof Library) {
            return poi.getNote();

        } else {
            return null;
        }
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        final LatLngBounds bounds = LatLngBounds.builder()
                .include(new LatLng(43.473655, -80.556242))
                .include(new LatLng(43.465495, -80.537446))
                .build();

        final int padding = Px.fromDp(16);

        map.setIndoorEnabled(false);
        map.setBuildingsEnabled(true);
        map.setMapType(MapUtils.googleMapType(getContext()));
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnMapLongClickListener(this);
        map.getUiSettings().setAllGesturesEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

        MapUtils.setLocationEnabled(getActivity(), map);

        if (mResponse != null) {
            showPointsOfInterestInfo(map);
        }
    }

    @Override
    public String getContentType() {
        return ModuleType.POI;
    }

    @Override
    public int getMapViewId() {
        return R.id.points_of_interest_map;
    }

    private interface InfoFetcher {
        void fetch();
    }

}
