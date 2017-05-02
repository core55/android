package io.github.core55.joinup;

import java.util.HashMap;

public class DataHolder {
    private HashMap<String, String> user = new HashMap<>();
    private String jwt;

    private static final DataHolder holder = new DataHolder();

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
}

