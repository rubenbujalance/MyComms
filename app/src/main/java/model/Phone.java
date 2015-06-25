package model;

public class Phone {
    private String phone;
    private String country;

    public Phone(String phone, String country) {
        this.phone = phone;
        this.country = country;
    }

    public Phone() {
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
