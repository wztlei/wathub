package io.github.wztlei.wathub.ui.modules.courses.views;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.widget.FrameLayout;

import io.github.wztlei.wathub.model.CombinedCourseInfo;

import butterknife.ButterKnife;

public abstract class BaseCourseView extends FrameLayout {
  public BaseCourseView(final Context context) {
    super(context);

    init();
  }

  private void init() {
    setLayoutParams(generateDefaultLayoutParams());

    final int layoutId = getLayoutId();
    if (layoutId != 0) {
      inflate(getContext(), layoutId, this);
    }

    ButterKnife.bind(this);
  }

  protected abstract @LayoutRes
  int getLayoutId();

  public abstract void bind(final CombinedCourseInfo info);

}
