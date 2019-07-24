package io.github.wztlei.wathub.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class PhotoUrl {

    @SerializedName("type")
    String mType;

    @SerializedName("_content")
    String mUrl;

    public String getType() {
        return mType;
    }

    public String getUrl() {
        return mUrl;
    }

    /* package */ static class UrlList {
        @SerializedName("url")
        List<PhotoUrl> mUrls;

        List<PhotoUrl> getUrls() {
            return (mUrls == null) ? new ArrayList<>() : mUrls;
        }
    }

}
