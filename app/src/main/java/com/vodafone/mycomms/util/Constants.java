package com.vodafone.mycomms.util;

import com.squareup.okhttp.MediaType;

import java.text.SimpleDateFormat;

public class Constants {
    public static final String TAG = "MyComms";

    //Shared preferences
    public static final String MYCOMMS_SHARED_PREFS = "mycomms";

    //API Constants
    public static final String API_HTTP_HEADER_VERSION = "x-mycomms-version";
    public static final String API_HTTP_HEADER_CONTENTTYPE = "Content-Type";
    public static final String API_HTTP_HEADER_AUTHORIZATION = "Authorization";
    public static final String API_HTTP_HEADER_USER_AGENT = "user-agent";
    public static final String GCM_SENDER_ID = "1063414309124";

    //Chat Constants
    public static final int LEFT_CHAT = 0;
    public static final int RIGHT_CHAT = 1;
    public static final int LEFT_IMAGE_CHAT = 2;
    public static final int RIGHT_IMAGE_CHAT = 3;
    public static final float CHAT_UNREAD_REGULAR_SIZE = 12; //9sp
    public static final float CHAT_UNREAD_MORE_THAN_99_SIZE = 9; //7sp

    //Profile Constants
    public static final int MY_SETTINGS = 0;
    public static final int MY_PROFILE = 1;
    public static final int MY_ACCOUNTS = 2;
    public static final String PROFILE_ID_SHARED_PREF = "mycomms_profile_id";
    public static final String PROFILE_FULLNAME_SHARED_PREF = "mycomms_profile_fullname";
    public static final String ACCESS_TOKEN_SHARED_PREF = "mycomms_access_token";
    public static final String PLATFORMS_SHARED_PREF = "mycomms_profile_platforms";
    public static final String DEVICE_ID_SHARED_PREF = "device_Id" ;
    public static final String TIMEZONE_SHARED_PREF = "timezone" ;
    public static final String IS_TOOLBAR_CLICKED = "toolbar_clicked";
    public static final String PROFILE_ACCESS_TOKEN = "accessToken";
    public static final String PROFILE_REFRESH_TOKEN = "refreshToken";
    public static final String PROFILE_EXPIRATION_TIME_MILLIS = "expirationTimeMillis";

    //Profile API & BD
    public static final String PROFILE_PRIVATE_TIMEZONE = "privateTimeZone";
    public static final String PROFILE_DONOTDISTURB = "doNotDisturb";
    public static final String PROFILE_HOLIDAY_END_DATE = "endDate";
    public static final String PROFILE_HOLIDAY_START_DATE = "startDate";
    public static final String PROFILE_HOLIDAY = "holiday";
    public static final String PROFILE_ID = "id";

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
    public static final String CONTACT_PAGINATION_PAGESIZE = "pageSize";
    public static final String CONTACT_LOCAL_CONTENT= "local_contact";
    public static final String CONTACT_SORT_HELPER= "sortHelper";
    public static final String CONTACT_SEARCH_HELPER= "searchHelper";
    public static final String CONTACT_DETAIL_INFO= "detailInfo";

    public static final String CONTACT_PHONE_WORK = "phone_work";
    public static final String CONTACT_PHONE_HOME = "phone_home";
    public static final String CONTACT_PHONE_MOBILE = "phone_mobile";

    public static final String CONTACT_IS_FAVORITE = "contactIsFavorite";

    public static final String MULTIPART_AVATAR = "avatar";
    public static final String MULTIPART_FILE = "file";

    public static final String PLATFORM_SALES_FORCE = "sf";
    public static final String PLATFORM_LOCAL = "local";
    public static final String PLATFORM_MY_COMMS = "mc";
    public static final String PLATFORM_GLOBAL_CONTACTS = "ly";

    public static final String PROFILE_SETTINGS = "settings";
    public static final String PROFILE_PLATFORMS = "platforms";

    public static final String NEWS_DATA = "data";
    public static final String NEWS_API_GET = "/api/news";
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

