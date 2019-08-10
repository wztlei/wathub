package io.github.wztlei.wathub.ui.modules;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.google.android.gms.maps.GoogleMap;

import java.util.Objects;

import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.utils.MapUtils;
import io.github.wztlei.wathub.utils.Px;

public class MapTypeDialog {

    public interface OnMapTypeSelectedListener {
        void onMapTypeSelected();
    }

    private SharedPreferences mSharedPreferences;
    private RadioGroup mMapTypeRadioGroup;
    private AlertDialog mDialog;

    /**
     * Displays a dialog containing a radio button group to select a map type.
     *
     * @param context   the context that creates the dialog
     * @param listener  the listener that receives a callback for when the OK button is selected
     */
    public MapTypeDialog(final Context context,
                         final MapTypeDialog.OnMapTypeSelectedListener listener) {
        @SuppressLint("InflateParams")
        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_map_type, null);

        // Get the shared preferences editor, radio button group, and the map type
        mSharedPreferences = context.getSharedPreferences(
                Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        mMapTypeRadioGroup = view.findViewById(R.id.map_type_radio_group);
        int mapType = MapUtils.googleMapType(context);

        // Select the desired map type
        switch (mapType) {
            case GoogleMap.MAP_TYPE_SATELLITE:
                mMapTypeRadioGroup.check(R.id.satellite_radio_button);
                break;
            case GoogleMap.MAP_TYPE_HYBRID:
                mMapTypeRadioGroup.check(R.id.hybrid_radio_button);
                break;
            case GoogleMap.MAP_TYPE_NORMAL:
                mMapTypeRadioGroup.check(R.id.road_map_radio_button);
                break;
            default:
                mMapTypeRadioGroup.check(R.id.satellite_radio_button);
                break;
        }

        // Create an alert dialog
        mDialog = new AlertDialog.Builder(context)
                .setView(view)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog1, which) -> {
                    saveMapType();

                    if (listener != null) {
                        listener.onMapTypeSelected();
                    }
                })
                .create();
    }

    /**
     * Displays the map type alert dialog.
     */
    public void show() {
        Objects.requireNonNull(mDialog.getWindow()).getDecorView();
        mDialog.getWindow().setLayout((int) (Px.width() * 0.75f), ViewGroup.LayoutParams.WRAP_CONTENT);
        mDialog.show();
    }

    /**
     * Saves the map type as a string in shared preferences.
     */
    private void saveMapType() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        switch (mMapTypeRadioGroup.getCheckedRadioButtonId()) {
            case R.id.satellite_radio_button:
                editor.putString(Constants.MAP_TYPE_KEY,"Satellite");
                break;
            case (R.id.hybrid_radio_button):
                editor.putString(Constants.MAP_TYPE_KEY, "Hybrid");
                break;
            case (R.id.road_map_radio_button):
                editor.putString(Constants.MAP_TYPE_KEY, "Road Map");
                break;
        }

        editor.apply();
    }
}
