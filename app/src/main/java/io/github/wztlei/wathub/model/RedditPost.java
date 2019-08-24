package io.github.wztlei.wathub.model;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import io.github.wztlei.wathub.utils.DateTimeUtils;

public class RedditPost {
    private String author;
    private String creationTime;
    private String domain;
    private String title;
    private String linkFlair;
    private String selftext;
    private String url;
    private int score;
    private int numComments;
    private int numDiamonds;
    private int numGolds;
    private int numSilvers;
    private boolean isVideo;
    private boolean isImage;

    public static final String DEFAULT_UWATERLOO_DOMAIN = "self.uwaterloo";
    private static final String MINUTE_SUFFIX = "m";
    private static final String HOUR_SUFFIX = "h";
    private static final String DAY_SUFFIX = "d";
    private static final String MONTH_SUFFIX = "mo";
    private static final String YEAR_SUFFIX = "y";

    /**
     * Constructor method for a Reddit post.
     *
     * @param data the data for the Reddit post stored as a JSON object
     */
    RedditPost(JSONObject data) {
        try {
            this.author = data.getString("author");
            this.domain = data.getString("domain");
            this.title = data.getString("title");
            this.linkFlair = data.getString("link_flair_text");
            this.selftext = data.getString("selftext");
            this.url = data.getString("url");
            this.creationTime = formatCreationTime((long) data.getDouble("created_utc"));
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

    /**
     * Returns a string displaying how much time has occurred since the creation of the post.
     *
     * @param   creationUtcSeconds  the creation time in seconds as measured from the Unit epoch
     * @return                      the formatted string
     */
    private String formatCreationTime(long creationUtcSeconds) {
        long nowUtcSeconds = System.currentTimeMillis() / DateTimeUtils.MS_PER_SECOND;
        long numSecondsAgo = nowUtcSeconds - creationUtcSeconds;

        // Determine what time unit to use
        if (numSecondsAgo < 0) {
            // Should not happen unless the creation is in the future
            return 0 + MINUTE_SUFFIX;
        } else if (numSecondsAgo < DateTimeUtils.SECONDS_PER_HOUR) {
            // For posts created minute(s) ago
            return (numSecondsAgo / DateTimeUtils.SECONDS_PER_MIN) + MINUTE_SUFFIX;
        } else if (numSecondsAgo < DateTimeUtils.SECONDS_PER_DAY) {
            // For posts created hour(s) ago
            return (numSecondsAgo / DateTimeUtils.SECONDS_PER_HOUR) + HOUR_SUFFIX;
        } else if (numSecondsAgo < DateTimeUtils.APPROX_SECONDS_PER_MONTH) {
            // For posts created day(s) ago
            return (numSecondsAgo / DateTimeUtils.SECONDS_PER_DAY) + DAY_SUFFIX;
        } else if (numSecondsAgo < DateTimeUtils.APPROX_SECONDS_PER_YEAR) {
            // For posts created month(s) ago
            return (numSecondsAgo / DateTimeUtils.APPROX_SECONDS_PER_MONTH) + MONTH_SUFFIX;
        } else {
            // For posts created year(s) ago
            return (numSecondsAgo / DateTimeUtils.APPROX_SECONDS_PER_YEAR) + YEAR_SUFFIX;
        }
    }

    /**
     * Returns the string value stored at a specific key in a JSON object, or null if the key does
     * not exist or the value is null. JSONObject.getString(key) will return "null" if the value is
     * null, so this method is necessary.
     *
     * @param   jsonObject  the JSON object storing the key-value pair
     * @param   key         the key at which to retrieve the value
     * @return              the value mapped by the key or null
     */
    private String getStringByKey(JSONObject jsonObject, String key) throws JSONException {
        if (jsonObject.has(key) && !jsonObject.isNull(key)) {
            return jsonObject.getString(key);
        } else {
            return null;
        }
    }

    /**
     * Returns the number of gildings stored in the JSON object if the type exists, or 0 otherwise.
     *
     * @param   gildings    a map with key-value pairs representing the type and number of gildings
     * @param   gildingType the type of gilding to retrieve
     * @return              the number of gildings if the gilding types exists, or 0 otherwise.
     */
    private int getGildingNum(JSONObject gildings, String gildingType) throws JSONException {
        if (gildings.has(gildingType)) {
            return gildings.getInt(gildingType);
        } else {
            return 0;
        }
    }

    public String getAuthor() {
        return author;
    }

    public String getCreationTime() {
        return creationTime;
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
