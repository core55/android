/*
  Authors: S. Stefani
 */

package io.github.core55.joinup.helpers;

import java.util.HashMap;

import io.github.core55.joinup.entities.User;

public class DataHolder {

    private static final DataHolder holder = new DataHolder();

    private User user;
    private String jwt;
    private boolean isAuthenticated;

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
}

