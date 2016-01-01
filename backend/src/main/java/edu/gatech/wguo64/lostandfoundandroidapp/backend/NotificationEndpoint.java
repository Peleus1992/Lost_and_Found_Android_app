package edu.gatech.wguo64.lostandfoundandroidapp.backend;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.ObjectifyService;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nonnull;


import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.TokenRegistration;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by mkatri on 12/3/15.
 */


@Api(
        name = "lostAndFound",
        version = "v1",
        resource = "reports",
        scopes = {Constants.EMAIL_SCOPE, Constants.PROFILE_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID,
                com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(
                ownerDomain = "api.lostandfound.cc.gatech.edu",
                ownerName = "api.lostandfound.cc.gatech.edu",
                packagePath = ""
        )
)
public class NotificationEndpoint {

    private static final Logger logger = Logger.getLogger(NotificationEndpoint
            .class.getName());

    static {
        // Typically you would register this inside an OfyServive wrapper.
        // See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(TokenRegistration.class);
    }

    @ApiMethod(
            name = "notification.registerToken",
            path = "token",
            httpMethod = ApiMethod.HttpMethod.GET)
    public void registerToken(@Nonnull @Named("token") String token, User user)
            throws
            OAuthRequestException {
        if (user == null) {
            logger.exiting(NotificationEndpoint.class.toString(), "Not logged" +
                    " " +
                    "in.");
            throw new OAuthRequestException("You need to login to register " +
                    "tokens.");
        }
        //TODO use transaction
        TokenRegistration registration = ofy().load().type(TokenRegistration
                .class)
                .filter("userId", user.getEmail()).first().now();
        if (registration == null) {
            registration = new TokenRegistration();
            registration.setUserId(user.getEmail());
        }
        registration.setToken(token);
        ofy().save().entity(registration).now();
    }

    public void sendNotification(@Named("message") String message) throws
            IOException {
        if (message == null || message.trim().length() == 0) {
            logger.warning("Not sending message because it is empty");
            return;
        }
        // crop longer messages
        if (message.length() > 1000) {
            message = message.substring(0, 1000) + "[...]";
        }
        Sender sender = new Sender(Constants.GCM_API_KEY);
        Message msg = new Message.Builder().addData("message", message).build();
        List<TokenRegistration> records = ofy().load().type(TokenRegistration
                .class).list();
        for (TokenRegistration record : records) {
            Result result = sender.send(msg, record.getToken(), 5);
            if (result.getMessageId() != null) {
                logger.info("Message sent to " + record.getToken());
                String canonicalRegId = result.getCanonicalRegistrationId();
                if (canonicalRegId != null) {
                    // if the regId changed, we have to update the datastore
                    logger.info("Registration Id changed for " + record
                            .getToken() + " updating to " + canonicalRegId);
                    record.setToken(canonicalRegId);
                    ofy().save().entity(record).now();
                }
            } else {
                String error = result.getErrorCodeName();
                if (error.equals(com.google.android.gcm.server.Constants
                        .ERROR_NOT_REGISTERED)) {
                    logger.warning("Registration Id " + record.getToken() +
                            " no longer registered with GCM, removing from " +
                            "datastore");
                    // if the device is no longer registered with Gcm, remove
                    // it from the datastore
                    ofy().delete().entity(record).now();
                } else {
                    logger.warning("Error when sending message : " + error);
                }
            }
        }
    }
}
