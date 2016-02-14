package edu.gatech.wguo64.lostandfoundandroidapp.backend.model;

import com.googlecode.objectify.annotation.Entity;

/**
 * Created by guoweidong on 2/12/16.
 */

@Entity
public class MyReport {
    Report report;
    boolean isLostReport;
    boolean status;
    public MyReport(Report report, boolean isLostReport, boolean status) {
        this.report = report;
        this.isLostReport = isLostReport;
        this.status = status;
    }

    public Report getReport() {
        return report;
    }

    public boolean getIsLostReport() {
        return isLostReport;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
