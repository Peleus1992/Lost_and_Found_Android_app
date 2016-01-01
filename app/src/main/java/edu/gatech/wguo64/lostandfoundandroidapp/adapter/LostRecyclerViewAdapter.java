package edu.gatech.wguo64.lostandfoundandroidapp.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.gatech.cc.lostandfound.api.lostAndFound.model.LostReport;
import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.activity.DetailLostActivity;
import edu.gatech.wguo64.lostandfoundandroidapp.fragment.LostFragment;
import edu.gatech.wguo64.lostandfoundandroidapp.time.TimeManager;

/**
 * Created by guoweidong on 10/24/15.
 */
public class LostRecyclerViewAdapter extends RecyclerView.Adapter<LostRecyclerViewAdapter.ViewHolder> {

    private List<LostReport> reports;
    private int rowLayout;
    private LostFragment fragment;

    public LostRecyclerViewAdapter(List<LostReport> reports, int rowLayout, LostFragment fragment) {
        this.reports = reports;
        this.rowLayout = rowLayout;
        this.fragment = fragment;
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

    public void addObjects(List<LostReport> reports) {
        this.reports.addAll(reports);
        this.notifyItemRangeInserted(0, reports.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        final LostReport report = reports.get(i);

        viewHolder.title.setText(report.getTitle());
        viewHolder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(fragment.getActivity(), DetailLostActivity.class);

                intent.putExtra("reportId", report.getId());
                fragment.getActivity().startActivity(intent);
            }
        });
        viewHolder.nickname.setText(report.getUserNickname());
        viewHolder.timestamp.setText(TimeManager.getTimeDifferential(report.getCreated().getValue()));
        viewHolder.emailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", report.getUserNickname() + "@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
                fragment.getActivity().startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });
        if(report.getFound()) {
            viewHolder.found.setText("Found");
            viewHolder.found.setTextColor(Color.GREEN);
        } else {
            viewHolder.found.setText("Not Found");
            viewHolder.found.setTextColor(Color.RED);
        }
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

        public TextView title;
        public TextView nickname;
        public TextView timestamp;
        public ImageView emailBtn;
        public TextView found;


        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            nickname = (TextView) itemView.findViewById(R.id.nickname);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
            emailBtn = (ImageView) itemView.findViewById(R.id.emailBtn);
            found = (TextView) itemView.findViewById(R.id.found);
        }

    }
}
