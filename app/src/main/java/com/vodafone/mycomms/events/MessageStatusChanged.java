package com.vodafone.mycomms.events;

/**
 * Created by str_rbm on 04/06/2015.
 */
public class MessageStatusChanged {
    private String id;
    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
