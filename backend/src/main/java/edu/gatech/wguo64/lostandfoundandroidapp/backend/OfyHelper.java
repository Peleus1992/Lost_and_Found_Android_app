package edu.gatech.wguo64.lostandfoundandroidapp.backend;

import com.googlecode.objectify.ObjectifyService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.gatech.wguo64.lostandfoundandroidapp.backend.model.LostReport;


/**
 * Created by mkatri on 11/22/15.
 */
public class OfyHelper implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ObjectifyService.register(LostReport.class);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
