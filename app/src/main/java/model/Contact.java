package model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by str_vig on 28/04/2015.
 */
public class Contact extends RealmObject{

    @PrimaryKey
    private String id;

    private String platform;
    private String firstName;
    private String lastName;
    private String avatar;
    private String pathAvatar;
    private String phones;
    private String emails;
    private String position;
    private String company;
    private String timezone;
    private int lastSeen;
    private String officeLocation;
    private String availability;
    private String presence;
    private String country;

    public Contact() {
    }

    public Contact(String initString)
    {
        this.id = initString;
        this.platform = initString;
        this.firstName = initString;
        this.lastName = initString;
        this.avatar = initString;
        this.setPathAvatar(pathAvatar);
        this.phones = initString;
        this.emails = initString;
        this.position = initString;
        this.company = initString;
        this.timezone = initString;
        this.lastSeen = 0;
        this.officeLocation = initString;
        this.availability = initString;
        this.presence = initString;
        this.country = initString;
    }

    public Contact(String id, String platform, String firstName, String lastName, String avatar, String pathAvatar, String phones, String emails, String position, String company, String timezone, int lastSeen, String officeLocation, String availability, String presence, String country) {
        this.id = id;
        this.platform = platform;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatar = avatar;
        this.setPathAvatar(pathAvatar);
        this.phones = phones;
        this.emails = emails;
        this.position = position;
        this.company = company;
        this.timezone = timezone;
        this.lastSeen = lastSeen;
        this.officeLocation = officeLocation;
        this.availability = availability;
        this.presence = presence;
        this.country = country;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public int getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(int lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }

    public String getPhones() {
        return phones;
    }

    public void setPhones(String phones) {
        this.phones = phones;
    }

    public String getEmails() {
        return emails;
    }

    public void setEmails(String emails) {
        this.emails = emails;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getPresence() {
        return presence;
    }

    public void setPresence(String presence) {
        this.presence = presence;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPathAvatar() {
        return pathAvatar;
    }

    public void setPathAvatar(String pathAvatar) {
        this.pathAvatar = pathAvatar;
    }

}
