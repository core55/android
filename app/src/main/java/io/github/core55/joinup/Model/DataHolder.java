/*
  Authors: S. Stefani
 */

package io.github.core55.joinup.Model;

import io.github.core55.joinup.Entity.Meetup;
import io.github.core55.joinup.Entity.User;

public class DataHolder {

    private static final DataHolder holder = new DataHolder();

    private User user;
    private String jwt;
    private boolean isAuthenticated;
    private boolean isAnonymous;
    private Meetup meetup;

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
}

