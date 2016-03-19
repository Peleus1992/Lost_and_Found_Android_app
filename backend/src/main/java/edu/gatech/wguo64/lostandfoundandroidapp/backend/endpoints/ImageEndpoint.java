package edu.gatech.wguo64.lostandfoundandroidapp.backend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.gatech.wguo64.lostandfoundandroidapp.backend.constants.Credentials;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.Feedback;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.Report;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.Response;

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
public class ImageEndpoint {

    private static final Logger logger = Logger.getLogger(ImageEndpoint.class.getName());

    static {
        logger.setLevel(Level.INFO);
    }

    /**
    * Returns the {@link Report} with the corresponding ID.
    *
    * @param user the google account user
    * @return the url of image to be stored
    * @throws OAuthRequestException
    **/
    @ApiMethod(
            name = "image.newImageUrl",
            path = "image/newImageUrl",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Response newImageUrl(User user) throws OAuthRequestException {
        if(user == null) {
            logger.exiting(ReportEndpoint.class.toString(), "Not logged in.");
            throw new OAuthRequestException("You need to login to file reports.");
        }
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        String url = blobstoreService.createUploadUrl("/uploadImage");
        logger.info("Create new Image URL: " + url);
        Response response = new Response();
        response.setStringResponse(url);
        return response;
    }
}