    public static final String CONTACT_API_GET_PROFILE = "/api/me";
    public static final String CONTACT_API_GET_CONTACTS = "/api/me/contact?p=mc";
    public static final String CONTACT_API_GET_CONTACTS_BASIC_CALL = "/api/me/contact?p=";
    public static final String CONTACT_API_GET_CONTACTS_IDS = "/api/me/contact?ids=";
    public static final String CONTACT_API_GET_FAVOURITES = "/api/me/favourites";
    public static final String CONTACT_API_GET_RECENTS = "/api/me/recents";
    public static final String CONTACT_API_POST_RECENTS = "/api/me/recents";
    public static final String CONTACT_API_POST_FAVOURITE = "/api/me/favourites";
    public static final String CONTACT_API_DEL_FAVOURITE = "/api/me/favourites/";
    public static final String CONTACT_API_POST_AVATAR = "/api/me/avatar";
    public static final String CONTACT_API_POST_FILE = "/api/uploadFile";
    public static final String CONTACT_SEND_INVITATION = "/api/me/invite";

    public static final String API_VERSION = "/version";

    public static final String PROFILE_API_LOGOUT = "/auth/logout";

    //Contact Avatar BD Fields
    public static final String AVATAR_CONTACT_ID = "contactId";
    public static final String CONTACT_AVATAR_DIR = "/avatars/files/";
    public static final String CONTACT_CHAT_FILES = "/chat/files/";

    //Chat BD Fields
    public static final String CHAT_MESSAGE_FIELD_ID = "id";
    public static final String CHAT_MESSAGE_FIELD_PROFILE_ID = "profile_id";
    public static final String CHAT_MESSAGE_FIELD_CONTACT_ID = "contact_id";
    public static final String CHAT_MESSAGE_FIELD_GROUP_ID = "group_id";
    public static final String CHAT_MESSAGE_FIELD_DIRECTION = "direction";
    public static final String CHAT_MESSAGE_FIELD_READ = "read";
    public static final String CHAT_FIELD_PROFILE_ID = "profile_id";
    public static final String CHAT_FIELD_CONTACT_ID = "contact_id";
    public static final String CHAT_FIELD_LAST_MESSAGE_TIME = "lastMessageTime";

    //Chat constants
    public static final int CHAT_MESSAGE_TYPE_TEXT = 0;
    public static final int CHAT_MESSAGE_TYPE_IMAGE = 1;
    public static final String CHAT_MESSAGE_DIRECTION_SENT = "0";
    public static final String CHAT_MESSAGE_DIRECTION_RECEIVED = "1";
    public static final String CHAT_MESSAGE_STATUS_NOT_SENT = "not_sent";
    public static final String CHAT_MESSAGE_STATUS_SENT = "sent";
    public static final String CHAT_MESSAGE_STATUS_DELIVERED = "delivered";
    public static final String CHAT_MESSAGE_STATUS_READ = "read";
    public static final String CHAT_MESSAGE_NOT_READ = "0";
    public static final String CHAT_MESSAGE_READ = "1";
    public static final String CHAT_VIEW_CHAT_LIST = "Chat_List";
    public static final String CHAT_VIEW_CONTACT_DETAIL = "Contact_Detail";
    public static final String CHAT_VIEW_CONTACT_LIST = "Contact_List";

    //XMPP Constants
    public static final int XMPP_PARAM_PORT = 5222;
    public static final String XMPP_PARAM_DOMAIN = "my-comms.com";
    public static final String XMPP_MESSAGE_MEDIATYPE_TEXT = "text";
    public static final String XMPP_MESSAGE_MEDIATYPE_IMAGE = "image";
    public static final String XMPP_STANZA_TYPE_CHAT = "chat";
    public static final String XMPP_STANZA_TYPE_GROUPCHAT = "groupchat";
    public static final String XMPP_STANZA_TYPE_PENDINGMESSAGES = "pendingMessages";
    public static final String XMPP_STANZA_TYPE_GET = "get";
    public static final String XMPP_STANZA_TYPE_RESULT = "result";
    public static final String XMPP_ELEMENT_IQ = "iq";
    public static final String XMPP_ELEMENT_MESSAGE = "message";
    public static final String XMPP_ELEMENT_PING = "ping";
    public static final String XMPP_ELEMENT_BODY = "body";
    public static final String XMPP_ATTR_TYPE = "type";
    public static final String XMPP_ATTR_MEDIATYPE = "mediaType";
    public static final String XMPP_ATTR_FROM = "from";
    public static final String XMPP_ATTR_PENDING = "pending";
    public static final String XMPP_ATTR_TO = "to";
    public static final String XMPP_ATTR_ID = "id";
    public static final String XMPP_ATTR_STATUS = "status";
    public static final String XMPP_ATTR_FILEURL = "fileUrl";
    public static final String XMPP_ATTR_RECEIVER = "receiver";
    public static final String XMPP_ATTR_SENT = "sent";

