package model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by str_vig on 28/04/2015.
 */
public class ContactAvatar extends RealmObject{

    @PrimaryKey
    private String contactId;

    private String url;
    private String path;

    public ContactAvatar() {
    }

    public ContactAvatar(String contactId, String url, String pathAvatar) {
        this.contactId = contactId;
        this.url = url;
        this.path = pathAvatar;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String id) {
        this.contactId = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
