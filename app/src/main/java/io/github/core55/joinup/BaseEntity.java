package io.github.core55.joinup;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by prst on 2017-04-27.
 */

public abstract class BaseEntity {

    private final Long id;

    private String createdAt;
    private String updatedAt;

    protected BaseEntity() {
        id = null;
    }

    public Long getId() {
        return id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt() {
        this.createdAt = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt() {
        this.updatedAt = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    }

}
