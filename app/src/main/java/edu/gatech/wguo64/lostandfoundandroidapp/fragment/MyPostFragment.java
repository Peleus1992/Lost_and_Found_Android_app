package edu.gatech.wguo64.lostandfoundandroidapp.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.gatech.cc.lostandfound.api.lostAndFound.model.CollectionResponseFoundReport;
import edu.gatech.cc.lostandfound.api.lostAndFound.model.CollectionResponseLostReport;
import edu.gatech.cc.lostandfound.api.lostAndFound.model.FoundReport;
import edu.gatech.cc.lostandfound.api.lostAndFound.model.LostReport;
import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.adapter.MyPostRecyclerViewAdapter;
import edu.gatech.wguo64.lostandfoundandroidapp.entity.MyPost;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;


public class MyPostFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;

    private MyPostRecyclerViewAdapter rvAdapter;
    private ArrayList<MyPost> myPosts = new ArrayList<MyPost>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_posts, container, false);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        rvAdapter = new MyPostRecyclerViewAdapter(new ArrayList<MyPost>(), this);
        mRecyclerView.setAdapter(rvAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new InitializeObjectsTask().execute();
            }
        });

        new InitializeObjectsTask().execute();

        mRecyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        return view;
    }
    public void updateObjects() {
        new InitializeObjectsTask().execute();
    }
    public void setmProgressBar(boolean isVisible) {
        if(isVisible) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private class InitializeObjectsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            rvAdapter.clearObjects();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            myPosts.clear();

            try {
                CollectionResponseFoundReport foundReports = Api.getClient().foundReport().myReports().list()
                        .execute();
                CollectionResponseLostReport lostReports = Api.getClient().lostReport().myReports().list()
                        .execute();
                List<FoundReport> foundReportList = foundReports.getItems() == null ? new ArrayList<FoundReport>() : foundReports.getItems();
                List<LostReport> lostReportList = lostReports.getItems() == null ? new ArrayList<LostReport>() : lostReports.getItems();
                int i = 0, j = 0;
                while(i < foundReportList.size() && j < lostReportList.size()) {
                    FoundReport foundReport = foundReportList.get(i);
                    LostReport lostReport = lostReportList.get(j);

                    if(foundReport.getCreated().getValue() >= lostReport.getCreated().getValue()) {
                        myPosts.add(new MyPost(foundReport.getId(),
                                                true,
                                                foundReport.getTitle(),
                                                foundReport.getCreated().getValue(),
                                                foundReport.getReturned()));
                        i++;
                    } else {
                        myPosts.add(new MyPost(lostReport.getId(),
                                false,
                                lostReport.getTitle(),
                                lostReport.getCreated().getValue(),
                                lostReport.getFound()));
                        j++;
                    }
                }
                while(i < foundReportList.size()) {
                    FoundReport foundReport = foundReportList.get(i);
                    myPosts.add(new MyPost(foundReport.getId(),
                            true,
                            foundReport.getTitle(),
                            foundReport.getCreated().getValue(),
                            foundReport.getReturned()));
                    i++;
                }
                while(j < lostReportList.size()) {
                    LostReport lostReport = lostReportList.get(j);
                    myPosts.add(new MyPost(lostReport.getId(),
                            false,
                            lostReport.getTitle(),
                            lostReport.getCreated().getValue(),
                            lostReport.getFound()));
                    j++;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("myinfo", e.getLocalizedMessage() + e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            //handle visibility
            super.onPostExecute(param);

            mRecyclerView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);

            //set data for list
            rvAdapter.addObjects(myPosts);
            mSwipeRefreshLayout.setRefreshing(false);

        }
    }
}
