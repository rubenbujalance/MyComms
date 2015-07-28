package model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GlobalContactsSettings extends RealmObject {

    @PrimaryKey
    private String profileId;

    private String user; //This field can contain either username or email, depending on the user
    private String password;

    public GlobalContactsSettings() {}

    public GlobalContactsSettings(String profileId, String user,String password) {
        this.setProfileId(profileId);
        this.setUser(user);
        this.setPassword(password);
    }


    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
