package com.vodafone.mycomms;

/**
 * Created by str_rbm on 09/04/2015.
 */
public class EndpointWrapper {

    private static final String baseURL = "qa.my-comms.com";
    private static final String baseNewsURL = "qa-news.my-comms.com";
    private static final String xmppHost = "qa-msg.my-comms.com";
    private static final String ldapDiscover = "https://weblync13-rat.vodafone.com/Autodiscover/AutodiscoverService.svc/root?originalDomain=vodafone.com";

    public static String getBaseURL() {
        return baseURL;
    }
    public static String getBaseNewsURL() {
        return baseNewsURL;
    }
    public static String getXMPPHost() {
        return xmppHost;
    }
    public static String getLDAPDiscover() {
        return ldapDiscover;
    }
}