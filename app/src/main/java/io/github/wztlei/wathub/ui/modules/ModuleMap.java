package io.github.wztlei.wathub.ui.modules;

import com.deange.uwaterlooapi.annotations.ModuleInfo;

import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.ui.modules.buildings.BuildingFragment;
import io.github.wztlei.wathub.ui.modules.buildings.ListBuildingsFragment;
import io.github.wztlei.wathub.ui.modules.courses.CoursesFragment;
import io.github.wztlei.wathub.ui.modules.events.EventFragment;
import io.github.wztlei.wathub.ui.modules.events.EventsFragment;
import io.github.wztlei.wathub.ui.modules.foodservices.AnnouncementsFragment;
import io.github.wztlei.wathub.ui.modules.foodservices.LocationsFragment;
import io.github.wztlei.wathub.ui.modules.foodservices.MenusFragment;
import io.github.wztlei.wathub.ui.modules.foodservices.NotesFragment;
import io.github.wztlei.wathub.ui.modules.importantdates.ImportantDatesFragment;
import io.github.wztlei.wathub.ui.modules.news.NewsFragment;
import io.github.wztlei.wathub.ui.modules.news.NewsListFragment;
import io.github.wztlei.wathub.ui.modules.openclassroom.OpenClassroomFragment;
import io.github.wztlei.wathub.ui.modules.parking.ParkingFragment;
import io.github.wztlei.wathub.ui.modules.poi.PointsOfInterestFragment;
import io.github.wztlei.wathub.ui.modules.resources.RedditFragment;
import io.github.wztlei.wathub.ui.modules.resources.GooseWatchFragment;
import io.github.wztlei.wathub.ui.modules.resources.SitesFragment;
import io.github.wztlei.wathub.ui.modules.resources.SunshineListFragment;
import io.github.wztlei.wathub.ui.modules.watcard.WatcardBalanceFragment;
import io.github.wztlei.wathub.ui.modules.weather.WeatherFragment;

import java.lang.String;
import java.util.HashMap;
import java.util.Map;

final class ModuleMap {
    private static final Map<String, ModuleInfo> sEndpoints = new HashMap<>();

    static {
        sEndpoints.put("/buildings/*",
                new ModuleInfo(BuildingFragment.class, 0));

        sEndpoints.put("/buildings/list",
                new ModuleInfo(ListBuildingsFragment.class, R.layout.module_buildings));

        sEndpoints.put("/courses/*",
                new ModuleInfo(CoursesFragment.class, R.layout.module_courses));

        sEndpoints.put("/events/*/*",
                new ModuleInfo(EventFragment.class, 0));

        sEndpoints.put("/events",
                new ModuleInfo(EventsFragment.class, R.layout.module_events));

        sEndpoints.put("/foodservices/announcements",
                new ModuleInfo(AnnouncementsFragment.class, R.layout.module_foodservices_announcements));

        sEndpoints.put("/foodservices/locations",
                new ModuleInfo(LocationsFragment.class, R.layout.module_foodservices_locations));

        sEndpoints.put("/foodservices/menu",
                new ModuleInfo(MenusFragment.class, R.layout.module_foodservices_menus));

        sEndpoints.put("/foodservices/notes",
                new ModuleInfo(NotesFragment.class, R.layout.module_foodservices_notes));

        sEndpoints.put("/news/*/*",
                new ModuleInfo(NewsFragment.class, 0));

        sEndpoints.put("/news",
                new ModuleInfo(NewsListFragment.class, R.layout.module_news));

        sEndpoints.put("/terms/*/importantdates",
                new ModuleInfo(ImportantDatesFragment.class, R.layout.module_importantdates));

        sEndpoints.put("/parking/watpark",
                new ModuleInfo(ParkingFragment.class, R.layout.module_parking));

        sEndpoints.put("/poi",
                new ModuleInfo(PointsOfInterestFragment.class, R.layout.module_poi));

        sEndpoints.put("/resources/goosewatch",
                new ModuleInfo(GooseWatchFragment.class, R.layout.module_resources_goosewatch));

        sEndpoints.put("/resources/sites",
                new ModuleInfo(SitesFragment.class, R.layout.module_resources_sites));

        sEndpoints.put("/resources/sunshinelist",
                new ModuleInfo(SunshineListFragment.class, R.layout.module_resources_sunshine));

        sEndpoints.put("/watcard/balance",
                new ModuleInfo(WatcardBalanceFragment.class, R.layout.module_watcard_balance));

        sEndpoints.put("/weather/current",
                new ModuleInfo(WeatherFragment.class, R.layout.module_weather));

        sEndpoints.put("/wathub/openclassrooms",
                new ModuleInfo(OpenClassroomFragment.class, R.layout.module_open_classrooms));

        sEndpoints.put("/wathub/reddit",
                new ModuleInfo(RedditFragment.class, R.layout.module_reddit));
    }

    @SuppressWarnings("RegExpRedundantEscape")
    static ModuleInfo getFragmentInfo(final String endpoint) {
        String path = endpoint;
        path = path.replace(".json", "");
        path = path.replaceAll("\\{[^\\}]*\\}", "*");
        return sEndpoints.get(path);
    }
}
