/*
  Authors: S. Stefani
 */

package io.github.core55.joinup.Model;

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
