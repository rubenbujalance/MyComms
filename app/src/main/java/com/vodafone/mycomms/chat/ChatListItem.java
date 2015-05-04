package com.vodafone.mycomms.chat;

public class ChatListItem {
    private String chatText;
    private int chatType;

    public ChatListItem(String chatText, int chatType) {
        this.chatText = chatText;
        this.chatType = chatType;
    }

    public String getChatText() {
        return chatText;
    }

    public void setChatText(String chatText) {
        this.chatText = chatText;
    }

    public int getChatType() {
        return chatType;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }
}
