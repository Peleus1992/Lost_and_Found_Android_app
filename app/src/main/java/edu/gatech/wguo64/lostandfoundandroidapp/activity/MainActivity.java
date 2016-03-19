package edu.gatech.wguo64.lostandfoundandroidapp.activity;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.adapter.ViewPagerAdapter;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.Feedback;
import edu.gatech.wguo64.lostandfoundandroidapp.constants.Preferences;
import edu.gatech.wguo64.lostandfoundandroidapp.constants.RequestCodes;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;
import edu.gatech.wguo64.lostandfoundandroidapp.notification.RegistrationIntentService;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.ImageDownloader;

public class MainActivity extends AppCompatActivity implements NavigationView
        .OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener, GoogleApiClient.OnConnectionFailedListener {
    public final static String TAG = MainActivity.class.getName();

    public SharedPreferences preferences;

    public final static String[] titles = {"Lost", "Found", "My Post"};

    public View rootView;
    public Toolbar toolbar;
    public DrawerLayout drawerLayout;
    public ViewPager viewPager;
    public FloatingActionButton fabBtn;
    public NavigationView navigation;
    public ImageView userImage;
    public TextView username;
    public TextView emailaddress;

    public ActionBarDrawerToggle drawerToggle;

    public ViewPagerAdapter viewPagerAdapter;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        inflateViews();

        setGoogleApiClient();

        setNotificationClient();

        setUI();

    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, connectionResult.getErrorMessage());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        switch (id) {
            case R.id.action_logout:
                logout();
                break;
            case R.id.action_search:
                startSearchReportActivity();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        drawerLayout.closeDrawers();
        onItemSelected(item.getItemId());
        return false;
    }

    @Override
    public void onPageSelected(int position) {
        getSupportActionBar().setTitle(titles[position]);
        switch (position) {
            case 0:
            case 1:
                fabBtn.setVisibility(View.VISIBLE);
                break;
            case 2:
                fabBtn.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int
            positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void inflateViews() {
        rootView = (View) findViewById(R.id.rootView);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigation = (NavigationView) findViewById(R.id.navigation);
        fabBtn = (FloatingActionButton) findViewById(R.id.fabBtn);
        View headerLayout = navigation.inflateHeaderView(R.layout
                .navigation_drawer_header);
        userImage = (ImageView) headerLayout.findViewById(R.id.userImage);
        username = (TextView) headerLayout.findViewById(R.id.username);
        emailaddress = (TextView) headerLayout.findViewById(R.id.emailaddress);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
    }

    private void setGoogleApiClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void setUI() {
        /**
         * Initiate toolbar and navigation drawer.
         */
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(titles[0]);
        drawerToggle = new ActionBarDrawerToggle(MainActivity.this,
                drawerLayout, toolbar, R.string.drawer_open_description, R
                .string.drawer_close_description);
        drawerLayout.setDrawerListener(drawerToggle);

        /**
         * Initiate user info.
         */
        navigation.setNavigationItemSelectedListener(this);
        String userEmailAddress = preferences.getString(Preferences.ACCOUNT_NAME, null);
        emailaddress.setText(userEmailAddress);
        new ImageDownloader(userImage).execute(preferences.getString(Preferences
                .ACCOUNT_PHOTO_URL, null));

        /**
         * Initiate viewpager
         */
        viewPagerAdapter = new ViewPagerAdapter
                (getSupportFragmentManager(), titles.length);
        viewPager.addOnPageChangeListener(this);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(0);

        /**
         * Initiate fab
         */
        fabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startReportActivity();
            }
        });
    }


    private void startReportActivity() {
        Intent i = new Intent(this, ReportActivity.class);
        startActivity(i);
    }

    private void startSearchReportActivity() {
        Intent intent = new Intent(this, SearchReportActivity.class);
        startActivity(intent);
    }
    private void onItemSelected(int itemID) {
        switch (itemID) {
            case R.id.menu_item_lost:
                viewPager.setCurrentItem(0);
                break;
            case R.id.menu_item_found:
                viewPager.setCurrentItem(1);
                break;
            case R.id.menu_item_my_posts:
                viewPager.setCurrentItem(2);
                break;
            case R.id.menu_item_help:
                startFeedbackDialog();
                break;
            case R.id.menu_item_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void startFeedbackDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Feedback");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected;
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String content = input.getText().toString();
                if(content.length() < 10) {
                    Toast.makeText(MainActivity.this, "Feedback should be at least 10 characters.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Feedback feedback = new Feedback();
                feedback.setUserEmail(preferences.getString(Preferences.ACCOUNT_NAME, null));
                feedback.setContent(content);
                new FeedbackUploader().execute(feedback);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private class FeedbackUploader extends AsyncTask<Feedback, Void, Feedback> {
        @Override
        protected Feedback doInBackground(Feedback... params) {
            try {
                if(params.length > 0 && params[0] != null) {
                    return Api.getClient().feedback().insertFeedback(params[0]).execute();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Feedback feedback) {
            super.onPostExecute(feedback);
            if(feedback != null) {
                Toast.makeText(MainActivity.this
                        , "Successfully submit feedback! Thank you for your time!"
                        , Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this
                        , "Sorry, feedback is not successfully submitted."
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void logout() {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            finish();
                        } else {
                            Log.e(TAG, getString(R.string.failure_log_out));
                            Snackbar.make(rootView, R.string.failure_log_out, Snackbar.LENGTH_SHORT);
                        }
                    }
                }
        );
        onLogout();
    }

    private void onLogout() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system preferences.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode,
                        RequestCodes.PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "checkPlayServices: This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void setNotificationClient() {

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

}
