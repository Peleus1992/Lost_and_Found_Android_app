package edu.gatech.wguo64.lostandfoundandroidapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.gatech.cc.lostandfound.api.lostAndFound.model.FoundReport;
import edu.gatech.cc.lostandfound.api.lostAndFound.model.LostReport;
import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.fragment.MyPostFragment;

/**
 * Created by guoweidong on 10/24/15.
 */
public class SearchFoundRecyclerViewAdapter extends RecyclerView.Adapter<SearchFoundRecyclerViewAdapter.ViewHolder> {

    private List<FoundReport> reports;
    private Context context;

    public SearchFoundRecyclerViewAdapter(List<FoundReport> reports, Context context) {
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

    public void addObjects(List<FoundReport> reports) {
        this.reports.addAll(reports);
        this.notifyItemRangeInserted(getItemCount(), reports.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_my_posts, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        final FoundReport report = reports.get(i);

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
