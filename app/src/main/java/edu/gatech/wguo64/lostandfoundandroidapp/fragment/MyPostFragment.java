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

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.gatech.cc.lostandfound.api.lostAndFound.model.CollectionResponseFoundReport;
import edu.gatech.cc.lostandfound.api.lostAndFound.model.CollectionResponseLostReport;
import edu.gatech.cc.lostandfound.api.lostAndFound.model.FoundReport;
import edu.gatech.cc.lostandfound.api.lostAndFound.model.LostReport;
import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.adapter.LostRecyclerViewAdapter;
import edu.gatech.wguo64.lostandfoundandroidapp.adapter.MyPostRecyclerViewAdapter;
import edu.gatech.wguo64.lostandfoundandroidapp.entity.MyPost;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;


public class MyPostFragment extends Fragment implements SwipyRefreshLayout.OnRefreshListener {
    public final static String TAG = MyPostFragment.class.getName();
    public View rootView;
    public SwipyRefreshLayout swipyRefreshLayout;
    public RecyclerView recyclerView;
    public ProgressBar progressBar;

    private MyPostRecyclerViewAdapter adapter;

    private String cursor;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lost, container, false);

        inflateViews(view);

        setUIs();

        updateObjects();

        return view;
    }
    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {
        if(direction == SwipyRefreshLayoutDirection.TOP) {
            updateObjects();
        } else if(direction == SwipyRefreshLayoutDirection.BOTTOM) {
            appendObjects();
        }
    }

    private void inflateViews(View view) {
        rootView = view.findViewById(R.id.rootView);
        swipyRefreshLayout = (SwipyRefreshLayout)view.findViewById(R.id.swipyRefreshLayout);
        recyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
    }

    private void setUIs() {
        swipyRefreshLayout.setOnRefreshListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyPostRecyclerViewAdapter(new
                ArrayList<MyPost>(), getContext());
        recyclerView.setAdapter(adapter);
    }

    private void updateObjects() {
        new InitializeObjectsTask().execute();
    }

    private void appendObjects() {
//        new AppendObjectsTask().execute(cursor);
    }

    private class InitializeObjectsTask extends AsyncTask<Void, Void, ArrayList<MyPost>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            adapter.clearObjects();
        }

        @Override
        protected ArrayList<MyPost> doInBackground(Void... params) {
            ArrayList<MyPost> myPosts = new ArrayList<>();
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

            return myPosts;
        }

        @Override
        protected void onPostExecute(ArrayList<MyPost> myPosts) {
            //handle visibility
            super.onPostExecute(myPosts);

            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            swipyRefreshLayout.setRefreshing(false);

            //set data for list
            adapter.addObjects(myPosts);
        }
    }
}
