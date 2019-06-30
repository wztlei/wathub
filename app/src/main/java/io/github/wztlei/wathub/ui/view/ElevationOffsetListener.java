package io.github.wztlei.wathub.ui.view;

import android.support.design.widget.AppBarLayout;

public class ElevationOffsetListener implements AppBarLayout.OnOffsetChangedListener {

    private float mTargetElevation;

    public ElevationOffsetListener(final float elevation) {
        mTargetElevation = elevation;
    }

    @Override
    public void onOffsetChanged(final AppBarLayout appBarLayout, int offset) {
        offset = Math.abs(offset);

        mTargetElevation = Math.max(mTargetElevation, appBarLayout.getTargetElevation());
        if (offset >= appBarLayout.getTotalScrollRange() - appBarLayout.getHeight()) {
            float flexibleSpace = appBarLayout.getTotalScrollRange() - offset;
            float ratio = 1 - (flexibleSpace / appBarLayout.getHeight());
            float elevation = ratio * mTargetElevation;
            appBarLayout.setElevation(elevation);
        } else {
            appBarLayout.setElevation(0);
        }

    }
}