    public static final String DATE_DISPLAY_FORMAT = "MMM d, yyyy";
    public static final String API_DATE_FULL_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public final static SimpleDateFormat SIMPLE_DATE_FORMAT_DISPLAY = new SimpleDateFormat (DATE_DISPLAY_FORMAT);

    public static boolean isSearchBarFocusRequested = false;
    public static boolean isDashboardOrigin = false;
    public static final String IS_LOCAL_CONTACTS_LOADING_ENABLED = "is_local_contacts_loading_enabled";
    public static final String IS_GLOBAL_CONTACTS_LOADING_ENABLED = "is_global_contacts_loading_enabled";

    public static final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");
    public static final String FIRST_TIME_AVATAR_DELIVERY = "first_time_avatar_delivery";

    public static final String GROUP_CHAT_PREVIOUS_ACTIVITY = "previous_activity";
    public static final String GROUP_CHAT_LIST_ACTIVITY = "GroupChatListActivity";
    public static final String CHAT_LIST_ACTIVITY = "ChatListActivity";
    public static final String DASHBOARD_ACTIVITY = "DashBoardActivity";
    public static final String GROUP_CHAT_ID = "group_chat_id";
    public static final String IS_GROUP_CHAT = "is_group_chat";

    public static final String GROUP_CHAT_API = "/api/me/group";
    public static final String SINGLE_GROUP_CHAT_API = "/api/group";
    public static final String GROUP_CHAT_API_SET_MEMBERS = "/api/group";

    public static final String GROUP_CHAT_REALM_ID = "id";
    public static final String GROUP_CHAT_REALM_PROFILE_ID = "profileId";

    public static final String INTERNAL_URL_PATTERN_NEWS = "news/";

    public static final String DEVICE_DEFAULT_USER_AGENT = "Android";

    public static final int MAX_AVATAR_WIDTH_OR_HEIGHT = 640;

    //LDAP Settings constants
    public static final String LDAP_SETTINGS_FIELD_PROFILE_ID = "profileId";
    public static final String LDAP_SETTINGS_FIELD_USER = "user";
    public static final String LDAP_SETTINGS_FIELD_PASSWORD = "password";
    public static final String LDAP_SETTINGS_FIELD_TOKEN = "token";
    public static final String LDAP_SETTINGS_FIELD_TOKEN_TYPE = "tokenType";
    public static final String LDAP_SETTINGS_FIELD_URL = "url";
    public static final String LDAP_SETTINGS_FIELD_TOKEN_TYPE_HEADER = "tt";
    public static final String LDAP_SETTINGS_FIELD_TOKEN_HEADER = "lt";
    public static final String LDAP_API_CALL_PLATFORM = "ly";

    //Platform Search Order
    public static final long ORDER_MYCOMMS = 0;
    public static final long ORDER_GLOBAL_CONTACTS = 1;
    public static final long ORDER_SALES_FORCE = 2;
    public static final long ORDER_LOCAL = 3;

    //Notifications
    public static final String NOTIFICATION_EXTRA_KEY = "notification";
    public static final String NOTIFICATION_BUNDLE_FROM_KEY = "gcm.notification.messageFrom";
    public static final String NOTIFICATION_BUNDLE_TYPE_KEY = "gcm.notification.type";
    public static final String NOTIFICATION_BUNDLE_CHAT_TYPE_VALUE = "chat";
    public static final String NOTIFICATION_BUNDLE_GROUPCHAT_TYPE_VALUE = "groupchat";
    public static final String GO_TO_CHAT_INTENT_KEY = "goToChat";

    //Others

    public static final String IS_APP_CRASHED_EXTRA = "is_app_crashed_extra";
    public static final String APP_CRASH_MESSAGE = "app_crash_message_extra";
    public static final int REQUEST_START_ACTIVITY_FOR_APP_CRASH = 1;

    public static final int IDLE_TIME = 90000;//90 seconds
}
