package com.vodafone.mycomms.constants;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;

/**
 * Created by str_evc on 21/05/2015.
 */
public class Constants {

    public static final String VALID_EMAIL = "valid@test.com";
    public static final String INVALID_EMAIL = "invalid";
    public static final String PASSWORD = "password";
    public static final String FIRSTNAME = "firstname";
    public static final String LASTNAME = "lastname";
    public static final String VALID_VERSION_RESPONSE = "{\"err\":\"invalid_version\",\"des\":\"Must update to last application version\",\"data\":\"https://s3-us-west-2.amazonaws.com/mycomms-android/MyComms/android/dev/91/MyComms-i.apk\"}";
    public static final String INVALID_VERSION_RESPONSE = "{\"err\":\"invalid_version\",\"des\":\"Must update to last application version\",\"data\":\"https://s3-us-west-2.amazonaws.com/mycomms-android/MyComms/android/dev/91/MyComms-i.apk\"}";
    public static final String LOGIN_OK_RESPONSE = "{\"accessToken\": \"c7n_CO-qva-s9vgLfbljwQKLqf9hQVhMFNvWgP-ula0O-SG0DYdXPgI6zt1cgdZuBfvLSFXdjc_T2hpGNJ_mv3M_IClqDYqUAUNCFeiLPtUJIvvoO5IKYXlPgYHkCZsZ0Maf6bGXhLXLIyZQcjPvLtovTLgEN0tQZIpfMIVFpG4\",\"refreshToken\": \"RaHZJLVyVc7ZxyDEJsTZcLpXVxmPnUKzHJ3cofn2HYyTYV0B9wQyCVPsNZuVWRKr8o99gutaENceAfVbeSPvcHeiQaiAeQmcZpxwEXj7aza8t7jjTlImw59f6sj6RVhokHtrokRCNIzxC7Jfe8qhJoGZ6WnSaEJlh1EFJFqag0M\",\"expiresIn\": 1}";
    public static final String LOGIN_USER_NOT_FOUND_RESPONSE = "{\"err\":\"user_not_found\"}";
    public static final String USER_ALREADY_EXISTS_RESPONSE = "{\"err\": \"auth_proxy_user_error\",\"des\": \"user already exists\"}";
    public static final String USER_PHONE_NOT_VERIFIED_RESPONSE = "{\"err\": \"auth_proxy_verified_error\",\"des\": \"User phone not verified\"}";
    public static final String USER_DOMAIN_NOT_ALLOWED_RESPONSE = "{\"err\": \"user_domain_not_allowed\",\"des\": \"Sorry your email domain is not authorised for this service\"}";
}
