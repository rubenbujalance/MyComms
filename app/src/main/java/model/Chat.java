package model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Chat extends RealmObject{

    @PrimaryKey
    private String id;//Concatenation of: <profileid>_<contact_id>

    private String profile_id;
    private String contact_id;
    private String contactName;
    private String contactSurname;
    private String lastMessage_id;
    private String lastMessage;
    private long lastMessageTime;

    public Chat() {}

    public Chat(String profile_id, String contact_id, String contactName, String contactSurname)
    {
        this.setProfile_id(profile_id);
        this.setContact_id(contact_id);
        this.setLastMessage_id("");
        this.setContactName(contactName);
        this.setContactSurname(contactSurname);
        this.setLastMessage("");
        this.setLastMessageTime(0);

        this.setId(profile_id + "_" + contact_id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfile_id() {
        return profile_id;
    }

    public void setProfile_id(String profile_id) {
        this.profile_id = profile_id;
    }

    public String getContact_id() {
        return contact_id;
    }

    public void setContact_id(String contact_id) {
        this.contact_id = contact_id;
    }

    public String getLastMessage_id() {
        return lastMessage_id;
    }

    public void setLastMessage_id(String lastMessage_id) {
        this.lastMessage_id = lastMessage_id;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactSurname() {
        return contactSurname;
    }

    public void setContactSurname(String contactSurname) {
        this.contactSurname = contactSurname;
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
}
