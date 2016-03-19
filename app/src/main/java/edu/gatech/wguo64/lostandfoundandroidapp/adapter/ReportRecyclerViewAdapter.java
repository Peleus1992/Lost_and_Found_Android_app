package edu.gatech.wguo64.lostandfoundandroidapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.plus.PlusShare;

import java.util.List;

import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.activity.CommentActivity;
import edu.gatech.wguo64.lostandfoundandroidapp.activity.DetailReportActivity;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.Report;
import edu.gatech.wguo64.lostandfoundandroidapp.googlemaps.LocationHelper;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.ImageDownloader;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.TextTrimmer;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.TimeConvertor;

/**
 * Created by guoweidong on 10/24/15.
 */
public class ReportRecyclerViewAdapter extends RecyclerView.Adapter<ReportRecyclerViewAdapter.ViewHolder>
    implements View.OnClickListener {
    public static final String TAG = ReportRecyclerViewAdapter.class.getName();

    private List<Report> reports;
    private Context context;

    public ReportRecyclerViewAdapter(List<Report> reports, Context context) {
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
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_report, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        final Report report = reports.get(i);

        //User photo
        new ImageDownloader(viewHolder.userPhotoImg).execute(report.getPhotoUrl());
        viewHolder.userPhotoImg.setOnClickListener(this);
        viewHolder.userPhotoImg.setTag(report.getId());
        //Title
        viewHolder.titleTxt.setText(report.getTitle());
        viewHolder.titleTxt.setOnClickListener(this);
        viewHolder.titleTxt.setTag(report.getId());
        //User Email
        viewHolder.userEmailTxt.setText(report.getUserEmail());
        //Timestamp
        viewHolder.timestampTxt.setText(TimeConvertor.getTimeDifferential(report.getCreated().getValue()));
        //Description
        viewHolder.descriptionTxt.setText(TextTrimmer.trim(report.getDescription()));
        viewHolder.descriptionTxt.setOnClickListener(this);
        viewHolder.descriptionTxt.setTag(report.getId());
        //Object image
        if(report.getImageUrl() != null) {
            Log.d(TAG, report.getImageUrl());
            new ImageDownloader(viewHolder.objectImage).execute(report.getImageUrl());
        }
        viewHolder.objectImage.setOnClickListener(this);
        viewHolder.objectImage.setTag(report.getId());
        //Status
        viewHolder.statusTxt.setText(report.getReportType() ? (report.getStatus() ? "Found" : "Not Found")
                : (report.getStatus() ? "Returned" : "Not Returned"));
        viewHolder.statusTxt.setTextColor(report.getStatus() ? Color.GREEN : Color.RED);
        //Email button
        viewHolder.emailBtn.setOnClickListener(this);
        viewHolder.emailBtn.setTag(report.getUserEmail());
        //Comment button
        viewHolder.commentBtn.setOnClickListener(this);
        viewHolder.commentBtn.setTag(report.getId());
        //Share button
        viewHolder.shareBtn.setOnClickListener(this);
        viewHolder.shareBtn.setTag(report);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.userPhotoImg:
            case R.id.descriptionTxt:
            case R.id.objectImage:
            case R.id.titleTxt: {
                Intent intent = new Intent(context, DetailReportActivity.class);
                intent.putExtra("reportId", (Long) v.getTag());
                context.startActivity(intent);
                break;
            }
            case R.id.emailBtn:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", (String)v.getTag(), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
                context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
                break;
            case R.id.commentBtn: {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("reportId", (Long) v.getTag());
                context.startActivity(intent);
                break;
            }
            case R.id.shareBtn:
                PlusShare.Builder builder = new PlusShare.Builder((AppCompatActivity)context);

                // Set call-to-action metadata.
                builder.addCallToAction(
                        "CREATE_ITEM", /** call-to-action button label */
                        Uri.parse("http://plus.google.com/pages/create"), /** call-to-action url (for desktop use) */
                        "/pages/create" /** call to action deep-link ID (for mobile use), 512 characters or fewer */);

                // Set the content url (for desktop use).
                builder.setContentUrl(Uri.parse("https://plus.google.com/pages/"));

                // Set the target deep-link ID (for mobile use).
                builder.setContentDeepLinkId("/pages/",
                        null, null, null);

                // Set the share text.
                Report report = (Report)v.getTag();
                String text = "Found Report by " + report.getUserEmail() + "\r\n" + "\r\n"
                        + "Report type: " + (report.getReportType() ? "Lost report" : "Found report")
                        + "Title: " + report.getTitle() + "\r\n" + "\r\n"
                        + "Description: " + report.getDescription() + "\r\n" + "\r\n"
                        + "When: " + (report.getTime() == null ? "Not clear." : report.getTime()) + "\r\n" + "\r\n"
                        + "Where: " + (report.getLocation() == null ? "Not clear." : LocationHelper.getAddress(context, new LatLng(report.getLocation().getLatitude()
                        , report.getLocation().getLongitude()))) + "\r\n" + "\r\n"
                        + "Photo url: " + report.getImageUrl() + "\r\n";
                builder.setText(text);
                ((AppCompatActivity)context).startActivityForResult(builder.getIntent(), 0);
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
        public TextView userEmailTxt;
        public TextView timestampTxt;
        public TextView descriptionTxt;
        public ImageView objectImage;
        public TextView statusTxt;
        public Button emailBtn;
        public Button commentBtn;
        public Button shareBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            userPhotoImg = (ImageView) itemView.findViewById(R.id.userPhotoImg);
            titleTxt = (TextView) itemView.findViewById(R.id.titleTxt);
            userEmailTxt = (TextView) itemView.findViewById(R.id.userEmailTxt);
            timestampTxt = (TextView) itemView.findViewById(R.id.timestampTxt);
            descriptionTxt = (TextView) itemView.findViewById(R.id.descriptionTxt);
            objectImage = (ImageView) itemView.findViewById(R.id.objectImage);
            statusTxt = (TextView) itemView.findViewById(R.id.statusTxt);
            emailBtn = (Button) itemView.findViewById(R.id.emailBtn);
            commentBtn = (Button) itemView.findViewById(R.id.commentBtn);
            shareBtn = (Button) itemView.findViewById(R.id.shareBtn);
        }

    }


}
