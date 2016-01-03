package edu.gatech.wguo64.lostandfoundandroidapp.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.ArrayList;

import edu.gatech.cc.lostandfound.api.lostAndFound.model.CollectionResponseFoundReport;
import edu.gatech.cc.lostandfound.api.lostAndFound.model.CollectionResponseLostReport;
import edu.gatech.cc.lostandfound.api.lostAndFound.model.FoundReport;
import edu.gatech.cc.lostandfound.api.lostAndFound.model.LostReport;
import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.adapter.SearchFoundRecyclerViewAdapter;
import edu.gatech.wguo64.lostandfoundandroidapp.adapter.SearchLostRecyclerViewAdapter;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;

public class SearchFoundActivity extends AppCompatActivity implements SwipyRefreshLayout.OnRefreshListener {
    public final static String TAG = SearchFoundActivity.class.getName();

    public View rootView;
    public Toolbar toolbar;
    public SwipyRefreshLayout swipyRefreshLayout;
    public RecyclerView recyclerView;
    public ProgressBar progressBar;

    public SearchFoundRecyclerViewAdapter adapter;

    private String query;
    private String cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_report);

        inflateViews();
        setUIs();

        handleIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_report, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        return true;
    }
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {
        if(direction == SwipyRefreshLayoutDirection.BOTTOM) {
            appendObjects();
        }
    }

    private void inflateViews() {
        rootView = (View)findViewById(R.id.rootView);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        swipyRefreshLayout = (SwipyRefreshLayout)findViewById(R.id.swipyRefreshLayout);
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
    }

    private void setUIs() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchFoundActivity.this.onBackPressed();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchFoundRecyclerViewAdapter(new
                ArrayList<FoundReport>(), this);
        recyclerView.setAdapter(adapter);

        swipyRefreshLayout.setOnRefreshListener(this);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search
            initializeObjects();
        }
    }

    private void initializeObjects() {
        new InitializeObjectsTask().execute(query);
    }

    private void appendObjects() {
        new AppendObjectsTask().execute(query, cursor);
    }

    private class InitializeObjectsTask extends AsyncTask<String, Void, CollectionResponseFoundReport> {

        @Override
        protected void onPreExecute() {
            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            adapter.clearObjects();
            super.onPreExecute();
        }

        @Override
        protected CollectionResponseFoundReport doInBackground(String... params) {
            String qry = params[0];
            CollectionResponseFoundReport reports = null;
            try {
                reports = Api.getClient().foundReport().search(qry).execute();
            } catch (Exception e) {
                Log.d(TAG, "InitializeObjectsTask: " + e.getLocalizedMessage());
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
            } else {
                Snackbar.make(rootView, R.string.failure_search, Snackbar.LENGTH_SHORT).show();
            }
            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            //set data for list
            adapter.addObjects(foundReports);
        }

    }

    private class AppendObjectsTask extends AsyncTask<String, Void, CollectionResponseFoundReport> {

        @Override
        protected CollectionResponseFoundReport doInBackground(String... params) {
            String qry = params[0];
            String cur = params[1];
            CollectionResponseFoundReport reports = null;
            try {
                reports = Api.getClient().foundReport().search(qry).setCursor(cur).execute();
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
                Snackbar.make(rootView, R.string.failure_search, Snackbar.LENGTH_SHORT).show();
            }
            //set data for list
            adapter.addObjects(foundReports);
            swipyRefreshLayout.setRefreshing(false);
        }

    }
}
