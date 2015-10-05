package com.vodafone.mycomms.test.util;

import android.annotation.SuppressLint;
import android.view.View;

import com.vodafone.mycomms.util.Constants;

import org.jivesoftware.smack.util.PacketParserUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.junit.Assert;
import org.robolectric.Robolectric;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;
import java.util.Collection;

import model.Chat;
import model.Contact;
import model.FavouriteContact;
import model.GroupChat;
import model.News;
import model.RecentContact;
import model.UserProfile;

/**
 * Created by str_oan on 03/09/2015.
 */
public class MockDataForTests
{
    public static ArrayList<News> getMockNewsArrayList()
    {
        News mockNews = new News();
        ArrayList<News> mockNewsArrayList = new ArrayList<>();
        mockNews.setAuthor_avatar("mockAvatar");
        mockNews.setAuthor_name("mockName");
        mockNews.setCreated_at(Long.parseLong("12345678"));
        mockNews.setHtml("mockHtml");
        mockNews.setImage("mockURL");
        mockNews.setLink("mockLink");
        mockNews.setTitle("mockTitle");
        mockNews.setUpdated_at(Long.parseLong("123456789"));
        mockNews.setUuid("mockUID");

        mockNewsArrayList.add(mockNews);
        mockNews = new News();
        mockNews.setAuthor_avatar("mockAvatar2");
        mockNews.setAuthor_name("mockName2");
        mockNews.setCreated_at(Long.parseLong("12345678"));
        mockNews.setHtml("mockHtml2");
        mockNews.setImage("mockURL2");
        mockNews.setLink("mockLink2");
        mockNews.setTitle("mockTitle2");
        mockNews.setUpdated_at(Long.parseLong("123456789"));
        mockNews.setUuid("mockUID2");

        mockNewsArrayList.add(mockNews);
        mockNews = new News();
        mockNews.setAuthor_avatar("mockAvatar3");
        mockNews.setAuthor_name("mockName3");
        mockNews.setCreated_at(Long.parseLong("12345678"));
        mockNews.setHtml("mockHtml3");
        mockNews.setImage("mockURL3");
        mockNews.setLink("mockLink3");
        mockNews.setTitle("mockTitle3");
        mockNews.setUpdated_at(Long.parseLong("123456789"));
        mockNews.setUuid("mockUID3");

        mockNewsArrayList.add(mockNews);

        return mockNewsArrayList;
    }

    public static ArrayList<RecentContact> getMockRecentContactsList()
    {
        ArrayList<RecentContact> recentList = new ArrayList<>();
        RecentContact mockRecentContact = new RecentContact();

        mockRecentContact.setContactId("mc_55409316799f7e1a109446f4");
        mockRecentContact.setId("mc_55409316799f7e1a109446f4");
        mockRecentContact.setAction("sms");
        mockRecentContact.setPlatform("mc");
        mockRecentContact.setFirstName("Albert");
        mockRecentContact.setLastName("Mialet");
        mockRecentContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockRecentContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockRecentContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockRecentContact.setCompany("Stratesys");
        mockRecentContact.setPosition("Senior Developer Consultant");
        mockRecentContact.setOfficeLocation("Barcelona");
        mockRecentContact.setTimezone("Europe/Madrid");
        mockRecentContact.setAvailability("DADFE1");
        mockRecentContact.setTimestamp(Long.parseLong("1440658545874"));

        recentList.add(mockRecentContact);
        mockRecentContact = new RecentContact();

        mockRecentContact.setContactId("mg_55dc2a35a297b90a726e4cc2");
        mockRecentContact.setId("mg_55dc2a35a297b90a726e4cc2");
        mockRecentContact.setUniqueId("unique_mg_55dc2a35a297b90a726e4cc2");
        mockRecentContact.setAction("sms");
        mockRecentContact.setTimestamp(Long.parseLong("1440571515241"));
        mockRecentContact.setAvailability("available");

        recentList.add(mockRecentContact);
        mockRecentContact = new RecentContact();

        mockRecentContact.setContactId("mc_55409316799f7e1a109446f4");
        mockRecentContact.setId("mc_55409316799f7e1a109446f4");
        mockRecentContact.setAction("call");
        mockRecentContact.setPlatform("mc");
        mockRecentContact.setFirstName("Albert");
        mockRecentContact.setLastName("Mialet");
        mockRecentContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockRecentContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockRecentContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockRecentContact.setCompany("Stratesys");
        mockRecentContact.setPosition("Senior Developer Consultant");
        mockRecentContact.setOfficeLocation("Barcelona");
        mockRecentContact.setTimezone("Europe/Madrid");
        mockRecentContact.setAvailability("DADFE1");
        mockRecentContact.setTimestamp(Long.parseLong("1440658545874"));

        recentList.add(mockRecentContact);
        mockRecentContact = new RecentContact();

        mockRecentContact.setContactId("mc_55409316799f7e1a109446f4");
        mockRecentContact.setId("mc_55409316799f7e1a109446f4");
        mockRecentContact.setAction("email");
        mockRecentContact.setPlatform("mc");
        mockRecentContact.setFirstName("Albert");
        mockRecentContact.setLastName("Mialet");
        mockRecentContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockRecentContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockRecentContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockRecentContact.setCompany("Stratesys");
        mockRecentContact.setPosition("Senior Developer Consultant");
        mockRecentContact.setOfficeLocation("Barcelona");
        mockRecentContact.setTimezone("Europe/Madrid");
        mockRecentContact.setAvailability("DADFE1");
        mockRecentContact.setTimestamp(Long.parseLong("1440658545874"));

        recentList.add(mockRecentContact);

        return recentList;
    }

