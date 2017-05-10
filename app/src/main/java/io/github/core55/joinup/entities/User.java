package io.github.core55.joinup.entities;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by prst on 2017-04-27.
 */

public class User extends BaseEntity implements Parcelable {

    private String nickname;
    private Double lastLongitude;
    private Double lastLatitude;
    private String username;
    private String status;
    private String profilePicture;


    //private List<String> meetupsList = new ArrayList<>();

    protected User() {
        super();
    }

    public User(Double lastLongitude, Double lastLatitude) {
        this();
        this.lastLongitude = lastLongitude;
        this.lastLatitude = lastLatitude;
    }

    private User(Parcel in) {
        id = in.readLong();
        nickname = in.readString();
        lastLongitude = in.readDouble();
        lastLatitude = in.readDouble();
        username = in.readString();
        //in.readList(meetupsList, User.class.getClassLoader());
    }

    public static User fromJson(JSONObject jsonUser) {
        User u = new User();

        try {
            u.id = jsonUser.getLong("id");
            u.nickname = jsonUser.getString("nickname");
            u.lastLongitude = jsonUser.getDouble("lastLongitude");
            u.lastLatitude = jsonUser.getDouble("lastLatitude");
            u.username = jsonUser.getString("username");
            u.createdAt = jsonUser.getString("createdAt");
            u.updatedAt = jsonUser.getString("updatedAt");

            /*
            List<String> meetupsList = new ArrayList<>();
            if (jsonMeetupsArray != null) {
                for (int i = 0; i < jsonMeetupsArray.length(); i++) {
                    meetupsList.add(jsonMeetupsArray.getJSONObject(i).getString("hash"));
                }
            }
            u.meetupsList = new ArrayList<>(meetupsList);
            */

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public User(String nickname, String status, String profilePicture) {
        this.nickname = nickname;
        this.status = status;
        this.profilePicture = profilePicture;
    }

    /*
    public List<String> getMeetupsList() { return meetupsList; }
    */

    /*
    public void setMeetupsList(List<String> meetupsList) {
        this.meetupsList = meetupsList;
    }
    */

    /*
     * Methods for parcelable object
     */

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeString(nickname);
        out.writeDouble(lastLongitude);
        out.writeDouble(lastLatitude);
        out.writeString(username);
        //out.writeList(meetupsList);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

}
