package edu.gatech.wguo64.lostandfoundandroidapp.backend;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.search.Cursor;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.ISearchServiceFactory;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.MatchScorer;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.SortExpression;
import com.google.appengine.api.search.SortOptions;
import com.google.appengine.api.search.StatusCode;
import com.google.appengine.repackaged.com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;

import edu.gatech.wguo64.lostandfoundandroidapp.backend.QueryHelper;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.FoundReport;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.LostReport;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.MyReport;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.Report;

import static edu.gatech.wguo64.lostandfoundandroidapp.backend.OfyService.ofy;

/**
 * Created by guoweidong on 2/21/16.
 */
public class SearchHelper {
    private static final String INDEX_NAME = "report";
    private static final int DEFAULT_LIST_LIMIT = 5;
    private static Index getIndex() {
        IndexSpec indexSpec = IndexSpec.newBuilder().setName(INDEX_NAME).build();
        return SearchServiceFactory.getSearchService().getIndex(indexSpec);
    }

    public static void addReport(Report report) {
        GeoPoint geoPoint = null;
        if(report instanceof LostReport) {
            GeoPt geoPt = ((LostReport) report).getLocation();
            geoPoint = geoPt == null ? null : new GeoPoint(geoPt.getLatitude(), geoPt.getLongitude());
        } else if(report instanceof FoundReport) {
            GeoPt geoPt = ((FoundReport) report).getLocation();
            geoPoint = geoPt == null ? null : new GeoPoint(geoPt.getLatitude(), geoPt.getLongitude());
        }
        Document.Builder docBuilder = Document.newBuilder()
                .setId("" + report.getId()) // Setting the document identifer is optional. If omitted, the search service will create an identifier.
                .addField(Field.newBuilder().setName("title").setText(report.getTitle()))
                .addField(Field.newBuilder().setName("description").setText(report.getDescription()))
                .addField(Field.newBuilder().setName("created").setDate(report.getCreated()));
        if(geoPoint != null) {
            docBuilder.addField(Field.newBuilder().setName("location").setGeoPoint(geoPoint));
        }
        Document doc = docBuilder.build();
        try {
            getIndex().put(doc);
        } catch (PutException e) {
            if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())) {
                // retry putting the document
                getIndex().put(doc);
            }
        }
    }

    public static void removeReport(Long reportId) {
        getIndex().delete("" + reportId);
    }

    public static CollectionResponse<Report> searchByText(String queryString, String cursorString) {
        if(cursorString == null) {
            return CollectionResponse.<Report>builder().setItems(new ArrayList<Report>()).build();
        }
        queryString = cleanQuery(queryString);

        SortOptions sortOptions = SortOptions.newBuilder()
                .setMatchScorer(MatchScorer.newBuilder().build())
                .addSortExpression(SortExpression.newBuilder().setExpression("_score"))
                .addSortExpression(SortExpression.newBuilder()
                        .setExpression("created")
                        .setDirection(SortExpression.SortDirection.DESCENDING))
                .setLimit(DEFAULT_LIST_LIMIT)
                .build();

        QueryOptions.Builder queryOptionsBuilder = QueryOptions.newBuilder()
                .setLimit(DEFAULT_LIST_LIMIT)
                .setReturningIdsOnly(true)
                .setSortOptions(sortOptions);

        Cursor cursor = null;
        if(!"".equals(cursorString)) {
            cursor = Cursor.newBuilder().build(cursorString);
        } else {
            cursor = Cursor.newBuilder().build();
        }

        queryOptionsBuilder.setCursor(cursor);

        QueryOptions queryOptions = queryOptionsBuilder.build();

        Query query = Query.newBuilder().setOptions(queryOptions).build(queryString);

        Results<ScoredDocument> results = getIndex().search(query);

        List<Report> myReportList = new ArrayList<>();
        for (ScoredDocument match : results.getResults()) {
            Report report = ofy().load().type(Report
                    .class).id(Long.parseLong(match.getId())).now();
            if(report != null) {
                myReportList.add(report);
            }
        }

        CollectionResponse.Builder<Report> reportBuilder = CollectionResponse.<Report>builder().setItems(myReportList);
        if(results.getCursor() != null) {
            reportBuilder.setNextPageToken(results.getCursor().toWebSafeString());
        }
        return reportBuilder.build();
    }

    public static CollectionResponse<Report> searchByLocationAndTitle(GeoPt geoPt, String title, int distance) {
        String queryString = "(" + cleanQuery(title)
                + ") AND distance(location, geopoint(" +
                geoPt.getLatitude() + "," + geoPt.getLongitude() + ")) < " + distance;

        SortOptions sortOptions = SortOptions.newBuilder()
                .setMatchScorer(MatchScorer.newBuilder().build())
                .addSortExpression(SortExpression.newBuilder().setExpression("_score"))
                .addSortExpression(SortExpression.newBuilder()
                        .setExpression("created")
                        .setDirection(SortExpression.SortDirection.DESCENDING))
                .setLimit(DEFAULT_LIST_LIMIT)
                .build();

        QueryOptions.Builder queryOptionsBuilder = QueryOptions.newBuilder()
                .setLimit(DEFAULT_LIST_LIMIT)
                .setReturningIdsOnly(true)
                .setSortOptions(sortOptions);

        QueryOptions queryOptions = queryOptionsBuilder.build();

        Query query = Query.newBuilder().setOptions(queryOptions).build(queryString);

        Results<ScoredDocument> results = getIndex().search(query);

        List<Report> myReportList = new ArrayList<>();

        for (ScoredDocument match : results.getResults()) {
            Report report = ofy().load().type(Report
                    .class).id(Long.parseLong(match.getId())).now();
            if(report != null) {
                myReportList.add(report);
            }
        }

        return CollectionResponse.<Report>builder().setItems(myReportList).build();
    }

    private static String cleanQuery(String queryString) {
        return Joiner.on(" OR ").join(
                (queryString).trim().replace("\\", "").replace("\"",
                        "").split("\\s+"));
    }
}
