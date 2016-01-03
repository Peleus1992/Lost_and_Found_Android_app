package edu.gatech.wguo64.lostandfoundandroidapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import edu.gatech.cc.lostandfound.api.lostAndFound.model.FoundReport;
import edu.gatech.cc.lostandfound.api.lostAndFound.model.LostReport;
import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.activity.DetailFoundActivity;
import edu.gatech.wguo64.lostandfoundandroidapp.activity.DetailLostActivity;
import edu.gatech.wguo64.lostandfoundandroidapp.entity.MyPost;
import edu.gatech.wguo64.lostandfoundandroidapp.fragment.MyPostFragment;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;
import edu.gatech.wguo64.lostandfoundandroidapp.time.TimeManager;

/**
 * Created by guoweidong on 10/24/15.
 */
public class SearchLostRecyclerViewAdapter extends RecyclerView.Adapter<SearchLostRecyclerViewAdapter.ViewHolder> {

    private List<LostReport> reports;
    private Context context;

    public SearchLostRecyclerViewAdapter(List<LostReport> reports, Context context) {
        this.reports = reports;
        this.context = context;
    }


    public void clearObjects() {
        int size = reports.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                reports.remove(0);
            }

            this.notifyItemRangeRemoved(0, size);
        }
    }

    public void addObjects(List<LostReport> reports) {
        this.reports.addAll(reports);
        this.notifyItemRangeInserted(getItemCount(), reports.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_search_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        final LostReport report = reports.get(i);

        viewHolder.titleTxt.setText(report.getTitle());

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return reports == null ? 0 : reports.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView searchFlagImg;
        public TextView titleTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            searchFlagImg = (ImageView)itemView.findViewById(R.id.searchFlagImg);
            titleTxt = (TextView)itemView.findViewById(R.id.titleTxt);
        }

    }
}