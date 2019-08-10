package io.github.wztlei.wathub.ui.modules.home;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.deange.uwaterlooapi.model.foodservices.Location;

import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.ui.modules.ModuleHostActivity;
import io.github.wztlei.wathub.ui.modules.base.BaseApiModuleFragment;
import io.github.wztlei.wathub.ui.modules.foodservices.LocationFragment;
import io.github.wztlei.wathub.utils.MathUtils;

import java.util.List;
import java.util.Locale;

public class NearbyLocationsAdapter extends ArrayAdapter<Location> implements View.OnClickListener {

    private final float[] mDistanceHolder = new float[1];
    private float[] mCurrentLocation;

    NearbyLocationsAdapter(
            final Context context,
            final List<Location> locations,
            final android.location.Location currentLocation) {
        super(context, 0, locations);

        if (currentLocation != null) {
            mCurrentLocation = new float[]{
                    (float) currentLocation.getLatitude(),
                    (float) currentLocation.getLongitude(),
            };
        }
    }

    void updateCurrentLocation(final android.location.Location currentLocation) {
        if (currentLocation != null) {
            mCurrentLocation = new float[]{
                    (float) currentLocation.getLatitude(),
                    (float) currentLocation.getLongitude(),
            };

        } else {
            mCurrentLocation = null;
        }

        notifyDataSetChanged();
    }

    void updateLocations(final List<Location> locations) {
        clear();
        addAll(locations);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(
            final int position,
            final View convertView,
            @NonNull final ViewGroup parent) {
        final View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_nearby_location, parent,
                    false);
        }

        final Location location = getItem(position);

        if (location != null) {
            ((TextView) view.findViewById(R.id.nearby_location_title)).setText(location.getName());
            ((TextView) view.findViewById(R.id.nearby_location_distance)).setText(formatDistance(location));

            view.setOnClickListener(this);
            view.setTag(position);
        }


        return view;
    }

    private String formatDistance(final Location location) {
        if (mCurrentLocation == null) {
            return getContext().getString(R.string.nearby_locations_waiting);
        }

        final float[] coordinates = location.getLocation();
        android.location.Location.distanceBetween(
                mCurrentLocation[0], mCurrentLocation[1], coordinates[0], coordinates[1], mDistanceHolder);
        float distance = mDistanceHolder[0];

        String suffix = "m";
        if (distance > 1000) {
            suffix = "km";
            distance /= 1000f;
        }

        return MathUtils.formatFloat(String.format((Locale) null, "%.1f", distance)) + " " + suffix;
    }

    @Override
    public void onClick(final View v) {
        final int position = (int) v.getTag();
        final Location location = getItem(position);

        // TODO BUG #1: Potential cause of Android 7/Nougat TransactionTooLargeException
        getContext().startActivity(ModuleHostActivity.getStartIntent(
                getContext(),
                LocationFragment.class.getCanonicalName(),
                BaseApiModuleFragment.newBundle(location)));
    }
}
