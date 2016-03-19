package edu.gatech.wguo64.lostandfoundandroidapp.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.activity.DetailReportActivity;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.Report;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.TextTrimmer;

/**
 * Created by guoweidong on 10/24/15.
 */
public class SearchReportRecyclerViewAdapter extends RecyclerView.Adapter<SearchReportRecyclerViewAdapter.ViewHolder> {

    public final static String TAG = SearchReportRecyclerViewAdapter.class.getName();

    private List<Report> reports;
    private Context context;

    public ProgressDialog progressDialog;

    public SearchReportRecyclerViewAdapter(List<Report> reports, Context context) {
        this.reports = reports;
        this.context = context;
    }

    public void clearObjects() {
        int size = this.reports.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                reports.remove(0);
            }

            this.notifyItemRangeRemoved(0, size);
        }
    }

    public void addObjects(List<Report> reports) {
        this.reports.addAll(reports);
        this.notifyItemRangeInserted(getItemCount() - reports.size(), reports.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_search_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        final Report report = reports.get(i);
        viewHolder.typeImg.setImageDrawable(context.getDrawable(report.getReportType()
                ? R.drawable.ic_lost_red_56dp : R.drawable.ic_found_green_56dp));
        //Title
        viewHolder.titleTxt.setText(report.getTitle());
        //Description
        viewHolder.descriptionTxt.setText(TextTrimmer.trim(report.getDescription(), 70));

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailReportActivity.class);
                intent.putExtra("reportId", report.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reports == null ? 0 : reports.size();
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
