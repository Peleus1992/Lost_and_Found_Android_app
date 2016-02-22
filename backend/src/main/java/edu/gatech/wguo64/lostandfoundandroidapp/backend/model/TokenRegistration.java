package edu.gatech.wguo64.lostandfoundandroidapp.backend.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by mkatri on 12/3/15.
 */
@Entity
public class TokenRegistration {

    @Id
    Long id;
    String token;
    @Index
    String userEmail;

    //TODO support multiple tokens per user
    public TokenRegistration() {
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
