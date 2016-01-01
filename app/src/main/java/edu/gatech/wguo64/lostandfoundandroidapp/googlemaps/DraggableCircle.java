package edu.gatech.wguo64.lostandfoundandroidapp.googlemaps;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.gatech.wguo64.lostandfoundandroidapp.entity.Position;

/**
 * Created by guoweidong on 11/1/15.
 */
public class DraggableCircle {
    public static final double RADIUS_OF_EARTH_METERS = 6371009;
    private static final int FILL_COLOR = Color.argb(122, 255, 0, 0);
    private static final int STORKE_COLOR = Color.BLACK;
    private static final int STROKE_WIDTH = 0;
    private static final double DEFAULT_RADIUS = 25;
    private final Marker centerMarker;

    private final Circle circle;

    private String address;

    private GoogleMap mMap;

    public DraggableCircle(GoogleMap mMap, LatLng center, String address) {
        this.mMap = mMap;
        centerMarker = mMap.addMarker(new MarkerOptions()
                .position(center)
                .title("Delete")
                .draggable(true));

        circle = mMap.addCircle(new CircleOptions()
                .center(center)
                .radius(DEFAULT_RADIUS)
                .strokeWidth(STROKE_WIDTH)
                .strokeColor(STORKE_COLOR)
                .fillColor(FILL_COLOR));
        this.address = address;
    }

    public boolean onCenterMarkerMoved(Marker marker) {
        if (marker.equals(centerMarker)) {
            circle.setCenter(marker.getPosition());
            return true;
        }
        return false;
    }

    public boolean remove(Marker marker) {
        if (marker.equals(centerMarker)) {
            circle.remove();
            centerMarker.remove();
            return true;
        }
        return false;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Address: " + address + "\n" +
                "Latitude:" + centerMarker.getPosition().latitude + "\n" +
                "Longitude:" + centerMarker.getPosition().longitude + "\n" +
                "Radius:" + DEFAULT_RADIUS;
    }

    public void getPosition(Position pos) {
        pos.address = address;
        pos.lat = (float) centerMarker.getPosition().latitude;
        pos.lng = (float) centerMarker.getPosition().longitude;
    }
}
