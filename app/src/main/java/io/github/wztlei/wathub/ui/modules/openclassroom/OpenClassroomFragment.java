package io.github.wztlei.wathub.ui.modules.openclassroom;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.common.UpperCaseTextWatcher;
import io.github.wztlei.wathub.controller.RoomScheduleManager;
import io.github.wztlei.wathub.ui.StringAdapter;
import io.github.wztlei.wathub.ui.modules.base.BaseModuleFragment;
import io.github.wztlei.wathub.ui.modules.courses.SubjectAdapter;

public class OpenClassroomFragment extends BaseModuleFragment {

    @BindView(R.id.building_open_classroom_spinner)
    Spinner mBuildingSpinner;

    private RoomScheduleManager roomScheduleManager;


    @Override
    public final View onCreateView(
            final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_module, container, false);
        final ViewGroup parent = root.findViewById(R.id.container_content_view);
        final View contentView = inflater.inflate(R.layout.fragment_open_classrooms, parent, false);

        ButterKnife.bind(this, contentView);
        roomScheduleManager = RoomScheduleManager.getInstance();

        if (contentView.getParent() == null) {
            parent.addView(contentView);
        }

        // Use an adapter to display all potentially available buildings
        StringAdapter adapter = new StringAdapter(getContext(), roomScheduleManager.getBuildings());
        adapter.setViewLayoutId(android.R.layout.simple_spinner_item);
        mBuildingSpinner.setAdapter(adapter);

        return root;
    }

    @Override
    public String getToolbarTitle() {
        return "Open Classrooms";
    }
}
