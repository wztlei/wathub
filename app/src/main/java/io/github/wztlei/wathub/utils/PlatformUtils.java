package io.github.wztlei.wathub.utils;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import io.github.wztlei.wathub.R;

@SuppressWarnings("unused")
public final class PlatformUtils {

    @SuppressLint("ObsoleteSdkInt")
    public static boolean hasIcs() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    @SuppressLint("ObsoleteSdkInt")
    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    @SuppressLint("ObsoleteSdkInt")
    public static boolean hasJellyBeanMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    @SuppressLint("ObsoleteSdkInt")
    public static boolean hasJellyBeanMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    @SuppressLint("ObsoleteSdkInt")
    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean hasMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static void copyToClipboard(final Context context, final String text) {
        final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(
                Context.CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newPlainText(context.getString(R.string.app_name), text);

        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, R.string.clipboard_copied, Toast.LENGTH_SHORT).show();
        }
    }

    private PlatformUtils() {
        // Uninstantiable
    }
}
