package edu.gatech.wguo64.lostandfoundandroidapp.backend.model;

import com.googlecode.objectify.annotation.Entity;

/**
 * Created by guoweidong on 2/18/16.
 */
@Entity
public class Response {
    String stringResponse;

    public void setStringResponse(String stringResponse) {
        this.stringResponse = stringResponse;
    }

    public String getStringResponse() {
        return stringResponse;
    }
}
