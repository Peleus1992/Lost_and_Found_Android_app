package edu.gatech.wguo64.lostandfoundandroidapp.backend.helpers;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.search.Cursor;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GeoPoint;
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

import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.Report;

import static edu.gatech.wguo64.lostandfoundandroidapp.backend.helpers.OfyService.ofy;

/**
 * Created by guoweidong on 2/21/16.
 */
public class SearchHelper {
    private static final String INDEX_NAME_FOR_MATCH = "match";
    private static final String INDEX_NAME_FOR_SEARCH = "search";
    private static final int DEFAULT_LIST_LIMIT = 5;
    private static Index getSearchIndex() {
        IndexSpec indexSpec = IndexSpec.newBuilder().setName(INDEX_NAME_FOR_SEARCH).build();
        return SearchServiceFactory.getSearchService().getIndex(indexSpec);
    }
    private static Index getMatchIndex() {
        IndexSpec indexSpec = IndexSpec.newBuilder().setName(INDEX_NAME_FOR_MATCH).build();
        return SearchServiceFactory.getSearchService().getIndex(indexSpec);
    }


    public static void addReport(Report report) {
        // Build document for match
        Document.Builder docBuilder1 = Document.newBuilder()
                .setId("" + report.getId()) // Setting the document identifer is optional. If omitted, the search service will create an identifier.
                .addField(Field.newBuilder().setName("created").setDate(report.getCreated()))
                .addField(Field.newBuilder().setName("tags").setText(report.getTagsString()));

        if(report.getLocation() != null) {
            GeoPt geoPt = report.getLocation();
            GeoPoint geoPoint = new GeoPoint(geoPt.getLatitude(), geoPt.getLongitude());
            docBuilder1.addField(Field.newBuilder().setName("location").setGeoPoint(geoPoint));
        }

        Document docForMatch = docBuilder1.build();

        try {
            getMatchIndex().put(docForMatch);
        } catch (PutException e) {
            if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())) {
                // retry putting the document
                getMatchIndex().put(docForMatch);
            }
        }

        //Build document for search

        Document.Builder docBuilder2 = Document.newBuilder()
                .setId("" + report.getId()) // Setting the document identifer is optional. If omitted, the search service will create an identifier.
                .addField(Field.newBuilder().setName("title").setText(report.getTitle()))
                .addField(Field.newBuilder().setName("description").setText(report.getDescription()))
                .addField(Field.newBuilder().setName("created").setDate(report.getCreated()));

        Document docForSearch = docBuilder2.build();

        try {
            getSearchIndex().put(docForSearch);
        } catch (PutException e) {
            if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())) {
                // retry putting the document
                getSearchIndex().put(docForSearch);
            }
        }
    }

    public static void removeReport(Long reportId) {
        getSearchIndex().delete("" + reportId);
        getMatchIndex().delete("" + reportId);
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

        Results<ScoredDocument> results = getSearchIndex().search(query);

        List<Report> reports = new ArrayList<>();
        for (ScoredDocument match : results.getResults()) {
            Report report = ofy().load().type(Report
                    .class).id(Long.parseLong(match.getId())).now();
            if(report != null) {
                reports.add(report);
            }
        }

        CollectionResponse.Builder<Report> reportBuilder = CollectionResponse.<Report>builder().setItems(reports);
        if(results.getCursor() != null) {
            reportBuilder.setNextPageToken(results.getCursor().toWebSafeString());
        }
        return reportBuilder.build();
    }

    public static CollectionResponse<Report> searchByLocationAndTitle(GeoPt geoPt, String tags, int distance) {
        String queryString = "(" + cleanQuery(tags)
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

        Results<ScoredDocument> results = getMatchIndex().search(query);

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
