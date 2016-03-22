package edu.gatech.wguo64.lostandfoundandroidapp.backend.helpers;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.Comment;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.Feedback;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.Report;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.TokenRegistration;

/**
 * Created by guoweidong on 2/13/16.
 */
public class OfyService {
    static {
        ObjectifyService.register(Report.class);
        ObjectifyService.register(TokenRegistration.class);
        ObjectifyService.register(Comment.class);
        ObjectifyService.register(Feedback.class);
    }
    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}
