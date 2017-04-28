package io.github.core55.joinup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by prst on 2017-04-27.
 */

public class User extends BaseEntity {

    private String nickname;
    private Double lastLongitude;
    private Double lastLatitude;
    private String username;
    private List<String> meetupsList = new ArrayList<>();
    //private List<Location> locations;

    protected User() {
        super();
    }

    public User(Double lastLongitude, Double lastLatitude) {
        this();
        this.lastLongitude = lastLongitude;
        this.lastLatitude = lastLatitude;
        //setCreatedAt();
        //setUpdatedAt();
    }

    public static User fromJson(JSONObject jsonUser, JSONArray jsonMeetupsArray) {
        User u = new User();

        try {
            u.nickname = jsonUser.getString("nickname");
            u.lastLongitude = jsonUser.getDouble("lastLongitude");
            u.lastLatitude = jsonUser.getDouble("lastLatitude");
            u.username = jsonUser.getString("username");
            u.createdAt = jsonUser.getString("createdAt");
            u.updatedAt = jsonUser.getString("updatedAt");

            List<String> meetupsList = new ArrayList<>();
            if (jsonMeetupsArray != null) {
                for (int i = 0; i < jsonMeetupsArray.length(); i++) {
                    meetupsList.add(jsonMeetupsArray.getJSONObject(i).getString("hash"));
                }
            }
            u.meetupsList = new ArrayList<>(meetupsList);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return u;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Double getLastLongitude() {
        return lastLongitude;
    }

    public void setLastLongitude(Double lastLongitude) {
        this.lastLongitude = lastLongitude;
    }

    public Double getLastLatitude() {
        return lastLatitude;
    }

    public void setLastLatitude(Double lastLatitude) {
        this.lastLatitude = lastLatitude;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getMeetupsList() {
        return meetupsList;
    }

    public void setMeetupsList(List<String> meetupsList) {
        this.meetupsList = meetupsList;
    }
}
