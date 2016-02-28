package edu.gatech.wguo64.lostandfoundandroidapp.backend.model;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by mkatri on 11/22/15.
 */

@Entity
public abstract class Report {

    //TODO consider changing id type to string
    @Id
    Long id;
    String title;
    String description;
    @Index
    Date created;
    @Index
    String userId;
    @Index
    String userEmail;
    String photoUrl;

    List<Ref<Comment>> comments;

    public Report() {
        created = new Date();
        comments = new ArrayList<>();
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
