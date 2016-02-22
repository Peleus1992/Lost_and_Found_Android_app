package edu.gatech.wguo64.lostandfoundandroidapp.backend.endpoints;

/**
 * Created by guoweidong on 1/24/16.
 */

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
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
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.FoundReport;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.LostReport;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.MyReport;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.Report;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.SearchHelper;

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
public class MyReportEndpoints {
    private static final Logger logger = Logger.getLogger(MyReportEndpoints.class.getName());
    private static final int DEFAULT_LIST_LIMIT = 5;

    /**
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @param user
     * @return a response that encapsulates the result list and the next page
     * @throws OAuthRequestException
     */
    @ApiMethod(
            name = "myReport.list",
            path = "myReport/list",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<MyReport> listUserReports(
            @Nullable @Named ("cursor") final String cursor,
            @Nullable @Named("limit") final Integer limit,
            final User user) throws OAuthRequestException {
        if (user == null) {
            logger.exiting(FoundReportEndpoint.class.toString(), "Not logged in.");
            throw new OAuthRequestException("You need to login to list your reports.");
        }
        logger.info("For user: " + user.getEmail());

        Query<Report> query = ofy().load().type(Report.class).filter("userEmail", user
                .getEmail()).order("-created").limit(limit == null ? DEFAULT_LIST_LIMIT : limit);

        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Report> queryIterator = query.iterator();
        List<MyReport> reportList = new ArrayList<>();
        while (queryIterator.hasNext()) {
            Report report = queryIterator.next();
            MyReport myReport = null;
            if(report instanceof LostReport) {
                myReport = new MyReport(report, true, ((LostReport)report).getFound());
            } else {
                myReport = new MyReport(report, false, ((FoundReport)report).getReturned());
            }
            reportList.add(myReport);
        }
        return CollectionResponse.<MyReport>builder().setItems
                (reportList).setNextPageToken(queryIterator.getCursor()
                .toWebSafeString()).build();
    }

    /**
     * @param id  the id of the report
     * @param user
     * @return a response that encapsulates the result list and the next page
     * @throws OAuthRequestException
     */
    @ApiMethod(
            name = "myReport.updateStatus",
            path = "myReport/updateStatus",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public MyReport updateStatus(
            @Named("id") final Long id
            , final User user) throws OAuthRequestException {
        if (user == null) {
            logger.exiting(FoundReportEndpoint.class.toString(), "Not logged " +
                    "in.");
            throw new OAuthRequestException("You need to login to list your " +
                    "reports.");
        }
        logger.info("For user: " + user.getEmail());


        Report report = ofy().load().type(Report.class).id(id).now();
        MyReport myReport = null;
        if (report instanceof LostReport) {
            LostReport lostReport = (LostReport) report;
//            lostReport.setFound(true);
            if (ofy().load().type(LostReport.class).id(id).now() != null) {
//                ofy().save().entity(lostReport).now();
                lostReport.setFound(true);
                myReport = new MyReport(lostReport, true, true);
            } else {
                logger.warning("Update LostReport Status: id not found.");
            }

        } else if (report instanceof FoundReport) {
            FoundReport foundReport = (FoundReport) report;
//            foundReport.setReturned(true);
            if (ofy().load().type(FoundReport.class).id(id).now() != null) {
//                ofy().save().entity(foundReport).now();
                foundReport.setReturned(true);
                myReport = new MyReport(foundReport, false, true);
            } else {
                logger.warning("Update FoundReport Status: id not found.");
            }

        } else {
            logger.warning("Update Report Status: unknown class.");
        }

        return myReport;

    }

    /**
     * @param id  the id of the report
     * @param user
     * @return a response that encapsulates the result list and the next page
     * @throws OAuthRequestException
     */
    @ApiMethod(
            name = "myReport.delete",
            path = "myReport/delete",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void delete(
            @Named("id") final Long id
            , final User user) throws OAuthRequestException {
        if (user == null) {
            logger.exiting(FoundReportEndpoint.class.toString(), "Not logged " +
                    "in.");
            throw new OAuthRequestException("You need to login to list your " +
                    "reports.");
        }
        logger.info("For user: " + user.getEmail());

        Report report = ofy().load().type(Report.class).id(id).now();
        if (report instanceof LostReport) {
            LostReport lostReport = (LostReport) report;
            if (ofy().load().type(LostReport.class).id(id).now() != null) {
                ofy().delete().type(LostReport.class).id(id).now();
                SearchHelper.removeReport(id);
            } else {
                logger.warning("Update LostReport Status: id not found.");
            }

        } else if (report instanceof FoundReport) {
            FoundReport foundReport = (FoundReport) report;
            if (ofy().load().type(FoundReport.class).id(id).now() != null) {
                ofy().delete().type(FoundReport.class).id(id).now();
            } else {
                logger.warning("Update FoundReport Status: id not found.");
            }

        } else {
            logger.warning("Update Report Status: unknown class.");
        }
    }


    /**
     * @param query
     * @param cursor used for pagination to determine which page to return
     * @param user
     * @return a response that encapsulates the result list and the next page
     * @throws OAuthRequestException
     */
    @ApiMethod(
            name = "myReport.search",
            path = "myReport/search",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<MyReport> search(
            @Named ("query") final String query,
            @Nullable @Named ("cursor") final String cursor,
            final User user) throws OAuthRequestException {
        if (user == null) {
            logger.exiting(FoundReportEndpoint.class.toString(), "Not logged in.");
            throw new OAuthRequestException("You need to login to list your reports.");
        }
        logger.info("For user: " + user.getEmail());

        CollectionResponse<Report> collectionResponse = SearchHelper.searchByText(query, cursor);
        Collection<Report> reports = collectionResponse.getItems();
        List<MyReport> myReports = new ArrayList<>();
        for(Report report : reports) {
            MyReport myReport = null;
//            logger.info("myReport : " + myReport);
            if(report instanceof LostReport) {
                myReport = new MyReport(report, true, ((LostReport)report).getFound());
            } else {
                myReport = new MyReport(report, false, ((FoundReport)report).getReturned());
            }
            myReports.add(myReport);
        }

        return CollectionResponse.<MyReport>builder().setItems
                (myReports).setNextPageToken(collectionResponse.getNextPageToken()).build();
    }
}