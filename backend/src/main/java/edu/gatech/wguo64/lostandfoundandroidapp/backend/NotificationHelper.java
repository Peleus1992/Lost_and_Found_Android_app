package edu.gatech.wguo64.lostandfoundandroidapp.backend;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.gatech.wguo64.lostandfoundandroidapp.backend.constants.Credentials;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.TokenRegistration;

import static edu.gatech.wguo64.lostandfoundandroidapp.backend.OfyService.ofy;

/**
 * Created by mkatri on 12/4/15.
 */
public class NotificationHelper {
    private static final Logger logger = Logger.getLogger(NotificationEndpoint
            .class.getName());
    static {
        logger.setLevel(Level.INFO);
    }

    public static void notify(Collection<String> userIds, String message) {
        if (message == null || message.trim().length() == 0) {
            logger.info("Not sending message because it is empty");
            return;
        }
        // crop longer messages
        if (message.length() > 1000) {
            message = message.substring(0, 1000) + "[...]";
        }

        Sender sender = new Sender(Credentials.SERVER_KEY);
        for (String userId : userIds) {
            TokenRegistration record = ofy().load().type(TokenRegistration
                    .class).filter("userId", userId).first().now();

            Message msg = new Message.Builder().addData("message", message)
                    .build();

            try {
                Result result = sender.send(msg, record.getToken(), 5);
                if (result.getMessageId() != null) {
                    logger.info("Message sent to " + record.getToken());
                    String canonicalRegId = result.getCanonicalRegistrationId();
                    if (canonicalRegId != null) {
                        // if the regId changed, we have to update the datastore
                        logger.warning("Registration Id changed for " + record
                                .getToken() + " updating to " + canonicalRegId);
                        record.setToken(canonicalRegId);
                        ofy().save().entity(record).now();
                    }
                } else {
                    String error = result.getErrorCodeName();
                    if (error.equals(com.google.android.gcm.server.Constants
                            .ERROR_NOT_REGISTERED)) {
                        logger.warning("Registration Id " + record.getToken() +
                                " no longer registered with GCM, removing " +
                                "from " +

                                "datastore");
                        // if the device is no longer registered with Gcm,
                        // remove
                        // it from the datastore
                        ofy().delete().entity(record).now();
                    } else {
                        logger.warning("Error when sending message : " + error);
                    }
                }
            } catch (IOException e) {
                logger.warning("Error when sending message to : "+userId);
            }
        }
    }
}
