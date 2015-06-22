package com.vodafone.mycomms.util;

import java.text.SimpleDateFormat;

public class Constants {
    public static final String TAG = "MyComms";

    //Shared preferences
    public static final String MYCOMMS_SHARED_PREFS = "mycomms";

    //Chat Constants
    public static final int LEFT_CHAT = 0;
    public static final int RIGHT_CHAT = 1;

    //Profile Constants
    public static final int MY_SETTINGS = 0;
    public static final int MY_PROFILE = 1;
    public static final String PROFILE_ID_SHARED_PREF = "mycomms_profile_id";
    public static final String PROFILE_PLATFORMS_SHARED_PREF = "mycomms_profile_platforms";
    public static final String PROFILE_FULLNAME_SHARED_PREF = "mycomms_profile_fullname";
    public static final String ACCESS_TOKEN_SHARED_PREF = "mycomms_access_token";
    public static final String PLATFORMS_SHARED_PREF = "mycomms_profile_platforms";
    public static final String DEVICE_ID_SHARED_PREF = "device_Id" ;
    public static final String TIMEZONE_SHARED_PREF = "timezone" ;

    //Profile API & BD
    public static final String PROFILE_PRIVATE_TIMEZONE = "privateTimeZone";
    public static final String PROFILE_DONOTDISTURB = "doNotDisturb";
    public static final String PROFILE_PHONE = "phone";
    public static final String PROFILE_EMAIL = "email";
    public static final String PROFILE_DEVICE_ID = "deviceId";
    public static final String PROFILE_HOLIDAY_END_DATE = "holidayEndDate";
    public static final String PROFILE_HOLIDAY = "holiday";

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
    public static final String CONTACT_PROFILE_ID = "profileId";
    public static final String CONTACT_CONTACT_ID = "contactId";
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
    public static final String CONTACT_LOCAL_CONTENT= "local_contact";

    public static final String CONTACT_IS_FAVORITE = "contactIsFavorite";

    public static final String MULTIPART_AVATAR = "avatar";
    public static final String MULTIPART_FILE = "file";


    public static final String PLATFORM_SALES_FORCE = "sf";
    public static final String PLATFORM_LOCAL = "local";
    public static final String PLATFORM_MY_COMMS = "mc";


    public static final String PROFILE_SETTINGS = "settings";
    public static final String PROFILE_PLATFORMS = "platforms";

    //Contacts API Calls
    public static final String NEWS = "news";
    public static final String NEWS_DATA = "data";
    public static final String NEWS_API_GET = "/api/news";
    public static final String NEWS_PAGINATION = "pagination";
    public static final String NEWS_PAGINATION_MORE_PAGES = "morePages";
    public static final String NEWS_PAGINATION_OFFSET = "offset";
    public static final String NEWS_PAGINATION_PAGESIZE = "pageSize";
    public static final String NEWS_UUID = "uuid";
    public static final String NEWS_TITLE = "title";
    public static final String NEWS_HTML = "html";
    public static final String NEWS_IMAGE = "image";
    public static final String NEWS_LINK = "link";
    public static final String NEWS_AUTHOR_NAME = "author_name";
    public static final String NEWS_AUTHOR_AVATAR = "author_avatar";
    public static final String NEWS_CREATED_AT = "created_at";
    public static final String NEWS_UPDATED_AT = "updated_at";
    public static final String NEWS_PUBLISHED_AT = "published_at";

    //Contacts API Calls
    public static final String CONTACT_API_GET_CONTACTS = "/api/me/contact?p=mc";
    public static final String CONTACT_API_GET_CONTACTS_BASIC_CALL = "/api/me/contact?p=";
    public static final String CONTACT_API_GET_FAVOURITES = "/api/me/favourites";
    public static final String CONTACT_API_GET_RECENTS = "/api/me/recents";
    public static final String CONTACT_API_POST_RECENTS = "/api/me/recents";
    public static final String CONTACT_API_POST_FAVOURITE = "/api/me/favourites";
    public static final String CONTACT_API_DEL_FAVOURITE = "/api/me/favourites/";
    public static final String CONTACT_API_POST_AVATAR = "https://int.my-comms.com/api/me/avatar";
    public static final String CONTACT_API_POST_FILE = "https://int.my-comms.com/api/uploadFile";

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
    public static final String CHAT_MESSAGE_STATUS_NOT_SENT = "not_sent";
    public static final String CHAT_MESSAGE_STATUS_SENT = "sent";
    public static final String CHAT_MESSAGE_STATUS_DELIVERED = "delivered";
    public static final String CHAT_MESSAGE_STATUS_READ = "read";
    public static final String CHAT_MESSAGE_NOT_READ = "0";
    public static final String CHAT_MESSAGE_READ = "1";
    public static final String CHAT_PREVIOUS_VIEW = "Chat_Previous_View";
    public static final String CHAT_VIEW_CHAT_LIST = "Chat_List";
    public static final String CHAT_VIEW_CONTACT_DETAIL = "Contact_Detail";
    public static final String CHAT_VIEW_CONTACT_LIST = "Contact_List";

    //XMPP Constants
    public static final int XMPP_PARAM_PORT = 5222;
    public static final String XMPP_PARAM_DOMAIN = "my-comms.com";
    public static final String XMPP_MESSAGE_MEDIATYPE_TEXT = "text";
    public static final String XMPP_MESSAGE_TYPE_CHAT = "chat";
    public static final String XMPP_IQ_TYPE_CHAT = "chat";


    public static final String BUNDLE_DASHBOARD_ACTIVITY = "is_dashboard_activity";

    //Kind of files to send as multipart;
    public static final String IMAGE_FILE_TO_SEND = "image_file";
    public static final String SIMPLE_FILE_TO_SEND = "simple_file";

    public static final String DATE_DISPLAY_FORMAT = "MMM d, yyyy";
    public final static SimpleDateFormat SIMPLE_DATE_FORMAT_DISPLAY = new SimpleDateFormat (DATE_DISPLAY_FORMAT);
    public static String toolbar = "toolbar";

    public static boolean isSearchBarFocusRequested = false;
}
