package edu.gatech.wguo64.lostandfoundandroidapp.utility;

/**
 * Created by guoweidong on 2/12/16.
 */
public class TextTrimmer {
    public final static int DEFAULT_LENGTH = 160;
    public static String trim(String txt) {
        return trim(txt, DEFAULT_LENGTH);
    }
    public static String trim(String txt, int length) {
        if(txt == null || txt.length() <= length) {
            return txt;
        }
        return txt.substring(0, length) + "...";
    }
}
