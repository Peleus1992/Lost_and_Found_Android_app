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
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.MyReport;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.Report;
import edu.gatech.wguo64.lostandfoundandroidapp.fragment.MyPostFragment;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.TextTrimmer;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.TimeConvertor;

/**
 * Created by guoweidong on 10/24/15.
 */
public class MyPostRecyclerViewAdapter extends RecyclerView.Adapter<MyPostRecyclerViewAdapter.ViewHolder>
    implements View.OnClickListener {

    public final static String TAG = MyPostRecyclerViewAdapter.class.getName();

    private List<MyReport> myReports;
    private Context context;
    private MyPostFragment fragment;

    public ProgressDialog progressDialog;

    public MyPostRecyclerViewAdapter(List<MyReport> myReports, Context context, MyPostFragment fragment) {
        this.myReports = myReports;
        this.context = context;
        this.fragment = fragment;
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
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_my_posts, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        final MyReport myReport = myReports.get(i);
        final Report report = myReport.getReport();
        viewHolder.reportTypeImg.setImageDrawable(context.getDrawable(myReport.getIsLostReport()
                ? R.drawable.ic_lost_red_56dp : R.drawable.ic_found_green_56dp));
        //Title
        viewHolder.titleTxt.setText(report.getTitle());
        //Timestamp
        viewHolder.timestampTxt.setText(TimeConvertor.getTimeDifferential(report.getCreated().getValue()));
        //Description
        viewHolder.descriptionTxt.setText(TextTrimmer.trim(report.getDescription()));
        //Status
        viewHolder.statusTxt.setText(myReport.getIsLostReport()
                ? (myReport.getStatus() ? "Found" : "Not Found")
                : (myReport.getStatus() ? "Returned" : "Not Returned"));
        viewHolder.statusTxt.setTextColor(myReport.getStatus() ? Color.GREEN : Color.RED);
        //Email button
        viewHolder.emailBtn.setOnClickListener(this);
        viewHolder.emailBtn.setTag(report.getUserEmail());
        //Comment button
        viewHolder.commentBtn.setOnClickListener(this);
        //Share button
        viewHolder.shareBtn.setOnClickListener(this);
        //Status button
        if(myReport.getStatus()) {
            viewHolder.statusBtn.setVisibility(View.GONE);
        } else {
            viewHolder.statusBtn.setVisibility(View.VISIBLE);
            viewHolder.statusBtn.setText(myReport.getIsLostReport() ? "I have found it" : "I have returned it");
            viewHolder.statusBtn.setOnClickListener(this);
            viewHolder.statusBtn.setTag(report.getId());
        }

        //Delete button
        viewHolder.deleteBtn.setOnClickListener(this);
        viewHolder.deleteBtn.setTag(report.getId());

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
                        "mailto", (String) v.getTag(), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
                context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
                break;
            case R.id.commentBtn:
                break;
            case R.id.shareBtn:
                break;
            case R.id.statusBtn:
                if(v.getTag() != null && v.getTag() instanceof Long) {
                    updateStatus((Long) v.getTag());
                }
                break;
            case R.id.deleteBtn:
                if(v.getTag() != null && v.getTag() instanceof Long) {
                    deleteMyReport((Long)v.getTag());
                }
                break;

        }

    }

    @Override
    public int getItemCount() {
        return myReports == null ? 0 : myReports.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView reportTypeImg;
        public TextView titleTxt;
        public TextView timestampTxt;
        public TextView descriptionTxt;
        public TextView statusTxt;
        public Button emailBtn;
        public Button commentBtn;
        public Button shareBtn;
        public Button statusBtn;
        public Button deleteBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            reportTypeImg = (ImageView)itemView.findViewById(R.id.reportTypeImg);
            titleTxt = (TextView) itemView.findViewById(R.id.titleTxt);
            timestampTxt = (TextView) itemView.findViewById(R.id.timestampTxt);
            descriptionTxt = (TextView) itemView.findViewById(R.id.descriptionTxt);
            statusTxt = (TextView) itemView.findViewById(R.id.statusTxt);
            emailBtn = (Button) itemView.findViewById(R.id.emailBtn);
            commentBtn = (Button) itemView.findViewById(R.id.commentBtn);
            shareBtn = (Button) itemView.findViewById(R.id.shareBtn);
            statusBtn = (Button) itemView.findViewById(R.id.statusBtn);
            deleteBtn = (Button) itemView.findViewById(R.id.deleteBtn);
        }

    }

    private void updateStatus(final Long id) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage("Are you sure,You wanted to change status?");

        alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                new AsyncTask<Long, Void, MyReport>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressDialog = new ProgressDialog(context);
                        progressDialog.setTitle(R.string.progress_dialog_title_update);
                        progressDialog.setMessage(context.getString(R.string.progress_dialog_message_please_wait));
                        progressDialog.show();
                    }

                    @Override
                    protected MyReport doInBackground(Long...params) {
                        if(params[0] == null) {
                            Log.d(TAG, "Alert User: ID is null.");
                            return null;
                        }
                        try {
                            return Api.getClient().myReport().updateStatus(params[0]).execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(MyReport myReport) {

                        super.onPostExecute(myReport);
                        progressDialog.dismiss();
                        if(myReport == null || !myReport.getStatus()) {
                            Toast.makeText(context, "Update status failed. Please try layer.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Update status success.", Toast.LENGTH_SHORT).show();
                        }
                        fragment.updateObjects();
                    }
                }.execute(id);
            }
        });

        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void deleteMyReport(final Long id) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage("Are you sure,You wanted to delete the report?");

        alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                new AsyncTask<Long, Void, Void>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressDialog = new ProgressDialog(context);
                        progressDialog.setTitle(R.string.progress_dialog_title_update);
                        progressDialog.setMessage(context.getString(R.string.progress_dialog_message_please_wait));
                        progressDialog.show();
                    }

                    @Override
                    protected Void doInBackground(Long...params) {
                        if(params[0] == null) {
                            Log.d(TAG, "Alert User: ID is null.");
                            return null;
                        }
                        try {
                            Api.getClient().myReport().delete(params[0]).execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void v) {
                        super.onPostExecute(v);
                        progressDialog.dismiss();
                        fragment.updateObjects();
                    }
                }.execute(id);
            }
        });

        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
