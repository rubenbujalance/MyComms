package com.vodafone.mycomms.realm;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.R;
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
                        , ""
                        , ""
                );

    }

    public GroupChat updatedGroupChatInstance(GroupChat chat, ChatMessage chatMsg) {
        GroupChat updatedChat = new GroupChat(chat.getId(), chat.getProfileId(),
                chat.getCreatorId(), chat.getName(), chat.getAvatar(), chat.getAbout(),
                chat.getMembers(), chat.getOwners(), chatMsg.getTimestamp(),
                chatMsg.getText(), chatMsg.getId());

        return updatedChat;
    }

    public ChatMessage newGroupChatMessageInstance(String group_id, String contact_id,
                                                   String direction, int type, String text,
                                                   String resourceUri)
    {
        long timestamp = Calendar.getInstance().getTimeInMillis();

        ChatMessage chatMessage = new ChatMessage(_profile_id,contact_id,group_id,timestamp,
                direction,type,text,resourceUri,Constants.CHAT_MESSAGE_NOT_READ,
                Constants.CHAT_MESSAGE_STATUS_NOT_SENT);

        return chatMessage;
    }

    public ChatMessage newGroupChatMessageInstance(String group_id, String contact_id,
                                                   String direction, int type, String text,
                                                   String resourceUri, String id, long timestamp)
    {
        ChatMessage chatMessage = new ChatMessage(_profile_id,contact_id,group_id,timestamp,
                direction,type,text,resourceUri,Constants.CHAT_MESSAGE_NOT_READ,
                Constants.CHAT_MESSAGE_STATUS_NOT_SENT, id);

        return chatMessage;
    }

    public void insertOrUpdateGroupChat (GroupChat newChat)
    {
        if(newChat==null) return;
        try {
            GroupChat TEST = newChat;
            Log.i(Constants.TAG, "RealmGroupChatTransactions.insertOrUpdateGroupChat: newChatGroup " + newChat.getId());
            mRealm.beginTransaction();
            mRealm.copyToRealmOrUpdate(newChat);

            mRealm.commitTransaction();
        } catch (Exception e){
            Log.e(Constants.TAG, "RealmGroupChatTransactions.insertOrUpdateGroupChat: ", e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
    }

    public boolean insertGroupChatMessage (String groupId, ChatMessage newChatMessage){
        if(newChatMessage==null) return false;

        try {
            //Insert new chat message
            mRealm.beginTransaction();
            newChatMessage.setProfile_id(_profile_id);
            mRealm.copyToRealmOrUpdate(newChatMessage);

            //Update associated Chat with new last message
            GroupChat chat = getGroupChatById(groupId);

            if(chat!=null) {
                chat.setLastMessage_id(newChatMessage.getId());

                String lastText;
                if (newChatMessage.getType() == Constants.CHAT_MESSAGE_TYPE_TEXT)
                    lastText = newChatMessage.getText();
                else lastText = mContext.getString(R.string.image);

                if (newChatMessage.getDirection().equals(Constants.CHAT_MESSAGE_DIRECTION_SENT))
                    chat.setLastMessage(mContext.getResources().getString(R.string.chat_me_text) + lastText);
                else chat.setLastMessage(lastText);

                chat.setLastMessageTime(newChatMessage.getTimestamp());
            }

            mRealm.commitTransaction();

        } catch (Exception e){
            Log.e(Constants.TAG, "RealmChatTransactions.insertChatMessage: ", e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
            return false;
        }

        return true;
    }

    public GroupChat getGroupChatById(String id)
    {
        try
        {
            String test = id;
            RealmQuery<GroupChat> query = mRealm.where(GroupChat.class);
            query.equalTo(Constants.GROUP_CHAT_REALM_ID, id);
            return query.findFirst();
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "RealmGroupChatTransactions.getGroupChatById: ", e);
            Crashlytics.logException(e);
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
        catch (Exception e ) {
            Log.e(Constants.TAG, "RealmGroupChatTransactions.getAllGroupChats: ", e);
            Crashlytics.logException(e);
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
            Log.e(Constants.TAG, "RealmGroupChatTransactions.getAllGroupChatMessages: ", e);
            Crashlytics.logException(e);
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
            Log.e(Constants.TAG, "RealmGroupChatTransactions.getNotReadReceivedGroupChatMessages: ", e);
            Crashlytics.logException(e);
            return null;
        }

        return messages;
    }

    public ChatMessage getGroupChatMessageById(String groupId){
        ChatMessage chatMessage = null;

        try {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            query.equalTo(Constants.CHAT_MESSAGE_FIELD_ID, groupId);
            chatMessage = query.findFirst();
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmGroupChatTransactions.getGroupChatMessageById: ", e);
            Crashlytics.logException(e);
        }

        return chatMessage;
    }

    public boolean existsChatMessageById(String id){
        boolean exists = false;

        try {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            query.equalTo(Constants.CHAT_MESSAGE_FIELD_ID, id);
            long count = query.count();
            if(count>0) exists = true;

        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmGroupChatTransactions.existsChatMessageById: ", e);
            Crashlytics.logException(e);
            return false;
        }

        return exists;
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
            Log.e(Constants.TAG, "RealmGroupChatTransactions.setGroupChatAllReceivedMessagesAsRead: ", e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
    }

    public long getGroupChatPendingMessagesCount(String groupId){
        if(groupId==null || _profile_id==null) return 0;

        long count = 0;

        try {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            count = query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_GROUP_ID, groupId)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_READ, Constants.CHAT_MESSAGE_NOT_READ)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_DIRECTION, Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)
                    .count();
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmGroupChatTransactions.getGroupChatPendingMessagesCount: ", e);
            Crashlytics.logException(e);
        }

        return count;
    }

    public long getAllChatPendingMessagesCount(){
        if(_profile_id==null) return 0;

        long count = 0;

        try {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            count = query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_READ, Constants.CHAT_MESSAGE_NOT_READ)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_DIRECTION, Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)
                    .count();
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmGroupChatTransactions.getAllChatPendingMessagesCount: ", e);
        }

        return count;
    }

    public void closeRealm() {if(mRealm!=null) mRealm.close();}

}
