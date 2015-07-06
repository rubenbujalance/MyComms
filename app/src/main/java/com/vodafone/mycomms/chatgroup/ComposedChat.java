package com.vodafone.mycomms.chatgroup;

import model.Chat;
import model.GroupChat;

/**
 * Created by str_oan on 02/07/2015.
 */
public class ComposedChat
{
    private Chat chat;
    private GroupChat groupChat;

    public ComposedChat(Chat chat, GroupChat groupChat) {
        this.chat = chat;
        this.groupChat = groupChat;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public GroupChat getGroupChat() {
        return groupChat;
    }

    public void setGroupChat(GroupChat groupChat) {
        this.groupChat = groupChat;
    }
}
