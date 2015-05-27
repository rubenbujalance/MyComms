package model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ChatListItem extends RealmObject {
    @PrimaryKey
    private String chatSenderId;

    private String chatSenderReceiverId;

    private String chatSenderName;
    private String chatReceiverName;
    private String lastMessage;
    private String lastMessageTime;

    public ChatListItem(){
    }

    public ChatListItem(String chatSenderId, String chatSenderReceiverId, String chatSenderName, String chatReceiverName, String lastMessage, String lastMessageTime) {
        this.chatSenderId = chatSenderId;
        this.chatSenderReceiverId = chatSenderReceiverId;
        this.chatSenderName = chatSenderName;
        this.chatReceiverName = chatReceiverName;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }

    public String getChatSenderId() {
        return chatSenderId;
    }

    public void setChatSenderId(String chatSenderId) {
        this.chatSenderId = chatSenderId;
    }

    public String getChatSenderReceiverId() {
        return chatSenderReceiverId;
    }

    public void setChatSenderReceiverId(String chatSenderReceiverId) {
        this.chatSenderReceiverId = chatSenderReceiverId;
    }

    public String getChatSenderName() {
        return chatSenderName;
    }

    public void setChatSenderName(String chatSenderName) {
        this.chatSenderName = chatSenderName;
    }

    public String getChatReceiverName() {
        return chatReceiverName;
    }

    public void setChatReceiverName(String chatReceiverName) {
        this.chatReceiverName = chatReceiverName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
}
