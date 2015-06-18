package com.vodafone.mycomms.events;

import org.xmlpull.v1.XmlPullParser;

/**
 * Created by str_rbm on 04/06/2015.
 */
public class MessageReceivedEvent {
    private XmlPullParser parser;

    public XmlPullParser getParser() {
        return parser;
    }

    public void setParser(XmlPullParser parser) {
        this.parser = parser;
    }
}
