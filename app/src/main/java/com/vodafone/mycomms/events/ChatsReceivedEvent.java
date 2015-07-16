package com.vodafone.mycomms.events;

import model.ChatMessage;

/**
 * Created by str_rbm on 04/06/2015.
 */
public class ChatsReceivedEvent {
    private ChatMessage message;
    private int pendingMessages;

    public ChatMessage getMessage() {
        return message;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }

    public int getPendingMessages() {
        return pendingMessages;
    }

    public void setPendingMessages(int pendingMessages) {
        this.pendingMessages = pendingMessages;
    }
}

