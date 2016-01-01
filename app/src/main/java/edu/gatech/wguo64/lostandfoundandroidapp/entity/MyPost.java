package edu.gatech.wguo64.lostandfoundandroidapp.entity;

/**
 * Created by guoweidong on 12/1/15.
 */
public class MyPost {
    public long id;
    public boolean isFoundReport;
    public String title;
    public long created;
    public boolean status;
    public MyPost(long id, boolean isFoundReport, String title, long created, boolean status) {
        this.id = id;
        this.isFoundReport = isFoundReport;
        this.title = title;
        this.created = created;
        this.status = status;
    }
}