    public static ArrayList<FavouriteContact> getMockFavouriteContactsList()
    {
        ArrayList<FavouriteContact> list = new ArrayList<>();
        FavouriteContact mockFavouriteContact = new FavouriteContact();

        mockFavouriteContact.setContactId("mc_55409316799f7e1a109446f4");
        mockFavouriteContact.setId("mc_55409316799f7e1a109446f4");
        mockFavouriteContact.setPlatform("mc");
        mockFavouriteContact.setFirstName("Albert");
        mockFavouriteContact.setLastName("Mialet");
        mockFavouriteContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockFavouriteContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockFavouriteContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockFavouriteContact.setPresence(
                "{\n" +
                        "\"icon\": \"mockIcon\"\n," +
                        "\"detail\": \"#LOCAL_TIME#\"\n" +
                        "}");
        mockFavouriteContact.setCompany("Stratesys");
        mockFavouriteContact.setPosition("Senior Developer Consultant");
        mockFavouriteContact.setOfficeLocation("Barcelona");
        mockFavouriteContact.setTimezone("Europe/Madrid");
        mockFavouriteContact.setAvailability("DADFE1");
        mockFavouriteContact.setCountry("Spain");

        list.add(mockFavouriteContact);
        mockFavouriteContact = new FavouriteContact();

        mockFavouriteContact.setContactId("mc_55409316799f7e1a109446f4");
        mockFavouriteContact.setId("mc_55409316799f7e1a109446f4");
        mockFavouriteContact.setPlatform("sf");
        mockFavouriteContact.setFirstName("Albert");
        mockFavouriteContact.setLastName("Mialet");
        mockFavouriteContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockFavouriteContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockFavouriteContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockFavouriteContact.setCompany("Stratesys");
        mockFavouriteContact.setPresence(
                "{\n" +
                        "\"icon\": \"mockIcon\"\n," +
                        "\"detail\": \"#LOCAL_TIME#\"\n" +
                        "}");
        mockFavouriteContact.setPosition("Senior Developer Consultant");
        mockFavouriteContact.setOfficeLocation("Barcelona");
        mockFavouriteContact.setTimezone("Europe/Madrid");
        mockFavouriteContact.setAvailability("DADFE1");
        mockFavouriteContact.setCountry("Spain");

        list.add(mockFavouriteContact);
        mockFavouriteContact = new FavouriteContact();

        mockFavouriteContact.setContactId("mc_55409316799f7e1a109446f4");
        mockFavouriteContact.setId("mc_55409316799f7e1a109446f4");
        mockFavouriteContact.setPlatform("local");
        mockFavouriteContact.setFirstName("Albert");
        mockFavouriteContact.setLastName("Mialet");
        mockFavouriteContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockFavouriteContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockFavouriteContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockFavouriteContact.setCompany("Stratesys");
        mockFavouriteContact.setPresence(
                "{\n" +
                        "\"icon\": \"mockIcon\"\n," +
                        "\"detail\": \"#LOCAL_TIME#\"\n" +
                        "}");
        mockFavouriteContact.setPosition("Senior Developer Consultant");
        mockFavouriteContact.setOfficeLocation("Barcelona");
        mockFavouriteContact.setTimezone("Europe/Madrid");
        mockFavouriteContact.setAvailability("DADFE1");
        mockFavouriteContact.setCountry("Spain");

        list.add(mockFavouriteContact);

        return list;
    }

    public static ArrayList<RecentContact> getMockRecentContactsList_OnlyOneGroupChat()
    {
        ArrayList<RecentContact> recentList = new ArrayList<>();
        RecentContact mockRecentContact = new RecentContact();

        mockRecentContact.setContactId("mg_55dc2a35a297b90a726e4cc2");
        mockRecentContact.setId("mg_55dc2a35a297b90a726e4cc2");
        mockRecentContact.setUniqueId("unique_mg_55dc2a35a297b90a726e4cc2");
        mockRecentContact.setAction("sms");
        mockRecentContact.setTimestamp(Long.parseLong("1440571515241"));
        mockRecentContact.setAvailability("available");

        recentList.add(mockRecentContact);

        return recentList;
    }

