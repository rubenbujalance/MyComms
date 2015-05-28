package com.vodafone.mycomms.util;

/**
 * Created by str_vig on 22/04/2015.
 */
public class Constants {
    public static final String TAG = "MyComms";

    //Chat Constants
    public static final int LEFT_CHAT = 0;
    public static final int RIGHT_CHAT = 1;

    //Profile Constants
    public static final int PREFERENCES = 0;
    public static final int ACCOUNTS = 1;
    public static final int MY_PROFILE = 1;

    //Footer Constants
    public static final int TOOLBAR_CONTACTS = 0;
    public static final int TOOLBAR_DASHBOARD = 1;
    public static final int TOOLBAR_RECENTS = 2;

    //Contacts Constants
    public static final int CONTACTS_FAVOURITE = 0;
    public static final int CONTACTS_RECENT = 1;
    public static final int CONTACTS_ALL = 2;
    public static final String CONTACTS_ACTION_CALL = "call";
    public static final String CONTACTS_ACTION_SMS = "sms";
    public static final String CONTACTS_ACTION_EMAIL = "email";

    //Contacts API and BD Fields
    public static final String CONTACT_DATA = "data";
    public static final String CONTACT_ID = "id";
    public static final String CONTACT_PLATFORM = "platform";
    public static final String CONTACT_FNAME = "firstName";
    public static final String CONTACT_LNAME = "lastName";
    public static final String CONTACT_AVATAR = "avatar";
    public static final String CONTACT_POSITION = "position";
    public static final String CONTACT_COMPANY = "company";
    public static final String CONTACT_TIMEZONE = "timeZone";
    public static final String CONTACT_LASTSEEN = "lastSeen";
    public static final String CONTACT_OFFICE_LOC = "officeLocation";
    public static final String CONTACT_PHONES = "phones";
    public static final String CONTACT_PHONE = "phone";
    public static final String CONTACT_EMAILS = "emails";
    public static final String CONTACT_EMAIL = "email";
    public static final String CONTACT_AVAILABILITY = "availability";
    public static final String CONTACT_PRESENCE = "presence";
    public static final String CONTACT_COUNTRY = "country";
    public static final String CONTACT_FAVOURITES = "favourites";
    public static final String CONTACT_RECENTS = "recents";
    public static final String CONTACT_RECENTS_ACTION = "action";
    public static final String CONTACT_RECENTS_ACTION_TIME = "timestamp";
    public static final String CONTACT_PAGINATION = "pagination";
    public static final String CONTACT_PAGINATION_MORE_PAGES = "morePages";
    public static final String CONTACT_PAGINATION_OFFSET = "offset";
    public static final String CONTACT_PAGINATION_PAGESIZE = "pageSize";

    //Contacts API Calls
    public static final String CONTACT_API_GET_CONTACTS = "/api/me/contact?p=mc";
    public static final String CONTACT_API_GET_FAVOURITES = "/api/me/favourites";
    public static final String CONTACT_API_GET_RECENTS = "/api/me/recents";
    public static final String CONTACT_API_POST_RECENTS = "/api/me/recents";
    public static final String CONTACT_API_POST_FAVOURITE = "/api/me/favourites";
    public static final String CONTACT_API_DEL_FAVOURITE = "/api/me/favourites/";

    //Contact Avatar BD Fields
    public static final String AVATAR_CONTACT_ID = "contactId";
    public static final String AVATAR_URL = "urlAvatar";
    public static final String AVATAR_PATH = "pathAvatar";
    public static final String CONTACT_AVATAR_DIR = "/avatars/files/";

    //Chat BD Fields
    public static final String CHAT_MESSAGE_ID = "id";
    public static final String CHAT_MESSAGE_PROFILE_ID = "profile_id";
    public static final String CHAT_MESSAGE_CONTACT_ID = "contact_id";
    public static final String CHAT_MESSAGE_TIMESTAMP = "timestamp";
    public static final String CHAT_MESSAGE_DIRECTION = "direction";
    public static final String CHAT_MESSAGE_TYPE = "type";
    public static final String CHAT_MESSAGE_TEXT = "text";
    public static final String CHAT_MESSAGE_RESOURCE_URI = "resourceUri";
    public static final String CHAT_MESSAGE_READ = "read";
    public static final String CHAT_MESSAGE_DELIVERED = "delivered";
    public static final String CHAT_ID = "id";
    public static final String CHAT_PROFILE_ID = "profile_id";
    public static final String CHAT_CONTACT_ID = "contact_id";
    public static final String CHAT_LAST_MESSAGE_ID = "lastMessage_id";

}
