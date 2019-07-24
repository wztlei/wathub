package io.github.wztlei.wathub.model;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("WeakerAccess")
public class PhotoTitle {

    @SerializedName("_content")
    String mContent;

    public String getContent() {
        return mContent;
    }

    @Override
    public String toString() {
        return mContent;
    }
}
