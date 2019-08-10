package io.github.wztlei.wathub.ui.modules.importantdates;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Calendar;
import io.github.wztlei.wathub.Constants;
import io.github.wztlei.wathub.R;

import butterknife.BindView;
import io.github.wztlei.wathub.controller.ImportantDatesManager;
import io.github.wztlei.wathub.ui.StringAdapter;
import io.github.wztlei.wathub.ui.modules.base.BaseModuleFragment;

public class ImportantDatesFragment extends BaseModuleFragment {

    @BindView(R.id.term_important_dates_spinner)
    Spinner mTermSpinner;
    // @BindView(r.id.somebuttom clickable)
    // button
    @BindView(R.id.important_dates_layout)
    ViewGroup mImportantDatesLayout;

    private ImportantDatesManager mImportantDatesManager;
    private Context mContext;
    private MenuItem mRefreshMenuItem;


    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater,
                                   final ViewGroup container,
                                   final Bundle savedInstanceState) {

        // Set up the view
        View root = inflater.inflate(R.layout.fragment_module, container, false);
        ViewGroup parent = root.findViewById(R.id.container_content_view);
        View contentView = inflater.inflate(R.layout.fragment_important_dates,
                parent, false);
        parent.addView(contentView);
        setHasOptionsMenu(true);

        // Initialize instances



        // Term selection information
        updateTermInformation();



        return root;
    };


    /**
     * Methods to control the important dates that can be seen
     */

    // Allow the user to select the term (current, next term, term after that)
    private void updateTermInformation() {
        // terms from
        String[] terms;


        // default/term or last term selected
        String lastTermSelected;

        // set spinner to the selection before update




        /*
        // Get the options for the buildings dropdown
        String[] buildings = mRoomScheduleManager.getBuildings();
        StringAdapter buildingsAdapter = new StringAdapter(mContext, buildings);
        buildingsAdapter.setViewLayoutId(android.R.layout.simple_spinner_item);
        mBuildingsSpinner.setAdapter(buildingsAdapter);

        // Remember the last building selected
        String lastBuildingQueried = mSharedPreferences.getString(Constants.BUILDING_KEY, "");
        int indexLastBuildingQueried = Arrays.asList(buildings).indexOf(lastBuildingQueried);

        // Set the spinner to the selection before the update
        if (indexLastBuildingQueried != -1) {
            mBuildingsSpinner.setSelection(indexLastBuildingQueried);
        }
        */
    }




}
