package com.vodafone.mycomms;

import android.graphics.Bitmap;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

//import com.google.gson.Gson;

/**
 * Created by str_rbm on 07/04/2015.
 */

public final class UserProfile implements Serializable{

    /*
    Fields
     */

    private static String _firstName;
    private static String _lastName;
    private static String _mail;
    private static String _password;
    private static String _countryISO;
    private static String _phone;
    private static String _officeLocation;
    private static String _companyName;
    private static String _position;
    private static String _avatar;
    private static String _oauthPrefix;

    private static Bitmap _photoBitmap;
    private static String _photoPath;

    /*
    OAuth Token
     */
    private static String _oauth;

    /*
    File name
     */

    public static final String fileName = "userprofile.json";

    /*
    Getters and Setters
     */

    public static String getFirstName() {
        return _firstName;
    }

    public static void setFirstName(String firstName) {
        _firstName = firstName;
    }

    public static String getLastName() {
        return _lastName;
    }

    public static void setLastName(String lastName) {
        _lastName = lastName;
    }

    public static String getMail() {
        return _mail;
    }

    public static void setMail(String mail) {
        _mail = mail;
    }

    public static String getPassword() {
        return _password;
    }

    public static void setPassword(String password) {
        _password = password;
    }

    public static String getCountryISO() {
        return _countryISO;
    }

    public static void setCountryISO(String countryISO) {
        _countryISO = countryISO;
    }

    public static String getPhone() {
        return _phone;
    }

    public static void setPhone(String phone) {
        _phone = phone;
    }

    public static String getOfficeLocation() {
        return _officeLocation;
    }

    public static void setOfficeLocation(String officeLocation) {
        _officeLocation = officeLocation;
    }

    public static String getCompanyName() {
        return _companyName;
    }

    public static void setCompanyName(String companyName) {
        _companyName = companyName;
    }

    public static String getPosition() {
        return _position;
    }

    public static void setPosition(String position) {
        _position = position;
    }

    public static Bitmap getPhotoBitmap() {
        return _photoBitmap;
    }

    public static void setPhotoBitmap(Bitmap photoBitmap) {
        _photoBitmap = photoBitmap;
    }

    public static String getAvatar() {
        return _avatar;
    }

    public static void setAvatar(String avatar) {
        _avatar = avatar;
    }

    public static String getOauth() {
        return _oauth;
    }

    public static void setOauth(String oauth) {
        UserProfile._oauth = oauth;
    }

    public static String getOauthPrefix() {
        return _oauthPrefix;
    }

    public static void setOauthPrefix(String oauthPrefix) {
        _oauthPrefix = oauthPrefix;
    }

    public static String getPhotoPath() {
        return _photoPath;
    }

    public static void setPhotoPath(String photoPath) {
        UserProfile._photoPath = photoPath;
    }


    /*
    Other methods
     */

    private static boolean uploadPhoto()
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        _photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        JSONObject body = new JSONObject();
//        body.put("file",byteArray);
//
//        APIWrapper.httpPostAPI("/api/uploadFile", , new HashMap())

        return true;
    }

    public static HashMap<String,Object> getHashMap()
    {
        HashMap<String, Object> body = new HashMap<>();
        if(_firstName != null) body.put("firstName",_firstName);
        if(_lastName != null) body.put("lastName",_lastName);
        if(_countryISO != null) body.put("country",_countryISO);
        if(_phone != null) body.put("phone",_phone);
        if(_mail != null) body.put("email",_mail);
        if(_password != null) body.put("password",_password);
        if(_companyName != null) body.put("company",_companyName);
        if(_position != null) body.put("position",_position);
        if(_officeLocation != null) body.put("officeLocation",_officeLocation);
        if(_oauth != null) body.put(_oauthPrefix,_oauth);
        if(_avatar != null) body.put("avatar", _avatar);

        return body;
    }
}
