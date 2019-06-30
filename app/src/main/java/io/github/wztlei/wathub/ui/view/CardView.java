package io.github.wztlei.wathub.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.utils.Px;

public class CardView extends android.support.v7.widget.CardView {

  public CardView(final Context context) {
    this(context, null);
  }

  public CardView(final Context context, final AttributeSet attrs) {
    this(context, attrs, R.attr.cardViewStyle);
  }

  public CardView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  private void init() {
    if (isInEditMode()) {
      return;
    }

    if (getCardElevation() == 0) {
      setCardElevation(Px.fromDp(4));
    }

    if (getRadius() == 0) {
      setRadius(Px.fromDp(2));
    }
  }

}
