package edu.gatech.wguo64.lostandfoundandroidapp.utility;

import java.util.Calendar;

/**
 * Created by guoweidong on 11/6/15.
 */
public class TimeConvertor {
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
    public static String getDateTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        return calendar.get(Calendar.MONTH) + "/"
                + calendar.get(Calendar.DAY_OF_MONTH) + "/"
                + calendar.get(Calendar.YEAR) + " "
                + calendar.get(Calendar.HOUR_OF_DAY) + ":"
                + calendar.get(Calendar.MINUTE);
    }
}
