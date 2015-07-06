package model;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by str_vig on 28/04/2015.
 */
public class ChatMessage extends RealmObject{

    @PrimaryKey
    private String id; //Concatenation of: <profileid>_<contact_id>_<timestamp>

    @Index
    private String profile_id;
    @Index
    private String contact_id;

    private long timestamp;

    private String direction; //0-Sent; 1-Received. String just in case we need an index
    private int type; //Text-0, picture-1,..., map, video, file, etc.
    private String text;
    private String resourceUri;
    @Index
    private String read; //0-No; 1-Yes. It's string in case we need an index
    private String status; //not_sent; sent; delivered; read

    public ChatMessage() {}

    public ChatMessage(String profile_id, String contact_id, long timestamp, String direction,
                       int type, String text, String resourceUri, String read, String status)
    {
        this.setProfile_id(profile_id);
        this.setContact_id(contact_id);
        this.setTimestamp(timestamp);
        this.setDirection(direction);
        this.setType(type);
        this.setText(text);
        this.setResourceUri(resourceUri);
        this.setRead(read);
        this.setStatus(status);

        this.setId(profile_id + "_" + contact_id + "_" + timestamp);
    }

    public ChatMessage(String profile_id, String contact_id, long timestamp, String direction,
                       int type, String text, String resourceUri, String read, String status, String id)
    {
        this.setProfile_id(profile_id);
        this.setContact_id(contact_id);
        this.setTimestamp(timestamp);
        this.setDirection(direction);
        this.setType(type);
        this.setText(text);
        this.setResourceUri(resourceUri);
        this.setRead(read);
        this.setStatus(status);

        this.setId(id);
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getResourceUri() {
        return resourceUri;
    }

    public void setResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
    }

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
