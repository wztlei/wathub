package io.github.wztlei.wathub.ui.modules.resources;


import android.animation.LayoutTransition;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.deange.uwaterlooapi.UWaterlooApi;
import com.deange.uwaterlooapi.annotations.ModuleFragment;
import com.deange.uwaterlooapi.model.Metadata;
import com.deange.uwaterlooapi.model.common.Responses;
import com.deange.uwaterlooapi.model.resources.GooseNest;

import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.ui.modules.MapTypeDialog;
import io.github.wztlei.wathub.ui.modules.ModuleType;
import io.github.wztlei.wathub.ui.modules.base.BaseMapFragment;
import io.github.wztlei.wathub.utils.DateUtils;
import io.github.wztlei.wathub.utils.MapUtils;
import io.github.wztlei.wathub.utils.Px;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;

@ModuleFragment(
        path = "/resources/goosewatch",
        layout = R.layout.module_resources_goosewatch
)
public class GooseWatchFragment extends BaseMapFragment<Responses.GooseWatch, GooseNest>
        implements GoogleMap.OnMarkerClickListener, MapTypeDialog.OnMapTypeSelectedListener {

    public static final String TAG = GooseWatchFragment.class.getSimpleName();

    @BindView(R.id.goosewatch_empty_view)
    View mEmptyView;
    @BindView(R.id.goosewatch_nest_info)
    ViewGroup mInfoRoot;
    @BindView(android.R.id.text1)
    TextView mNestDetails;
    @BindView(android.R.id.text2)
    TextView mNestUpdated;

    private List<GooseNest> mResponse;

    @Override
    protected View getContentView(final LayoutInflater inflater, final ViewGroup parent) {
        final View view = inflater.inflate(R.layout.fragment_goosewatch, parent, false);
        ButterKnife.bind(this, view);

        mInfoRoot.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_poi_layers, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.menu_layers) {
            MapTypeDialog mapTypeDialog = new MapTypeDialog(getContext(), this);
            mapTypeDialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Call<Responses.GooseWatch> onLoadData(final UWaterlooApi api) {
        return api.Resources.getGeeseNests();
    }

    @Override
    public void onBindData(final Metadata metadata, final List<GooseNest> data) {
        mResponse = data;
        mMapView.getMapAsync(this::showNests);
    }

    @Override
    public String getContentType() {
        return ModuleType.GOOSEWATCH;
    }

    @Override
    public String getToolbarTitle() {
        return getString(R.string.title_resources_goosewatch);
    }

    @Override
    public void onMapTypeSelected() {
        mMapView.getMapAsync(this::showNests);
    }

    private void showNests(final GoogleMap map) {
        if (mResponse == null || mResponse.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
            return;
        } else {
            mEmptyView.setVisibility(View.GONE);
        }

        map.clear();

        for (final GooseNest nest : mResponse) {
            map.addMarker(
                    new MarkerOptions().position(getLatLng(nest))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_poi_alert))
                            .title(nest.getLocationDescription())
                            .snippet(getString(
                                    R.string.goosewatch_last_updated,
                                    DateUtils.formatDate(getContext(), nest.getUpdatedDate())))
            );
        }

        final LatLngBounds.Builder builder = LatLngBounds.builder();
        for (final GooseNest nest : mResponse) {
            builder.include(getLatLng(nest));
        }
        final LatLngBounds bounds = builder.build();
        final int padding = Px.fromDp(16);

        map.setIndoorEnabled(false);
        map.setBuildingsEnabled(true);
        map.setOnMapClickListener(this);
        map.setMapType(MapUtils.googleMapType(getContext()));
        map.getUiSettings().setAllGesturesEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

        MapUtils.setLocationEnabled(getActivity(), map);
    }

    private LatLng getLatLng(final GooseNest nest) {
        final float[] location = nest.getLocation();
        return new LatLng(location[0], location[1]);
    }

    @Override
    protected void onRefreshRequested() {
        hideNestDetails();
    }

    @Override
    public void onMapClick(final LatLng latLng) {
        hideNestDetails();
    }

    private void hideNestDetails() {
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

    @Override
    public boolean onMarkerClick(final Marker marker) {
        marker.hideInfoWindow();
        final LatLng position = marker.getPosition();

        for (final GooseNest nest : mResponse) {
            final float[] points = nest.getLocation();
            if (position.latitude == points[0] && position.longitude == points[1]) {
                onNestDetailsRequested(nest);
                return true;
            }
        }
        return false;
    }

    private void onNestDetailsRequested(final GooseNest nest) {
        mMapView.getMapAsync(map -> map.animateCamera(CameraUpdateFactory.newLatLng(getLatLng(nest))));

        if (mInfoRoot.getVisibility() == View.GONE) {
            final Animation animIn = AnimationUtils.loadAnimation(getContext(), R.anim.top_in);
            mInfoRoot.startAnimation(animIn);
            mInfoRoot.setVisibility(View.VISIBLE);

            getHostActivity().getToolbar().setElevation(0);
        }

        String title = nest.getLocationDescription();
        if (TextUtils.isEmpty(title)) {
            title = getString(R.string.goosewatch_no_title);
        }
        final String date = getString(
                R.string.goosewatch_last_updated,
                DateUtils.getTimeDifference(getResources(), nest.getUpdatedDate().getTime()).toLowerCase());

        mNestDetails.setText(title);
        mNestUpdated.setText(date);
    }

    @Override
    public int getMapViewId() {
        return R.id.goosewatch_map;
    }
}
