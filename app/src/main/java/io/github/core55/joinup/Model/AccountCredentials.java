package io.github.core55.joinup.Model;

public class AccountCredentials {

    private String username;
    private String password;
    private String oldUsername;

    public AccountCredentials() {
    }

    public AccountCredentials(String username, String password) {
        this();
        this.username = username;
        this.password = password;
    }

    public AccountCredentials(String username, String password, String oldUsername) {
        this();
        this.username = username;
        this.password = password;
        this.oldUsername = oldUsername;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOldUsername() {
        return oldUsername;
    }

    public void setOldUsername(String oldUsername) {
        this.oldUsername = oldUsername;
    }
}
