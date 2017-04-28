package io.github.core55.joinup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by prst on 2017-04-27.
 */

public class Meetup extends BaseEntity {

    private Double centerLongitude;
    private Double centerLatitude;
    private Integer zoomLevel;
    private String hash;
    private Double pinLongitude;
    private Double pinLatitude;
    private String name;
    private List<User> users = new ArrayList<>();

    protected Meetup() {
        super();
    }

    public Meetup(Double centerLongitude, Double centerLatitude, Integer zoomLevel) {
        this();
        this.centerLongitude = centerLongitude;
        this.centerLatitude = centerLatitude;
        this.zoomLevel = zoomLevel;
        setCreatedAt();
        setUpdatedAt();
    }


    // Decodes business json into business model object
    public static Meetup fromJson(JSONObject jsonObject) {

        Meetup m = new Meetup();

        // Deserialize json into object fields
        try {
            m.centerLongitude = jsonObject.getDouble("id");
            m.centerLatitude = jsonObject.getDouble("name");
            m.zoomLevel = jsonObject.getInt("display_phone");
            m.hash = jsonObject.getString("image_url");
            m.pinLongitude = jsonObject.getDouble("id");
            m.pinLatitude = jsonObject.getDouble("id");
            m.name = jsonObject.getString("id");

            //m.users = jsonObject.getJSONArray("id");

            List<User> list = new ArrayList<>();
            JSONArray jsonArray = jsonObject.getJSONArray("id");
            if (jsonArray != null) {
                int len = jsonArray.length();
                for (int i = 0; i < len; i++) {
                    //list.add((User) jsonArray.get(i));

                    User u = new User();



                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        // Return new object
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

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
