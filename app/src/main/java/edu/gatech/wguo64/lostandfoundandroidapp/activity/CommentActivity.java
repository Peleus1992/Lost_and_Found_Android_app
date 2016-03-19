package edu.gatech.wguo64.lostandfoundandroidapp.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.adapter.CommentRecyclerViewAdapter;
import edu.gatech.wguo64.lostandfoundandroidapp.adapter.SearchReportRecyclerViewAdapter;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.CollectionResponseComment;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.Comment;
import edu.gatech.wguo64.lostandfoundandroidapp.constants.Preferences;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;

/**
 * Created by guoweidong on 2/28/16.
 */
public class CommentActivity extends AppCompatActivity {
    public final static String TAG = CommentActivity.class.getName();

    public View rootView;
    public Toolbar toolbar;
    public RecyclerView recyclerView;
    public ProgressBar progressBar;
    public EditText commentEdit;

    public CommentRecyclerViewAdapter adapter;

    List<Comment> commentList = new ArrayList<>();

    Long reportId;

    public SharedPreferences preferences;

    public final static String TITLE = "Comment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        //Preferences
        preferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        inflateViews();
        setUIs();

        handleIntent(getIntent());
    }

    private void inflateViews() {
        rootView = findViewById(R.id.rootView);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        commentEdit = (EditText)findViewById(R.id.commentEdit);
    }

    private void setUIs() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(TITLE);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentActivity.this.onBackPressed();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommentRecyclerViewAdapter(new
                ArrayList<Comment>(), this);
        recyclerView.setAdapter(adapter);

        commentEdit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    Log.d(TAG, "Comment text: " + v.getText());
                    Comment comment = new Comment();
                    comment.setPhotoUrl(preferences.getString(Preferences.ACCOUNT_PHOTO_URL, null));
                    comment.setUseremail(preferences.getString(Preferences.ACCOUNT_NAME, null));
                    comment.setContent(v.getText().toString());
                    new CommentUploader().execute(comment);
                    commentEdit.setText("");
                    return true;
                }
                return false;
            }
        });
    }

    private void handleIntent(Intent intent) {
        reportId = intent.getLongExtra("reportId", -1);
        if(reportId == -1) {
            Log.d(TAG, "no reportId");
            finish();
        }
        initializeObjects(reportId);
    }


    private void initializeObjects(Long id) {
        new InitializeObjectsTask().execute(id);
    }

    private class InitializeObjectsTask extends AsyncTask<Long, Void, CollectionResponseComment> {

        @Override
        protected void onPreExecute() {
            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            adapter.clearObjects();
            super.onPreExecute();
        }

        @Override
        protected CollectionResponseComment doInBackground(Long... params) {
            Long id = params[0];
            try {
                // Cursor set "" here, because in Index and Document, the cursor return null when no more items
                return Api.getClient().report().getComments(id).execute();
            } catch (Exception e) {
                Log.d(TAG, "InitializeObjectsTask: " + e.getLocalizedMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(CollectionResponseComment comments) {
            //handle visibility
            super.onPostExecute(comments);
            if(comments != null) {
                if(comments.getItems() != null) {
                    commentList.addAll(comments.getItems());
                }
            } else {
                Snackbar.make(rootView, "Comment load failure", Snackbar.LENGTH_SHORT).show();
            }
            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            //set data for list
            adapter.addObjects(commentList);
        }

    }

    class CommentUploader extends AsyncTask<Comment, Void, Comment> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Comment doInBackground(Comment... params) {
            try {
                return Api.getClient().report().insertComment(reportId, params[0]).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Comment comment) {
            super.onPostExecute(comment);
            if(comment == null) {
                Toast.makeText(CommentActivity.this, "Cannot update comment", Toast.LENGTH_SHORT).show();
                return;
            }
            List<Comment> newComments = new ArrayList<>();
            newComments.add(comment);
            adapter.addObjects(newComments);
            progressBar.setVisibility(View.GONE);
        }
    }
}
