package edu.gatech.wguo64.lostandfoundandroidapp.backend.tasks;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.taskqueue.DeferredTask;

import java.util.HashSet;
import java.util.Set;

import edu.gatech.wguo64.lostandfoundandroidapp.backend.helpers.NotificationHelper;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.FoundReport;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.LostReport;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.Report;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.helpers.SearchHelper;

import static edu.gatech.wguo64.lostandfoundandroidapp.backend.helpers.OfyService.ofy;

public class FoundMatchDeferredTask implements DeferredTask {
        // TODO add time lost and creation to query
        final private Long id;

        public FoundMatchDeferredTask(Long id) {
            this.id = id;
        }

        @Override
        public void run() {
            FoundReport foundReport = ofy().load().type(FoundReport.class).id(id).now();

            if (foundReport == null || foundReport.getLocation() == null) {
                return;
            }

            CollectionResponse<Report> reportCollectionResponse = SearchHelper
                    .searchByLocationAndTitle(foundReport.getLocation(), foundReport.getTitle(), 50);

            Set<String> userEmails = new HashSet<>();
            for (Report report : reportCollectionResponse.getItems()) {
                if(report instanceof LostReport) {
                    userEmails.add(report.getUserEmail());
                }
            }


            if (!userEmails.isEmpty()) {
                NotificationHelper.notify(userEmails, "Found " + foundReport
                        .getId());
            }
        }
    }