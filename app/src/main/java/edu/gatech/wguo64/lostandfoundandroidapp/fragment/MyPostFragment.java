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
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.CollectionResponseReport;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.Report;
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
                ArrayList<Report>(), getContext(), this);
        recyclerView.setAdapter(adapter);
    }

    public void updateObjects() {
        new InitializeObjectsTask().execute();
    }

    private void appendObjects() {
        new AppendObjectsTask().execute(cursor);
    }

    private class InitializeObjectsTask extends AsyncTask<Void, Void, CollectionResponseReport> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            swipyRefreshLayout.setEnabled(false);
            adapter.clearObjects();
        }

        @Override
        protected CollectionResponseReport doInBackground(Void... params) {
            CollectionResponseReport reports = null;
            try {
                reports = Api.getClient().report().listMyReport().execute();
            } catch (IOException e) {
                Log.d(TAG, "InitializeObjects: " + e.getLocalizedMessage());
            }
            return reports;
        }

        @Override
        protected void onPostExecute(CollectionResponseReport collectionResponseReport) {
            //handle visibility
            super.onPostExecute(collectionResponseReport);
            ArrayList<Report> reports = new ArrayList<>();
            if(collectionResponseReport != null) {
                if(collectionResponseReport.getItems() != null) {
                    reports.addAll(collectionResponseReport.getItems());
                }
                cursor = collectionResponseReport.getNextPageToken();
            } else {
                Snackbar.make(rootView, R.string.failure_update, Snackbar.LENGTH_SHORT).show();
            }
            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            swipyRefreshLayout.setEnabled(true);
            swipyRefreshLayout.setRefreshing(false);
            adapter.addObjects(reports);
        }
    }

    private class AppendObjectsTask extends AsyncTask<String, Void, CollectionResponseReport> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipyRefreshLayout.setEnabled(false);
        }

        @Override
        protected CollectionResponseReport doInBackground(String... params) {
            String cur = params[0];
            CollectionResponseReport reports = null;
            try {
                reports = Api.getClient().report().listMyReport().setCursor(cur).execute();
            } catch (Exception e) {
                Log.d(TAG, "AppendObjectsTask: " + e.getLocalizedMessage());
            }
            return reports;
        }

        @Override
        protected void onPostExecute(CollectionResponseReport collectionResponseReport) {
            //handle visibility
            ArrayList<Report> reports = new ArrayList<>();
            if(collectionResponseReport != null) {
                if(collectionResponseReport.getItems() != null) {
                    reports.addAll(collectionResponseReport.getItems());
                }
                cursor = collectionResponseReport.getNextPageToken();
            } else {
                Snackbar.make(rootView, R.string.failure_update, Snackbar.LENGTH_SHORT).show();
            }
            swipyRefreshLayout.setEnabled(true);
            swipyRefreshLayout.setRefreshing(false);
            //set data for list
            adapter.addObjects(reports);

        }

    }

}
