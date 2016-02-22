package edu.gatech.wguo64.lostandfoundandroidapp.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.activity.DetailFoundActivity;
import edu.gatech.wguo64.lostandfoundandroidapp.activity.DetailLostActivity;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.MyReport;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.Report;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.TextTrimmer;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.TimeConvertor;

/**
 * Created by guoweidong on 10/24/15.
 */
public class SearchReportRecyclerViewAdapter extends RecyclerView.Adapter<SearchReportRecyclerViewAdapter.ViewHolder> {

    public final static String TAG = SearchReportRecyclerViewAdapter.class.getName();

    private List<MyReport> myReports;
    private Context context;

    public ProgressDialog progressDialog;

    public SearchReportRecyclerViewAdapter(List<MyReport> myReports, Context context) {
        this.myReports = myReports;
        this.context = context;
    }

    public void clearObjects() {
        int size = this.myReports.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                myReports.remove(0);
            }

            this.notifyItemRangeRemoved(0, size);
        }
    }

    public void addObjects(List<MyReport> myReports) {
        this.myReports.addAll(myReports);
        this.notifyItemRangeInserted(getItemCount() - myReports.size(), myReports.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_search_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        final MyReport myReport = myReports.get(i);
        final Report report = myReport.getReport();
        viewHolder.typeImg.setImageDrawable(context.getDrawable(myReport.getIsLostReport()
                ? R.drawable.ic_lost_red_56dp : R.drawable.ic_found_green_56dp));
        //Title
        viewHolder.titleTxt.setText(report.getTitle());
        //Description
        viewHolder.descriptionTxt.setText(TextTrimmer.trim(report.getDescription(), 70));

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, myReport.getIsLostReport()
                        ? DetailLostActivity.class : DetailFoundActivity.class);
                intent.putExtra("reportId", report.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return myReports == null ? 0 : myReports.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView typeImg;
        public TextView titleTxt;
        public TextView descriptionTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            typeImg = (ImageView)itemView.findViewById(R.id.typeImg);
            titleTxt = (TextView) itemView.findViewById(R.id.titleTxt);
            descriptionTxt = (TextView) itemView.findViewById(R.id.descriptionTxt);
        }

    }
}
