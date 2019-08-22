package io.github.wztlei.wathub.model;

import org.json.JSONException;
import org.json.JSONObject;

public class RedditPost {
    private String author;
    private String domain;
    private String title;
    private String linkFlair;
    private String selftext;
    private String url;
    private int createdUtc;
    private int score;
    private int numComments;
    private int numDiamonds;
    private int numGolds;
    private int numSilvers;
    private boolean isVideo;
    private boolean isImage;

    public RedditPost(JSONObject data) {
        try {
            this.author = data.getString("author");
            this.domain = data.getString("domain");
            this.title = data.getString("title");
            this.linkFlair = data.getString("link_flair_text");
            this.selftext = data.getString("selftext");
            this.url = data.getString("url");
            this.createdUtc = (int) data.getDouble("created_utc");
            this.score = data.getInt("score");
            this.numComments = data.getInt("num_comments");

            JSONObject gildings = data.getJSONObject("gildings");
            this.numDiamonds = getGildingNum(gildings, "gid_3");
            this.numGolds = getGildingNum(gildings, "gid_2");
            this.numSilvers = getGildingNum(gildings, "gid_1");
            this.isVideo = data.getBoolean("is_video");
            this.isImage = this.domain.equals("i.redd.it") || this.domain.equals("imgur.com");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int getGildingNum(JSONObject gildings, String key) throws JSONException {
        if (gildings.has(key)) {
            return gildings.getInt(key);
        } else {
            return 0;
        }
    }

    public String getAuthor() {
        return author;
    }

    public String getDomain() {
        return domain;
    }

    public String getTitle() {
        return title;
    }

    public String getLinkFlair() {
        return linkFlair;
    }

    public String getSelftext() {
        return selftext;
    }

    public String getUrl() {
        return url;
    }

    public int getCreatedUtc() {
        return createdUtc;
    }

    public int getScore() {
        return score;
    }

    public int getNumComments() {
        return numComments;
    }

    public int getNumDiamonds() {
        return numDiamonds;
    }

    public int getNumGolds() {
        return numGolds;
    }

    public int getNumSilvers() {
        return numSilvers;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public boolean isImage() {
        return isImage;
    }
}
