package com.vodafone.mycomms.constants;

/**
 * Created by str_evc on 21/05/2015.
 */
public class Constants {

    //LOGIN TESTS
    public static final String VALID_EMAIL = "valid@test.com";
    public static final String INVALID_EMAIL = "invalid";
    public static final String PASSWORD = "Password1";
    public static final String ANOTHER_PASSWORD = "Password2";
    public static final String PHONE = "012345678";
    public static final String FIRSTNAME = "firstname";
    public static final String LASTNAME = "lastname";
    public static final String PIN = "1234";
    public static final String PROFILE_ID = "mc_55b2ee34784677e356dc498f";
    public static final int FORGOT_PASSWORD_ACTIVITY = 1;
    public static final String ACCESS_TOKEN = "c7n_CO-qva-s9vgLfbljwQKLqf9hQVhMFNvWgP-ula0O-SG0DYdXPgI6zt1cgdZuBfvLSFXdjc_T2hpGNJ_mv3M_IClqDYqUAUNCFeiLPtUJIvvoO5IKYXlPgYHkCZsZ0Maf6bGXhLXLIyZQcjPvLtovTLgEN0tQZIpfMIVFpG4";
    public static final String REFRESH_TOKEN = "RaHZJLVyVc7ZxyDEJsTZcLpXVxmPnUKzHJ3cofn2HYyTYV0B9wQyCVPsNZuVWRKr8o99gutaENceAfVbeSPvcHeiQaiAeQmcZpxwEXj7aza8t7jjTlImw59f6sj6RVhokHtrokRCNIzxC7Jfe8qhJoGZ6WnSaEJlh1EFJFqag0M";
    public static final long EXPIRES_IN = 1000L;
    public static final String VALID_VERSION_RESPONSE = "{\"err\":\"invalid_version\",\"des\":\"Must update to last application version\",\"data\":\"https://s3-us-west-2.amazonaws.com/mycomms-android/MyComms/android/dev/91/MyComms-i.apk\"}";
    public static final String INVALID_VERSION_RESPONSE = "{\"err\":\"invalid_version\",\"des\":\"Must update to last application version\",\"data\":\"https://s3-us-west-2.amazonaws.com/mycomms-android/MyComms/android/dev/91/MyComms-i.apk\"}";
    public static final String LOGIN_OK_RESPONSE = "{\"accessToken\": \"c7n_CO-qva-s9vgLfbljwQKLqf9hQVhMFNvWgP-ula0O-SG0DYdXPgI6zt1cgdZuBfvLSFXdjc_T2hpGNJ_mv3M_IClqDYqUAUNCFeiLPtUJIvvoO5IKYXlPgYHkCZsZ0Maf6bGXhLXLIyZQcjPvLtovTLgEN0tQZIpfMIVFpG4\",\"refreshToken\": \"RaHZJLVyVc7ZxyDEJsTZcLpXVxmPnUKzHJ3cofn2HYyTYV0B9wQyCVPsNZuVWRKr8o99gutaENceAfVbeSPvcHeiQaiAeQmcZpxwEXj7aza8t7jjTlImw59f6sj6RVhokHtrokRCNIzxC7Jfe8qhJoGZ6WnSaEJlh1EFJFqag0M\",\"expiresIn\": 1}";
    public static final String LOGIN_USER_NOT_FOUND_RESPONSE = "{\"err\":\"user_not_found\"}";
    public static final String USER_ALREADY_EXISTS_RESPONSE = "{\"err\": \"auth_proxy_user_error\",\"des\": \"user already exists\"}";
    public static final String USER_PHONE_NOT_VERIFIED_RESPONSE = "{\"err\": \"auth_proxy_verified_error\",\"des\": \"User phone not verified\"}";
    public static final String USER_DOMAIN_NOT_ALLOWED_RESPONSE = "{\"err\": \"user_domain_not_allowed\",\"des\": \"Sorry your email domain is not authorised for this service\"}";
    public static final String CHECK_PHONE_OK_RESPONSE = "{\"des\": \"eulogi_vidal@stratesys-ts.com\"}";
    public static final String OAUTH_RESPONSE = "{\"name\":\"Ruben\",\"lastname\":\"Bujalance\",\"email\":\"ruben_bujalance@stratesys-ts.com\",\"sf\":\"qtnQ8xhhx_VxpNQfEIyongbtY7_92evEg08CcCOzgyr04jPi3ccxCOglRaenQrALFydHe9rwpovvff_CJkwcXDzbd95MhAW3gMitQklcHQsZNroVAJVyx4i0WYNiE0l5nkSCj_kiyO40cclH8PcIoWLURVLICXuUQyrFBd5hto4aTAAnjXuGwLiZftiv2720nfOGcGxPgmgkVraOVQssHmclHDrbBcGpO6UhZSqmlzWlwrjgShl4HMfNCpuz-y4nza4Oe1jle3n2RS3FNNPIoehtTvh2sB_PyzEWnv0HTsNnGZZ5GubmMVNvLEyJhaV9mw-1-V-UGnK4X8BF4rX_hb_Gu_KHciN0zdhcpNz7-WwbvIeoY7ftBtNr6XHGk8V8u7zAvMXuPMY2iFaLHIx4PhcDfP2K8PxBk1k1fbBoKMku1RK0e-qi-LEDKKcVg7FdmeUwwi1tYz5QQNhwyfwFT7uTWCHFOAcM-4XDqjTFJcGf2Kv_tnbO7BqUsLoSFegzl5qlu9xaat4BPGg2xp1HH5WCUfC1SRDkCNh3KltGqvn4_J900g6o8F62saSMwCl9V21_E84DjmxUHS7nUooaCHEUw07EnJDeGABAgyAYCChyekhItIs4i0DEmnP3i3Q-Pk-JGD7XNJEwhmVKSJ37LKYt3zCOsxdzCH7nZ_jDuTJkCn4qZrC722gpQ2GQeMmOaTrTjbr5ZcNmRFr8-ijvSaMaYJcQu9KkbOunUzHe-_7_kov8DvO6T0n-NJeCDZ7BrsRN9dgFPszpg5osf9ayQRkWBONZGF4SUx_ny9udCospVeyKnys8sfZymSHQQ6aWtsFu5y0DNjyJKHNGymnNospzoXY4csLwXYu8IT8sXuR--Ep7Ej_3jQCpk_v_Ztow\",\"officeLocation\":\"Barcelona Spain\",\"avatar\":\"https://s3-us-west-2.amazonaws.com/mycomms-avatars/_default_avatar.png\",\"country\":\"ES\",\"phone\":\"661350545\",\"position\":\"Senior Technical Consultant\"}";
    public static final String OAUTH_200_RESPONSE = "{\"accessToken\":\"mockAccessToken\",\"refreshToken\":\"mockRefreshToken\",\"expiresIn\":\"123456789\"}";


