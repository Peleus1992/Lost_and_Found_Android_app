package edu.gatech.wguo64.lostandfoundandroidapp.utility;

/**
 * Created by guoweidong on 2/12/16.
 */
public class TextTrimmer {
    public static String trim(String txt) {
        if(txt == null || txt.length() <= 160) {
            return txt;
        }
        return txt.substring(0, 160) + "...";
    }
}
