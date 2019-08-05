package io.github.wztlei.wathub.ui.modules.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.deange.uwaterlooapi.model.AbstractModel;
import com.deange.uwaterlooapi.model.Metadata;
import com.deange.uwaterlooapi.model.common.SimpleListResponse;

import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.ui.ModuleAdapter;

import java.util.Collections;

public abstract class BaseListApiModuleFragment<T extends SimpleListResponse<V>, V extends AbstractModel>
        extends BaseApiModuleFragment<T, V> {

    private ListView mListView;

    @Override
    protected View getContentView(final LayoutInflater inflater, final ViewGroup parent) {
        final View root = inflater.inflate(getLayoutId(), parent, false);

        mListView = root.findViewById(android.R.id.list);
        if (mListView == null) {
            throw new IllegalStateException("ListView must have id android.R.id.list");
        }

        return root;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setAdapter(getAdapter());
    }

    protected int getLayoutId() {
        return R.layout.fragment_simple_listview;
    }

    public ListView getListView() {
        return mListView;
    }

    public void notifyDataSetChanged() {
        if (mListView.getAdapter() instanceof BaseAdapter) {
            ((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    public final void onBindData(final Metadata metadata, final V data) {
        onBindData(metadata, Collections.singletonList(data));
    }

    public abstract ModuleAdapter getAdapter();
}
