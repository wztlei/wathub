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
    @BindView(R.id.loading_layout)
    ViewGroup mLoadingLayout;

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
        updateTermSpinner();

        return root;
    }

    @Override
    public String getToolbarTitle() {
        return getString(R.string.title_important_dates);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        // inflate the menu
        inflater.inflate(R.menu.menu_info_and_refresh, menu);
        mRefreshMenuItem = menu.findItem(R.id.menu_refresh);

        setSpinnerSelectionListener();

    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menu_refresh) {
            displayLoadingScreen(mLoadingLayout, mRefreshMenuItem, false);
            // method to handle a refresh
            return true;
        }
        else if (menuItem.getItemId() == R.id.menu_info) {
            // Creates an alert dialog displaying important info about the important dates data
            new AlertDialog.Builder(mContext)
                    .setTitle(getString(R.string.important_dates_dialog_title))
                    .setMessage(getString(R.string.important_dates_dialog_message))
                    .setPositiveButton(android.R.string.ok, (dialog1, which) -> {
                    })
                    .create()
                    .show();
            return true;
        }
        else {
            return super.onOptionsItemSelected(menuItem);
        }
    }


    /**
     * Updates the terms available to view (current, next, next)
     */
    private void updateTermSpinner() {
        // get the terms that can be viewed
        String[] terms = mImportantDatesManager.getTerms();
        StringAdapter termsAdapter = new StringAdapter(mContext, terms);
        termsAdapter.setViewLayoutId(android.R.layout.simple_spinner_item);
        mTermSpinner.setAdapter(termsAdapter);

        // maybe add a default/last term selected?
    }


    /**
     *
     */
    private void setSpinnerSelectionListener() {
        mTermSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // display the results
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }


    /**
     *  Display the important dates queried
     */
    private void displayImportantDates(boolean displayDefault) {


    }
}
