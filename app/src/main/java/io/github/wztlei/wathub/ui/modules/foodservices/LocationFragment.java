package io.github.wztlei.wathub.ui.modules.foodservices;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.deange.uwaterlooapi.model.BaseResponse;
import com.deange.uwaterlooapi.model.Metadata;
import com.deange.uwaterlooapi.model.foodservices.Location;
import com.deange.uwaterlooapi.model.foodservices.OperatingHours;

import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.ui.modules.ModuleType;
import io.github.wztlei.wathub.ui.modules.base.BaseApiMapFragment;
import io.github.wztlei.wathub.ui.view.OperatingHoursView;
import io.github.wztlei.wathub.utils.IntentUtils;
import io.github.wztlei.wathub.utils.Joiner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;

public class LocationFragment extends BaseApiMapFragment<BaseResponse, Location>
        implements OnMapReadyCallback {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.list_location_collapsing_toolbar)
    CollapsingToolbarLayout mToolbarLayout;
    @BindView(R.id.list_location_description)
    TextView mDescriptionView;
    @BindView(R.id.list_location_open_now)
    TextView mOpenNowView;
    @BindView(R.id.list_location_hours)
    OperatingHoursView mWeekHoursView;
    @BindView(R.id.list_location_closed_days)
    TextView mClosedDays;
    @BindView(R.id.list_location_special_hours)
    OperatingHoursView mSpecialHoursView;

    @Override
    protected View getContentView(final LayoutInflater inflater, final ViewGroup parent) {
        final View view = inflater.inflate(R.layout.fragment_foodservices_location, parent, false);

        ButterKnife.bind(this, view);

        getHostActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
        getHostActivity().getToolbar().setVisibility(View.GONE);
        getHostActivity().setSupportActionBar(mToolbar);

        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMapView.getMapAsync(this);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_location_map, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.menu_maps) {
            openLocationInMaps();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Location onLoadData() {
        return getModel();
    }

    @Override
    public void onBindData(final Metadata metadata, final Location location) {
        mToolbarLayout.setTitle(getLocationTitle());


        final String description = location.getDescription();
        if (!TextUtils.isEmpty(description)) {
            mDescriptionView.setText(Html.fromHtml(description));
        }

        final Context context = getContext();
        if (location.isOpenNow()) {
            mOpenNowView.setTextColor(
                    ContextCompat.getColor(context, R.color.foodservices_location_open));
            mOpenNowView.setText(R.string.foodservices_location_open_now);
        } else {
            mOpenNowView.setTextColor(
                    ContextCompat.getColor(context, R.color.foodservices_location_closed));
            mOpenNowView.setText(R.string.foodservices_location_closed_now);
        }

        mWeekHoursView.setMode(OperatingHoursView.MODE_DAYS_OF_WEEK);
        mWeekHoursView.setHours(location.getHours());

        final List<Location.Range> datesClosed = location.getDatesClosed();
        final List<Location.SpecialRange> specialHours = location.getSpecialOperatingHours();

        showSection(mClosedDays, !datesClosed.isEmpty());
        showSection(mSpecialHoursView, !specialHours.isEmpty());
        mClosedDays.setText(Joiner.on("\n").joinObjects(datesClosed));

        final Map<String, OperatingHours> hoursMap = new LinkedHashMap<>();
        for (final Location.SpecialRange specialRange : specialHours) {
            hoursMap.put(
                    specialRange.formatDate(),
                    OperatingHours.create(specialRange.getOpen(), specialRange.getClose(), false));
        }
        mSpecialHoursView.setMode(OperatingHoursView.MODE_MANUAL);
        mSpecialHoursView.setHours(hoursMap);

        // Reset bold status of all fields
        mWeekHoursView.unbold();
        mSpecialHoursView.unbold();
        final CharSequence text = mClosedDays.getText();
        if (text instanceof Spannable) {
            final Spannable spannable = ((Spannable) text);
            for (final Object span : spannable.getSpans(0, spannable.length() - 1,
                    CalligraphyTypefaceSpan.class)) {
                spannable.removeSpan(span);
            }
        }

        // Handle bold status of the current time range (if any)
        final Date now = new Date();

        for (int i = 0; i < datesClosed.size(); i++) {
            if (datesClosed.get(i).contains(now)) {
                boldClosedDay(i);
                break;
            }
        }

        for (final Location.SpecialRange specialRange : specialHours) {
            if (specialRange.contains(now)) {
                mSpecialHoursView.bold(specialRange.formatDate());
                break;
            }
        }

        // Fallback to default bold if closed/special cases do not apply
        mWeekHoursView.setTodayBold();
    }

    private void boldClosedDay(final int field) {
        final String text = mClosedDays.getText().toString();

        int start = 0;
        for (int i = 0; i < field; i++) {
            start = text.indexOf('\n', start + 1);
            if (start == -1) {
                // Run out of fields!
                return;
            }
        }

        int end = text.indexOf('\n', start + 1);
        if (end == -1) {
            end = text.length();
        }

        final SpannableString ss = new SpannableString(text);
        ss.setSpan(null, start, end, 0);
        mClosedDays.setText(ss);
    }

    private void showSection(final View view, final boolean show) {

        final int visibility = show ? View.VISIBLE : View.GONE;
        final ViewGroup parent = (ViewGroup) view.getParent();
        final ViewGroup mainParent = (ViewGroup) parent.getParent();
        final int parentIndex = mainParent.indexOfChild(parent);
        final int dividerIndex = parentIndex - 1;

        mainParent.getChildAt(parentIndex).setVisibility(visibility);
        mainParent.getChildAt(dividerIndex).setVisibility(visibility);
    }

    @Override
    public void onMapClick(final LatLng latLng) {
        // Nothing to do here
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        final Location location = getModel();
        final float[] coordinates = location.getLocation();
        final LatLng latLng = new LatLng(coordinates[0], coordinates[1]);

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setIndoorEnabled(false);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        map.getUiSettings().setAllGesturesEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(false);

        map.addMarker(
                new MarkerOptions().icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .position(latLng));
    }

    private void openLocationInMaps() {
        final Location location = getModel();
        final String uri = IntentUtils.makeGeoIntentString(location.getLocation(), getLocationTitle());

        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        final Context context = getActivity();
        if (IntentUtils.isIntentSupported(context, intent)) {
            startActivity(intent);
        }
    }

    private String getLocationTitle() {
        final Location location = getModel();
        return location.getName().split(" - ")[0];
    }

    @Override
    public String getContentType() {
        return ModuleType.LOCATION;
    }

    @Override
    public int getMapViewId() {
        return R.id.list_location_map_view;
    }
}
