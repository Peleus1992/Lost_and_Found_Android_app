package edu.gatech.wguo64.lostandfoundandroidapp.backend;

import com.google.appengine.api.users.User;

/**
 * Created by mkatri on 12/3/15.
 */
public class UserHelper {

    public static User fixUser(User user) {
//        if (user.getUserId() == null) {
//            ofy().save().entity(user).now();
//            user = ofy().load().entity(user).now();
//            ofy().delete().entity(user).now();
//        }
        return user;
    }
}
