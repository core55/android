package io.github.core55.joinup.Model;

public class StringResponse {

    private long timestamp;
    private String message;

    public StringResponse() {
    }

    public StringResponse(String message) {
        this();
        this.setTimestamp();
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp() {
        this.timestamp = System.currentTimeMillis() / 1000L;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