    public static ArrayList<RecentContact> getMockRecentContactsList_OneRecent_ActionCall()
    {
        ArrayList<RecentContact> recentList = new ArrayList<>();
        RecentContact mockRecentContact = new RecentContact();

        mockRecentContact.setContactId("mc_55409316799f7e1a109446f4");
        mockRecentContact.setId("mc_55409316799f7e1a109446f4");
        mockRecentContact.setAction("call");
        mockRecentContact.setPlatform("mc");
        mockRecentContact.setFirstName("Albert");
        mockRecentContact.setLastName("Mialet");
        mockRecentContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockRecentContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockRecentContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockRecentContact.setCompany("Stratesys");
        mockRecentContact.setPosition("Senior Developer Consultant");
        mockRecentContact.setOfficeLocation("Barcelona");
        mockRecentContact.setTimezone("Europe/Madrid");
        mockRecentContact.setAvailability("DADFE1");
        mockRecentContact.setTimestamp(Long.parseLong("1440658545874"));

        recentList.add(mockRecentContact);

        return recentList;
    }

    public static ArrayList<RecentContact> getMockRecentContactsList_OneRecent_ActionSMS_MyComms()
    {
        ArrayList<RecentContact> recentList = new ArrayList<>();
        RecentContact mockRecentContact = new RecentContact();

        mockRecentContact.setContactId("mc_55409316799f7e1a109446f4");
        mockRecentContact.setId("mc_55409316799f7e1a109446f4");
        mockRecentContact.setAction("sms");
        mockRecentContact.setPlatform("mc");
        mockRecentContact.setFirstName("Albert");
        mockRecentContact.setLastName("Mialet");
        mockRecentContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockRecentContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockRecentContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockRecentContact.setCompany("Stratesys");
        mockRecentContact.setPosition("Senior Developer Consultant");
        mockRecentContact.setOfficeLocation("Barcelona");
        mockRecentContact.setTimezone("Europe/Madrid");
        mockRecentContact.setAvailability("DADFE1");
        mockRecentContact.setTimestamp(Long.parseLong("1440658545874"));

        recentList.add(mockRecentContact);

        return recentList;
    }

    public static ArrayList<RecentContact> getMockRecentContactsList_OneRecent_ActionSMS_Global()
    {
        ArrayList<RecentContact> recentList = new ArrayList<>();
        RecentContact mockRecentContact = new RecentContact();

        mockRecentContact.setContactId("mc_55409316799f7e1a109446f4");
        mockRecentContact.setId("mc_55409316799f7e1a109446f4");
        mockRecentContact.setAction("sms");
        mockRecentContact.setPlatform("ly");
        mockRecentContact.setFirstName("Albert");
        mockRecentContact.setLastName("Mialet");
        mockRecentContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockRecentContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockRecentContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockRecentContact.setCompany("Stratesys");
        mockRecentContact.setPosition("Senior Developer Consultant");
        mockRecentContact.setOfficeLocation("Barcelona");
        mockRecentContact.setTimezone("Europe/Madrid");
        mockRecentContact.setAvailability("DADFE1");
        mockRecentContact.setTimestamp(Long.parseLong("1440658545874"));

        recentList.add(mockRecentContact);

        return recentList;
    }

    public static ArrayList<RecentContact> getMockRecentContactsList_OneRecent_ActionEMAIL()
    {
        ArrayList<RecentContact> recentList = new ArrayList<>();
        RecentContact mockRecentContact = new RecentContact();

        mockRecentContact.setContactId("mc_55409316799f7e1a109446f4");
        mockRecentContact.setId("mc_55409316799f7e1a109446f4");
        mockRecentContact.setAction("email");
        mockRecentContact.setPlatform("mc");
        mockRecentContact.setFirstName("Albert");
        mockRecentContact.setLastName("Mialet");
        mockRecentContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockRecentContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockRecentContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockRecentContact.setCompany("Stratesys");
        mockRecentContact.setPosition("Senior Developer Consultant");
        mockRecentContact.setOfficeLocation("Barcelona");
        mockRecentContact.setTimezone("Europe/Madrid");
        mockRecentContact.setAvailability("DADFE1");
        mockRecentContact.setTimestamp(Long.parseLong("1440658545874"));

        recentList.add(mockRecentContact);

        return recentList;
    }

    public static ArrayList<RecentContact> getMockRecentContactsList_WithWrongData()
    {
        ArrayList<RecentContact> recentList = new ArrayList<>();
        RecentContact mockRecentContact = new RecentContact();

        mockRecentContact.setAction("sms");
        mockRecentContact.setPlatform("mc");
        mockRecentContact.setFirstName("Albert");
        mockRecentContact.setLastName("Mialet");
        mockRecentContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockRecentContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockRecentContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockRecentContact.setCompany("Stratesys");
        mockRecentContact.setPosition("Senior Developer Consultant");
        mockRecentContact.setOfficeLocation("Barcelona");
        mockRecentContact.setTimezone("Europe/Madrid");
        mockRecentContact.setAvailability("DADFE1");
        mockRecentContact.setTimestamp(Long.parseLong("1440658545874"));

        recentList.add(mockRecentContact);
        mockRecentContact = new RecentContact();

        mockRecentContact.setAction("sms");
        mockRecentContact.setTimestamp(Long.parseLong("1440571515241"));
        mockRecentContact.setAvailability("available");

        recentList.add(mockRecentContact);

        return recentList;
    }

