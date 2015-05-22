package com.vodafone.mycomms.test.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.robolectric.shadows.FakeHttp;

/**
 * Created by str_evc on 21/05/2015.
 */
public class Util {

    private static final ProtocolVersion DEFAULT_HTTP_VERSION = HttpVersion.HTTP_1_1;
    private static final int DEFAULT_HTTP_RETURN_CODE = 204;
    private static final String DEFAULT_HTTP_RETURN_REASON = "OK";

    public static HttpResponse buildOkResponse() throws Exception {
        return buildOkResponse(null);
    }

    public static HttpResponse buildOkResponse(String stringEntity) throws Exception {
        return buildResponse(DEFAULT_HTTP_VERSION, DEFAULT_HTTP_RETURN_CODE, DEFAULT_HTTP_RETURN_REASON, stringEntity);
    }

    public static HttpResponse buildResponse(int code) throws Exception {
        return buildResponse(code, null);
    }

    public static HttpResponse buildResponse(int code, String stringEntity) throws Exception {
        return buildResponse(DEFAULT_HTTP_VERSION, code, DEFAULT_HTTP_RETURN_REASON, stringEntity);
    }

    public static HttpResponse buildResponse(ProtocolVersion ver, int code, String reason, String stringEntity) throws Exception {
        HttpResponse httpResponse = new BasicHttpResponse(ver, code, reason);
        httpResponse.setHeader("Content-Type", "application/json");
        if (stringEntity != null && stringEntity.length() > 0) {
            HttpEntity entity = new StringEntity(stringEntity);
            httpResponse.setEntity(entity);
        }
        return httpResponse;
    }

}
