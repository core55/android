/*
  Authors: S. Stefani
 */

package io.github.core55.joinup.Model;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

import io.github.core55.joinup.Activity.MapActivity;
import io.github.core55.joinup.Entity.Meetup;
import io.github.core55.joinup.Entity.User;
import io.github.core55.joinup.Helper.DrawerFragment;

public class DataHolder {

    private static final DataHolder holder = new DataHolder();

    private User user;
    private String jwt;
    private boolean isAuthenticated;
    private boolean isAnonymous;
    private Meetup meetup;
    private List<User> userList;
    private DrawerFragment drawer;

    public MapActivity getActivity() {
        return activity;
    }

    public void setActivity(MapActivity activity) {
        this.activity = activity;
    }

    private MapActivity activity;


    public DrawerFragment getDrawer() {return drawer; }

    public void setDrawer(DrawerFragment drawer) {this.drawer = drawer;}

    public static DataHolder getInstance() {
        return holder;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    public Meetup getMeetup() {
        return meetup;
    }

    public void setMeetup(Meetup meetup) {
        this.meetup = meetup;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }
}

