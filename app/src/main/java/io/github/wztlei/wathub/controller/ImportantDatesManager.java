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
import io.github.wztlei.wathub.model.RoomTimeInterval;
import io.github.wztlei.wathub.model.RoomTimeIntervalList;
import io.github.wztlei.wathub.net.Calls;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImportantDatesManager {

    private static String[] sTerms;


    /**
     *
     * @return a list of terms that can be viewed
     */
    public String[] getTerms() {
        return sTerms == null ? new String[0] : sTerms;
    }
}
