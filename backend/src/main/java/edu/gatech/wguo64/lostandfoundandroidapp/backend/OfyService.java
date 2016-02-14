package edu.gatech.wguo64.lostandfoundandroidapp.backend;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.FoundReport;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.LostReport;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.Report;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.TokenRegistration;

/**
 * Created by guoweidong on 2/13/16.
 */
public class OfyService {
    static {
        ObjectifyService.register(Report.class);
        ObjectifyService.register(FoundReport.class);
        ObjectifyService.register(LostReport.class);
        ObjectifyService.register(TokenRegistration.class);
    }
    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}
