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
import com.deange.uwaterlooapi.model.common.Responses;
import com.deange.uwaterlooapi.model.news.NewsDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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


public class ImportantDatesFragment extends BaseListApiModuleFragment<Responses.News, NewsDetails>
        implements ModuleListItemListener{

    private final List<NewsDetails> mResponse = new ArrayList<>();

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
    public Call<Responses.News> onLoadData(final UWaterlooApi api) {
        return api.News.getNews();
    }


    @Override
    public String getContentType() {
        return ModuleType.IMPORTANT_DATES_LIST;
    }


    @Override
    public void onItemClicked(final int position) {

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

        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public NewsDetails getItem(final int position) {
            return mResponse.get(position);
        }
    }

}
