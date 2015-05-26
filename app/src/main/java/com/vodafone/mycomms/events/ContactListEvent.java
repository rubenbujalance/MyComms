package com.vodafone.mycomms.events;

import org.json.JSONObject;

public class ContactListEvent {
    public final JSONObject mJSONObject;

    public ContactListEvent(JSONObject JSONObject) {
        mJSONObject = JSONObject;
    }
}
