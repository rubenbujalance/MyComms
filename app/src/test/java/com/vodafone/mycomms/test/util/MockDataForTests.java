package com.vodafone.mycomms.test.util;

import java.util.ArrayList;

import model.Contact;
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
        mockRecentContact.setPhones("[{\"phones\": [\n" +
                "{\n" +
                "\"country\": \"ES\",\n" +
                "\"phone\": \"+34659562976\"\n" +
                "}\n" +
                "]}]");
        mockRecentContact.setEmails("[{\"emails\": [\n" +
                "{\n" +
                "\"email\": \"vdf01@stratesys-ts.com\"\n" +
                "}\n" +
                "]}]");
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

        return recentList;
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

        //Contact 2
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

        //Contact 3
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
        mockContact.setProfileId("mc_5570340e7eb7c3512f2f9bf2");

        contactList.add(mockContact);
        mockContact = new Contact();

        //Contact 8
        mockContact.setContactId("mc_5535b2ac13be4b7975c51600");
        mockContact.setId("mc_5535b2ac13be4b7975c51600");
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
        mockUserProfile.setLastName("mockMyLastName");
        mockUserProfile.setId("mc_5570340e7eb7c3512f2f9bf2");

        return mockUserProfile;
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
}
