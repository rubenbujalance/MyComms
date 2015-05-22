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

    public static final String VALID_VERSION_RESPONSE = "{\"err\":\"invalid_version\",\"des\":\"Must update to last application version\",\"data\":\"https://s3-us-west-2.amazonaws.com/mycomms-android/MyComms/android/dev/91/MyComms-i.apk\"}";
    public static final String INVALID_VERSION_RESPONSE = "{\"err\":\"invalid_version\",\"des\":\"Must update to last application version\",\"data\":\"https://s3-us-west-2.amazonaws.com/mycomms-android/MyComms/android/dev/91/MyComms-i.apk\"}";
    public static final String LOGIN_OK_RESPONSE = "{\"err\":\"user_not_found\"}";
    public static final String LOGIN_USER_NOT_FOUND_RESPONSE = "{\"err\":\"user_not_found\"}";

}
