package model;

/**
 * Created by str_vig on 28/04/2015.
 */
public class Contact {
    private String firstName;
    private String lastName;
    private String occupation;
    private String city;
    private String company;
    private boolean isDayTime;
    private String time;
    private String country;
    private String id;
    private boolean isFavourite;
    private boolean isRecent;


    public Contact (String id, String firstName, String lastName, String occupation, String city, String company, boolean isDayTime, String time, String country, boolean isFavourite, boolean isRecent){
        setId(id);
        setFirstName(firstName);
        setLastName(lastName);
        setOccupation(occupation);
        setCity(city);
        setCompany(company);
        setDayTime(isDayTime);
        setTime(time);
        setCountry(country);
        setFavourite(isFavourite);
        setRecent(isRecent);
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

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public boolean isDayTime() {
        return isDayTime;
    }

    public void setDayTime(boolean isDayTime) {
        this.isDayTime = isDayTime;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean isFavourite) {
        this.isFavourite = isFavourite;
    }

    public boolean isRecent() {
        return isRecent;
    }

    public void setRecent(boolean isRecent) {
        this.isRecent = isRecent;
    }
}
