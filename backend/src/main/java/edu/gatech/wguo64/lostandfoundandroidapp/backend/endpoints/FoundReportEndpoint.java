package edu.gatech.wguo64.lostandfoundandroidapp.backend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import edu.gatech.wguo64.lostandfoundandroidapp.backend.constants.Credentials;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.FoundReport;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.Response;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.SearchHelper;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.tasks.FoundMatchDeferredTask;

import static edu.gatech.wguo64.lostandfoundandroidapp.backend.OfyService.ofy;

/**
 * WARNING: This generated code is intended as a sample or starting point for
 * using a
 * Google Cloud Endpoints RESTful API with an Objectify entity. It provides
 * no data access
 * restrictions and no data validation.
 * <p/>
 * DO NOT deploy this code unchanged as part of a real application to real
 * users.
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
public class FoundReportEndpoint {

    private static final Logger logger = Logger.getLogger(FoundReportEndpoint.class.getName());
    private static final int DEFAULT_LIST_LIMIT = 5;
    //expression [a-zA-Z\\d-]{1,100}
    private static final String MATCH_QUEUE = "matchQueue";

    /**
     * Returns the {@link FoundReport} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code FoundReport} with the
     *                           provided ID.
     */
    @ApiMethod(
            name = "foundReport.lightGet",
            path = "foundReport/lightGet",
            httpMethod = ApiMethod.HttpMethod.GET)
    public FoundReport lightGet(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting FoundReport with ID: " + id);
        FoundReport foundReport = ofy().load().type(FoundReport.class).id(id).now();
        if (foundReport == null) {
            throw new NotFoundException("Could not find FoundReport with ID: " + id);
        }
        //Reduce file size

        return foundReport;
    }

    /**
     * Returns the {@link FoundReport} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the image of entity with the corresponding ID
     * @throws NotFoundException if there is no {@code FoundReport} with the
     *                           provided ID.
     */
    @ApiMethod(
            name = "foundReport.get",
            path = "foundReport/get",
            httpMethod = ApiMethod.HttpMethod.GET)
    public FoundReport get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting FoundReport with ID: " + id);
        FoundReport foundReport = ofy().load().type(FoundReport.class).id(id).now();
        if (foundReport == null) {
            throw new NotFoundException("Could not find FoundReport with ID: " + id);
        }
        return foundReport;
    }

    /**
     * Returns the {@link FoundReport} with the corresponding ID.
     *
     * @param user the google account user
     * @return the url of image to be stored
     * @throws OAuthRequestException
     */
    @ApiMethod(
            name = "foundReport.newImageURL",
            path = "foundReport/newImageURL",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Response newImageURL(User user) throws OAuthRequestException {
        if(user == null) {
            logger.exiting(FoundReportEndpoint.class.toString(), "Not logged in.");
            throw new OAuthRequestException("You need to login to file reports.");
        }
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        String url = blobstoreService.createUploadUrl("/uploadImage");
        logger.info("Create new Image URL: " + url);
        Response response = new Response();
        response.setStringResponse(url);
        return response;
    }

    /**
     * Inserts a new {@code FoundReport}.
     */
    @ApiMethod(
            name = "foundReport.insert",
            path = "foundReport/insert",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void insert(FoundReport foundReport, User user) throws OAuthRequestException, BadRequestException {

        if (user == null) {
            logger.exiting(FoundReportEndpoint.class.toString(), "Not logged in.");
            throw new OAuthRequestException("You need to login to file reports.");
        }

        if (foundReport.getId() != null) {
            throw new BadRequestException("Invalid report object.");
        }

        logger.info("For user: " + user.getEmail());
        foundReport.setUserId(user.getUserId());
        foundReport.setUserEmail(user.getEmail());

        ofy().save().entity(foundReport).now();

        SearchHelper.addReport(foundReport);
        Queue queue = QueueFactory.getQueue(MATCH_QUEUE);
        queue.add(TaskOptions.Builder.withPayload
                (new FoundMatchDeferredTask(foundReport.getId())));
    }

    /**
     * Updates an existing {@code FoundReport}.
     *
     * @param foundReport the desired state of the entity
     * @param user          the user of the entity to be updated
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an
     *                           existing
     *                           {@code FoundReport}
     */
    @ApiMethod(
            name = "foundReport.update",
            path = "foundReport/update",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void update(final FoundReport foundReport, User user)
            throws OAuthRequestException, BadRequestException, NotFoundException {
        if (user == null) {
            logger.warning("Not logged in.");
            throw new OAuthRequestException("You need to login to modify reports.");
        }

        if (foundReport.getId() == null) {
            throw new BadRequestException("Invalid report object.");
        }

        if(user.getUserId() != foundReport.getUserId()) {
            throw new BadRequestException("Invalid User id.");
        }

        final Long id = foundReport.getId();
        //transaction
        ofy().transact(new Work<VoidWork>() {
            @Override
            public VoidWork run() {
                if (ofy().load().type(FoundReport.class).id(id).now() != null) {
                    ofy().save().entity(foundReport).now();
                } else {
                    logger.warning("Update FoundReport: id not found.");
                }
                return null;
            }
        });
    }

    /**
     * Deletes the specified {@code FoundReport}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an
     *                           existing
     *                           {@code FoundReport}
     */
    @ApiMethod(
            name = "foundReport.remove",
            path = "foundReport/remove",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") final Long id, final User user) throws NotFoundException, OAuthRequestException {

        if (user == null) {
            throw new OAuthRequestException("You need to login to modify " +
                    "reports.");
        }

        //transaction
        ofy().transact(new Work<VoidWork>() {
            @Override
            public VoidWork run() {
                FoundReport record = null;
                if ((record = ofy().load().type(FoundReport.class).id(id).now()) != null) {
                    if (user.getUserId() == record.getUserId()) {
                        ofy().delete().type(FoundReport.class).id(id).now();
                    } else {
                        logger.warning("Delete FoundReport: user id not match.");
                    }
                } else {
                    logger.warning("Delete FoundReport: id not found.");
                }
                return null;
            }
        });
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page
     * token/cursor
     */
    @ApiMethod(
            name = "foundReport.list",
            path = "foundReport/list",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<FoundReport> list(@Nullable @Named("cursor") String cursor,
                                                @Nullable @Named("limit") Integer limit) {
        /**
         * When creating projection queries, the ID property should not be projected.
         * It is still included in the resulting entity, but including it in the
         * projections causes the query to turn up no results. The ID property is
         * not stored the same way as the other property fields in the entity.
         *
         * Also, projection requires index, so to prevent consumption of much resource
         * It is better not to use projection.
         */
        Query<FoundReport> query = ofy().load().type(FoundReport.class)
//                .project("title", "description", "created", "userId", "userEmail", "photoUrl",
//                        "timeFound", "location", "returned")
                .order("-created")
                .limit(limit == null ? DEFAULT_LIST_LIMIT : limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<FoundReport> queryIterator = query.iterator();
        List<FoundReport> foundReportList = new ArrayList<FoundReport>();
        while (queryIterator.hasNext()) {
            foundReportList.add(queryIterator.next());
        }
        return CollectionResponse.<FoundReport>builder().setItems
                (foundReportList).setNextPageToken(queryIterator.getCursor()
                .toWebSafeString()).build();
    }


}