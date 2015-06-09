package com.vodafone.mycomms.util;

/**
 * Created by str_vig on 22/04/2015.
 */
public class Constants {
    public static final String TAG = "MyComms";

    //Shared preferences
    public static final String MYCOMMS_SHARED_PREFS = "mycomms";

    //Chat Constants
    public static final int LEFT_CHAT = 0;
    public static final int RIGHT_CHAT = 1;

    //Profile Constants
    public static final int PREFERENCES = 0;
    public static final int ACCOUNTS = 1;
    public static final int MY_PROFILE = 1;
    public static final String PROFILE_ID_SHARED_PREF = "mycomms_profile_id";
    public static final String ACCESS_TOKEN_SHARED_PREF = "mycomms_access_token";

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

    public static final String PROFILE_SETTINGS = "settings";
    public static final String PROFILE_PLATFORMS = "platforms";

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
    public static final String CHAT_MESSAGE_FIELD_ID = "id";
    public static final String CHAT_MESSAGE_FIELD_PROFILE_ID = "profile_id";
    public static final String CHAT_MESSAGE_FIELD_CONTACT_ID = "contact_id";
    public static final String CHAT_MESSAGE_FIELD_TIMESTAMP = "timestamp";
    public static final String CHAT_MESSAGE_FIELD_DIRECTION = "direction";
    public static final String CHAT_MESSAGE_FIELD_TYPE = "type";
    public static final String CHAT_MESSAGE_FIELD_TEXT = "text";
    public static final String CHAT_MESSAGE_FIELD_RESOURCE_URI = "resourceUri";
    public static final String CHAT_MESSAGE_FIELD_READ = "read";
    public static final String CHAT_MESSAGE_FIELD_STATUS = "status";
    public static final String CHAT_FIELD_ID = "id";
    public static final String CHAT_FIELD_PROFILE_ID = "profile_id";
    public static final String CHAT_FIELD_CONTACT_ID = "contact_id";
    public static final String CHAT_FIELD_LAST_MESSAGE_ID = "lastMessage_id";
    public static final String CHAT_FIELD_LAST_MESSAGE_TIME = "lastMessageTime";

    //Chat constants
    public static final int CHAT_MESSAGE_TYPE_TEXT = 0;
    public static final int CHAT_MESSAGE_TYPE_PICTURE = 1;
    public static final String CHAT_MESSAGE_DIRECTION_SENT = "0";
    public static final String CHAT_MESSAGE_DIRECTION_RECEIVED = "1";
    public static final String CHAT_MESSAGE_STATUS_NOT_SENT = "0";
    public static final String CHAT_MESSAGE_STATUS_SENT = "1";
    public static final String CHAT_MESSAGE_STATUS_DELIVERED = "2";
    public static final String CHAT_MESSAGE_STATUS_READ = "3";
    public static final String CHAT_MESSAGE_NOT_READ = "0";
    public static final String CHAT_MESSAGE_READ = "1";
    public static final String CHAT_PREVIOUS_VIEW = "Chat_Previous_View";
    public static final String CHAT_VIEW_CHAT_LIST = "Chat_List";
    public static final String CHAT_VIEW_CONTACT_DETAIL = "Contact_Detail";
    public static final String CHAT_VIEW_CONTACT_LIST = "Contact_List";

    //XMPP Constants
    public static final int XMPP_PARAM_PORT = 5222;
    public static final String XMPP_PARAM_HOST = "securejabber.me";
    public static final String XMPP_PARAM_SERVICE_NAME = "securejabber.me";
    public static final String XMPP_PARAM_DOMAIN = "securejabber.me";

    public static final String BUNDLE_DASHBOARD_ACTIVITY = "is_dashboard_activity";

}
