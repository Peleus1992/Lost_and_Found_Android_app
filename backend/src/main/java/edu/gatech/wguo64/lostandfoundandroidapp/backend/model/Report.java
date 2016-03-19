package edu.gatech.wguo64.lostandfoundandroidapp.backend.model;

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.search.Field;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by mkatri on 11/22/15.
 */

@Entity
public class Report {

    //TODO consider changing id type to string
    @Id
    Long id;
    String title;
    String description;

    List<String> tags;
    @Index
    Date created;
    @Index
    String userId;
    @Index
    String userEmail;
    String photoUrl;
    @Index
    boolean reportType;
    @Index
    Date time;
    GeoPt location;
    String imageUrl;
    String imageKey;
    boolean status;

    List<Ref<Comment>> comments;

    public Report() {
        created = new Date();
        comments = new ArrayList<>();
        status = false;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getTagsString() {
        if(tags == null) {
            return "";
        }
        StringBuilder sbTags = new StringBuilder();
        for(String t : tags) {
            sbTags.append(t + " ");
        }
        return sbTags.toString();
    }

    public boolean getReportType() {
        return reportType;
    }

    public void setReportType(boolean reportType) {
        this.reportType = reportType;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public GeoPt getLocation() {
        return location;
    }

    public void setLocation(GeoPt location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }



    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreated() {
        return created;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public List<Comment> getComments() {
        List<Comment> commentList = new ArrayList<>();
        for(Ref<Comment> r : comments) {
            if(r != null) {
                commentList.add(r.get());
            }
        }
        return commentList;
    }

    public void addComment(Key<Comment> key) {
        Ref<Comment> r = Ref.create(key);
        comments.add(r);
    }

}
