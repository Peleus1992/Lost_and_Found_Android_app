package edu.gatech.wguo64.lostandfoundandroidapp.time;

import java.util.Calendar;

/**
 * Created by guoweidong on 11/6/15.
 */
public class TimeManager {
    public static String getTimeDifferential(long timestamp) {
        long now = Calendar.getInstance().getTimeInMillis();
        long diff = (now - timestamp) / 1000;
        if (diff < 0) {
            return "time leak";
        } else if (diff < 60) {
            return diff + " seconds ago";
        } else if (diff < 3600) {
            return diff / 60 + " minutes ago";
        } else if (diff < 86400) {
            return (diff / 3600) + " hours ago";
        } else {
            return (diff / 86400) + " days ago";
        }
    }
}