    //LDAP TESTS
    public static final String LDAP_USER = "ruben_bujalance";
    public static final String LDAP_PASSWORD = "123456789";
    public static final String LDAP_TOKEN = "cwt=AAEBHAEFAAAAAAAFFQAAAHgunRNCx3RSgoumKIbaUwCBENsx6jq4WrhZu86hmdu8GriCAg9mgyAUTV3bOCa0m37xnUa9tEGpw4QiH3RWvoW43-eGaQMeWoYI24-6Ay6j0ggNEIYYH10smKhXkptZU8OMRKo";
    public static final String LDAP_TOKEN_TYPE = "Bearer";
    public static final String LDAP_RETURN_URL = "https://weblync13-rat.vodafone.com/Autodiscover/AutodiscoverService.svc/root/oauth/user?originalDomain=vodafone.com";
    public static final String LDAP_DISCOVER_RESPONSE_OK = "{\n\"_links\": {\n\"self\": {\n\"href\": \"https://weblync13-rat.vodafone.com/Autodiscover/AutodiscoverService.svc/root?originalDomain=vodafone.com\"\n},\n\"user\": {\n\"href\": \"mockUrl\"\n},\n\"xframe\": {\n\"href\": \"https://weblync13-rat.vodafone.com/Autodiscover/XFrame/XFrame.html\"\n}\n}\n}";
    public static final String LDAP_USER_RESPONSE_HEADER_KEY = "WWW-Authenticate";
    public static final String LDAP_USER_RESPONSE_HEADER_OK = "Bearer trusted_issuers=\"\", client_id=\"00000004-0000-0ff1-ce00-000000000000\", MsRtcOAuth href=\"mockUrl\",grant_type=\"urn:microsoft.rtc:windows,urn:microsoft.rtc:anonmeeting,password\"";
    public static final String LDAP_AUTH_RESPONSE_OK = "{\n\"access_token\": \"cwt=AAEBHAEFAAAAAAAFFQAAAHgunRNCx3RSgoumKIbaUwCBENsx6jq4WrhZu86hmdu8GriCAg9mgyAUTV3bOCa0m37xnUa9tEGpw4QiH3RWvoW43-eGaQMeWoYI24-6Ay6j0ggNEIYYH10smKhXkptZU8OMRKo\",\n\"expires_in\": 28489,\n\"ms_rtc_identityscope\": \"local\",\n\"token_type\": \"Bearer\"\n}";
    public static final String LDAP_AUTH_RESPONSE_ERROR = "{\n\"ERROR_access_token\": \"cwt=AAEBHAEFAAAAAAAFFQAAAHgunRNCx3RSgoumKIbaUwCBENsx6jq4WrhZu86hmdu8GriCAg9mgyAUTV3bOCa0m37xnUa9tEGpw4QiH3RWvoW43-eGaQMeWoYI24-6Ay6j0ggNEIYYH10smKhXkptZU8OMRKo\",\n\"expires_in\": 28489,\n\"ms_rtc_identityscope\": \"local\",\n\"token_type\": \"Bearer\"\n}";
    public static final String LDAP_AUTH_URL = "https://weblync13-rat.vodafone.com/WebTicket/oauthtoken";

