package edu.gatech.wguo64.lostandfoundandroidapp.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.GeoPt;
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
public class LostReportEndpoint {

    protected static final String DOC_INDEX = "lostReport";
    private static final String MATCH_QUEUE = "matchLostReport";
    private static final Logger logger = Logger.getLogger(LostReportEndpoint
            .class.getName());
    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper.
        // See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(LostReport.class);
    }

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
            path = "lostReport/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public LostReport get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting LostReport with ID: " + id);
        LostReport lostReport = ofy().load().type(LostReport.class).id(id)
                .now();
        if (lostReport == null) {
            throw new NotFoundException("Could not find LostReport with ID: "
                    + id);
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
    public LostReport insert(LostReport lostReport, User user) throws
            OAuthRequestException, BadRequestException {
        if (user == null) {
            logger.exiting(LostReportEndpoint.class.toString(), "Not logged " +
                    "in.");
            throw new OAuthRequestException("You need to login to file " +
                    "reports.");
        }
        if (lostReport.getId() != null) {
            throw new BadRequestException("Invalid report object.");
        }
        user = UserHelper.fixUser(user);
        logger.info("For user: " + user.getEmail());
        lostReport.setUserId(user.getEmail());
        lostReport.setUserNickname(user.getNickname());

        ofy().save().entity(lostReport).now();
        logger.info("Created LostReport.");

        Document.Builder docBuilder = Document.newBuilder().setId(lostReport
                .getId() + "");
        docBuilder.addField(Field.newBuilder().setName("title").setText
                (lostReport.getTitle()));
        docBuilder.addField(Field.newBuilder().setName("description").setText
                (lostReport.getDescription()));
        docBuilder.addField(Field.newBuilder().setName("timeLost").setDate
                (lostReport.getTimeLost()));
        docBuilder.addField(Field.newBuilder().setName("created").setDate
                (lostReport.getCreated()));
        docBuilder.addFacet(Facet.withAtom("found", "false"));
        for (GeoPt location : lostReport.getLocations()) {
            docBuilder.addField(Field.newBuilder().setName("location")
                    .setGeoPoint(new GeoPoint(location.getLatitude(),
                            location.getLongitude())));
        }

        Document doc = docBuilder.build();
        IndexSpec indexSpec = IndexSpec.newBuilder().setName(DOC_INDEX)
                .build();
        Index index = SearchServiceFactory.getSearchService().getIndex
                (indexSpec);
        try {
            index.put(doc);
            Queue queue = QueueFactory.getQueue(MATCH_QUEUE);
            queue.add(TaskOptions.Builder.withPayload
                    (new LostMatchDeferredTask(lostReport.getId())));
        } catch (PutException e) {
            // TODO should retry
            logger.throwing(this.getClass().toString(), "insert", e);
        }
        return ofy().load().entity(lostReport).now();
    }

    @ApiMethod(
            name = "lostReport.test",
            path = "lostReport/test/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public void test(@Named("id") Long id) {
        Queue queue = QueueFactory.getQueue(MATCH_QUEUE);
        queue.add(TaskOptions.Builder.withPayload
                (new LostMatchDeferredTask(id)));
    }


    /**
     * Updates an existing {@code LostReport}.
     *
     * @param id         the ID of the entity to be updated
     * @param lostReport the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an
     *                           existing
     *                           {@code LostReport}
     */
    @ApiMethod(
            name = "lostReport.update",
            path = "lostReport/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public LostReport update(@Named("id") Long id, LostReport
            lostReport, User user) throws NotFoundException,
            BadRequestException, OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("You need to login to modify " +
                    "reports.");
        }
        user = UserHelper.fixUser(user);
        if (!id.equals(lostReport.getId())) {
            throw new BadRequestException("Invalid report object.");
        }
        checkOwns(id, user);
        ofy().save().entity(lostReport).now();
        logger.info("Updated LostReport: " + lostReport);
        return ofy().load().entity(lostReport).now();
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
            path = "lostReport/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id, User user) throws
            NotFoundException, OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("You need to login to modify " +
                    "reports.");
        }
        user = UserHelper.fixUser(user);
        logger.info("For user: " + user.getEmail());
        checkOwns(id, user);
        ofy().delete().type(LostReport.class).id(id).now();
        logger.info("Deleted LostReport with ID: " + id);
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
            path = "lostReport",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<LostReport> list(@Nullable @Named("cursor")
                                               String cursor, @Nullable
                                               @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<LostReport> query = ofy().load().type(LostReport.class).limit
                (limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<LostReport> queryIterator = query.iterator();
        List<LostReport> lostReportList = new ArrayList<LostReport>(limit);
        while (queryIterator.hasNext()) {
            lostReportList.add(queryIterator.next());
        }
        return CollectionResponse.<LostReport>builder().setItems
                (lostReportList).setNextPageToken(queryIterator.getCursor()
                .toWebSafeString()).build();
    }

    @ApiMethod(
            name = "lostReport.myReports.list",
            path = "lostReport/myReports",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<LostReport> listUserReports(@Nullable @Named
            ("cursor") String cursor, @Nullable @Named("limit") Integer
                                                                  limit,
                                                          User user) throws
            OAuthRequestException {
        if (user == null) {
            logger.exiting(LostReportEndpoint.class.toString(), "Not logged " +
                    "in.");
            throw new OAuthRequestException("You need to login to list your " +
                    "reports.");
        }
        user = UserHelper.fixUser(user);
        logger.info("For user: " + user.getEmail());

        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<LostReport> query = ofy().load().type(LostReport.class).filter
                ("userId", user.getEmail()).order("-created").limit(limit);

        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<LostReport> queryIterator = query.iterator();
        List<LostReport> lostReportList = new ArrayList<LostReport>(limit);
        while (queryIterator.hasNext()) {
            lostReportList.add(queryIterator.next());
        }
        return CollectionResponse.<LostReport>builder().setItems
                (lostReportList).setNextPageToken(queryIterator.getCursor()
                .toWebSafeString()).build();
    }

    @ApiMethod(
            name = "lostReport.search",
            path = "lostReport/search",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<LostReport> search(@Nonnull @Named("q") String
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

        List<LostReport> lostReportList = new ArrayList<>(results
                .getNumberReturned());

        for (ScoredDocument match : results.getResults()) {
            LostReport lostReport = ofy().load().type(LostReport
                    .class).id(Long.parseLong(match.getId())).now();
            if (lostReport != null)
                lostReportList.add(lostReport);
        }

        return CollectionResponse.<LostReport>builder().setItems
                (lostReportList).build();

    }

    private void checkOwns(Long id, User user) throws NotFoundException,
            OAuthRequestException {
        try {
            LostReport report = ofy().load().type(LostReport.class).id(id)
                    .safe();
            if (!report.getUserId().equals(user.getEmail())) {
                throw new OAuthRequestException("You do not have the " +
                        "premission to modify this report.");
            }
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find LostReport with ID: "
                    + id);
        }
    }

    public static class LostMatchDeferredTask implements DeferredTask {

        final private Long id;

        public LostMatchDeferredTask(Long id) {
            this.id = id;
        }

        @Override
        public void run() {
            // TODO add time lost and creation to query
            LostReport lostReport = ofy().load().type(LostReport.class).id
                    (id)
                    .now();
            if (lostReport == null) {
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

            StringBuilder sb = new StringBuilder("(" + QueryHelper.sanitize
                    (lostReport.getTitle() + " " +
                            lostReport.getDescription())
                    + ")");
            if (lostReport.getLocations().size() > 0) {
                sb.append(" AND (");
            }
            for (int i = 0; i < lostReport.getLocations().size(); ++i) {
                if (i > 0) {
                    sb.append(" OR ");
                }
                GeoPt location = lostReport.getLocations().get(i);
                sb.append("distance(location, geopoint(" +
                        location.getLatitude() + "," +
                        location.getLongitude() + ")) < 26");
            }
            if (lostReport.getLocations().size() > 0) {
                sb.append(")");
            }
            com.google.appengine.api.search.Query query = com.google.appengine
                    .api.search.Query.newBuilder()
                    .setOptions(queryOptions).build(sb.toString());

            IndexSpec indexSpec = IndexSpec.newBuilder().setName
                    (FoundReportEndpoint.DOC_INDEX)
                    .build();
            Index index = SearchServiceFactory.getSearchService().getIndex
                    (indexSpec);

            Results<ScoredDocument> results = index.search(query);

            ArrayList<Long> ids = new ArrayList<>();
            Set<String> users = new HashSet<>();
            for (ScoredDocument doc : results.getResults()) {
                ids.add(Long.parseLong(doc.getId()));
                FoundReport foundReport = ofy().load().type(FoundReport
                        .class).id(Long.parseLong(doc.getId())).now();
                if (foundReport != null) {
                    if (!foundReport.getReturned())
                        users.add(foundReport.getUserId());
                }

            }

            if (!users.isEmpty()) {
                NotificationHelper.notify(users, "Lost" + lostReport.getId());

            }

            logger.warning(String.format("Found %d results matching lost " +
                    "report with id " +
                    "%s: [%s]", results.getNumberFound(), id, ids));
        }
    }
}