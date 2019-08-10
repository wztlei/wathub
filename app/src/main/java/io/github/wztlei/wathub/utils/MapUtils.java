package io.github.wztlei.wathub.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import com.google.android.gms.maps.GoogleMap;

import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

public class MapUtils {

    public static final String LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String DEFAULT_MAP_TYPE = "Satellite";
    public static final int DEFAULT_POI_FLAGS = 0;

    public static void setLocationEnabled(final Activity activity, final GoogleMap map) {
        if (activity == null || map == null) {
            return;
        }

        if (!setLocationIfPossible(activity, map)) {
            Nammu.askForPermission(activity, LOCATION_PERMISSION, new PermissionCallback() {
                @Override
                public void permissionGranted() {
                    setLocationEnabled(activity, map);
                }

                @Override
                public void permissionRefused() {}
            });
        }
    }

    /**
     * Returns the Google Map type from shared preferences or the default map type if none exists.
     *
     * @param   context     the context from which a map type is requested
     * @return              the Google Map type represented by an integer
     */
    public static int googleMapType(Context context) {
        String mapType = context
                .getSharedPreferences(Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                .getString(Constants.MAP_TYPE_KEY, DEFAULT_MAP_TYPE);

        switch (mapType) {
            case "Road Map":
                return GoogleMap.MAP_TYPE_NORMAL;
            case "Hybrid":
                return GoogleMap.MAP_TYPE_HYBRID;
            case "Satellite":
                return GoogleMap.MAP_TYPE_SATELLITE;
            default:
                return GoogleMap.MAP_TYPE_SATELLITE;
        }
    }

    @SuppressLint("MissingPermission")
    private static boolean setLocationIfPossible(final Activity activity, final GoogleMap map) {
        if (hasPermission(activity)) {
            map.setMyLocationEnabled(true);
            return true;
        } else {
            return false;
        }
    }

    private static boolean hasPermission(final Activity activity) {
        return Nammu.hasPermission(activity, LOCATION_PERMISSION);
    }


}
