package model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GlobalContactsSettings extends RealmObject {

    @PrimaryKey
    private String profileId;

    private String user; //This field can contain either username or email, depending on the user
    private String password;
    private String token;
    private String tokenType;
    private String url;

    public GlobalContactsSettings() {}

    public GlobalContactsSettings(String profileId, String user,String password, String token,
                                  String tokenType, String url) {
        this.setProfileId(profileId);
        this.setUser(user);
        this.setPassword(password);
        this.setToken(token);
        this.setTokenType(tokenType);
        this.setUrl(url);
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