    //MYCOMMS BASEURL TESTS
    public static final String BASEURL_AUTH_URL = "https://int.my-comms.com";
    public static final String BASEURL_RESPONSE_HEADER_OK = "Access-Control-Allow-Credentials=\"true\", Access-Control-Allow-Headers=\"x-mycomms-version,content-type,Authorization\", Access-Control-Allow-Methods=\"OPTION,GET,PUT,POST,DELETE,HEAD\", Access-Control-Allow-Origin = \"https://int-marketing.my-comms.com\", Connection = \"keep-alive\", Content-Length = \"12851\", Content-Type = \"application/json\", Date = \"Mon, 31 Aug 2015 07:54:33 GMT\", Server = \"nginx/1.4.6 (Ubuntu)\", X-Frame-Options = \"SAMEORIGIN\"";
    public static final String BASEURL_RESPONSE_HEADER_KEY = "WWW-Authenticate";

    //SETTINGS TESTS
    public static final String VACATION_END_DATE = "2015-09-10T00:00:00.000+0200";
    public static final String SETTINGS_JSON_TIMEZONE_PRIVATE = "\"{\\\"privateTimeZone\\\":true,\\\"holiday\\\":{\\\"startDate\\\":\\\"2015-09-08T22:00:00.000Z\\\",\\\"endDate\\\":\\\"2015-09-10T21:59:59.999Z\\\"}}\"";
    public static final String SETTINGS_JSON_TIMEZONE_PUBLIC = "\"{\\\"holiday\\\":{\\\"startDate\\\":\\\"2015-09-08T22:00:00.000Z\\\",\\\"endDate\\\":\\\"2015-09-10T21:59:59.999Z\\\"}}\"";
    public static final String SETTINGS_JSON_DONOTDISTURB = "\"{\\\"doNotDisturb\\\":true,\\\"holiday\\\":{\\\"startDate\\\":\\\"2015-09-08T22:00:00.000Z\\\",\\\"endDate\\\":\\\"2015-09-10T21:59:59.999Z\\\"}}\"";
    public static final String SETTINGS_JSON_FULL = "{\"privateTimeZone\":true,\"doNotDisturb\":true,\"holiday\":{\"startDate\":\"2015-09-09T22:00:00.000Z\",\"endDate\":\"2015-10-19T21:59:59.999Z\"}}";
    public static final String SETTINGS_JSON_NO_HOLIDAY = "{\"privateTimeZone\":true,\"doNotDisturb\":true}";
    public static final String SETTINGS_JSON_NO_DONOTDISTURB = "{\"privateTimeZone\":true}";
    public static final String SETTINGS_JSON_NO_TIMEZONE = "{\"doNotDisturb\":true}";

}
