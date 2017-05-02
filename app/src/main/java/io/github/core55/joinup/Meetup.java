package io.github.core55.joinup;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by prst on 2017-04-27.
 */

public class Meetup extends BaseEntity implements Parcelable {

    private Double centerLongitude;
    private Double centerLatitude;
    private Integer zoomLevel;
    private String hash;
    private Double pinLongitude;
    private Double pinLatitude;
    private String name;
    private List<Long> usersList = new ArrayList<>();

    protected Meetup() {
        super();
    }

    public Meetup(Double centerLongitude, Double centerLatitude, Integer zoomLevel) {
        this();
        this.centerLongitude = centerLongitude;
        this.centerLatitude = centerLatitude;
        this.zoomLevel = zoomLevel;
    }

    private Meetup (Parcel in) {
        centerLongitude = in.readDouble();
        centerLatitude = in.readDouble();
        zoomLevel = in.readInt();
        hash = in.readString();
        pinLongitude = in.readDouble();
        pinLatitude = in.readDouble();
        name = in.readString();
        in.readList(usersList, Meetup.class.getClassLoader());
    }

    public static Meetup fromJson(JSONObject jsonMeetup, JSONArray jsonUsersArray) {
        Meetup m = new Meetup();

        try {
            m.centerLongitude = jsonMeetup.getDouble("centerLongitude");
            m.centerLatitude = jsonMeetup.getDouble("centerLatitude");
            m.zoomLevel = jsonMeetup.getInt("zoomLevel");
            m.hash = jsonMeetup.getString("hash");
            m.pinLongitude = jsonMeetup.getDouble("pinLongitude");
            m.pinLatitude = jsonMeetup.getDouble("pinLatitude");
            m.name = jsonMeetup.getString("name");
            m.createdAt = jsonMeetup.getString("createdAt");
            m.updatedAt = jsonMeetup.getString("updatedAt");

            List<Long> usersList = new ArrayList<>();
            if (jsonUsersArray != null) {
                for (int i = 0; i < jsonUsersArray.length(); i++) {
                    usersList.add(jsonUsersArray.getJSONObject(i).getLong("id"));
                }
            }
            m.usersList = new ArrayList<>(usersList);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return m;
    }

    public Double getCenterLongitude() {
        return centerLongitude;
    }

    public void setCenterLongitude(Double centerLongitude) {
        this.centerLongitude = centerLongitude;
    }

    public Double getCenterLatitude() {
        return centerLatitude;
    }

    public void setCenterLatitude(Double centerLatitude) {
        this.centerLatitude = centerLatitude;
    }

    public Integer getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(Integer zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Double getPinLongitude() {
        return pinLongitude;
    }

    public void setPinLongitude(Double pinLongitude) {
        this.pinLongitude = pinLongitude;
    }

    public Double getPinLatitude() {
        return pinLatitude;
    }

    public void setPinLatitude(Double pinLatitude) {
        this.pinLatitude = pinLatitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Long> getUsersList() {
        return usersList;
    }

    public void setUsersList(List<Long> usersList) {
        this.usersList = usersList;
    }


    /*
     * Methods for parcelable object
     */

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(centerLongitude);
        out.writeDouble(centerLatitude);
        out.writeInt(zoomLevel);
        out.writeString(hash);
        out.writeDouble(pinLongitude);
        out.writeDouble(pinLatitude);
        out.writeString(name);
        out.writeList(usersList);
    }

    public static final Parcelable.Creator<Meetup> CREATOR = new Parcelable.Creator<Meetup>() {
        public Meetup createFromParcel(Parcel in) {
            return new Meetup(in);
        }

        public Meetup[] newArray(int size) {
            return new Meetup[size];
        }
    };
}
