package com.vodafone.mycomms;

/**
 * Created by str_rbm on 09/04/2015.
 */
public class EndpointWrapper {

    private static final String baseURL = "int.my-comms.com";
    private static final String baseNewsURL = "int-news.my-comms.com";

    public static String getBaseURL() {
        return baseURL;
    }
    public static String getBaseNewsURL() {
        return baseNewsURL;
    }
}
