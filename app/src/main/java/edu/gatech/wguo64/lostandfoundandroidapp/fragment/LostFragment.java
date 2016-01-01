package edu.gatech.wguo64.lostandfoundandroidapp.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.ArrayList;

import edu.gatech.cc.lostandfound.api.lostAndFound.model.CollectionResponseLostReport;
import edu.gatech.cc.lostandfound.api.lostAndFound.model.LostReport;
import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.adapter.LostRecyclerViewAdapter;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;


public class LostFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;

    private LostRecyclerViewAdapter rvAdapter;
    private ArrayList<LostReport> reports = new
            ArrayList<LostReport>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lost, container, false);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        rvAdapter = new LostRecyclerViewAdapter(new
                ArrayList<LostReport>(), R.layout.cardview_lost,
                this);
        mRecyclerView.setAdapter(rvAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id
                .swipe_container);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R
                .color.colorAccent));
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout
                .OnRefreshListener() {
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
    public void searchObjects(String keywords) {
        new InitializeObjectsTask().execute(keywords);
    }
    private class InitializeObjectsTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            rvAdapter.clearObjects();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            reports.clear();

            try {
                CollectionResponseLostReport lostReports = null;
                if(params.length == 0) {
                    lostReports = Api.getClient().lostReport().list().execute();
                } else {
                    lostReports = Api.getClient().lostReport().search(params[0]).execute();
                }
                if (lostReports.getItems() != null) {
                    for (LostReport report : lostReports.getItems()) {
                        reports.add(report);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
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
            rvAdapter.addObjects(reports);
            mSwipeRefreshLayout.setRefreshing(false);

        }


    }

}
