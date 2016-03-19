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
import com.googlecode.objectify.Key;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import edu.gatech.wguo64.lostandfoundandroidapp.backend.constants.Credentials;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.helpers.SearchHelper;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.Comment;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.Report;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.Response;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.tasks.FoundMatchDeferredTask;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.tasks.LostMatchDeferredTask;

import static edu.gatech.wguo64.lostandfoundandroidapp.backend.helpers.OfyService.ofy;

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
public class ReportEndpoint {

    private static final Logger logger = Logger.getLogger(ReportEndpoint.class.getName());
    private static final int DEFAULT_LIST_LIMIT = 5;
    //expression [a-zA-Z\\d-]{1,100}
    private static final String MATCH_QUEUE = "matchQueue";

    /**
     * Returns the {@link Report} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the image of entity with the corresponding ID
     * @throws NotFoundException if there is no {@code FoundReport} with the
     *                           provided ID.
     */
    @ApiMethod(
            name = "report.get",
            path = "report/get",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Report get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting FoundReport with ID: " + id);
        Report report = ofy().load().type(Report.class).id(id).now();
        if (report == null) {
            throw new NotFoundException("Could not find FoundReport with ID: " + id);
        }
        return report;
    }

    /**
     * Inserts a new {@code FoundReport}.
     */
    @ApiMethod(
            name = "report.insert",
            path = "report/insert",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void insert(Report report, User user) throws OAuthRequestException, BadRequestException {

        if (user == null) {
            logger.exiting(ReportEndpoint.class.toString(), "Not logged in.");
            throw new OAuthRequestException("You need to login to file reports.");
        }

        if (report.getId() != null) {
            throw new BadRequestException("Invalid report object.");
        }

        logger.info("For user: " + user.getEmail());
        report.setUserId(user.getUserId());
        report.setUserEmail(user.getEmail());

        ofy().save().entity(report).now();

        SearchHelper.addReport(report);
        Queue queue = QueueFactory.getQueue(MATCH_QUEUE);
        if(report.getReportType()) {
            queue.add(TaskOptions.Builder.withPayload
                    (new LostMatchDeferredTask(report.getId())));
        } else {
            queue.add(TaskOptions.Builder.withPayload
                    (new FoundMatchDeferredTask(report.getId())));
        }
    }

    /**
     *
     * @param id the id of report
     * @param user          the user of the entity to be updated
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an
     *                           existing
     *                           {@code FoundReport}
     */
    @ApiMethod(
            name = "report.updateStatus",
            path = "report/updateStatus",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void updateStatus(@Named("id") Long id, User user)
            throws OAuthRequestException, BadRequestException, NotFoundException {
        if (user == null) {
            logger.warning("Not logged in.");
            throw new OAuthRequestException("You need to login to modify reports.");
        }

        if (id == null) {
            throw new BadRequestException("Invalid report object.");
        }

        Report report = ofy().load().type(Report.class).id(id).now();
        if(report == null) {
            logger.warning("Update LostReport Status: id not found.");
            return;
        }
        report.setStatus(true);
        ofy().save().entities(report).now();
    }

    /**
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an
     *                           existing
     */
    @ApiMethod(
            name = "report.remove",
            path = "report/remove",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id, User user) throws NotFoundException,
            OAuthRequestException, BadRequestException {

        if (user == null) {
            throw new OAuthRequestException("You need to login to modify " +
                    "reports.");
        }

        if (id == null) {
            throw new BadRequestException("Invalid report object.");
        }
        Report report = ofy().load().type(Report.class).id(id).now();
        if(report == null) {
            logger.warning("Delete LostReport Status: id not found.");
            return;
        }
        // Remove all comments
        List<Comment> commentList = report.getComments();
        for(Comment c : commentList) {
            ofy().delete().type(Comment.class).id(c.getId());
        }

        ofy().delete().type(Report.class).id(id).now();
        SearchHelper.removeReport(id);
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
            name = "report.listFoundReport",
            path = "report/listFoundReport",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Report> listFoundReport(@Nullable @Named("cursor") String cursor,
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
        Query<Report> query = ofy().load().type(Report.class)
                .filter("reportType", false) // Lost Report = true; Found Report = false
//                .project("title", "description", "created", "userId", "userEmail", "photoUrl",
//                        "timeFound", "location", "returned")
                .order("-created")
                .limit(limit == null ? DEFAULT_LIST_LIMIT : limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Report> queryIterator = query.iterator();
        List<Report> reports = new ArrayList<>();
        while (queryIterator.hasNext()) {
            reports.add(queryIterator.next());
        }
        return CollectionResponse.<Report>builder().setItems
                (reports).setNextPageToken(queryIterator.getCursor()
                .toWebSafeString()).build();
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
            name = "report.listLostReport",
            path = "report/listLostReport",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Report> listLostReport(@Nullable @Named("cursor") String cursor,
                                                      @Nullable @Named("limit") Integer limit) {

        Query<Report> query = ofy().load().type(Report.class)
                .filter("reportType", true) // Lost Report = true; Found Report = false
                .order("-created")
                .limit(limit == null ? DEFAULT_LIST_LIMIT : limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Report> queryIterator = query.iterator();
        List<Report> reports = new ArrayList<>();
        while (queryIterator.hasNext()) {
            reports.add(queryIterator.next());
        }
        return CollectionResponse.<Report>builder().setItems
                (reports).setNextPageToken(queryIterator.getCursor()
                .toWebSafeString()).build();
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
            name = "report.listMyReport",
            path = "report/listMyReport",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Report> listMyReport(@Nullable @Named ("cursor") String cursor,
                                                   @Nullable @Named("limit") Integer limit,
                                                   final User user) throws OAuthRequestException {
        if (user == null) {
            logger.exiting(ReportEndpoint.class.toString(), "Not logged in.");
            throw new OAuthRequestException("You need to login to list your reports.");
        }
        logger.info("For user: " + user.getEmail());

        Query<Report> query = ofy().load().type(Report.class)
                .filter("userEmail", user.getEmail())
                .order("-created")
                .limit(limit == null ? DEFAULT_LIST_LIMIT : limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Report> queryIterator = query.iterator();
        List<Report> reports = new ArrayList<>();
        while (queryIterator.hasNext()) {
            reports.add(queryIterator.next());
        }
        return CollectionResponse.<Report>builder().setItems
                (reports).setNextPageToken(queryIterator.getCursor()
                .toWebSafeString()).build();
    }

    /**
     * @param comment  the id of the report
     * @param user
     * @return a response that encapsulates the result list and the next page
     * @throws OAuthRequestException
     */
    @ApiMethod(
            name = "report.insertComment",
            path = "report/insertComment",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Comment insertComment(@Named("reportId") Long reportId, Comment comment
            , User user) throws OAuthRequestException {
        if (user == null) {
            logger.exiting(ReportEndpoint.class.toString(), "Not logged " +
                    "in.");
            throw new OAuthRequestException("You need to login to list your " +
                    "reports.");
        }
        if (comment == null) {
            logger.exiting(ReportEndpoint.class.toString(), "Empty comment");
            throw new OAuthRequestException("Empty comment");
        }
        logger.info("For user: " + user.getEmail());
        Key<Comment> key = ofy().save().entity(comment).now();
        Report report = ofy().load().type(Report.class).id(reportId).now();
        if(report == null) {
            logger.warning("Update LostReport Status: id not found.");
            return null;
        }

        report.addComment(key);
        ofy().save().entity(report).now();
        return comment;
    }

    /**
     *
     * @param id
     * @return
     * @throws OAuthRequestException
     */
    @ApiMethod(
            name = "report.getComments",
            path = "report/getComments",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Comment> getComments(@Named("id") Long id) throws OAuthRequestException {

        Report report = ofy().load().type(Report.class).id(id).now();
        if(report == null) {
            logger.warning("Update LostReport Status: id not found.");
            return null;
        }

        return CollectionResponse.<Comment>builder().setItems(report.getComments()).build();
    }

    /**
     * @param query
     * @param cursor used for pagination to determine which page to return
     * @param user
     * @return a response that encapsulates the result list and the next page
     * @throws OAuthRequestException
     */
    @ApiMethod(
            name = "report.search",
            path = "report/search",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Report> search(
            @Named ("query") final String query,
            @Nullable @Named ("cursor") final String cursor,
            final User user) throws OAuthRequestException {
        if (user == null) {
            logger.exiting(ReportEndpoint.class.toString(), "Not logged in.");
            throw new OAuthRequestException("You need to login to list your reports.");
        }
        logger.info("For user: " + user.getEmail());

        return SearchHelper.searchByText(query, cursor);
    }
}