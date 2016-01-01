package edu.gatech.wguo64.lostandfoundandroidapp.backend.model;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;

/**
 * Created by mkatri on 11/22/15.
 */
@Entity
public class FoundReport extends Report {
    @Index
    Date timeFound;
    Blob image;
    GeoPt location;
    @Index
    boolean returned;

    public FoundReport() {
        super();
        returned = false;
    }

    public Blob getImage() {
        return image;
    }

    public void setImage(Blob image) {
        this.image = image;
    }

    public GeoPt getLocation() {
        return location;
    }

    public void setLocation(GeoPt location) {
        this.location = location;
    }

    public Date getTimeFound() {
        return timeFound;
    }

    public void setTimeFound(Date timeFound) {
        this.timeFound = timeFound;
    }

    public boolean getReturned(){
        return returned;
    }

    public void setReturned(boolean returned) {
        this.returned = returned;
    }
}
