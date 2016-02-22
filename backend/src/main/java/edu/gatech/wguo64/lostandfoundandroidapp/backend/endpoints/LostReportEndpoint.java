package edu.gatech.wguo64.lostandfoundandroidapp.backend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
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
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.LostReport;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.SearchHelper;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.tasks.FoundMatchDeferredTask;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.tasks.LostMatchDeferredTask;

import static edu.gatech.wguo64.lostandfoundandroidapp.backend.OfyService.ofy;

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
public class LostReportEndpoint {

    private static final Logger logger = Logger.getLogger(LostReportEndpoint
            .class.getName());
    private static final int DEFAULT_LIST_LIMIT = 5;
    private static final String MATCH_QUEUE = "matchQueue";

    /**
     * Returns the {@link LostReport} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code LostReport} with the
     *                           provided ID.
     */
    @ApiMethod(
            name = "lostReport.get",
            path = "lostReport/get",
            httpMethod = ApiMethod.HttpMethod.GET)
    public LostReport get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting LostReport with ID: " + id);
        LostReport lostReport = ofy().load().type(LostReport.class).id(id).now();

        if (lostReport == null) {
            logger.warning("Could not find LostReport with ID: " + id);
            throw new NotFoundException("Could not find LostReport with ID: " + id);
        }

        return lostReport;
    }

    /**
     * Inserts a new {@code LostReport}.
     */
    @ApiMethod(
            name = "lostReport.insert",
            path = "lostReport",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void insert(LostReport lostReport, User user) throws OAuthRequestException, BadRequestException {

        if (user == null) {
            logger.warning("Not logged in.");
            throw new OAuthRequestException("You need to login to file reports.");
        }

        if (lostReport.getId() != null) {
            throw new BadRequestException("Invalid report object.");
        }

        //fix report
        lostReport.setUserId(user.getUserId());
        lostReport.setUserEmail(user.getEmail());
        //save
        ofy().save().entity(lostReport).now();

        SearchHelper.addReport(lostReport);
        Queue queue = QueueFactory.getQueue(MATCH_QUEUE);
        queue.add(TaskOptions.Builder.withPayload
                (new LostMatchDeferredTask(lostReport.getId())));
    }


    /**
     * Updates an existing {@code LostReport}.
     *
     * @param lostReport the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an
     *                           existing
     *                           {@code LostReport}
     */
    @ApiMethod(
            name = "lostReport.update",
            path = "lostReport/update",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void update(final LostReport lostReport, User user)
            throws NotFoundException, BadRequestException, OAuthRequestException {

        if (user == null) {
            logger.warning("Not logged in.");
            throw new OAuthRequestException("You need to login to modify reports.");
        }

        if (lostReport.getId() == null) {
            throw new BadRequestException("Invalid report object.");
        }

        if(user.getUserId() != lostReport.getUserId()) {
            throw new BadRequestException("Invalid User id.");
        }

        final Long id = lostReport.getId();
        //transaction
        ofy().transact(new Work<VoidWork>() {
            @Override
            public VoidWork run() {
                if (ofy().load().type(LostReport.class).id(id).now() != null) {
                    ofy().save().entity(lostReport).now();
                } else {
                    logger.warning("Update LostReport: id not found.");
                }
                return null;
            }
        });
    }

    /**
     * Deletes the specified {@code LostReport}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an
     *                           existing
     *                           {@code LostReport}
     */
    @ApiMethod(
            name = "lostReport.remove",
            path = "lostReport/remove",
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
                LostReport record = null;
                if ((record = ofy().load().type(LostReport.class).id(id).now()) != null) {
                    if (user.getUserId() == record.getUserId()) {
                        ofy().delete().type(LostReport.class).id(id).now();
                    } else {
                        logger.warning("Delete LostReport: user id not match.");
                    }
                } else {
                    logger.warning("Delete LostReport: id not found.");
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
            name = "lostReport.list",
            path = "lostReport/list",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<LostReport> list(@Nullable @Named("cursor") String cursor,
                                               @Nullable @Named("limit") Integer limit) {

        Query<LostReport> query = ofy().load().type(LostReport.class).order("-created")
                .limit(limit == null ? DEFAULT_LIST_LIMIT : limit);

        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        QueryResultIterator<LostReport> queryIterator = query.iterator();
        List<LostReport> lostReportList = new ArrayList<>();
        while (queryIterator.hasNext()) {
            lostReportList.add(queryIterator.next());
        }
        return CollectionResponse.<LostReport>builder().setItems
                (lostReportList).setNextPageToken(queryIterator.getCursor()
                .toWebSafeString()).build();
    }

    @ApiMethod(
            name = "lostReport.lightList",
            path = "lostReport/lightList",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<LostReport> lightList(@Nullable @Named("cursor")
                                               final String cursor, @Nullable
                                               @Named("limit") final Integer limit) {

        Query<LostReport> query = ofy().load().type(LostReport.class)
                .project("id", "title", "created", "userId", "userNickname",
                        "userEmail", "found")
                .limit(limit == null ? DEFAULT_LIST_LIMIT : limit);

        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        QueryResultIterator<LostReport> queryIterator = query.iterator();
        List<LostReport> lostReportList = new ArrayList<>();
        while (queryIterator.hasNext()) {
            lostReportList.add(queryIterator.next());
        }
        return CollectionResponse.<LostReport>builder().setItems
                (lostReportList).setNextPageToken(queryIterator.getCursor()
                .toWebSafeString()).build();

    }

}