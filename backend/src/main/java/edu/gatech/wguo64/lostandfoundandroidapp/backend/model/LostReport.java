package edu.gatech.wguo64.lostandfoundandroidapp.backend.model;


import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Subclass;

import java.util.Date;

/**
 * Created by mkatri on 11/22/15.
 */
@Subclass(index = true)
public class LostReport extends Report {
    @Index
    Date timeLost;
    GeoPt location;
    boolean found;

    public LostReport() {
        super();
        found = false;
    }

    public Date getTimeLost() {
        return timeLost;
    }

    public void setTimeLost(Date timeLost) {
        this.timeLost = timeLost;
    }

    public GeoPt getLocation() {
        return location;
    }

    public void setLocation(GeoPt location) {
        this.location = location;
    }

    public boolean getFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }
}
