package edu.gatech.wguo64.lostandfoundandroidapp.backend.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by guoweidong on 2/28/16.
 */
@Entity
public class Feedback {
    @Id Long id;
    String userEmail;
    String content;

    public Long getId() {
        return id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
