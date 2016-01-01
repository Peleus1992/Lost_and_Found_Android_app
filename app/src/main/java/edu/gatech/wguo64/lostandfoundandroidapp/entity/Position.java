package edu.gatech.wguo64.lostandfoundandroidapp.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by guoweidong on 11/5/15.
 */
public class Position implements Parcelable {
    public static final Creator<Position> CREATOR = new Creator<Position>() {
        @Override
        public Position createFromParcel(Parcel source) {
            return new Position(source);
        }

        @Override
        public Position[] newArray(int size) {
            return new Position[size];
        }

    };
    public String address;
    public float lat;
    public float lng;
    public float radius;

    public Position() {

    }

    private Position(Parcel in) {
        address = in.readString();
        lat = in.readFloat();
        lng = in.readFloat();
    }

    public Position(Float latitude, Float longitude) {
        lat = latitude;
        lng = longitude;
        address = "Unknown address";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeFloat(lat);
        dest.writeFloat(lng);

    }

    @Override
    public int describeContents() {
        return 0;
    }


}
