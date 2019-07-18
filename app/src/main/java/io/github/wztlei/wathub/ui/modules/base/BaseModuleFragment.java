package io.github.wztlei.wathub.ui.modules.base;

import android.support.v4.app.Fragment;

import io.github.wztlei.wathub.utils.Px;

public class BaseModuleFragment extends Fragment {
    public String getToolbarTitle() {
        return null;
    }

    public String getToolbarSubtitle() {
        return null;
    }

    public float getToolbarElevationPx() {
        // Overridden by subclasses
        return Px.fromDpF(8);
    }
}
