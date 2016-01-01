package edu.gatech.wguo64.lostandfoundandroidapp.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Facet;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.MatchScorer;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.SortExpression;
import com.google.appengine.api.search.SortOptions;
import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;

import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.FoundReport;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.LostReport;

import static com.googlecode.objectify.ObjectifyService.ofy;

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
public class FoundReportEndpoint {

    public static final String DOC_INDEX = "foundReport";
    private static final Logger logger = Logger.getLogger(FoundReportEndpoint
            .class.getName());
    private static final int DEFAULT_LIST_LIMIT = 20;
    private static final String MATCH_QUEUE = "matchFoundReport";

    static {
        // Typically you would register this inside an OfyServive wrapper.
        // See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(FoundReport.class);
    }

    /**
     * Returns the {@link FoundReport} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code FoundReport} with the
     *                           provided ID.
     */
    @ApiMethod(
            name = "foundReport.get",
            path = "foundReport/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public FoundReport get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting FoundReport with ID: " + id);
        FoundReport foundReport = ofy().load().type(FoundReport.class).id(id)
                .now();
        if (foundReport == null) {
            throw new NotFoundException("Could not find FoundReport with ID: " +
                    "" + id);
        }
        return foundReport;
    }

    /**
     * Inserts a new {@code FoundReport}.
     */
    @ApiMethod(
            name = "foundReport.insert",
            path = "foundReport",
            httpMethod = ApiMethod.HttpMethod.POST)
    public FoundReport insert(FoundReport foundReport, User user) throws
            OAuthRequestException, BadRequestException {
        if (user == null) {
            logger.exiting(FoundReportEndpoint.class.toString(), "Not logged " +
                    "in.");
            throw new OAuthRequestException("You need to login to file " +
                    "reports.");
        }
        if (foundReport.getId() != null) {
            throw new BadRequestException("Invalid report object.");
        }
        user = UserHelper.fixUser(user);
        logger.info("For user: " + user.getEmail());
        foundReport.setUserId(user.getEmail());
        foundReport.setUserNickname(user.getNickname());

        ofy().save().entity(foundReport).now();
        logger.info("Created FoundReport with ID: " + foundReport.getId());


        Document.Builder docBuilder = Document.newBuilder().setId(foundReport
                .getId() + "");
        docBuilder.addField(Field.newBuilder().setName("title").setText
                (foundReport.getTitle()));
        docBuilder.addField(Field.newBuilder().setName("description").setText
                (foundReport.getDescription()));
        docBuilder.addField(Field.newBuilder().setName("timeFound").setDate
                (foundReport.getTimeFound()));
        docBuilder.addField(Field.newBuilder().setName("created").setDate
                (foundReport.getCreated()));
        docBuilder.addFacet(Facet.withAtom("returned", "false"));
        docBuilder.addField(Field.newBuilder().setName("location")
                .setGeoPoint(new GeoPoint(foundReport.getLocation()
                        .getLatitude(), foundReport.getLocation()
                        .getLongitude())));

        Document doc = docBuilder.build();
        IndexSpec indexSpec = IndexSpec.newBuilder().setName(DOC_INDEX)
                .build();
        Index index = SearchServiceFactory.getSearchService().getIndex
                (indexSpec);
        try {
            index.put(doc);
            Queue queue = QueueFactory.getQueue(MATCH_QUEUE);
            queue.add(TaskOptions.Builder.withPayload
                    (new FoundMatchDeferredTask(foundReport.getId())));
        } catch (PutException e) {
            // TODO should retry
            logger.throwing(this.getClass().toString(), "insert", e);
        }

        return ofy().load().entity(foundReport).now();
    }

    @ApiMethod(
            name = "foundReport.test",
            path = "foundReport/test/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public void test(@Named("id") Long id) {
        Queue queue = QueueFactory.getQueue(MATCH_QUEUE);
        queue.add(TaskOptions.Builder.withPayload
                (new FoundMatchDeferredTask(id)));
    }

    /**
     * Updates an existing {@code FoundReport}.
     *
     * @param id          the ID of the entity to be updated
     * @param foundReport the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an
     *                           existing
     *                           {@code FoundReport}
     */
    @ApiMethod(
            name = "foundReport.update",
            path = "foundReport/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public FoundReport update(@Named("id") Long id, FoundReport foundReport)
            throws NotFoundException {
        // TODO: You should validate your ID parameter against your
        // resource's ID here.
        checkExists(id);
        ofy().save().entity(foundReport).now();
        logger.info("Updated FoundReport: " + foundReport);
        return ofy().load().entity(foundReport).now();
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
            path = "foundReport/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws
            NotFoundException {
        checkExists(id);
        ofy().delete().type(FoundReport.class).id(id).now();
        logger.info("Deleted FoundReport with ID: " + id);
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
            path = "foundReport",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<FoundReport> list(@Nullable @Named("cursor")
                                                String cursor, @Nullable
                                                @Named("limit") Integer limit) {
        //TODO sort by descending order of created
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<FoundReport> query = ofy().load().type(FoundReport.class).limit
                (limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<FoundReport> queryIterator = query.iterator();
        List<FoundReport> foundReportList = new ArrayList<FoundReport>(limit);
        while (queryIterator.hasNext()) {
            foundReportList.add(queryIterator.next());
        }
        return CollectionResponse.<FoundReport>builder().setItems
                (foundReportList).setNextPageToken(queryIterator.getCursor()
                .toWebSafeString()).build();
    }

    @ApiMethod(
            name = "foundReport.myReports.list",
            path = "foundReport/myReports",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<FoundReport> listUserReports(@Nullable @Named
            ("cursor") String cursor, @Nullable @Named("limit") Integer
                                                                   limit,
                                                           User user) throws
            OAuthRequestException {
        if (user == null) {
            logger.exiting(FoundReportEndpoint.class.toString(), "Not logged " +
                    "in.");
            throw new OAuthRequestException("You need to login to list your " +
                    "reports.");
        }
        user = UserHelper.fixUser(user);
        logger.info("For user: " + user.getEmail());

        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<FoundReport> query = ofy().load().type(FoundReport.class).filter
                ("userId", user
                        .getEmail()).order("-created").limit(limit);

        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<FoundReport> queryIterator = query.iterator();
        List<FoundReport> foundReportList = new ArrayList<FoundReport>(limit);
        while (queryIterator.hasNext()) {
            foundReportList.add(queryIterator.next());
        }
        return CollectionResponse.<FoundReport>builder().setItems
                (foundReportList).setNextPageToken(queryIterator.getCursor()
                .toWebSafeString()).build();
    }

    @ApiMethod(
            name = "foundReport.search",
            path = "foundReport/search",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<FoundReport> search(@Nonnull @Named("q") String
                                                          queryString, @Nullable
                                                  @Named("cursor") String
                                                          cursor, @Nullable
                                                  @Named("limit") Integer
                                                          limit) {
        // TODO may be use cursor and limit
        SortOptions sortOptions = SortOptions.newBuilder()
                .setMatchScorer(MatchScorer.newBuilder().build())
                .addSortExpression(SortExpression.newBuilder().setExpression
                        ("_score"))
                .addSortExpression(SortExpression.newBuilder().setExpression
                        ("_rank"))
                .addSortExpression(SortExpression.newBuilder()
                        .setExpression("created")
                        .setDirection(SortExpression.SortDirection.DESCENDING))
                .setLimit(DEFAULT_LIST_LIMIT)
                .build();

        QueryOptions queryOptions = QueryOptions.newBuilder()
                .setLimit(DEFAULT_LIST_LIMIT)
                .setReturningIdsOnly(true)
                .setSortOptions(sortOptions)
                .build();

        com.google.appengine.api.search.Query query = com.google.appengine
                .api.search.Query.newBuilder()
                .setOptions(queryOptions).build(QueryHelper.sanitize
                        (queryString));


        IndexSpec indexSpec = IndexSpec.newBuilder().setName(DOC_INDEX)
                .build();
        Index index = SearchServiceFactory.getSearchService().getIndex
                (indexSpec);

        Results<ScoredDocument> results = index.search(query);

        List<FoundReport> foundReportList = new ArrayList<>(results
                .getNumberReturned());

        for (ScoredDocument match : results.getResults()) {
            FoundReport foundReport = ofy().load().type(FoundReport
                    .class).id(Long.parseLong(match.getId())).now();
            if (foundReport != null)
                foundReportList.add(foundReport);
        }

        return CollectionResponse.<FoundReport>builder().setItems
                (foundReportList).build();

    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            ofy().load().type(FoundReport.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find FoundReport with ID: " +
                    "" + id);
        }
    }

    public static class FoundMatchDeferredTask implements DeferredTask {
        // TODO add time lost and creation to query
        final private Long id;

        public FoundMatchDeferredTask(Long id) {
            this.id = id;
        }

        @Override
        public void run() {
            FoundReport foundReport = ofy().load().type(FoundReport.class).id
                    (id)
                    .now();
            if (foundReport == null) {
                return;
            }
            SortOptions sortOptions = SortOptions.newBuilder()
                    .setMatchScorer(MatchScorer.newBuilder().build())
                    .addSortExpression(SortExpression.newBuilder().setExpression
                            ("_score"))
                    .addSortExpression(SortExpression.newBuilder().setExpression
                            ("_rank"))
                    .addSortExpression(SortExpression.newBuilder()
                            .setExpression("created")
                            .setDirection(SortExpression.SortDirection
                                    .DESCENDING))
                    .setLimit(DEFAULT_LIST_LIMIT)
                    .build();

            QueryOptions queryOptions = QueryOptions.newBuilder()
                    .setLimit(DEFAULT_LIST_LIMIT)
                    .setReturningIdsOnly(true)
                    .setSortOptions(sortOptions)
                    .build();

            com.google.appengine.api.search.Query query = com.google.appengine
                    .api.search.Query.newBuilder()
                    .setOptions(queryOptions).build("(" + QueryHelper.sanitize
                            (foundReport.getTitle() + " " +
                                    foundReport.getDescription())
                            + ") AND distance(location, geopoint(" +
                            foundReport.getLocation()
                                    .getLatitude() + "," +
                            foundReport.getLocation().getLongitude()
                            + ")) < " +
                            "26");

            IndexSpec indexSpec = IndexSpec.newBuilder().setName
                    (LostReportEndpoint.DOC_INDEX)
                    .build();
            Index index = SearchServiceFactory.getSearchService().getIndex
                    (indexSpec);

            Results<ScoredDocument> results = index.search(query);

            ArrayList<Long> ids = new ArrayList<>();
            Set<String> users = new HashSet<>();
            for (ScoredDocument doc : results.getResults()) {
                ids.add(Long.parseLong(doc.getId()));
                LostReport lostReport = ofy().load().type(LostReport
                        .class).id(Long.parseLong(doc.getId())).now();
                if (lostReport != null) {
                    if (!lostReport.getFound())
                        users.add(lostReport.getUserId());
                }

            }


            if (!users.isEmpty()) {
                NotificationHelper.notify(users, "Found" + foundReport
                        .getId());
            }

            logger.warning(String.format("Found %d results matching found " +
                    "report with id " +
                    "%s: [%s]", results.getNumberFound(), id, ids));
        }
    }
}