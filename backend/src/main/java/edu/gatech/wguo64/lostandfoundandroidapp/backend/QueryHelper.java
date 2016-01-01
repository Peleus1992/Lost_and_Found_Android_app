package edu.gatech.wguo64.lostandfoundandroidapp.backend;

import com.google.appengine.repackaged.com.google.common.base.Joiner;

/**
 * Created by mkatri on 12/3/15.
 */
public class QueryHelper {
    public static String sanitize(String queryString) {
        return Joiner.on(" OR ").join(
                (queryString).trim().replace("\\", "").replace("\"",
                        "").split("\\s+"));
    }
}
