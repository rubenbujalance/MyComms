package com.vodafone.mycomms.events;

import model.GroupChat;

/**
 * Created by str_rbm on 04/06/2015.
 */
public class GroupChatCreatedEvent {
    private GroupChat chat;

    public GroupChat getGroupChat() {
        return chat;
    }

    public void setGroupChat(GroupChat chat) {
        this.chat = chat;
    }
}