    public static ArrayList<Contact> getMockContactsList()
    {
        //Recent contact as contact
        ArrayList<Contact> contactList = new ArrayList<>();
        Contact mockContact = new Contact();

        mockContact.setContactId("mc_55409316799f7e1a109446f4");
        mockContact.setId("mc_55409316799f7e1a109446f4");
        mockContact.setPlatform("mc");
        mockContact.setFirstName("Albert");
        mockContact.setLastName("Mialet");
        mockContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockContact.setPresence
                ("{\n" +
                "\"icon\": \"mockIcon\"\n," +
                "\"detail\": \"#LOCAL_TIME#\"\n" +
                "}");
        mockContact.setCompany("Stratesys");
        mockContact.setPosition("Senior Developer Consultant");
        mockContact.setOfficeLocation("Barcelona");
        mockContact.setTimezone("Europe/Madrid");
        mockContact.setAvailability("DADFE1");
        mockContact.setProfileId("mc_5570340e7eb7c3512f2f9bf2");

        contactList.add(mockContact);
        mockContact = new Contact();


        //All the contacts for group chat


        //Contact 1
        mockContact.setContactId("mc_55d28046bfcac49d64d9d6af");
        mockContact.setId("mc_55d28046bfcac49d64d9d6af");
        mockContact.setPlatform("sf");
        mockContact.setFirstName("Albert");
        mockContact.setLastName("Mialet");
        mockContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@vodafone.com\"\n" +
                "}\n" +
                "]");
        mockContact.setCompany("Stratesys");
        mockContact.setPosition("Senior Developer Consultant");
        mockContact.setOfficeLocation("Barcelona");
        mockContact.setTimezone("Europe/Madrid");
        mockContact.setAvailability("DADFE1");
        mockContact.setProfileId("mc_5570340e7eb7c3512f2f9bf2");

        contactList.add(mockContact);
        mockContact = new Contact();

        //Contact 2
        mockContact.setContactId("mc_554b20fc80eb511a3c1d1262");
        mockContact.setId("mc_554b20fc80eb511a3c1d1262");
        mockContact.setPlatform("ly");
        mockContact.setFirstName("Albert");
        mockContact.setLastName("Mialet");
        mockContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockContact.setCompany("Stratesys");
        mockContact.setPosition("Senior Developer Consultant");
        mockContact.setOfficeLocation("Barcelona");
        mockContact.setTimezone("Europe/Madrid");
        mockContact.setAvailability("DADFE1");
        mockContact.setProfileId("mc_5570340e7eb7c3512f2f9bf2");

        contactList.add(mockContact);
        mockContact = new Contact();

        //Contact 3
        mockContact.setContactId("mc_55361a9cc729d4430b9722f3");
        mockContact.setId("mc_55361a9cc729d4430b9722f3");
        mockContact.setPlatform("local");
        mockContact.setFirstName("Albert");
        mockContact.setLastName("Mialet");
        mockContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockContact.setPresence(
                "{\n" +
                "\"detail\": \"#LOCAL_TIME#\"\n" +
                "}");
        mockContact.setCompany("Stratesys");
        mockContact.setPosition("Senior Developer Consultant");
        mockContact.setOfficeLocation("Barcelona");
        mockContact.setTimezone("Europe/Madrid");
        mockContact.setAvailability("DADFE1");
        mockContact.setProfileId("mc_5570340e7eb7c3512f2f9bf2");

        contactList.add(mockContact);
        mockContact = new Contact();

        //Contact 4
        mockContact.setContactId("mc_55409316799f7e1a109446f4");
        mockContact.setId("mc_55409316799f7e1a109446f4");
        mockContact.setPlatform("mc");
        mockContact.setFirstName("Albert");
        mockContact.setLastName("Mialet");
        mockContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockContact.setCompany("Stratesys");
        mockContact.setPosition("Senior Developer Consultant");
        mockContact.setOfficeLocation("Barcelona");
        mockContact.setTimezone("Europe/Madrid");
        mockContact.setAvailability("DADFE1");
        mockContact.setProfileId("mc_5570340e7eb7c3512f2f9bf2");

        contactList.add(mockContact);
        mockContact = new Contact();

        //Contact 5
        mockContact.setContactId("mc_55d28046bfcac49d64d9d6af");
        mockContact.setId("mc_55d28046bfcac49d64d9d6af");
        mockContact.setPlatform("mc");
        mockContact.setFirstName("Albert");
        mockContact.setLastName("Mialet");
        mockContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockContact.setCompany("Stratesys");
        mockContact.setPosition("Senior Developer Consultant");
        mockContact.setOfficeLocation("Barcelona");
        mockContact.setTimezone("Europe/Madrid");
        mockContact.setAvailability("DADFE1");
        mockContact.setProfileId("mc_5570340e7eb7c3512f2f9bf2");

        contactList.add(mockContact);
        mockContact = new Contact();

