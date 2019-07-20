package io.github.wztlei.wathub.ui.modules.openclassroom;

import io.github.wztlei.wathub.ui.modules.base.BaseModuleFragment;
import io.github.wztlei.wathub.utils.Px;

public class OpenClassroomFragment extends BaseModuleFragment {
    @Override
    public String getToolbarTitle() {
        return "Open Classrooms";
    }

    @Override
    public String getToolbarSubtitle() {
        return null;
    }

    @Override
    public float getToolbarElevationPx() {
        // Overridden by subclasses
        return Px.fromDpF(8);
    }
}
