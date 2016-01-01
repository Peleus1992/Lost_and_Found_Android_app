package edu.gatech.wguo64.lostandfoundandroidapp.activity;


import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.net.HttpURLConnection;
import java.net.URL;

import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.adapter.ViewPagerAdapter;
import edu.gatech.wguo64.lostandfoundandroidapp.fragment.FoundFragment;
import edu.gatech.wguo64.lostandfoundandroidapp.fragment.LostFragment;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;
import edu.gatech.wguo64.lostandfoundandroidapp.notification.RegistrationIntentService;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.Constants;

public class MainActivity extends AppCompatActivity implements NavigationView
        .OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener, GoogleApiClient.OnConnectionFailedListener {
    static final String[] titles = {"Lost", "Found", "My Post"};
    private static final String AUDIENCE =
            "server:client_id:179957261506-0v5m4i2bg8v9bj34ln03qs9b38gs93mo.apps.googleusercontent.com";
    public GoogleAccountCredential credential;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    CoordinatorLayout rootLayout;
    ViewPagerAdapter viewPagerAdapter;
    ViewPager viewPager;
    FloatingActionButton fabBtn;
    NavigationView navigation;
    ImageView userImage;
    TextView username;
    TextView emailaddress;
    SearchView searchView;
    MenuItem searchItem;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initialize Google Sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        //initiate toolbar and navigation drawer
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setNotificationClient();
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(titles[0]);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerToggle = new ActionBarDrawerToggle(MainActivity.this,
                drawerLayout, toolbar, R.string.drawer_open_description, R
                .string.drawer_close_description);
        drawerLayout.setDrawerListener(drawerToggle);

        //initiate user info
        navigation = (NavigationView) findViewById(R.id.navigation);
        navigation.setNavigationItemSelectedListener(this);
        View headerLayout = navigation.inflateHeaderView(R.layout
                .navigation_drawer_header);
        userImage = (ImageView) headerLayout.findViewById(R.id.userImage);
        username = (TextView) headerLayout.findViewById(R.id.username);
        emailaddress = (TextView) headerLayout.findViewById(R.id.emailaddress);
        String userEmailAddress = getSharedPreferences("LostAndFound", 0)
                .getString(Constants.PREF_ACCOUNT_NAME, null);
        emailaddress.setText(userEmailAddress);
        new AsyncTask<String, Void, Drawable>() {
            @Override
            protected Drawable doInBackground(String... params) {

                try {
                    URL url = new URL(params[0]);

                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.connect();
                    Drawable drawable = BitmapDrawable.createFromStream(connection.getInputStream(), params[0]);
                    return drawable;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Drawable drawable) {
                super.onPostExecute(drawable);
                userImage.setBackground(drawable);
            }
        }.execute(getSharedPreferences("LostAndFound", 0).getString(Constants
                .PREF_ACCOUNT_PHOTO_URL, null));
        //initiate viewpager
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPagerAdapter = new ViewPagerAdapter
                (getSupportFragmentManager(), titles.length);
        viewPager.addOnPageChangeListener(this);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(0);


        //initiate fab
        fabBtn = (FloatingActionButton) findViewById(R.id.fabBtn);
        fabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent i = new Intent(this, PinDropActivity.class);
                if (viewPager.getCurrentItem() == 0) {
                    startReportLostActivity();
                } else if (viewPager.getCurrentItem() == 1) {
                    startReportFoundActivity();
                }
            }
        });


        /**
         * For test only
         */
//        Test.loadReportedLostObjects(this);
//        Test.loadReportedFoundObjects(this);

        credential = GoogleAccountCredential
                .usingAudience(this,
                        AUDIENCE);
        Log.d("show me my name", userEmailAddress);
        credential.setSelectedAccountName(userEmailAddress);
        Log.d("show me what's set", credential.getSelectedAccountName());

        Api.initialize(credential);
    }

    public void startReportLostActivity() {
        Intent i = new Intent(this, ReportLostActivity.class);
        startActivity(i);
    }

    public void startReportFoundActivity() {
        Intent i = new Intent(this, ReportFoundActivity.class);
        startActivity(i);
    }


    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        Log.i("mylog", "hahhahaha");
//        drawerToggle.onConfigurationChanged(newConfig);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchItem = menu.findItem(R.id.action_search);
        return true;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("myinfo", connectionResult.getErrorMessage());
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
                Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                if(status.isSuccess()) {
                                    finish();
                                } else {
                                    Log.i("myinfo", "Sign out failure");
                                }
                            }
                        }
                );
                finish();
            case R.id.action_search:
                getSupportActionBar().setDisplayShowCustomEnabled(true);
                //enable it to display a
                // custom view in the action bar.
                item.setVisible(false);
                getSupportActionBar().setCustomView(R.layout.search_view);
                //add the custom view
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                //hide the title

                searchView = (SearchView) getSupportActionBar().getCustomView
                        ().findViewById(R.id.searchView); //the text editor


                //this is a listener to do a search when the user clicks on
                // search button
                searchView.setQueryHint("Enter here");
                searchView.setOnQueryTextFocusChangeListener(new View
                        .OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            getSupportActionBar().setDisplayShowCustomEnabled
                                    (false);
                            getSupportActionBar().setDisplayShowTitleEnabled
                                    (true);
                            searchItem.setVisible(true);
                        }
                    }
                });
                searchView.setOnQueryTextListener(new SearchView
                        .OnQueryTextListener() {

                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        Fragment fragment = ((ViewPagerAdapter) viewPager
                                .getAdapter()).getItem(viewPager
                                .getCurrentItem());
                        if (viewPager.getCurrentItem() == 0) {
                            ((LostFragment)viewPagerAdapter.getRegisteredFragment(0)).searchObjects(query);
                        } else if(viewPager.getCurrentItem() == 1) {
                            ((FoundFragment)viewPagerAdapter.getRegisteredFragment(1)).searchObjects(query);
                        }

                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                });
                searchView.setIconified(false);
                searchView.setFocusable(true);
                searchView.requestFocusFromTouch();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onItemSelected(int itemID) {
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
            default:
                break;
        }
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
                if (searchView != null) {
                    searchView.clearFocus();
                }
                searchItem.setVisible(true);
                fabBtn.setVisibility(View.VISIBLE);
                break;
            case 2:
                if(searchView != null) {
                    searchView.clearFocus();
                }
                searchItem.setVisible(false);
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


    //Notification
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i("myinfo", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void setNotificationClient() {

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }
}
