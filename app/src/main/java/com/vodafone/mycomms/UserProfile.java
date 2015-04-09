package com.vodafone.mycomms;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by str_rbm on 07/04/2015.
 */

public class UserProfile implements Serializable{

    /*
    Fields
     */

    private String firstName;
    private String lastName;
    private String mail;
    private String password;
    private String countryISO;
    private String phone;
    private String officeLocation;
    private String companyName;
    private String position;

    private Bitmap  photoBitmap;
    private Date    photoDate;

    /*
    File name
     */

    public static final String fileName = "userprofile.json";

    /*
    Getters and Setters
     */

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

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCountryISO() {
        return countryISO;
    }

    public void setCountryISO(String countryISO) {
        this.countryISO = countryISO;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Bitmap getPhotoBitmap() {
        return photoBitmap;
    }

    public void setPhotoBitmap(Bitmap photoBitmap) {
        this.photoBitmap = photoBitmap;
    }

    public Date getPhotoDate() {
        return photoDate;
    }

    public void setPhotoDate(Date photoDate) {
        this.photoDate = photoDate;
    }

    /*
    Other methods
     */

    public boolean saveUserProfile(Context context)
    {
        Gson gson = new Gson();
        String json = gson.toJson(this);

        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(UserProfile.fileName, Context.MODE_PRIVATE);
            outputStream.write(json.getBytes());
            outputStream.flush();
            outputStream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static UserProfile readUserProfile(Context context)
    {
        Gson gson = new Gson();

        FileInputStream fis;
        String json;
        UserProfile userProfile = null;

        try {
            fis = context.openFileInput(UserProfile.fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            bufferedReader.close();
            inputStreamReader.close();

            json = sb.toString();

            userProfile = gson.fromJson(json, UserProfile.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return userProfile;
    }
}