package edu.gatech.wguo64.lostandfoundandroidapp.backend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.gatech.wguo64.lostandfoundandroidapp.backend.constants.Credentials;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.Feedback;

import static edu.gatech.wguo64.lostandfoundandroidapp.backend.helpers.OfyService.ofy;

/**
 * Created by mkatri on 12/3/15.
 */


@Api(
        name = "myApi",
        version = "v1",
        scopes = {Credentials.EMAIL_SCOPE, Credentials.PROFILE_SCOPE},
        clientIds = {Credentials.WEB_CLIENT_ID, Credentials.ANDROID_CLIENT_ID,
                com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Credentials.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(
                ownerDomain = "backend.lostandfoundandroidapp.wguo64.gatech.edu",
                ownerName = "backend.lostandfoundandroidapp.wguo64.gatech.edu",
                packagePath = ""
        )
)
public class FeedbackEndpoint {

    private static final Logger logger = Logger.getLogger(FeedbackEndpoint
            .class.getName());

    static {
        logger.setLevel(Level.INFO);
    }

    @ApiMethod(
            name = "feedback.insertFeedback",
            path = "feedback/insertFeedback",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Feedback insertFeedback(Feedback feedback, User user)
            throws OAuthRequestException {
        if (user == null) {
            logger.warning("Not logged in.");
            throw new OAuthRequestException("You need to login.");
        }
        if (feedback == null) {
            logger.warning("Null feedback");
            throw new OAuthRequestException("Null feedback");
        }
        if (!user.getEmail().equals(feedback.getUserEmail())) {
            logger.warning("User email is different from feedback.userEmail");
            throw new OAuthRequestException("User email is different from feedback.userEmail.");
        }
        // Only ancestor queries are allowed in transaction

        ofy().save().entity(feedback).now();

        return feedback;
    }
}
