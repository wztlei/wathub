package io.github.wztlei.wathub.ui.modules;

import com.deange.uwaterlooapi.annotations.ModuleInfo;

import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.ui.modules.buildings.BuildingFragmentApi;
import io.github.wztlei.wathub.ui.modules.buildings.ListBuildingsFragmentApi;
import io.github.wztlei.wathub.ui.modules.courses.CoursesFragmentApi;
import io.github.wztlei.wathub.ui.modules.events.EventFragmentApi;
import io.github.wztlei.wathub.ui.modules.events.EventsFragmentApi;
import io.github.wztlei.wathub.ui.modules.foodservices.AnnouncementsFragmentApi;
import io.github.wztlei.wathub.ui.modules.foodservices.LocationsFragmentApi;
import io.github.wztlei.wathub.ui.modules.foodservices.MenusFragmentApi;
import io.github.wztlei.wathub.ui.modules.foodservices.NotesFragmentApi;
import io.github.wztlei.wathub.ui.modules.news.NewsFragmentApi;
import io.github.wztlei.wathub.ui.modules.news.NewsListFragmentApi;
import io.github.wztlei.wathub.ui.modules.openclassroom.OpenClassroomFragment;
import io.github.wztlei.wathub.ui.modules.parking.ParkingFragmentApi;
import io.github.wztlei.wathub.ui.modules.poi.PointsOfInterestFragmentApi;
import io.github.wztlei.wathub.ui.modules.resources.GooseWatchFragmentApi;
import io.github.wztlei.wathub.ui.modules.resources.SitesFragmentApi;
import io.github.wztlei.wathub.ui.modules.resources.SunshineListFragmentApi;
import io.github.wztlei.wathub.ui.modules.watcard.WatcardBalanceFragmentApi;
import io.github.wztlei.wathub.ui.modules.weather.WeatherFragmentApi;

import java.lang.String;
import java.util.HashMap;
import java.util.Map;

final class ModuleMap {
    private static final Map<String, ModuleInfo> sEndpoints = new HashMap<>();

    static {
        sEndpoints.put("/buildings/*",
                new ModuleInfo(BuildingFragmentApi.class, 0));

        sEndpoints.put("/buildings/list",
                new ModuleInfo(ListBuildingsFragmentApi.class, R.layout.module_buildings));

        sEndpoints.put("/courses/*",
                new ModuleInfo(CoursesFragmentApi.class, R.layout.module_courses));

        sEndpoints.put("/events/*/*",
                new ModuleInfo(EventFragmentApi.class, 0));

        sEndpoints.put("/events",
                new ModuleInfo(EventsFragmentApi.class, R.layout.module_events));

        sEndpoints.put("/foodservices/announcements",
                new ModuleInfo(AnnouncementsFragmentApi.class, R.layout.module_foodservices_announcements));

        sEndpoints.put("/foodservices/locations",
                new ModuleInfo(LocationsFragmentApi.class, R.layout.module_foodservices_locations));

        sEndpoints.put("/foodservices/menu",
                new ModuleInfo(MenusFragmentApi.class, R.layout.module_foodservices_menus));

        sEndpoints.put("/foodservices/notes",
                new ModuleInfo(NotesFragmentApi.class, R.layout.module_foodservices_notes));

        sEndpoints.put("/news/*/*",
                new ModuleInfo(NewsFragmentApi.class, 0));

        sEndpoints.put("/news",
                new ModuleInfo(NewsListFragmentApi.class, R.layout.module_news));

        sEndpoints.put("/parking/watpark",
                new ModuleInfo(ParkingFragmentApi.class, R.layout.module_parking));

        sEndpoints.put("/poi",
                new ModuleInfo(PointsOfInterestFragmentApi.class, R.layout.module_poi));

        sEndpoints.put("/resources/goosewatch",
                new ModuleInfo(GooseWatchFragmentApi.class, R.layout.module_resources_goosewatch));

        sEndpoints.put("/resources/sites",
                new ModuleInfo(SitesFragmentApi.class, R.layout.module_resources_sites));

        sEndpoints.put("/resources/sunshinelist",
                new ModuleInfo(SunshineListFragmentApi.class, R.layout.module_resources_sunshine));

        sEndpoints.put("/watcard/balance",
                new ModuleInfo(WatcardBalanceFragmentApi.class, R.layout.module_watcard_balance));

        sEndpoints.put("/weather/current",
                new ModuleInfo(WeatherFragmentApi.class, R.layout.module_weather));

        sEndpoints.put("/wathub/openclassrooms",
                new ModuleInfo(OpenClassroomFragment.class, R.layout.module_open_classrooms));
    }

    public static ModuleInfo getFragmentInfo(final String endpoint) {
        String path = endpoint;
        path = path.replace(".json", "");
        path = path.replaceAll("\\{[^\\}]*\\}", "*");
        return sEndpoints.get(path);
    }
}
