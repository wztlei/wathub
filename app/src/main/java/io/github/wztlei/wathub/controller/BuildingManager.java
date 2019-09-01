package io.github.wztlei.wathub.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.deange.uwaterlooapi.UWaterlooApi;
import com.deange.uwaterlooapi.model.buildings.Building;
import com.deange.uwaterlooapi.model.common.Responses;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.github.wztlei.wathub.ApiKeys;
import io.github.wztlei.wathub.Constants;
import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.net.Calls;

public class BuildingManager {
    private static BuildingManager sInstance;
    private static SharedPreferences sSharedPreferences;
    private static JSONObject sBuildingCodesMap;

    /**
     * Initializes the static instance of a BuildingManager.
     *
     * @param context the context in which the BuildingManager is initialized
     */
    public static void init(Context context) {
        if (sInstance != null) {
            throw new IllegalStateException("BuildingManager already instantiated!");
        }
        sInstance = new BuildingManager(context);
    }

    /**
     * Returns the static instance of a BuildingManager.
     *
     * @return the static instance of a BuildingManager.
     */
    public static BuildingManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("BuildingManager not instantiated!");
        }
        return sInstance;
    }

    /**
     * Constructor for a BuildingManager object.
     *
     * @param context the context in which the BuildingManager is created
     */
    private BuildingManager(Context context) {
        sSharedPreferences = context.getSharedPreferences(
                Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        String json = sSharedPreferences.getString(Constants.BUILDINGS_JSON_KEY, null);

        // Determine if a buildings json object was already stored in shared preferences
        if (json != null) {
            try {
                // Use the json object in shared preferences
                sBuildingCodesMap = new JSONObject(json);
            } catch (JSONException e) {
                sBuildingCodesMap = new JSONObject();
                e.printStackTrace();
            }
        } else {
            try {
                // Use the json file from res/raw
                readBuildingsResourceJson(context);
            } catch (IOException | JSONException e) {
                sBuildingCodesMap = new JSONObject();
                e.printStackTrace();
            }
        }

        // Use the API to load the updated buildings list
        new LoadBuildingsListTask().execute();
    }

    /**
     * Returns the building's full name based on a building code.
     *
     * @param   buildingCode    the code of  the building
     * @return                  the full name of the building
     */
    public String getBuildingFullName(String buildingCode) {
        try {
            // Special cases where a custom full names is more useful than the official full name
            switch (buildingCode) {
                case "AHS": return "Applied Health Sciences Expansion";
                case "E8":  return "Engineering 8";
                case "E9":  return "Engineering 9";
                case "E10": return "Engineering 10";
                case "SJ1": return "St. Jerome's University - 1";
                case "SJ2": return "St. Jerome's University - 2";
                case "SJ3": return "St. Jerome's University - 3";
                case "SJ4": return "St. Jerome's University - 4";
                case "SJ5": return "St. Jerome's University - 5";
            }

            // Use sBuildingCodesMap to determine the building's full name if possible
            if (sBuildingCodesMap.has(buildingCode)) {
                return sBuildingCodesMap.getString(buildingCode);
            } else {
                return buildingCode;
            }
        } catch (Exception e) {
            return buildingCode;
        }
    }

    /**
     * Reads the building JSON file from res/raw to create a map
     * associating building codes to full building names.
     *
     * @param context the context in which the BuildingManager is created
     */
    private static void readBuildingsResourceJson(Context context)
            throws JSONException, IOException {
        // Open and read the buildings list json file
        InputStream is = context.getResources().openRawResource(R.raw.buildings_list);
        byte[] buffer = new byte[is.available()];
        // noinspection ResultOfMethodCallIgnored
        is.read(buffer);
        is.close();

        // Get the data for all of the buildings
        JSONObject buildingsResponse = new JSONObject(new String(buffer));
        JSONArray buildingListData = buildingsResponse.getJSONArray("data");
        JSONObject newBuildingCodesMap = new JSONObject();

        // Iterate through every building in the list of buildings
        for (int i = 0; i < buildingListData.length(); i++) {
            // Get the code and the full name of the building
            JSONObject building = buildingListData.getJSONObject(i);
            String buildingCode = building.getString("building_code");
            String newBuildingName = building.getString("building_name");

            // Determine if the building code is in the new map
            if (newBuildingCodesMap.has(buildingCode)) {
                String buildingNameInMap = newBuildingCodesMap.getString(buildingCode);

                // Put the new building name in the map if the new name is shorter
                if (newBuildingName.length() < buildingNameInMap.length()) {
                    newBuildingCodesMap.put(buildingCode, newBuildingName);
                }
            } else {
                newBuildingCodesMap.put(buildingCode, newBuildingName);
            }
        }

        sBuildingCodesMap = newBuildingCodesMap;
    }

    /**
     * Updates a JSON object storing the association
     *
     * @param   buildings       a list of buildings containing data about building codes and names
     */
    private static void updateBuildingCodesMap(List<Building> buildings) throws JSONException {
        // Create a new JSON object
        JSONObject newBuildingCodesMap = new JSONObject();

        // Iterate through each building
        for (Building building : buildings) {
            // Get the building code and the building name
            String buildingCode = building.getBuildingCode();
            String newBuildingName = building.getBuildingName();

            // Determine if the building codes map already has the building code
            if (newBuildingCodesMap.has(buildingCode)) {
                String buildingNameInMap = newBuildingCodesMap.getString(buildingCode);

                // Only update the building's full name if the new name is shorter
                if (newBuildingName.length() < buildingNameInMap.length()) {
                    newBuildingCodesMap.put(buildingCode, newBuildingName);
                }
            } else {
                // Add a new building to the map
                newBuildingCodesMap.put(buildingCode, newBuildingName);
            }
        }

        // Update the map associating building codes with their names
        sBuildingCodesMap = newBuildingCodesMap;
    }

    /**
     * An AsyncTask to update sBuildingCodesMap by using the UWaterloo Open Data API
     * to retrieve a list of data about UWaterloo buildings.
     */
    private static class LoadBuildingsListTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            // Use the UWaterloo API to get the data for the list of buildings
            UWaterlooApi api = new UWaterlooApi(ApiKeys.UWATERLOO_API_KEY);
            Responses.Buildings response = Calls.unwrap(api.Buildings.getBuildings());

            if (response != null) {
                try {
                    // Update sBuildingCodesMap using the response which is a list of buildings
                    updateBuildingCodesMap(response.getData());

                    // Put the updated data in shared preferences for future recall
                    SharedPreferences.Editor editor = sSharedPreferences.edit();
                    editor.putString(Constants.BUILDINGS_JSON_KEY, sBuildingCodesMap.toString());
                    editor.apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Void data) {}
    }
}
