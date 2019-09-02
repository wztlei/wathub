package io.github.wztlei.wathub.ui.modules.resources;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.model.ThirdPartyApp;
import io.github.wztlei.wathub.ui.modules.base.BaseModuleFragment;
import io.github.wztlei.wathub.utils.IntentUtils;

public class ThirdPartyAppFragment extends BaseModuleFragment
        implements SwipeRefreshLayout.OnRefreshListener{
    @BindView(R.id.tp_apps_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.tp_apps_list)
    RecyclerView mThirdPartyAppsList;
    @BindView(R.id.tp_apps_loading_layout)
    ViewGroup mLoadingLayout;

    private Context mContext;
    private ArrayList<ThirdPartyApp> mThirdPartyApps;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
    
    @Override
    public final View onCreateView(
            final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState) {

        // Set up the view
        View root = inflater.inflate(R.layout.fragment_module, container, false);
        ViewGroup parent = root.findViewById(R.id.container_content_view);
        View contentView = inflater.inflate(R.layout.fragment_third_party_apps, parent, false);
        parent.addView(contentView);
        setHasOptionsMenu(true);

        // Initialize instance variables
        ButterKnife.bind(this, contentView);
        fillThirdPartyApps();

        // Attach an adapter onto the third-party app list
        mThirdPartyAppsList.setLayoutManager(new LinearLayoutManager(mContext));
        mThirdPartyAppsList.setAdapter(new ThirdPartyAppAdapter());

        // Set up listeners
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // Prepare the initial view
        showFixedLoadingScreen(mSwipeRefreshLayout, mLoadingLayout,
                null, true);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        // Inflate the menu
        inflater.inflate(R.menu.menu_info, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menu_info) {
            // Creates an alert dialog displaying a disclaimer about the third-party apps
            new AlertDialog.Builder(mContext)
                    .setTitle(getString(R.string.third_party_app_dialog_title))
                    .setMessage(getString(R.string.third_party_app_dialog_message))
                    .setPositiveButton(android.R.string.ok, (dialog1, which) -> {
                    })
                    .create()
                    .show();
            return true;
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onRefresh() {
        showFixedLoadingScreen(mSwipeRefreshLayout, mLoadingLayout,
                null, false);
    }

    @Override
    public String getToolbarTitle() {
        return getString(R.string.title_third_party_apps);
    }

    private void fillThirdPartyApps() {
        mThirdPartyApps = new ArrayList<>();
        mThirdPartyApps.add(ThirdPartyApp.buildApp(
                "WATisRain",
                "An Android app to help you navigate the buildings of UW without going outside.",
                "com.lucky.watisrain",
                R.drawable.watisrain,
                false
        ));
        mThirdPartyApps.add(ThirdPartyApp.buildWebsite(
                "UWFlow",
                "Plan courses. See what your friends are taking. Export your class and exam schedule.",
                "https://uwflow.com/courses",
                R.drawable.uwflow,
                false
        ));
        mThirdPartyApps.add(ThirdPartyApp.buildApp(
                "UWaterloo Portal",
                "Your digital assistant to keep you informed about your important UW info.",
                "ca.uwaterloo.portal",
                R.drawable.uwaterloo_portal,
                true
        ));
        mThirdPartyApps.add(ThirdPartyApp.buildApp(
                "UW Gym Schedule",
                "Retrieve University of Waterloo's open recreation schedule for eight different sports.",
                "com.Ruijie.uwGymSchedule",
                R.drawable.uw_gym_schedule,
                false
        ));
        mThirdPartyApps.add(ThirdPartyApp.buildApp(
                "WarriorRec",
                "Information about schedules and programs in the Physical Activities Complex and Columbia Icefield Facilities.",
                "com.innosoftfusiongo.waterloowarriorrec",
                R.drawable.warriorrec,
                true
        ));
        mThirdPartyApps.add(ThirdPartyApp.buildApp(
                "GRT easyGO",
                "The official GRT application to access to real-time bus information in Waterloo Region.",
                "ca.esolutionsgroup.grteasygo",
                R.drawable.grt_easygo,
                true
        ));
        mThirdPartyApps.add(ThirdPartyApp.buildApp(
                "Waterloo GRT Bus - MonTransit",
                "Bus schedules (available offline), real-time next departures, and the latest GRT news.",
                "org.mtransit.android.ca_grand_river_transit_bus",
                R.drawable.waterloo_grt_bus_montransit,
                false
        ));
        mThirdPartyApps.add(ThirdPartyApp.buildApp(
                "University of Waterloo Food Services",
                "Order ahead at the University of Waterloo using your WatCard or credit card, earn points and get rewarded.",
                "com.hangry.waterloo",
                R.drawable.uw_food_services,
                true
        ));
        mThirdPartyApps.add(ThirdPartyApp.buildApp(
                "MyWaterloo Helper",
                "A hub to view all your courses and to view the weather conditions in Waterloo.",
                "com.yeungalexm.mywaterloo",
                R.drawable.myuwaterloo_helper,
                false
                ));
    }

    private void openThirdPartyApp(ThirdPartyApp thirdPartyApp) {
        if (thirdPartyApp.isApp()) {
            String packageName = thirdPartyApp.getPackageName();
            boolean isAppInstalled = true;

            try {
                PackageManager packageManager = mContext.getPackageManager();
                packageManager.getPackageInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                isAppInstalled = false;
            } catch (Exception e) {
                e.printStackTrace();
                isAppInstalled = false;
            }

            if (isAppInstalled) {
                Intent launchIntent = mContext.getPackageManager()
                        .getLaunchIntentForPackage(packageName);

                // Null pointer check in case package name was not found
                if (launchIntent != null) {
                    startActivity(launchIntent);
                } else if (packageName.equals("org.mtransit.android.ca_grand_river_transit_bus")) {
                    launchIntent = mContext.getPackageManager()
                            .getLaunchIntentForPackage("org.mtransit.android");

                    if (launchIntent != null) {
                        startActivity(launchIntent);
                    }
                }
            } else {
                try {
                    String uriStr = "market://details?id=" + packageName;
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    String uriStr = "https://play.google.com/store/apps/details?id=" + packageName;
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)));
                }
            }
        } else if (thirdPartyApp.isWebsite()) {
            IntentUtils.openBrowser(mContext, thirdPartyApp.getUrl());
        }
    }

    /**
     * A custom RecyclerView Adapter for the list of third-party apps.
     */
    class ThirdPartyAppAdapter extends RecyclerView.Adapter<ThirdPartyAppViewHolder> {
        @NonNull
        @Override
        public ThirdPartyAppViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            // Use layout_schedule_item.xml as the layout for each individual recycler view item
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.list_item_third_party_app, viewGroup, false);
            return new ThirdPartyAppFragment.ThirdPartyAppViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ThirdPartyAppViewHolder viewHolder, int i) {
            // Display the third party app at index i in the recycler view
            ThirdPartyApp thirdPartyApp = mThirdPartyApps.get(i);
            viewHolder.iconImage.setImageResource(thirdPartyApp.getIconId());
            viewHolder.nameText.setText(thirdPartyApp.getName());
            viewHolder.descriptionText.setText(thirdPartyApp.getDescription());
        }

        @Override
        public int getItemCount() {
            return mThirdPartyApps.size();
        }
    }

    /**
     * A custom RecyclerView ViewHolder for an item in the list of third-party apps.
     */
    class ThirdPartyAppViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.third_party_app_card)
        CardView thirdPartyAppCard;
        @BindView(R.id.third_party_app_icon)
        ImageView iconImage;
        @BindView(R.id.third_party_app_name)
        TextView nameText;
        @BindView(R.id.third_party_app_description)
        TextView descriptionText;

        ThirdPartyAppViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            // Clicking the card will open the app, the app in the Google Play store, or the browser
            thirdPartyAppCard.setOnClickListener(view -> {
                int position = getAdapterPosition();

                if (position >= 0) {
                    try {
                        openThirdPartyApp(mThirdPartyApps.get(position));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    
    
}
