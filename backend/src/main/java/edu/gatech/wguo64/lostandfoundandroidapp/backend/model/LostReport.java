package edu.gatech.wguo64.lostandfoundandroidapp.backend.model;


import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;
import java.util.List;

/**
 * Created by mkatri on 11/22/15.
 */

@Entity
public class LostReport extends Report {
    @Index
    Date timeLost;
    List<GeoPt> locations;
    @Index
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

    public List<GeoPt> getLocations() {
        return locations;
    }

    public void setLocations(List<GeoPt> locations) {
        this.locations = locations;
    }

    public boolean getFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }
}
