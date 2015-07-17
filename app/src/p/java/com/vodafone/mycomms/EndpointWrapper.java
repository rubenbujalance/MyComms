package com.vodafone.mycomms;

/**
 * Created by str_rbm on 09/04/2015.
 */
public class EndpointWrapper {

    private static final String baseURL = "pro.my-comms.com";
    private static final String baseNewsURL = "news.my-comms.com";
    private static final String xmppHost = "pro-msg.my-comms.com";

    public static String getBaseURL() {
        return baseURL;
    }
    public static String getBaseNewsURL() {
        return baseNewsURL;
    }
    public static String getXMPPHost() {
        return xmppHost;
    }
}
