package com.vodafone.mycomms.contacts.connection;

import android.content.Context;
import android.util.Log;

import com.framework.library.connection.HttpConnection;
import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.RecentContactsReceivedEvent;
import com.vodafone.mycomms.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;

public class ContactSearchController extends BaseController {

    private final Realm mRealm;
    private Context mContext;
    private String mProfileId;
    private ContactSearchConnection mContactSearchConnection;
    private JSONObject mJSONRecents;
    private String idList = "";
    private int offsetPaging = 0;

    public ContactSearchController(Context appContext, Realm realm, String profileId) {
        super(appContext);
        this.mRealm = realm;
        this.mContext = appContext;
        this.mProfileId = profileId;
    }

    public void getContactById(JSONObject jsonObject) {
        Log.i(Constants.TAG, "ContactSearchController.getRecentList: ");
        if(mContactSearchConnection != null){
            mContactSearchConnection.cancel();
        }
        mJSONRecents = jsonObject;
        int method = HttpConnection.GET;
        idList = getContactIdList();
        if (idList!=null && !idList.equals("")) {
            String apiCall = Constants.CONTACT_API_GET_CONTACTS_IDS + idList;
            //Get all Contacts related to Recents
            mContactSearchConnection = new ContactSearchConnection(mContext, this, method, apiCall);
            mContactSearchConnection.request();
        }
    }

    private void getContactByIdPagination(){
        int method = HttpConnection.GET;
        if (idList!=null && !idList.equals("")) {
            String apiCall = Constants.CONTACT_API_GET_CONTACTS_IDS + idList + "&o=" + offsetPaging;
            mContactSearchConnection = new ContactSearchConnection(mContext, this, method, apiCall);
            mContactSearchConnection.request();
        }
    }

    private String getContactIdList() {
        Log.i(Constants.TAG, "ContactSearchController.getContactIdList: ");
        String ids = "";
        try {
            JSONArray jsonArray = mJSONRecents.getJSONArray(Constants.CONTACT_RECENTS);
            JSONObject jsonObject;
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString(Constants.CONTACT_ID)!=null
                        && !jsonObject.getString(Constants.CONTACT_ID).equals("")){
                    //ids = ids + "," + jsonObject.getString(Constants.CONTACT_ID);
                    ids = "mc_55409316799f7e1a109446f4,mc_553666cc7b2268b051c24b67,mc_55362023c729d4430b9722f4,mc_554b20fc80eb511a3c1d1262,mc_55361a9cc729d4430b9722f3,mc_5570340e7eb7c3512f2f9bf2,mc_5564538cd454a47a5905d4de,mc_5535b2ac13be4b7975c51600,mc_5537d27ed5f0aa881e4eea15,mc_553666cc7b2268b051c24b67,mc_555b5d7ce4d6a17707e64c83,mc_553626c0c729d4430b9722f5,mc_5535d627c729d4430b9722e9,mc_556cb232baa6d1f54b6f09b8,mc_555a0985121ef1695cc7c1c4,mc_5535e60ec729d4430b9722ea,mc_5536597eed882c9348ec77bf,mc_553913669c2c1aaa5c794455,mc_5536f49a7b2268b051c24b6c,mc_5538d5ba5f244b372b06a653,mc_554c8a9a638c3837502572cb,mc_55361215c729d4430b9722f2,mc_5535b45713be4b7975c51601,mc_5584a3dd087bbde07946836c,mc_5584a6c10dff7c937b022cf9,mc_553ecfa72f11d1676ccb1b0c,mc_558498a195a53775741f89cc,mc_55849e8ef841f79d76182407,mc_5535ab5313be4b7975c515fd,mc_5588348c1306f6d62df4cdcb,mc_553778f97b2268b051c24b85,mc_556fa74bb594a4d816e4cbba,mc_5535b16c13be4b7975c515fe,mc_555a0792121ef1695cc7c1c3,mc_554d29379d0098cf7f1c69c8,mc_558841b3996c7a9432ede7cd,mc_5535b20413be4b7975c515ff,mc_55671aecd18f452976bc4c2f,mc_5578d4ec2dca01d31e54ad81,mc_556704b3d18f452976bc4c2e,mc_55360043c729d4430b9722eb,mc_553f92e8799f7e1a109446f2";
                }
            }
        } catch (JSONException e){
            Log.e(Constants.TAG, "ContactSearchController.getContactIdList: ", e);
            return null;
        }
        return ids;
    }

    @Override
    public void onConnectionComplete(ConnectionResponse response) {
        super.onConnectionComplete(response);
        Log.i(Constants.TAG, "ContactSearchController.onConnectionComplete: ");
        String result = response.getData().toString();
        if (result != null && result.trim().length()>0) {
            try {
                //Check pagination
                JSONObject jsonResponse = new JSONObject(result);
                ContactController contactController = new ContactController(mContext, mRealm, mProfileId);
                contactController.insertContactListInRealm(jsonResponse);

                JSONObject jsonPagination = jsonResponse.getJSONObject(Constants.CONTACT_PAGINATION);
                if (jsonPagination.getBoolean(Constants.CONTACT_PAGINATION_MORE_PAGES)) {
                    int pageSize = jsonPagination.getInt(Constants.CONTACT_PAGINATION_PAGESIZE);
                    offsetPaging = offsetPaging + pageSize;
                    getContactByIdPagination();
                } else {
                    offsetPaging = 0;
                    contactController.insertRecentContactInRealm(mJSONRecents);
                    //Show Recents on Dashboard
                    BusProvider.getInstance().post(new RecentContactsReceivedEvent());
                }
            } catch (JSONException e) {
                Log.e(Constants.TAG, "ContactSearchController.onConnectionComplete: ", e);
            }
        }
    }
}