package model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by str_oan on 01/07/2015.
 */
public class GroupChat extends RealmObject
{
    @PrimaryKey
    private String id; //mg_RandomUUID

    private String creatorId;
    private String profileId;
    private String name;
    private String avatar;
    private String about;
    private String members; //Concatenation of members: contactId@contactId...@contactId
    private long lastMessageTime;
    private String lastMessage;
    private String lastMessage_id;

    public GroupChat (){}

    public GroupChat(String id, String profileId, String creatorId, String name, String avatar,
                     String
            about, String
            members, long lastMessageTime)
    {
        this.id = id;
        this.profileId = profileId;
        this.creatorId = creatorId;
        this.name = name;
        this.avatar = avatar;
        this.about = about;
        this.members = members;
        this.lastMessageTime = lastMessageTime;
        this.lastMessage = "";
        this.lastMessage_id = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String profileId) {
        this.creatorId = profileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessage_id() {
        return lastMessage_id;
    }

    public void setLastMessage_id(String lastMessage_id) {
        this.lastMessage_id = lastMessage_id;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }
}
