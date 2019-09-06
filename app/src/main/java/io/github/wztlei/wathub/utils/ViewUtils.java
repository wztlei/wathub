package io.github.wztlei.wathub.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

public final class ViewUtils {

    private ViewUtils() {
        throw new AssertionError();
    }

    @SuppressWarnings("unused")
    public static void showKeyboard(final Activity activity) {
        final InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    @SuppressWarnings("unused")
    public static void showKeyboard(final View view) {
        if (view == null || view.getContext() == null) {
            return;
        }

        final InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        }
    }

    public static void hideKeyboard(final Activity activity) {
        if (activity == null || activity.getWindow() == null) {
            return;
        }

        final InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    public static void setText(final TextView view, final CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            view.setText(text);
        }
    }

    public static void setDrawable(final ImageView view, final Drawable drawable) {
        if (drawable == null) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            view.setImageDrawable(drawable);
        }
    }

    @SuppressWarnings("unused")
    public static Spannable replaceUrlSpans(final Spanned oldText) {
        final URLSpan[] spans = oldText.getSpans(0, oldText.length() - 1, URLSpan.class);
        final Spannable text = Spannable.Factory.getInstance().newSpannable(oldText);
        for (final URLSpan span : spans) {
            final int start = text.getSpanStart(span);
            final int end = text.getSpanEnd(span);
            final int flags = text.getSpanFlags(span);

            text.removeSpan(span);
            text.setSpan(new UrlSpan(span.getURL()), start, end, flags);
        }

        return text;
    }

    private static class UrlSpan extends ClickableSpan {

        private final String mUrl;

        UrlSpan(final String url) {
            mUrl = url;
        }

        @Override
        public void onClick(@NonNull final View view) {
            IntentUtils.openBrowser(view.getContext(), mUrl);
        }
    }
}
