package edu.gatech.wguo64.lostandfoundandroidapp.backend.model;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Subclass;

import java.util.Date;

/**
 * Created by mkatri on 11/22/15.
 */

@Subclass(index = true)
public class FoundReport extends Report {
    @Index
    Date timeFound;
    GeoPt location;
    Blob iconImage;
    Blob image;
    @Index
    boolean returned;

    public FoundReport() {
        super();
        returned = false;
    }

    public Blob getIconImage() {
        return iconImage;
    }

    public void setIconImage(Blob iconImage) {
        this.iconImage = iconImage;
    }

    public Blob getImage() {
        return image;
    }

    public void setImage(Blob image) {
        this.image = image;
    }

    public Date getTimeFound() {
        return timeFound;
    }

    public void setTimeFound(Date timeFound) {
        this.timeFound = timeFound;
    }

    public GeoPt getLocation() {
        return location;
    }

    public void setLocation(GeoPt location) {
        this.location = location;
    }

    public boolean getReturned(){
        return returned;
    }

    public void setReturned(boolean returned) {
        this.returned = returned;
    }
}