        //Contact 6
        mockContact.setContactId("mc_554b20fc80eb511a3c1d1262");
        mockContact.setId("mc_554b20fc80eb511a3c1d1262");
        mockContact.setPlatform("mc");
        mockContact.setFirstName("Albert");
        mockContact.setLastName("Mialet");
        mockContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockContact.setCompany("Stratesys");
        mockContact.setPosition("Senior Developer Consultant");
        mockContact.setOfficeLocation("Barcelona");
        mockContact.setTimezone("Europe/Madrid");
        mockContact.setAvailability("DADFE1");
        mockContact.setProfileId("mc_5570340e7eb7c3512f2f9bf2");

        contactList.add(mockContact);
        mockContact = new Contact();

        //Contact 7
        mockContact.setContactId("mc_55361a9cc729d4430b9722f3");
        mockContact.setId("mc_55361a9cc729d4430b9722f3");
        mockContact.setPlatform("mc");
        mockContact.setFirstName("Albert");
        mockContact.setLastName("Mialet");
        mockContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockContact.setCompany("Stratesys");
        mockContact.setPosition("Senior Developer Consultant");
        mockContact.setOfficeLocation("Barcelona");
        mockContact.setTimezone("Europe/Madrid");
        mockContact.setAvailability("DADFE1");
        mockContact.setCountry("Spain");
        mockContact.setPresence(
                "{\n" +
                        "\"icon\": \"mockIcon\"\n" +
                "}");
        mockContact.setProfileId("mc_5570340e7eb7c3512f2f9bf2");

        contactList.add(mockContact);
        mockContact = new Contact();

        //Contact 8
        mockContact.setContactId("mc_5535b2ac13be4b7975c51600");
        mockContact.setId("mc_5535b2ac13be4b7975c51600");
        mockContact.setPlatform("mc");
        mockContact.setFirstName("No results found");
        mockContact.setLastName("Mialet");
        mockContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockContact.setCompany("Stratesys");
        mockContact.setPosition("Senior Developer Consultant");
        mockContact.setOfficeLocation("Barcelona");
        mockContact.setTimezone("Europe/Madrid");
        mockContact.setAvailability("DADFE1");
        mockContact.setProfileId("mc_5570340e7eb7c3512f2f9bf2");

        contactList.add(mockContact);

