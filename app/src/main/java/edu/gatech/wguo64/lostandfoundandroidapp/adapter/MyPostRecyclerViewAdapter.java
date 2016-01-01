package edu.gatech.wguo64.lostandfoundandroidapp.adapter;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
public class MyPostRecyclerViewAdapter extends RecyclerView.Adapter<MyPostRecyclerViewAdapter.ViewHolder> {

    private List<MyPost> myPosts;
    private MyPostFragment fragment;

    public MyPostRecyclerViewAdapter(List<MyPost> myPosts, MyPostFragment fragment) {
        this.myPosts = myPosts;
        this.fragment = fragment;
    }


    public void clearObjects() {
        int size = this.myPosts.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                myPosts.remove(0);
            }

            this.notifyItemRangeRemoved(0, size);
        }
    }

    public void addObjects(List<MyPost> myPosts) {
        this.myPosts.addAll(myPosts);
        this.notifyItemRangeInserted(0, myPosts.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int i) {


        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_my_posts, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        final MyPost myPost = myPosts.get(i);

        if(myPost.isFoundReport) {
            viewHolder.reportTypeImg.setBackground(fragment.getActivity().getDrawable(R.drawable.ic_wb_incandescent_black_24dp));
            viewHolder.statusBtn.setText("Returned");
            viewHolder.statusBtn.setChecked(myPost.status);

        } else {
            viewHolder.reportTypeImg.setBackground(fragment.getActivity().getDrawable(R.drawable.ic_mood_bad_black_24dp));
            viewHolder.statusBtn.setText("Found");
            viewHolder.statusBtn.setChecked(myPost.status);
        }
        viewHolder.statusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        fragment.setmProgressBar(true);
                    }

                    @Override
                    protected Void doInBackground(Void...
                                                          params) {
                        try {
                            if (myPost.isFoundReport) {
                                FoundReport foundReport = Api.getClient().foundReport().get(myPost.id).execute();
                                foundReport.setReturned(!foundReport.getReturned());
                                Api.getClient().foundReport().update(myPost.id, foundReport).execute();
                            } else {
                                LostReport lostReport = Api.getClient().lostReport().get(myPost.id).execute();
                                lostReport.setFound(!lostReport.getFound());
                                Api.getClient().lostReport().update(myPost.id, lostReport).execute();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i("myinfo", e.getLocalizedMessage() + e.getMessage());
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
//                        fragment.updateObjects();
                        fragment.setmProgressBar(false);
                    }
                }.execute();
            }
        });

        viewHolder.title.setText(myPost.title);
        viewHolder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if(myPost.isFoundReport) {
                    intent.setClass(fragment.getActivity(), DetailFoundActivity.class);
                } else {
                    intent.setClass(fragment.getActivity(), DetailLostActivity.class);
                }
                intent.putExtra("reportId", myPost.id);
                fragment.getActivity().startActivity(intent);
            }
        });
        viewHolder.timestamp.setText(TimeManager.getTimeDifferential(myPost.created));
        viewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        fragment.setmProgressBar(true);
                    }

                    @Override
                    protected Void doInBackground(Void...
                                                          params) {
                        try {
                            if(myPost.isFoundReport) {
                                Api.getClient().foundReport().remove(myPost.id).execute();
                            } else {
                                Api.getClient().lostReport().remove(myPost.id).execute();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        fragment.updateObjects();
                    }
                }.execute();
            }
        });

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return myPosts == null ? 0 : myPosts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView reportTypeImg;
        public TextView title;
        public TextView timestamp;
        public CheckBox statusBtn;
        public TextView deleteBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            reportTypeImg = (ImageView)itemView.findViewById(R.id.reportTypeImg);
            title = (TextView)itemView.findViewById(R.id.title);
            timestamp = (TextView)itemView.findViewById(R.id.timestamp);
            statusBtn = (CheckBox)itemView.findViewById(R.id.statusBtn);
            deleteBtn = (TextView)itemView.findViewById(R.id.deleteBtn);
        }

    }
}
