package io.github.core55.joinup.Model;

import io.github.core55.joinup.entities.User;

/**
 * Created by simone on 2017-05-10.
 */

public class AuthenticationResponse {
    private User user;
    private String jwt;

    public AuthenticationResponse() {
        super();
    }

    public AuthenticationResponse(User user, String jwt) {
        this();
        this.user = user;
        this.jwt = jwt;
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
}
