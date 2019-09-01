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
import android.text.Html;

import com.deange.uwaterlooapi.UWaterlooApi;
import com.deange.uwaterlooapi.annotations.ModuleFragment;
import com.deange.uwaterlooapi.model.Metadata;
import com.deange.uwaterlooapi.model.common.Responses;
import com.deange.uwaterlooapi.model.important_dates.ImportantDate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import io.github.wztlei.wathub.Constants;
import io.github.wztlei.wathub.R;

import butterknife.BindView;
import io.github.wztlei.wathub.controller.TermManager;
import io.github.wztlei.wathub.ui.ModuleAdapter;
import io.github.wztlei.wathub.ui.ModuleListItemListener;
import io.github.wztlei.wathub.ui.modules.ModuleType;
import io.github.wztlei.wathub.ui.modules.base.BaseListApiModuleFragment;
import io.github.wztlei.wathub.ui.StringAdapter;
import io.github.wztlei.wathub.ui.modules.base.BaseModuleFragment;
import io.github.wztlei.wathub.utils.IntentUtils;
import retrofit2.Call;

@ModuleFragment(
        path = "/terms/*/importantdates",
        layout = R.layout.module_importantdates // change the icon
)
public class ImportantDatesFragment extends BaseListApiModuleFragment<Responses.ImportantDates, ImportantDate>
        implements ModuleListItemListener {


    private int term_id;
    //private int firstDigit = 1000;
    //private int fallTerm = 9;
    //private int winterTerm = 1;
    //private int springTerm = 5;
    //private int termYear;

    //private TermManager mTermManager;

    //private int selectedTerm;
    private int nextTerm;
    private int termAfterNext;
    private boolean showAll = false;


    //create the list of important dates
    private final List<ImportantDate> mResponse = new ArrayList<>();

    /*
    @Override
    protected View getContentView(final LayoutInflater inflater, final ViewGroup parent) {
        final View root = super.getContentView(inflater, parent);

        final Spinner spinner = root.findViewById(R.id.term_important_dates_spinner);
        String[] terms = new String[3];
        terms[0] = Integer.toString(mTermManager.getInstance().currentTerm());
        // fill the string array (update with terms)
        // turn term number -> 'fall 2019' example


        spinner.setAdapter(new StringAdapter(getContext(), terms));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // refresh the content
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
            });

        return root;
    }
    */
    //

    @Override
    protected int getLayoutId() {
        // return R.layout.fragment_important_dates; // remove and show basic list
        return R.layout.fragment_simple_listview;
    }

    @Override
    public String getToolbarTitle() {
        return getString(R.string.title_important_dates);
    }

    @Override
    public ModuleAdapter getAdapter() {
        return new ImportantDatesAdapter(getActivity(), this);
    }

    @Override
    public Call<Responses.ImportantDates> onLoadData(final UWaterlooApi api) {
        //int selectedTerm;
        //selectedTerm = calculateTerm();
        int selectedTerm = 1199;
        return api.ImportantDates.getImportantDates(selectedTerm);
    }

    @Override
    public void onBindData(final Metadata metadata, final List<ImportantDate> data) {
        mResponse.clear();
        mResponse.addAll(data);

        Collections.sort(mResponse, Collections.reverseOrder());

        notifyDataSetChanged();
    }

    @Override
    public String getContentType() {
        return ModuleType.IMPORTANT_DATES;
    }

    @Override
    public void onItemClicked(final int position) {
        /*
         two options:
         1. directly link tho the url
         2. create an ImportantDatesViewFragment that contains some content --> current implementation
         */
        //final ImportantDate importantDates = mResponse.get(position);
        //final String urlLink = importantDates.getLink();
        //IntentUtils.openBrowser(getActivity(), urlLink);
    }

    private class ImportantDatesAdapter extends ModuleAdapter {

        ImportantDatesAdapter(final Context context, final ModuleListItemListener listener) {
            super(context, listener);
        }

        @Override
        public View newView(final Context context, final int position, final ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.simple_three_line_card_item, parent,
                    false);
        }

        @Override
        public void bindView(final Context context, final int position, final View view) {
            final ImportantDate importantdates = getItem(position);

            final String title = Html.fromHtml(importantdates.getTitle()).toString();
            final String desc = Html.fromHtml(importantdates.getBody()).toString();

            final String startDay = Html.fromHtml(importantdates.getStartDate()).toString();
            final String endDay = Html.fromHtml(importantdates.getEndDate()).toString();
            final String startToEnd = startDay + " to " + endDay;

            ((TextView) view.findViewById(android.R.id.text1)).setText(title);
            ((TextView) view.findViewById(android.R.id.text2)).setText(desc);
            ((TextView) view.findViewById(android.R.id.summary)).setText(startToEnd);
        }

        @Override
        public int getCount() {
            return mResponse.size();
        }

        @Override
        public ImportantDate getItem(final int position) {
            return mResponse.get(position);
        }
    }

    private void setSpinnerSelectedListener() {
        //mTermSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        	/*
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                displayImportantDates(true);
            }
            */

        //@Override
        //public void onNothingSelected(AdapterView<?> parent) {}
        //});
    }

    /**
     *
     */
    private void updateTermSpinner() {
        String[] terms = new String[3];
        terms[0] = Integer.toString(calculateTerm());
        // add method to count next 2 terms

        //StringAdapter termsAdapter = new StringAdapter(mContext,  terms);
        // int spinerSelectionIndex = mTermSpinner.getSelectedItemPosition();
        //termsAdapter.setViewlayoutId(android.R.layout.simple_spinner_item);

        // set the new dropdown options
        //mTermSpinner.setAdapter(termsAdapter);

    }


    /**
     * Calculates the TERM ID (ex. 1199 -> '1' for after 2000, '19' for 2019, '9' for September)
     *
     * @return
     */
    private int calculateTerm() {
        //int firstDigit = 1000;
        // int term = mTermManager.getInstance().currentTerm();
        //int tempYear = Calendar.getInstance().get(Calendar.YEAR) % 100;
        //int termYear = tempYear * 10;
        //int termMonth = getStartMonth();
        // int calculatedID = firstDigit + termYear + termMonth;
        //return (firstDigit + termYear + termMonth);
        return 0;
    }

    /**
     * Calculates the term starting month (1, 5, or 9)
     *
     * @return
     */
    private static int getStartMonth() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int returnedMonth;

        if ((month == 8 || month == 9 || month == 10 || month == 11)) {
            returnedMonth = 9;
        } else if ((month == 0 || month == 1 || month == 2 || month == 3)) {
            returnedMonth = 1;
        } else {
            returnedMonth = 5;
        }
        return returnedMonth;
    }
}
