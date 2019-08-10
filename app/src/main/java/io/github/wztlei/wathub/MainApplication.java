package io.github.wztlei.wathub;


import android.support.multidex.MultiDexApplication;
import android.util.Log;

import io.github.wztlei.wathub.controller.BuildingManager;
import io.github.wztlei.wathub.controller.EncryptionController;
import io.github.wztlei.wathub.controller.TermManager;
import io.github.wztlei.wathub.controller.WatcardManager;
import io.github.wztlei.wathub.controller.RoomScheduleManager;
import io.github.wztlei.wathub.utils.FontUtils;
import io.github.wztlei.wathub.controller.NetworkController;
import io.github.wztlei.wathub.utils.Px;

import net.danlew.android.joda.JodaTimeAndroid;

import pl.tajchert.nammu.Nammu;

public class MainApplication extends MultiDexApplication {

    @SuppressWarnings("unused")
    private static final String TAG = "WL/MainApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // Set up connectivity manager
        NetworkController.init(this);

        // Permissions library
        Nammu.init(this);

        // Set up dp-px converter
        Px.init(this);

        // Set up Calligraphy library
        FontUtils.init(this);

        // Joda Time config
        JodaTimeAndroid.init(this);

        // KeyStore loader
        EncryptionController.init(this);

        // Watcard manager
        WatcardManager.init(this);

        // Term manager
        TermManager.init(this);

        // Building manager
        BuildingManager.init(this);

        // Room schedule manager
        RoomScheduleManager.init(this);
        RoomScheduleManager.getInstance().refreshRoomScheduleAsync();
    }
}
