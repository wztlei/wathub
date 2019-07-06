package io.github.wztlei.wathub.utils;

import com.deange.uwaterlooapi.annotations.ModuleInfo;

import io.github.wztlei.wathub.ui.modules.buildings.BuildingFragment;
import io.github.wztlei.wathub.ui.modules.buildings.ListBuildingsFragment;
import io.github.wztlei.wathub.ui.modules.courses.CoursesFragment;
import io.github.wztlei.wathub.ui.modules.events.EventFragment;
import io.github.wztlei.wathub.ui.modules.events.EventsFragment;
import io.github.wztlei.wathub.ui.modules.foodservices.AnnouncementsFragment;
import io.github.wztlei.wathub.ui.modules.foodservices.LocationsFragment;
import io.github.wztlei.wathub.ui.modules.foodservices.MenusFragment;
import io.github.wztlei.wathub.ui.modules.foodservices.NotesFragment;
import io.github.wztlei.wathub.ui.modules.news.NewsFragment;
import io.github.wztlei.wathub.ui.modules.news.NewsListFragment;
import io.github.wztlei.wathub.ui.modules.parking.ParkingFragment;
import io.github.wztlei.wathub.ui.modules.poi.PointsOfInterestFragment;
import io.github.wztlei.wathub.ui.modules.resources.GooseWatchFragment;
import io.github.wztlei.wathub.ui.modules.resources.SitesFragment;
import io.github.wztlei.wathub.ui.modules.resources.SunshineListFragment;
import io.github.wztlei.wathub.ui.modules.watcard.WatcardBalanceFragment;
import io.github.wztlei.wathub.ui.modules.weather.WeatherFragment;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

public final class ModuleMap {
    private static final Map<String, ModuleInfo> sEndpoints = new HashMap<>();

    static {
        sEndpoints.put("/buildings/*",
                new ModuleInfo(BuildingFragment.class, 0));

        sEndpoints.put("/buildings/list",
                new ModuleInfo(ListBuildingsFragment.class, 2131427414));

        sEndpoints.put("/courses/*",
                new ModuleInfo(CoursesFragment.class, 2131427415));

        sEndpoints.put("/events/*/*",
                new ModuleInfo(EventFragment.class, 0));

        sEndpoints.put("/events",
                new ModuleInfo(EventsFragment.class, 2131427416));

        sEndpoints.put("/foodservices/announcements",
                new ModuleInfo(AnnouncementsFragment.class, 2131427417));

        sEndpoints.put("/foodservices/locations",
                new ModuleInfo(LocationsFragment.class, 2131427418));

        sEndpoints.put("/foodservices/menu",
                new ModuleInfo(MenusFragment.class, 2131427419));

        sEndpoints.put("/foodservices/notes",
                new ModuleInfo(NotesFragment.class, 2131427420));

        sEndpoints.put("/news/*/*",
                new ModuleInfo(NewsFragment.class, 0));

        sEndpoints.put("/news",
                new ModuleInfo(NewsListFragment.class, 2131427422));

        sEndpoints.put("/parking/watpark",
                new ModuleInfo(ParkingFragment.class, 2131427423));

        sEndpoints.put("/poi",
                new ModuleInfo(PointsOfInterestFragment.class, 2131427424));

        sEndpoints.put("/resources/goosewatch",
                new ModuleInfo(GooseWatchFragment.class, 2131427425));

        sEndpoints.put("/resources/sites",
                new ModuleInfo(SitesFragment.class, 2131427426));

        sEndpoints.put("/resources/sunshinelist",
                new ModuleInfo(SunshineListFragment.class, 2131427427));

        sEndpoints.put("/watcard/balance",
                new ModuleInfo(WatcardBalanceFragment.class, 2131427428));

        sEndpoints.put("/weather/current",
                new ModuleInfo(WeatherFragment.class, 2131427429));

    }

    public static ModuleInfo getFragmentInfo(final String endpoint) {
        String path = endpoint;
        path = path.replace(".json", "");
        path = path.replaceAll("\\{[^\\}]*\\}", "*");
        return sEndpoints.get(path);
    }
}
