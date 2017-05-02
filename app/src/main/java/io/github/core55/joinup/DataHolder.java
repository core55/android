package io.github.core55.joinup;

import java.util.HashMap;

public class DataHolder {
    HashMap<String, String> user = new HashMap<>();

    public HashMap<String, String> getUser() {
        return user;
    }

    public void setUser(HashMap<String, String> user) {
        this.user = user;
    }

    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() {return holder;}
}

