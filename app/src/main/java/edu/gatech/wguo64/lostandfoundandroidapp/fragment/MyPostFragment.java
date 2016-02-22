package edu.gatech.wguo64.lostandfoundandroidapp.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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

import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.adapter.MyPostRecyclerViewAdapter;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.CollectionResponseMyReport;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.MyReport;
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
                ArrayList<MyReport>(), getContext(), this);
        recyclerView.setAdapter(adapter);
    }

    public void updateObjects() {
        new InitializeObjectsTask().execute();
    }

    private void appendObjects() {
        new AppendObjectsTask().execute(cursor);
    }

    private class InitializeObjectsTask extends AsyncTask<Void, Void, CollectionResponseMyReport> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            adapter.clearObjects();
        }

        @Override
        protected CollectionResponseMyReport doInBackground(Void... params) {
            CollectionResponseMyReport reports = null;
            try {
                reports = Api.getClient().myReport().list().execute();
            } catch (IOException e) {
                Log.d(TAG, "InitializeObjects: " + e.getLocalizedMessage());
            }
            return reports;
        }

        @Override
        protected void onPostExecute(CollectionResponseMyReport collectionResponseMyReport) {
            //handle visibility
            super.onPostExecute(collectionResponseMyReport);
            ArrayList<MyReport> myReports = new ArrayList<>();
            if(collectionResponseMyReport != null) {
                if(collectionResponseMyReport.getItems() != null) {
                    myReports.addAll(collectionResponseMyReport.getItems());
                }
                cursor = collectionResponseMyReport.getNextPageToken();
            } else {
                Snackbar.make(rootView, R.string.failure_update, Snackbar.LENGTH_SHORT).show();
            }
            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            swipyRefreshLayout.setRefreshing(false);
            adapter.addObjects(myReports);
        }
    }

    private class AppendObjectsTask extends AsyncTask<String, Void, CollectionResponseMyReport> {

        @Override
        protected CollectionResponseMyReport doInBackground(String... params) {
            String cur = params[0];
            CollectionResponseMyReport reports = null;
            try {
                reports = Api.getClient().myReport().list().setCursor(cur).execute();
            } catch (Exception e) {
                Log.d(TAG, "AppendObjectsTask: " + e.getLocalizedMessage());
            }
            return reports;
        }

        @Override
        protected void onPostExecute(CollectionResponseMyReport collectionResponseMyReport) {
            //handle visibility
            ArrayList<MyReport> myReports = new ArrayList<>();
            if(collectionResponseMyReport != null) {
                if(collectionResponseMyReport.getItems() != null) {
                    myReports.addAll(collectionResponseMyReport.getItems());
                }
                cursor = collectionResponseMyReport.getNextPageToken();
            } else {
                Snackbar.make(rootView, R.string.failure_update, Snackbar.LENGTH_SHORT).show();
            }
            swipyRefreshLayout.setRefreshing(false);
            //set data for list
            adapter.addObjects(myReports);

        }

    }

}
