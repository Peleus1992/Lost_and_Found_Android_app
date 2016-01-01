package edu.gatech.wguo64.lostandfoundandroidapp.activity;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.entity.Position;
import edu.gatech.wguo64.lostandfoundandroidapp.googlemaps.DraggableCircle;
import edu.gatech.wguo64.lostandfoundandroidapp.googlemaps.LocationHelper;


public class PinDropActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnInfoWindowClickListener
{
    CoordinatorLayout coordinatorLayout;
    GoogleMap mMap;
    Geocoder geocoder;
    List<DraggableCircle> listCycle;
    Toolbar toolbar;
    MenuItem searchItem;
    SearchView searchView;
    FloatingActionButton fabBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(edu.gatech.wguo64.lostandfoundandroidapp.R.layout.activity_pin_drop);

        listCycle = new ArrayList<DraggableCircle>();

        toolbar = (Toolbar)findViewById(edu.gatech.wguo64.lostandfoundandroidapp.R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PinDropActivity.this.onBackPressed();
            }
        });
        coordinatorLayout = (CoordinatorLayout)findViewById(edu.gatech.wguo64.lostandfoundandroidapp.R.id.coordinatorLayout);
        fabBtn = (FloatingActionButton)findViewById(edu.gatech.wguo64.lostandfoundandroidapp.R.id.fabBtn);
        fabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Position> alPositions = new ArrayList<Position>();
                for(DraggableCircle circle : listCycle) {
                    Position pos = new Position();
                    circle.getPosition(pos);
                    alPositions.add(pos);
                }
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra("alPositions", alPositions);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        geocoder = new Geocoder(this, Locale.US);

        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocationName("Georgia Tech", 5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(addresses != null && addresses.size() != 0) {
            LatLng gt = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(gt, 15.0f));
        } else {
            Log.i("myinfo", "no location");
        }
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        Location myLoc = locationManager.getLastKnownLocation(provider);
        if(myLoc != null) {
            //Get my location
            LatLng latLng = new LatLng(myLoc.getLatitude(), myLoc.getLongitude());
            DraggableCircle circle = new DraggableCircle(mMap, latLng, getAddressFromLatLng(latLng));
            listCycle.add(circle);
        }
    }
    public String getAddressFromLatLng(LatLng latLng) {
        String address = "";
        try {
            List<Address> list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5);
            if(list.size() > 0) {
                StringBuffer sb = new StringBuffer();
                for(int i = 0; i < list.get(0).getMaxAddressLineIndex(); i++) {
                    sb.append(list.get(0).getAddressLine(i));
                }
                address = sb.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return address;
    }
    @Override
    public void onMapLongClick(LatLng latLng) {
        DraggableCircle circle = new DraggableCircle(mMap, latLng, getAddressFromLatLng(latLng));
        listCycle.add(circle);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        onMarkerMoved(marker);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        onMarkerMoved(marker);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        onMarkerMoved(marker);
    }

    public void onMarkerMoved(Marker marker) {
        for(DraggableCircle circle : listCycle) {
            if(circle.onCenterMarkerMoved(marker)) {
                circle.setAddress(getAddressFromLatLng(marker.getPosition()));
                break;
            }
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        for(DraggableCircle circle : listCycle) {
            if(circle.remove(marker)) {
                listCycle.remove(circle);
                break;
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pin_drop_menu, menu);
        return true;
    }


    class AutoCompleteTask extends AsyncTask<String, String, List<String>> {
        @Override
        protected List<String> doInBackground(String... params) {
            List<HashMap<String, String>> list = LocationHelper.getAutoCompletePlaces(params[0]);
            List<String> res = new ArrayList<String>();
            for(HashMap<String, String> hm : list) {
                res.add(hm.get("description"));
            }
            return res;
        }

        @Override
        protected void onPostExecute(List<String> listAddr) {
            super.onPostExecute(listAddr);
            MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "address"});
            for(int i = 0; i < listAddr.size(); i++) {
                cursor.addRow(new Object[]{i, listAddr.get(i)});
            }

            searchView.setSuggestionsAdapter(new SimpleCursorAdapter(PinDropActivity.this,
                    R.layout.search_suggestion_list_item, cursor,
                    new String[]{"address"}, new int[]{R.id.place},
                    SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER));

            searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                @Override
                public boolean onSuggestionSelect(int position) {
                    return false;
                }

                @Override
                public boolean onSuggestionClick(int position) {
                    Cursor cursor = searchView.getSuggestionsAdapter().getCursor();
                    searchView.setQuery((String)cursor.getString(cursor.getColumnIndex("address")), true);
                    return false;
                }
            });
        }

    }

    class PlaceDetailsTask extends AsyncTask<String, String, HashMap<String, String>> {
        @Override
        protected HashMap<String, String> doInBackground(String... params) {
            HashMap<String, String> hm = LocationHelper.getPlaceDetails(params[0]);
            return hm;
        }

        @Override
        protected void onPostExecute(HashMap<String, String> hm) {
            super.onPostExecute(hm);
            if(hm != null) {
                String address = hm.get("formatted_address");
                searchView.setQuery(address, false);

                Double lat = Double.parseDouble(hm.get("lat"));
                Double lng = Double.parseDouble(hm.get("lng"));
                LatLng latLng = new LatLng(lat, lng);
                DraggableCircle circle = new DraggableCircle(mMap, latLng, address);
                listCycle.add(circle);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
            } else {
                Snackbar.make(coordinatorLayout, "Your input address does not exist!", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                searchItem = item;
                getSupportActionBar().setDisplayShowCustomEnabled(true); //enable it to display a
                // custom view in the action bar.
                item.setVisible(false);
                getSupportActionBar().setCustomView(R.layout.search_view);//add the custom view
                getSupportActionBar().setDisplayShowTitleEnabled(false); //hide the title

                searchView = (SearchView)getSupportActionBar().getCustomView().findViewById(R.id.searchView); //the text editor


                //this is a listener to do a search when the user clicks on search button
                searchView.setQueryHint("Enter an address");
                searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            getSupportActionBar().setDisplayShowCustomEnabled(false);
                            getSupportActionBar().setDisplayShowTitleEnabled(true);
                            searchItem.setVisible(true);
                        }
                    }
                });
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    AutoCompleteTask autoCompleteTask = null;
                    PlaceDetailsTask placeDetailsTask = null;
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        if(placeDetailsTask == null || placeDetailsTask.getStatus() == AsyncTask.Status.FINISHED) {
                            placeDetailsTask = new PlaceDetailsTask();
                            placeDetailsTask.execute(query);
                        }
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if(newText == null || newText.length() < 2) {
                            return false;
                        }
                        if(autoCompleteTask == null || autoCompleteTask.getStatus() == AsyncTask.Status.FINISHED) {
                            autoCompleteTask = new AutoCompleteTask();
                            autoCompleteTask.execute(newText);
                        }

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
}

