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
import edu.gatech.wguo64.lostandfoundandroidapp.adapter.FoundRecyclerViewAdapter;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.CollectionResponseFoundReport;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.FoundReport;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;


public class FoundFragment extends Fragment implements SwipyRefreshLayout.OnRefreshListener {
    public final static String TAG = FoundFragment.class.getName();
    public View rootView;
    public SwipyRefreshLayout swipyRefreshLayout;
    public RecyclerView recyclerView;
    public ProgressBar progressBar;

    private FoundRecyclerViewAdapter adapter;

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
        adapter = new FoundRecyclerViewAdapter(new
                ArrayList<FoundReport>(), getContext());
        recyclerView.setAdapter(adapter);
    }

    private void updateObjects() {
        new InitializeObjectsTask().execute();
    }

    private void appendObjects() {
        new AppendObjectsTask().execute(cursor);
    }

    private class InitializeObjectsTask extends AsyncTask<Void, Void, CollectionResponseFoundReport> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            swipyRefreshLayout.setEnabled(false);
            adapter.clearObjects();
        }

        @Override
        protected CollectionResponseFoundReport doInBackground(Void... params) {
            CollectionResponseFoundReport reports = null;
            try {
                reports = Api.getClient().foundReport().list().execute();
            } catch (IOException e) {
                Log.d(TAG, "InitializeObjects: " + e.getLocalizedMessage());
            }
            return reports;
        }

        @Override
        protected void onPostExecute(CollectionResponseFoundReport reports) {
            //handle visibility
            super.onPostExecute(reports);
            ArrayList<FoundReport> foundReports = new ArrayList<>();
            if(reports != null) {
                if(reports.getItems() != null) {
                    foundReports.addAll(reports.getItems());
                }
                cursor = reports.getNextPageToken();
                Log.i(TAG, "InitializeObjects: Cursor :" + cursor);
            } else {
                Snackbar.make(rootView, R.string.failure_update, Snackbar.LENGTH_SHORT).show();
            }
            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            swipyRefreshLayout.setEnabled(true);
            swipyRefreshLayout.setRefreshing(false);
            adapter.addObjects(foundReports);
        }
    }

    private class AppendObjectsTask extends AsyncTask<String, Void, CollectionResponseFoundReport> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipyRefreshLayout.setEnabled(false);
        }

        @Override
        protected CollectionResponseFoundReport doInBackground(String... params) {
            String cur = params[0];
            CollectionResponseFoundReport reports = null;
            try {
                reports = Api.getClient().foundReport().list().setCursor(cur).execute();
            } catch (Exception e) {
                Log.d(TAG, "AppendObjectsTask: " + e.getLocalizedMessage());
            }
            return reports;
        }

        @Override
        protected void onPostExecute(CollectionResponseFoundReport reports) {
            //handle visibility
            ArrayList<FoundReport> foundReports = new ArrayList<>();
            if(reports != null) {
                if(reports.getItems() != null) {
                    foundReports.addAll(reports.getItems());
                }
                cursor = reports.getNextPageToken();
            } else {
                Snackbar.make(rootView, R.string.failure_update, Snackbar.LENGTH_SHORT).show();
            }
            swipyRefreshLayout.setEnabled(true);
            swipyRefreshLayout.setRefreshing(false);
            //set data for list
            adapter.addObjects(foundReports);

        }

    }

}
