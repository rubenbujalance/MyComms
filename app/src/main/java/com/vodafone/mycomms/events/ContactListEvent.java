package com.vodafone.mycomms.events;

import org.json.JSONObject;

/**
 * Created by AMG on 19/05/2015.
 */
public class ContactListEvent {
    public final JSONObject mJSONObject;

    public ContactListEvent(JSONObject JSONObject) {
        mJSONObject = JSONObject;
    }
}