        return contactList;
    }

    public static Contact getMockContactById(String id)
    {
        ArrayList<Contact> contactList = getMockContactsList();
        for(Contact c : contactList)
        {
            if(c.getContactId().equals(id))
                return c;
        }
        return null;
    }

    public static UserProfile getMockUserProfile()
    {
        UserProfile mockUserProfile = new UserProfile();
        mockUserProfile.setAvailability("DADFE1");
        mockUserProfile.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockUserProfile.setCompany("Stratesys");
        mockUserProfile.setPosition("Senior Developer Consultant");
        mockUserProfile.setOfficeLocation("Barcelona");
        mockUserProfile.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockUserProfile.setFirstName("mockMyName");
        mockUserProfile.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockUserProfile.setLastName("mockMyLastName");
        mockUserProfile.setId("mc_5570340e7eb7c3512f2f9bf2");
        mockUserProfile.setPlatform("mc");
        mockUserProfile.setLastSeen(123456);
        mockUserProfile.setPresence("mockPresence");
        mockUserProfile.setCountry("ESP");

        return mockUserProfile;
    }

    public static RecentContact getMockRecentContact()
    {
        RecentContact mockRecentContact = new RecentContact();
        mockRecentContact.setAvailability("DADFE1");
        mockRecentContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockRecentContact.setCompany("Stratesys");
        mockRecentContact.setPosition("Senior Developer Consultant");
        mockRecentContact.setOfficeLocation("Barcelona");
        mockRecentContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockRecentContact.setFirstName("mockMyName");
        mockRecentContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockRecentContact.setLastName("mockMyLastName");
        mockRecentContact.setId("mc_5570340e7eb7c3512f2f9bf2");
        mockRecentContact.setPlatform("mc");
        mockRecentContact.setLastSeen(123456);
        mockRecentContact.setPresence("mockPresence");
        mockRecentContact.setCountry("ESP");

        return mockRecentContact;
    }

    public static Contact getMockContact()
    {
        Contact mockContact = new Contact();
        mockContact.setAvailability("DADFE1");
        mockContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockContact.setCompany("Stratesys");
        mockContact.setPosition("Senior Developer Consultant");
        mockContact.setOfficeLocation("Barcelona");
        mockContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockContact.setFirstName("mockMyName");
        mockContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockContact.setLastName("mockMyLastName");
        mockContact.setId("mc_5570340e7eb7c3512f2f9bf2");
        mockContact.setPlatform("mc");
        mockContact.setLastSeen(123456);
        mockContact.setPresence("{\n" +
                "\"icon\": \"mockIcon\"\n," +
                "\"detail\": \"#LOCAL_TIME#\"\n" +
                "}");
        mockContact.setCountry("ESP");
        mockContact.setTimezone("mockTimeZone");
        mockContact.setPosition("mockPosition");
        mockContact.setContactId("mockContactId");
        mockContact.setStringField1("mockFiled");

        return mockContact;
    }

    public static JSONObject getContactJSONObject()
    {
        Contact contact = getMockContact();
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(Constants.CONTACT_ID, contact.getContactId());
            jsonObject.put(Constants.CONTACT_PLATFORM, contact.getPlatform());
            jsonObject.put(Constants.CONTACT_FNAME, contact.getFirstName());
            jsonObject.put(Constants.CONTACT_LNAME, contact.getLastName());
            jsonObject.put(Constants.CONTACT_AVATAR, contact.getAvatar());
            jsonObject.put(Constants.CONTACT_COMPANY, contact.getCompany());
            jsonObject.put(Constants.CONTACT_POSITION, contact.getPosition());
            jsonObject.put(Constants.CONTACT_TIMEZONE, contact.getTimezone());
            jsonObject.put(Constants.CONTACT_LASTSEEN, contact.getLastSeen());
            jsonObject.put(Constants.CONTACT_OFFICE_LOC, contact.getOfficeLocation());
            jsonObject.put(Constants.CONTACT_PHONES, new JSONArray(contact.getPhones()));
            jsonObject.put(Constants.CONTACT_EMAILS, new JSONArray(contact.getEmails()));
            jsonObject.put(Constants.CONTACT_AVAILABILITY, contact.getAvailability());
            jsonObject.put(Constants.CONTACT_PRESENCE, contact.getPresence());
            jsonObject.put(Constants.CONTACT_COUNTRY, contact.getCountry());
        }
        catch (Exception e)
        {
            System.err.println("******** getContactJSONObject JSONObject creation failed ********\n"+e.getMessage());
        }

        return jsonObject;
    }

    public static JSONObject getContactJSONObjectWithDataTagAsJSONArray()
    {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray.put(getContactJSONObject());
            jsonObject.put(Constants.CONTACT_DATA, jsonArray);
        }
        catch (Exception e)
        {
            System.err.println("******** getContactJSONObjectWithDataTag JSONObject creation failed ********\n"+e.getMessage());
        }

        return jsonObject;
    }

    public static JSONObject getContactDATAFromJSONObjectWithJSONArray()
    {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonArray.put(0, getContactJSONObject());
            jsonObject.put(Constants.CONTACT_DATA, jsonArray);
        }
        catch (Exception e)
        {
            System.err.println("******** getContactJSONObjecctWithJSONArray JSONObject creation failed ********\n"+e.getMessage());
        }
        return jsonObject;
    }

    public static JSONObject getContactFavoriteDATAFromJSONObjectWithJSONArray()
    {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonArray.put(0, getContactJSONObject());
            jsonObject.put(Constants.CONTACT_FAVOURITES, jsonArray);
        }
        catch (Exception e)
        {
            System.err.println("******** getContactJSONObjecctWithJSONArray JSONObject creation failed ********\n"+e.getMessage());
        }
        return jsonObject;
    }

    public static FavouriteContact getMockFavoriteContact()
    {
        FavouriteContact mockContact = new FavouriteContact();
        mockContact.setAvailability("DADFE1");
        mockContact.setAvatar("https://mycomms-avatars.s3-us-west-2.amazonaws.com/55409316799f7e1a109446f4_1437066718958d");
        mockContact.setCompany("Stratesys");
        mockContact.setPosition("Senior Developer Consultant");
        mockContact.setOfficeLocation("Barcelona");
        mockContact.setEmails("[\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]");
        mockContact.setFirstName("mockMyName");
        mockContact.setPhones("[\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]");
        mockContact.setLastName("mockMyLastName");
        mockContact.setId("mc_5570340e7eb7c3512f2f9bf2");
        mockContact.setPlatform("mc");
        mockContact.setLastSeen(123456);
        mockContact.setPresence("mockPresence");
        mockContact.setCountry("ESP");

        return mockContact;
    }

    public static GroupChat getMockGroupChat()
    {
        GroupChat mockGroupChat = new GroupChat();
        mockGroupChat.setId("mg_55dc2a35a297b90a726e4cc2");
        mockGroupChat.setAvatar("mockAvatar");
        mockGroupChat.setAbout("mockAbout");
        mockGroupChat.setCreatorId("mc_55409316799f7e1a109446f4");
        mockGroupChat.setLastMessage("mockLastMessage");
        mockGroupChat.setLastMessage_id("mockLastMessageId");
        mockGroupChat.setMembers("mc_5570340e7eb7c3512f2f9bf2@mc_5535b2ac13be4b7975c51600@mc_55409316799f7e1a109446f4");
        mockGroupChat.setLastMessageTime(Long.parseLong("123456789"));
        mockGroupChat.setName("mockName");
        mockGroupChat.setOwners("mc_5570340e7eb7c3512f2f9bf2@mc_5535b2ac13be4b7975c51600@mc_55409316799f7e1a109446f4");
        mockGroupChat.setProfileId("mc_55409316799f7e1a109446f4");
        return mockGroupChat;
    }

    public static GroupChat getMockGroupChat_WithWrongData()
    {
        GroupChat mockGroupChat = new GroupChat();
        mockGroupChat.setId("mg_55dc2a35a297b90a726e4cc2");
        mockGroupChat.setAvatar("mockAvatar");
        mockGroupChat.setAbout("mockAbout");
        mockGroupChat.setCreatorId("mc_55409316799f7e1a109446f4");
        mockGroupChat.setLastMessage("mockLastMessage");
        mockGroupChat.setLastMessage_id("mockLastMessageId");
        mockGroupChat.setLastMessageTime(Long.parseLong("123456789"));
        mockGroupChat.setName("mockName");
        mockGroupChat.setProfileId("mc_55409316799f7e1a109446f4");
        return mockGroupChat;
    }

    public static ArrayList<Chat> getEmptyChatList()
    {
        return new ArrayList<>();
    }

    public static ArrayList<GroupChat> getEmptyGroupChatList()
    {
        return new ArrayList<>();
    }

    public static ArrayList<Chat> getMockChatList()
    {
        ArrayList<Chat> chatList = new ArrayList<>();

        Chat chat = new Chat(
                "mc_55409316799f7e1a109446f4", //Profile_id
                "mc_5570340e7eb7c3512f2f9bf2", //Contact_id
                "1111_1111_1111_1111", //LastMessage_id
                "chat_message_1", //LastMessage
                1443437630 //LastMessage_Timestamp (2015/09/28 12:54)
                );
        chatList.add(chat);

        chat = new Chat(
                "mc_55409316799f7e1a109446f4", //Profile_id
                "mc_5535b2ac13be4b7975c51600", //Contact_id
                "1111_2222_2222_2222", //LastMessage_id
                "chat_message_2", //LastMessage
                1443437640 //LastMessage_Timestamp (2015/09/28 12:54)
                );
        chatList.add(chat);

        chat = new Chat(
                "mc_55409316799f7e1a109446f4", //Profile_id
                "mc_55509316799f7e1a109446f4", //Contact_id
                "1111_3333_3333_3333", //LastMessage_id
                "chat_message_3", //LastMessage
                1443437650 //LastMessage_Timestamp (2015/09/28 12:54)
                );
        chatList.add(chat);

        chat = new Chat(
                "mc_55409316799f7e1a109446f4", //Profile_id
                "mc_5565b2ac13be4b7975c51600", //Contact_id
                "1111_4444_4444_4444", //LastMessage_id
                "chat_message_4", //LastMessage
                1443437660 //LastMessage_Timestamp (2015/09/28 12:54)
                );
        chatList.add(chat);

        return chatList;
    }

    public static ArrayList<GroupChat> getMockGroupChatList()
    {
        ArrayList<GroupChat> groupChatList = new ArrayList<>();

        GroupChat groupChat = new GroupChat(
                "mg_55dc2a35a297b90a726e4cc1", //Group Id
                "mc_55409316799f7e1a109446f4", //Profile_id
                "mc_5570340e7eb7c3512f2f9bf2", //Creator_id
                "TestGroupChat1", //Name
                "testAvatar1", //Avatar
                "testAbout1", //About
                "mc_5570340e7eb7c3512f2f9bf2@mc_5535b2ac13be4b7975c51600@mc_55409316799f7e1a109446f4", //Members
                "mc_5570340e7eb7c3512f2f9bf2@mc_5535b2ac13be4b7975c51600@mc_55409316799f7e1a109446f4", //Owners
                1443437625, //LastMessage_Timestamp (2015/09/28 12:54)
                "groupchat_message_1", //LastMessage
                "2222_1111_1111_1111" //LastMessage_id
                );
        groupChatList.add(groupChat);

        groupChat = new GroupChat(
                "mg_55dc2a35a297b90a726e4cc2", //Group Id
                "mc_55409316799f7e1a109446f4", //Profile_id
                "mc_5535b2ac13be4b7975c51600", //Creator_id
                "TestGroupChat2", //Name
                "testAvatar2", //Avatar
                "testAbout2", //About
                "mc_5570340e7eb7c3512f2f9bf2@mc_5535b2ac13be4b7975c51600@mc_55409316799f7e1a109446f4", //Members
                "mc_5570340e7eb7c3512f2f9bf2@mc_5535b2ac13be4b7975c51600@mc_55409316799f7e1a109446f4", //Owners
                1443437635, //LastMessage_Timestamp (2015/09/28 12:54)
                "groupchat_message_2", //LastMessage
                "2222_2222_2222_2222" //LastMessage_id
        );
        groupChatList.add(groupChat);

        groupChat = new GroupChat(
                "mg_55dc2a35a297b90a726e4cc3", //Group Id
                "mc_55409316799f7e1a109446f4", //Profile_id
                "mc_55409316799f7e1a109446f4", //Creator_id
                "TestGroupChat3", //Name
                "testAvatar3", //Avatar
                "testAbout3", //About
                "mc_5570340e7eb7c3512f2f9bf2@mc_5535b2ac13be4b7975c51600@mc_55409316799f7e1a109446f4", //Members
                "mc_5570340e7eb7c3512f2f9bf2@mc_5535b2ac13be4b7975c51600@mc_55409316799f7e1a109446f4", //Owners
                1443437645, //LastMessage_Timestamp (2015/09/28 12:54)
                "groupchat_message_3", //LastMessage
                "2222_3333_3333_3333" //LastMessage_id
        );
        groupChatList.add(groupChat);

        groupChat = new GroupChat(
                "mg_55dc2a35a297b90a726e4cc4", //Group Id
                "mc_55409316799f7e1a109446f4", //Profile_id
                "mc_5570340e7eb7c3512f2f9bf2", //Creator_id
                "TestGroupChat4", //Name
                "testAvatar4", //Avatar
                "testAbout4", //About
                "mc_5570340e7eb7c3512f2f9bf2@mc_5535b2ac13be4b7975c51600@mc_55409316799f7e1a109446f4", //Members
                "mc_5570340e7eb7c3512f2f9bf2@mc_5535b2ac13be4b7975c51600@mc_55409316799f7e1a109446f4", //Owners
                1443437655, //LastMessage_Timestamp (2015/09/28 12:54)
                "groupchat_message_4", //LastMessage
                "2222_4444_4444_4444" //LastMessage_id
        );
        groupChatList.add(groupChat);

        return groupChatList;
    }

    public static void checkThreadSchedulers()
    {
        checkThreadSchedulers(1000);
    }

    public static void checkThreadSchedulers(long time)
    {
        try {
            Thread.sleep(time);
        }
        catch (Exception e)
        {
            Assert.fail();
        }

        boolean taskDetected = false;
        if(Robolectric.getBackgroundThreadScheduler().areAnyRunnable())
        {
            taskDetected = true;
            Robolectric.flushBackgroundThreadScheduler();
            while(Robolectric.getBackgroundThreadScheduler().areAnyRunnable())
            {
                Robolectric.flushBackgroundThreadScheduler();
            }
        }
        if(Robolectric.getForegroundThreadScheduler().areAnyRunnable())
        {
            taskDetected = true;
            Robolectric.flushForegroundThreadScheduler();
            while (Robolectric.getForegroundThreadScheduler().areAnyRunnable())
            {
                Robolectric.flushForegroundThreadScheduler();
            }
        }
        if(taskDetected)
            checkThreadSchedulers();
    }

    //Should be used with this params:
    // className: this.getClass().getSimpleName()
    // methodName: Thread.currentThread().getStackTrace()[1].getMethodName())
    public static void printStartTest(String className, String methodName)
    {
        System.out.println("Starting Test "+methodName+" from class "+className+".......");
    }

    //Should be used with this params:
    // className: this.getClass().getSimpleName()
    // methodName: Thread.currentThread().getStackTrace()[1].getMethodName())
    public static void printEndTest(String className, String methodName)
    {
        System.out.println("Test "+methodName+" from class "+className+" successfully finished!");
    }

    //CHAT
    public static XmlPullParser getMockChatMessageStanza() {
        String messageStanza = "<message type=\"chat\" id=\"A0858E94412145D9A82252FBA5E4A808\" to=\"mc_55409316799f7e1a109446f4@my-comms.com\" from=\"mc_5565b2ac13be4b7975c51600@my-comms.com/867949020576518\" mediaType=\"text\" sent=\"1443697338314\" status=\"sent\" receiver=\"mc_55409316799f7e1a109446f4@my-comms.com\"><body>Test</body></message>";
        return getParserFor(messageStanza, "message");
    }

    public static XmlPullParser getMockGroupChatMessageStanza() {
        String messageStanza = "<message type=\"groupchat\" to=\"mg_55dc2a35a297b90a726e4cc4@my-comms.com\" id=\"43A33E61-028C-41C0-BE39-6C7DEF7AD864\" mediaType=\"text\" from=\"mc_5570340e7eb7c3512f2f9bf2@my-comms.com/fb7fdb18e0fa3a0ae75651ce2691028f2ef9f81b53fa4799a78e70c228d4632a\" sent=\"1443708968430\" status=\"sent\" receiver=\"mc_55409316799f7e1a109446f4@my-comms.com\"><body>TestGroup</body></message>";
        return getParserFor(messageStanza, "message");
    }

    public static XmlPullParser getMockPong(String id) {
        String messageStanza = "<iq id=\""+id+"\" from=\"my-comms.com\" to=\"mc_55409316799f7e1a109446f4@my-comms.com/355847069754161\" type=\"result\"/>";
        return getParserFor(messageStanza, "iq");
    }

    public static XmlPullParser getMockPendingMessagesIQ() {
        String messageStanza = "<iq to=\"mc_55409316799f7e1a109446f4@my-comms.com/355847069754161\" type=\"pendingMessages\" from=\"host\" pending=\"9\"/>";
        return getParserFor(messageStanza, "iq");
    }

    private static XmlPullParser getParserFor(String stanza, String startTag) {
        XmlPullParser xmlPullParser = null;

        try {
            xmlPullParser = PacketParserUtils.getParserFor(stanza, startTag);
        } catch (Exception e) {}

        return xmlPullParser;
    }

    private static XmlPullParser getParserFor(String stanza) {
        XmlPullParser xmlPullParser = null;

        try {
            xmlPullParser = PacketParserUtils.getParserFor(stanza);
        } catch (Exception e) {}

        return xmlPullParser;
    }


}
