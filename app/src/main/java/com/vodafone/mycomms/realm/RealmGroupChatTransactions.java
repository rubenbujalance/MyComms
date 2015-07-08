package com.vodafone.mycomms.realm;

import android.content.Context;
import android.util.Log;

import com.vodafone.mycomms.util.Constants;

import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import model.ChatMessage;
import model.GroupChat;

/**
 * Created by str_oan on 01/07/2015.
 */
public class RealmGroupChatTransactions {
    private Realm mRealm;
    private String _profile_id;
    private Context mContext;
    private String LOG_TAG = RealmGroupChatTransactions.class.getSimpleName();

    public RealmGroupChatTransactions(Context context, String profile_id)
    {
        mContext = context;
        mRealm = Realm.getDefaultInstance();
        _profile_id = profile_id;
    }

    public GroupChat newGroupChatInstance
            (
                    String id
                    , String profile_id
                    , ArrayList<String> membersIds
                    , ArrayList<String> ownersIds
                    , String name
                    , String about
                    , String avatar
            )
    {
        long timestamp = Calendar.getInstance().getTimeInMillis();

        return new GroupChat
                (
                        id
                        , profile_id
                        , profile_id
                        , name
                        , avatar
                        , about
                        , generateComposedMembersId(membersIds)
                        , generateComposedMembersId(ownersIds)
                        , timestamp
                );

    }

    public ChatMessage newGroupChatMessageInstance(String contact_id, String group_id,
                                                   String direction, int type, String text,
                                                   String resourceUri)
    {
        long timestamp = Calendar.getInstance().getTimeInMillis();

        ChatMessage chatMessage = new ChatMessage(_profile_id,contact_id,group_id,timestamp,
                direction,type,text,resourceUri,Constants.CHAT_MESSAGE_NOT_READ,
                Constants.CHAT_MESSAGE_STATUS_NOT_SENT);

        return chatMessage;
    }

    public ChatMessage newGroupChatMessageInstance(String contact_id, String group_id,
                                                   String direction, int type, String text,
                                                   String resourceUri, String id)
    {
        long timestamp = Calendar.getInstance().getTimeInMillis();

        ChatMessage chatMessage = new ChatMessage(_profile_id,contact_id,group_id,timestamp,
                direction,type,text,resourceUri,Constants.CHAT_MESSAGE_NOT_READ,
                Constants.CHAT_MESSAGE_STATUS_NOT_SENT, id);

        return chatMessage;
    }

    public void insertOrUpdateGroupChat (GroupChat newChat)
    {
        if(newChat==null) return;
        try {
            mRealm.beginTransaction();
            mRealm.copyToRealmOrUpdate(newChat);

            mRealm.commitTransaction();
        } catch (Exception e){
            Log.e(Constants.TAG, LOG_TAG+".insertGroupChat: ",e);
            mRealm.cancelTransaction();
        }
    }

    public GroupChat getGroupChatById(String id)
    {
        try
        {
            RealmQuery<GroupChat> query = mRealm.where(GroupChat.class);
            query.equalTo(Constants.GROUP_CHAT_REALM_ID, id);
            return query.findFirst();
        }
        catch (Exception e )
        {
            Log.e(Constants.TAG, LOG_TAG + ".getGroupChatById: ", e);
            return  null;
        }
    }

    public ArrayList<GroupChat> getAllGroupChats()
    {
        try
        {
            RealmQuery<GroupChat> query = mRealm.where(GroupChat.class);
            query.equalTo(Constants.GROUP_CHAT_REALM_PROFILE_ID, _profile_id);
            RealmResults<GroupChat> result = query.findAll();
            ArrayList<GroupChat> groupChats = new ArrayList<>();

            for(GroupChat g : result)
            {
                groupChats.add(g);
            }
            return groupChats;
        }
        catch (Exception e )
        {
            Log.e(Constants.TAG, LOG_TAG+".getGroupChatById: " , e);
            return  null;
        }
    }

    public String generateComposedMembersId(ArrayList<String> contactsIds)
    {
        String composedId = null;
        for(String id : contactsIds)
        {
            if(null == composedId) composedId = id;
            else composedId = composedId + "@" + id;
        }
        return composedId;
    }

    public ArrayList<ChatMessage> getAllGroupChatMessages(String group_id)
    {
        if(_profile_id==null || group_id==null) return null;

        ArrayList<ChatMessage> chatMessageArray = null;

        try {
            chatMessageArray = new ArrayList<>();
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_GROUP_ID, group_id);

            RealmResults<ChatMessage> result1 = query.findAllSorted("timestamp");

            if (result1 != null) {
                int initialPoint = 0;
                if(result1.size()>50)
                    initialPoint = result1.size()-50;

                for (int i=initialPoint; i<result1.size(); i++) {
                    chatMessageArray.add(result1.get(i));
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.getAllChatMessages: ", e);
        }

        return chatMessageArray;
    }

    public ArrayList<ChatMessage> getNotReadReceivedGroupChatMessages (String groupId){
        //Sets all received messages of a contact as read
        if(groupId==null) return null;

        ArrayList<ChatMessage> messages = null;

        try {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_GROUP_ID, groupId)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_DIRECTION, Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)
                    .notEqualTo(Constants.CHAT_MESSAGE_FIELD_STATUS, Constants.CHAT_MESSAGE_STATUS_READ);

            RealmResults<ChatMessage> results = query.findAll();

            for (int i = 0; i<results.size(); i++)
            {
                if(messages==null) messages = new ArrayList<>();
                messages.add(results.get(i));
            }

        } catch (Exception e){
            e.printStackTrace();
            Log.e(Constants.TAG, "RealmChatTransactions.getNotReadReceivedContactChatMessages: ", e);
            return null;
        }

        return messages;
    }

    public void setGroupChatAllReceivedMessagesAsRead (String groupId){
        //Sets all received messages of a contact as read
        if(groupId==null) return;

        try {
            mRealm.beginTransaction();
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_GROUP_ID, groupId)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_DIRECTION, Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_READ, Constants.CHAT_MESSAGE_NOT_READ);

            RealmResults<ChatMessage> results = query.findAll();

            for (int i = 0; i<results.size(); i++)
                results.get(i).setRead(Constants.CHAT_MESSAGE_READ);

            mRealm.commitTransaction();

        } catch (Exception e){
            e.printStackTrace();
            Log.e(Constants.TAG, "RealmChatTransactions.setContactAllChatMessagesReceivedAsRead: ", e);
            mRealm.cancelTransaction();
        }
    }

    public void closeRealm() {if(mRealm!=null) mRealm.close();}

}
