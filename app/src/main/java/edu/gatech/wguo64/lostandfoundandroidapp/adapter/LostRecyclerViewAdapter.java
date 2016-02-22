package edu.gatech.wguo64.lostandfoundandroidapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.activity.DetailLostActivity;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.LostReport;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.ImageDownloader;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.TextTrimmer;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.TimeConvertor;

/**
 * Created by guoweidong on 10/24/15.
 */
public class LostRecyclerViewAdapter extends RecyclerView.Adapter<LostRecyclerViewAdapter.ViewHolder>
    implements View.OnClickListener {

    private List<LostReport> reports;
    private Context context;

    public LostRecyclerViewAdapter(List<LostReport> reports, Context context) {
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

    public void addObjects(List<LostReport> reports) {
        this.reports.addAll(reports);
        this.notifyItemRangeInserted(getItemCount() - reports.size(), reports.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_lost, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        final LostReport report = reports.get(i);
        //User photo
        new ImageDownloader(viewHolder.userPhotoImg).execute(report.getPhotoUrl());
        //Title
        viewHolder.titleTxt.setText(report.getTitle());
        viewHolder.titleTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailLostActivity.class);
                intent.putExtra("reportId", report.getId());
                context.startActivity(intent);
            }
        });
        //Timestamp
        viewHolder.timestampTxt.setText(TimeConvertor.getTimeDifferential(report.getCreated().getValue()));
        //Description
        viewHolder.descriptionTxt.setText(TextTrimmer.trim(report.getDescription()));
        //Status
        viewHolder.statusTxt.setText(report.getFound() ? "Found" : "Not Found");
        viewHolder.statusTxt.setTextColor(report.getFound() ? Color.GREEN : Color.RED);
        //Email button
        viewHolder.emailBtn.setOnClickListener(this);
        viewHolder.emailBtn.setTag(report.getUserEmail());
        //Comment button
        viewHolder.commentBtn.setOnClickListener(this);
        //Share button
        viewHolder.shareBtn.setOnClickListener(this);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.emailBtn:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", (String)v.getTag(), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
                context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
                break;
            case R.id.commentBtn:
                break;
            case R.id.shareBtn:
                break;

        }
    }

    @Override
    public int getItemCount() {
        return reports == null ? 0 : reports.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {


        public ImageView userPhotoImg;
        public TextView titleTxt;
        public TextView timestampTxt;
        public TextView descriptionTxt;
        public TextView statusTxt;
        public Button emailBtn;
        public Button commentBtn;
        public Button shareBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            userPhotoImg = (ImageView) itemView.findViewById(R.id.userPhotoImg);
            titleTxt = (TextView) itemView.findViewById(R.id.titleTxt);
            timestampTxt = (TextView) itemView.findViewById(R.id.timestampTxt);
            descriptionTxt = (TextView) itemView.findViewById(R.id.descriptionTxt);
            statusTxt = (TextView) itemView.findViewById(R.id.statusTxt);
            emailBtn = (Button) itemView.findViewById(R.id.emailBtn);
            commentBtn = (Button) itemView.findViewById(R.id.commentBtn);
            shareBtn = (Button) itemView.findViewById(R.id.shareBtn);
        }

    }
}
