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
import edu.gatech.wguo64.lostandfoundandroidapp.activity.DetailFoundActivity;
import edu.gatech.wguo64.lostandfoundandroidapp.activity.DetailLostActivity;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.Comment;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.Comment;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.Report;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.ImageDownloader;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.TextTrimmer;

/**
 * Created by guoweidong on 10/24/15.
 */
public class CommentRecyclerViewAdapter extends RecyclerView.Adapter<CommentRecyclerViewAdapter.ViewHolder> {

    public final static String TAG = CommentRecyclerViewAdapter.class.getName();

    private List<Comment> comments;
    private Context context;

    public ProgressDialog progressDialog;

    public CommentRecyclerViewAdapter(List<Comment> comments, Context context) {
        this.comments = comments;
        this.context = context;
    }

    public void clearObjects() {
        int size = this.comments.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                comments.remove(0);
            }

            this.notifyItemRangeRemoved(0, size);
        }
    }

    public void addObjects(List<Comment> comments) {
        this.comments.addAll(comments);
        this.notifyItemRangeInserted(getItemCount() - comments.size(), comments.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_comment, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        final Comment comment = comments.get(i);
        //User photo
        new ImageDownloader(viewHolder.userPhotoImg).execute(comment.getPhotoUrl());
        //User email
        viewHolder.userEmailTxt.setText(comment.getUseremail());
        //Description
        viewHolder.contentTxt.setText(comment.getContent());

    }

    @Override
    public int getItemCount() {
        return comments == null ? 0 : comments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView userPhotoImg;
        public TextView userEmailTxt;
        public TextView contentTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            userPhotoImg = (ImageView)itemView.findViewById(R.id.userPhotoImg);
            userEmailTxt = (TextView) itemView.findViewById(R.id.userEmailTxt);
            contentTxt = (TextView) itemView.findViewById(R.id.contentTxt);
        }

    }
}
