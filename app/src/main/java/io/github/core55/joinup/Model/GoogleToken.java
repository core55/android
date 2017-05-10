package io.github.core55.joinup.Model;

/**
 * Created by simone on 2017-05-10.
 */

public class GoogleToken {
    private String idToken;

    public GoogleToken() {
    }

    public GoogleToken(String idToken) {
        this();
        this.idToken = idToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
