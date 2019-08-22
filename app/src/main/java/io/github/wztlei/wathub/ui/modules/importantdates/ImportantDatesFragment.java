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

import com.deange.uwaterlooapi.UWaterlooApi;
import com.deange.uwaterlooapi.model.Metadata;
import com.deange.uwaterlooapi.model.common.Responses;
import com.deange.uwaterlooapi.model.important_dates.ImportantDatesDetails;
import android.text.Html;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import io.github.wztlei.wathub.Constants;
import io.github.wztlei.wathub.R;

import butterknife.BindView;
import io.github.wztlei.wathub.ui.ModuleAdapter;
import io.github.wztlei.wathub.ui.ModuleListItemListener;
import io.github.wztlei.wathub.ui.modules.ModuleType;
import io.github.wztlei.wathub.ui.modules.ModuleType;
import io.github.wztlei.wathub.ui.modules.base.BaseListApiModuleFragment;
import io.github.wztlei.wathub.controller.ImportantDatesManager;
import io.github.wztlei.wathub.model.ImportantDatesDisplay;
import io.github.wztlei.wathub.model.ImportantDatesList;
import io.github.wztlei.wathub.ui.StringAdapter;
import io.github.wztlei.wathub.ui.modules.base.BaseModuleFragment;
import retrofit2.Call;


public class ImportantDatesFragment extends BaseListApiModuleFragment<Responses.ImportantDates, ImportantDatesDetails>
        implements ModuleListItemListener{

    private int term_id;
    private int firstDigit = 1000;
    //private int fallTerm = 9;
    //private int winterTerm = 1;
    //private int springTerm = 5;
   // private int termYear;

    private int selectedTerm;
    private int nextTerm;
    private int termAfterNext;

    // create the list of important dates
    private final List<ImportantDatesDetails> mResponse = new ArrayList<>();


    // replace with onCreateView() -> to set up spinner and other content (dropdown)
    @Override
    protected int getLayoutId() {
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
        selectedTerm = calculateTerm();
        return api.ImportantDates.getImportantDates(selectedTerm);
    }

    @Override
    public void onBindData(final Metadata metadata, final List<ImportantDatesDetails> data) {
        mResponse.clear();
        mResponse.addAll(data);

       // Collections.sort(mResponse, Collections.reverseOrder());

        notifyDataSetChanged();
    }

    @Override
    public String getContentType() {
        return ModuleType.IMPORTANT_DATES_LIST;
    }

    @Override
    public void onItemClicked(final int position) {
        //two options:
        // 1. directly link tho the url
        // 2. create an ImportantDatesViewFragment that contains some content
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
            final ImportantDatesDetails importantdates = getItem(position);

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
        public ImportantDatesDetails getItem(final int position) {
            return mResponse.get(position);
        }
    }

    /**
     * Calculates the TERM ID (ex. 1199 -> '1' for after 2000, '19' for 2019, '9' for September)
     * @return
     */
    private int calculateTerm() {
        int tempYear = Calendar.getInstance().get(Calendar.YEAR) % 100;
        int termYear = tempYear * 10;
        int termMonth = getStartMonth();
        int calculatedID = firstDigit + termYear + termMonth;
        return calculatedID;
    }

    /**
     *  Calculates the term starting month (1, 5, or 9)
     * @return
     */
    private static int getStartMonth() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int returnedMonth;

        if ((month == 8 || month == 9 || month == 10 || month ==11)) {
            returnedMonth = 9;
        } else if ((month == 0 || month == 1 || month == 2 || month ==3)) {
            returnedMonth = 1;
        } else {
            returnedMonth = 5;
        }
        return returnedMonth;
    }
}
