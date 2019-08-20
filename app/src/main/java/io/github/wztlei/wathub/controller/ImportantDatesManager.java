package io.github.wztlei.wathub.controller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.deange.uwaterlooapi.UWaterlooApi;
import com.deange.uwaterlooapi.model.common.Responses;
import com.deange.uwaterlooapi.model.courses.Class;
import com.deange.uwaterlooapi.model.courses.ClassDate;
import com.deange.uwaterlooapi.model.courses.CourseSchedule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import io.github.wztlei.wathub.ApiKeys;
import io.github.wztlei.wathub.Constants;
import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.model.ImportantDatesDisplay;
import io.github.wztlei.wathub.model.ImportantDatesList;
import io.github.wztlei.wathub.net.Calls;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImportantDatesManager {

    private static Thread sApiRetrievalThread;
    private static final int DEFAULT_MAX_NETWORK_FAILURES = 3;

    private static String[] sTerms;



    private ImportantDatesManager(Context context) {
        sApiRetrievalThread = null;
    }


    /**
     *
     * @return a list of terms that can be viewed
     */
    public String[] getTerms() {
        return sTerms == null ? new String[0] : sTerms;
    }

    /**
     *
     */
    public void refreshDatesAsync() {
        refreshDatesAsync(null, DEFAULT_MAX_NETWORK_FAILURES,
                false, true);
    }

    /**
     * Retrieves the important dates
     */

    private void refreshDatesAsync(Activity activity, int maxFailures,
                                   boolean showFailureToast, boolean useApi) {
        try{
            // Create a request using the OkHttpClient library
            OkHttpClient okHttpClient = new OkHttpClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a list of the important dates
     */
    /*
    public ImportantDatesList findImportantDates(int term_id, boolean fromToday) {
        try {
            // set the JSONObjects
                ImportantDatesList returnedDates = new ImportantDatesList();

                // iterate through each important date

                return returnedDates;
            } catch(JSONException e){
                e.printStackTrace();
                return null;
            }
    }
    */
}
