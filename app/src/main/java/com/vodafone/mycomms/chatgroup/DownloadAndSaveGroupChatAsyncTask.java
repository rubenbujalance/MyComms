package com.vodafone.mycomms.chatgroup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.GroupChatCreatedEvent;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import model.ChatMessage;
import model.GroupChat;

/**
 * Created by str_rbm on 09/07/2015.
 */
public class DownloadAndSaveGroupChatAsyncTask extends AsyncTask<Object, Void, GroupChat> {

    Context context;
    String groupId;
    String endpoint;
    String profileId;
    String chatMessageId;

    @Override
    protected GroupChat doInBackground(Object... params) {
        context = (Context)params[0];
        endpoint = (String)params[1];
        groupId = (String)params[2];
        chatMessageId = (String)params[3];

        try {
            //ProfileId
            SharedPreferences sp = context.getSharedPreferences(Constants.MYCOMMS_SHARED_PREFS,
                    Context.MODE_PRIVATE);
            profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);
            if(profileId==null) {
                Log.e(Constants.TAG, "DownloadAndSaveGroupInfoAsyncTask.doInBackground: " +
                        "Error getting profile id from Shared Preferences");
                return null;
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "DownloadAndSaveGroupInfoAsyncTask.doInBackground: " +
                    "Error getting profile id from Shared Preferences",e);
            return null;
        }

        Response response;
        String jsonStr = null;
        RealmGroupChatTransactions groupTx = null;
        GroupChat chat = null;

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://" + EndpointWrapper.getBaseURL() +
                            endpoint + "/" + groupId)
                    .addHeader(Constants.API_HTTP_HEADER_VERSION,
                            Utils.getHttpHeaderVersion(context))
                    .addHeader(Constants.API_HTTP_HEADER_CONTENTTYPE,
                            Utils.getHttpHeaderContentType())
                    .addHeader(Constants.API_HTTP_HEADER_AUTHORIZATION,
                            Utils.getHttpHeaderAuth(context))
                    .build();

            response = client.newCall(request).execute();
            jsonStr = response.body().string();
            JSONObject json;

            if (jsonStr != null && jsonStr.length() > 0) {
                json = new JSONObject(jsonStr);
                String groupId = json.getString("id");
                String creator = json.getString("creator");
                JSONArray members = json.getJSONArray("members");

                ArrayList<String> membersIds = new ArrayList<>();
                ArrayList<String> ownersIds = new ArrayList<>();
                JSONObject member;
                String id;

                for(int i=0; i<members.length(); i++)
                {
                    member = members.getJSONObject(i);
                    id = member.getString("id");

                    membersIds.add(id);
                    if(member.has("owner"))
                        ownersIds.add(id);
                }

                groupTx =
                        new RealmGroupChatTransactions(context, profileId);

                chat = groupTx.newGroupChatInstance(
                        groupId, profileId, membersIds, ownersIds, "", "", "");

                //Set last message data
                ChatMessage newChatMessage = groupTx.getGroupChatMessageById(chatMessageId);
                if(newChatMessage!=null) {
                    chat.setLastMessage_id(newChatMessage.getId());

                    String lastText;
                    if (newChatMessage.getType() == Constants.CHAT_MESSAGE_TYPE_TEXT)
                        lastText = newChatMessage.getText();
                    else lastText = context.getString(R.string.image);

                    if (newChatMessage.getDirection().equals(Constants.CHAT_MESSAGE_DIRECTION_SENT))
                        chat.setLastMessage(context.getResources().getString(R.string.chat_me_text) + lastText);
                    else chat.setLastMessage(lastText);

                    chat.setLastMessageTime(newChatMessage.getTimestamp());
                }

                groupTx.insertOrUpdateGroupChat(chat);

                groupTx.closeRealm();
            }
            else {Log.e(Constants.TAG, "DownloadAndSaveGroupInfoAsyncTask.onPostExecute: " +
                    "Error receiving JSON for group " + groupId);}

        } catch (Exception e) {
            Log.e(Constants.TAG, "DownloadAndSaveGroupInfoAsyncTask.doInBackground: ",e);
            groupTx.closeRealm();
        }

        return chat;
    }

    @Override
    protected void onPostExecute(GroupChat chat) {
        if(chat!=null) {
            GroupChatCreatedEvent event = new GroupChatCreatedEvent();
            event.setGroupChat(chat);
            BusProvider.getInstance().post(event);
        }
    }
}
