package io.github.wztlei.wathub.model;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class RedditPostList extends ArrayList<RedditPost> {
    public RedditPostList(JSONArray jsonArray) {
        super();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                this.add(new RedditPost(jsonArray.getJSONObject(i).getJSONObject("data")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
