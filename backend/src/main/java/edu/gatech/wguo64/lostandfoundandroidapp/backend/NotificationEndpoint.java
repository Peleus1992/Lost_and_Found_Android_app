package edu.gatech.wguo64.lostandfoundandroidapp.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import edu.gatech.wguo64.lostandfoundandroidapp.backend.constants.Credentials;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.TokenRegistration;

import static edu.gatech.wguo64.lostandfoundandroidapp.backend.OfyService.ofy;

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
public class NotificationEndpoint {

    private static final Logger logger = Logger.getLogger(NotificationEndpoint
            .class.getName());

    static {
        logger.setLevel(Level.INFO);
    }

    @ApiMethod(
            name = "notification.registerToken",
            path = "notification/registerToken",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void registerToken(@Nonnull @Named("token")final String token, final User user)
            throws OAuthRequestException {
        if (user == null) {
            logger.warning("Not logged in.");
            throw new OAuthRequestException("You need to login to register tokens.");
        }
        // Do transaction
        ofy().transact(new Work<VoidWork>() {
            @Override
            public VoidWork run() {
                TokenRegistration registration = ofy().load().type(TokenRegistration.class)
                        .filter("userId", user.getUserId()).first().now();
                // If the user is the first time to register.
                if (registration == null) {
                    registration = new TokenRegistration();
                    registration.setUserId(user.getUserId());
                }
                registration.setToken(token);
                ofy().save().entity(registration).now();
                return null;
            }
        });
    }
}
