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
public class RealmGroupChatTransactions
{
    private String _profile_id;
    private Context mContext;
    private String LOG_TAG = RealmGroupChatTransactions.class.getSimpleName();

    public RealmGroupChatTransactions(Context context, String profile_id)
    {
        mContext = context;
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
                                                   String resourceUri, String status, String read)
    {
        long timestamp = Calendar.getInstance().getTimeInMillis();

        ChatMessage chatMessage = new ChatMessage(_profile_id,contact_id,group_id,timestamp,
                direction,type,text,resourceUri,read,status);

        return chatMessage;
    }

    public ChatMessage newGroupChatMessageInstance(String group_id, String contact_id,
                                                   String direction, int type, String text,
                                                   String resourceUri, String id, long timestamp,
                                                   String status, String read)
    {
        ChatMessage chatMessage = new ChatMessage(_profile_id,contact_id,group_id,timestamp,
                direction,type,text,resourceUri,read,status, id);

        return chatMessage;
    }

    public void insertOrUpdateGroupChat (GroupChat newChat, Realm realm)
    {
        if(newChat==null) return;

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            GroupChat TEST = newChat;
            Log.i(Constants.TAG, "RealmGroupChatTransactions.insertOrUpdateGroupChat: newChatGroup " + newChat.getId());
            mRealm.beginTransaction();
            mRealm.copyToRealmOrUpdate(newChat);
            mRealm.commitTransaction();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmGroupChatTransactions.insertOrUpdateGroupChat: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public void updateGroupChatInstance(GroupChat groupChat, String name,String avatar, String
            about, String members, long lastMessageTime, String lastMessage, String
            lastMessage_id,String owners, Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            mRealm.beginTransaction();
            if(null != name)
                groupChat.setName(name);
            if(null != avatar)
                groupChat.setAvatar(avatar);
            if(null != about)
                groupChat.setAbout(about);
            if(null != members)
                groupChat.setMembers(members);
            if(0 != lastMessageTime)
                groupChat.setLastMessageTime(lastMessageTime);
            if(null != lastMessage)
                groupChat.setLastMessage(lastMessage);
            if(null != lastMessage_id)
                groupChat.setLastMessage_id(lastMessage_id);
            if(null != owners)
                groupChat.setOwners(owners);
            mRealm.commitTransaction();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmGroupChatTransactions.updateGroupChatInstance: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public void updateGroupChatInstance(GroupChat groupChat, String members, String owners, Realm
            realm)
    {
        updateGroupChatInstance(groupChat, null, null, null, members, 0, null, null, owners, realm);
    }

    public boolean insertGroupChatMessage (String groupId, ChatMessage newChatMessage, Realm realm)
    {
        if(newChatMessage==null) return false;

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            //Insert new chat message
            mRealm.beginTransaction();
            newChatMessage.setProfile_id(_profile_id);
            mRealm.copyToRealmOrUpdate(newChatMessage);

            //Update associated Chat with new last message
            GroupChat chat = getGroupChatById(groupId, mRealm);

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

            return true;
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmGroupChatTransactions.insertGroupChatMessage: ", e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
            return false;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public GroupChat getGroupChatById(String id, Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            String test = id;
            RealmQuery<GroupChat> query = mRealm.where(GroupChat.class);
            query.equalTo(Constants.GROUP_CHAT_REALM_ID, id);
            return query.findFirst();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmGroupChatTransactions.getGroupChatById: ", e);
            Crashlytics.logException(e);
            return null;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public ArrayList<GroupChat> getAllGroupChats(Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
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
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmGroupChatTransactions.getAllGroupChats: ", e);
            Crashlytics.logException(e);
            return null;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
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

    public ArrayList<ChatMessage> getAllGroupChatMessages(String group_id, Realm realm)
    {
        if(_profile_id==null || group_id==null) return null;

        ArrayList<ChatMessage> chatMessageArray = null;

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
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

            return chatMessageArray;
        }
        catch(Exception e) 
        {
            Log.e(Constants.TAG, "RealmGroupChatTransactions.getAllGroupChatMessages: ",e);
            Crashlytics.logException(e);
            return null;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public ArrayList<ChatMessage> getNotReadReceivedGroupChatMessages (String groupId, Realm realm){
        //Sets all received messages of a contact as read
        if(groupId==null) return null;

        ArrayList<ChatMessage> messages = null;
        
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_GROUP_ID, groupId)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_DIRECTION, Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_READ, Constants.CHAT_MESSAGE_NOT_READ);

            RealmResults<ChatMessage> results = query.findAll();

            for (int i = 0; i<results.size(); i++)
            {
                if(messages==null) messages = new ArrayList<>();
                messages.add(results.get(i));
            }
            return messages;
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmGroupChatTransactions.getNotReadReceivedGroupChatMessages: ",e);
            Crashlytics.logException(e);
            return null;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public ChatMessage getGroupChatMessageById(String chatMessageId, Realm realm){
        ChatMessage chatMessage = null;

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            query.equalTo(Constants.CHAT_MESSAGE_FIELD_ID, chatMessageId);
            chatMessage = query.findFirst();
            return chatMessage;
        }
        catch(Exception e) {
            Log.e(Constants.TAG, "RealmGroupChatTransactions.getGroupChatMessageById: ",e);
            Crashlytics.logException(e);
            return null;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public boolean existsChatMessageById(String id, Realm realm)
    {
        boolean exists = false;

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            query.equalTo(Constants.CHAT_MESSAGE_FIELD_ID, id);
            long count = query.count();
            if(count>0) exists = true;
            return exists;
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmGroupChatTransactions.existsChatMessageById: ",e);
            Crashlytics.logException(e);
            return false;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public void setGroupChatAllReceivedMessagesAsRead (String groupId, Realm realm)
    {
        //Sets all received messages of a contact as read
        if(groupId==null) return;

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            mRealm.beginTransaction();
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_GROUP_ID, groupId)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_DIRECTION, Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_READ, Constants.CHAT_MESSAGE_NOT_READ);

            RealmResults<ChatMessage> results = query.findAll();
            int size = results.size();

            //Seteamos siempre el elemento 0 porque Realm recalcula la lista a cada SET
            for(int i = 0; i < size; i++)
            {
                results.get(0).setRead(Constants.CHAT_MESSAGE_READ);
            }
            mRealm.commitTransaction();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmGroupChatTransactions.setGroupChatAllReceivedMessagesAsRead: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public long getGroupChatPendingMessagesCount(String groupId, Realm realm)
    {
        if(groupId==null || _profile_id==null) return 0;

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            long count = query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_GROUP_ID, groupId)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_READ, Constants.CHAT_MESSAGE_NOT_READ)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_DIRECTION, Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)
                    .count();
            return count;
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmGroupChatTransactions.getGroupChatPendingMessagesCount: ",e);
            Crashlytics.logException(e);
            return 0;
        }
        finally
        {
            if(null == realm && null != mRealm)
                mRealm.close();
        }
    }

    public long getAllChatPendingMessagesCount(Realm realm)
    {
        if(_profile_id==null) return 0;

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            long count = query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_READ, Constants.CHAT_MESSAGE_NOT_READ)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_DIRECTION, Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)
                    .count();
            return count;
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmGroupChatTransactions.getAllChatPendingMessagesCount: ",e);
            Crashlytics.logException(e);
            return 0;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }
}
