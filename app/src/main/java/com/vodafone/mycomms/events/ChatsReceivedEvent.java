package com.vodafone.mycomms.events;

import model.ChatMessage;

/**
 * Created by str_rbm on 04/06/2015.
 */
public class ChatsReceivedEvent {
    private ChatMessage message;

    public ChatMessage getMessage() {
        return message;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }
}

