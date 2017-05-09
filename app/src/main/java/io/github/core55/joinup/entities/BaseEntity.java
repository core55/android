package io.github.core55.joinup.entities;


import android.os.Parcelable;

/**
 * Created by prst on 2017-04-27.
 */

public abstract class BaseEntity implements Parcelable {

    protected Long id;

    protected String createdAt;
    protected String updatedAt;

    protected BaseEntity() {
        id = null;
    }

    public Long getId() {
        return id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /*
     * Methods for parcelable object
     */

    public int describeContents() {
        return 0;
    }

}
