package io.github.wztlei.wathub.ui.modules.buildings;


import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.deange.uwaterlooapi.annotations.ModuleFragment;
import com.deange.uwaterlooapi.model.Metadata;
import com.deange.uwaterlooapi.model.buildings.Building;
import com.deange.uwaterlooapi.model.common.Responses;

import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.ui.MapActivity;
import io.github.wztlei.wathub.ui.modules.MapTypeDialog;
import io.github.wztlei.wathub.ui.modules.ModuleType;
import io.github.wztlei.wathub.ui.modules.base.BaseApiMapFragment;
import io.github.wztlei.wathub.utils.MapUtils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;

@ModuleFragment(path = "/buildings/*")
public class BuildingFragment extends BaseApiMapFragment<Responses.BuildingEntity, Building>
        implements MapTypeDialog.OnMapTypeSelectedListener {

    @BindView(R.id.building_empty_view)
    View mEmptyView;
    @BindView(R.id.building_name)
    TextView mNameView;

    private Building mBuilding;

    @Override
    protected View getContentView(final LayoutInflater inflater, final ViewGroup parent) {
        final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_building, parent, false);

        ButterKnife.bind(this, root);

        return root;
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
    public float getToolbarElevationPx() {
        return 0;
    }

    @Override
    public Building onLoadData() {
        return getModel();
    }

    @Override
    public void onBindData(final Metadata metadata, final Building data) {
        mBuilding = data;

        mNameView.setText(data.getBuildingName());

        mMapView.getMapAsync(this::showLocation);
    }

    @Override
    public String getContentType() {
        return ModuleType.BUILDING;
    }

    @Override
    public String getToolbarTitle() {
        if (mBuilding != null) {
            return mBuilding.getBuildingCode();
        } else {
            return "";
        }
    }

    @Override
    public void onMapTypeSelected() {
        mMapView.getMapAsync(this::showLocation);
    }

    private void showLocation(final GoogleMap map) {
        if (mBuilding == null) {
            return;
        }

        final float[] location = mBuilding.getLocation();

        if (location[0] == 0 && location[1] == 0) {
            mMapView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);

        } else {
            mMapView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);

            final LatLng buildingLocation = new LatLng(location[0], location[1]);

            map.clear();
            map.setIndoorEnabled(false);
            map.setBuildingsEnabled(true);
            map.setOnMapClickListener(this);
            map.setMapType(MapUtils.googleMapType(getContext()));
            map.getUiSettings().setAllGesturesEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(buildingLocation, 18));
            map.addMarker(new MarkerOptions().position(buildingLocation));
        }
    }

    @Override
    public int getMapViewId() {
        return R.id.building_map;
    }

    @Override
    public void onMapClick(final LatLng latLng) {
        startActivity(MapActivity.getMapActivityIntent(getActivity(), mBuilding));
    }
}
