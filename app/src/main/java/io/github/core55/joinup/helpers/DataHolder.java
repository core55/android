/*
  Authors: S. Stefani
 */

package io.github.core55.joinup.helpers;

import java.util.HashMap;

public class DataHolder {

    private static final DataHolder holder = new DataHolder();

    private HashMap<String, String> user = new HashMap<>();
    private String jwt;
    private boolean isAuthenticated;

    public static DataHolder getInstance() {
        return holder;
    }

    public HashMap<String, String> getUser() {
        return user;
    }

    public void setUser(HashMap<String, String> user) {
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

