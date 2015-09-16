package com.vodafone.mycomms.events;

/**
 * Created by str_rbm on 04/06/2015.
 */
public class GroupChatCreatedEvent {
    private String groupId;
    private boolean success;

    public String getGroupId() {
        return groupId;
    }
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}

