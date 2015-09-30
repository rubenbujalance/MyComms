package com.vodafone.mycomms.realm;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import model.Chat;
import model.ChatMessage;
import model.GroupChat;

public class RealmChatTransactions
{
    private static String _profile_id;
    private Context mContext;
    private RealmContactTransactions contactTx;

    public static RealmChatTransactions getInstance(Context context) {
        return new RealmChatTransactions(context);
    }

    public RealmChatTransactions(Context context) {

        mContext = context;
        _profile_id = null;
        SharedPreferences sp = context.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        if(sp==null) return;

        _profile_id = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);
        contactTx = new RealmContactTransactions(_profile_id);
    }

    /*
     ******** CHAT MESSAGE Transactions *********
     */

    //CONSTRUCTORS

    public ChatMessage newChatMessageInstance(String contact_id, String direction,
                                              int type, String text, String resourceUri,
                                              String status, String read)
    {
        long timestamp = Calendar.getInstance().getTimeInMillis();

        ChatMessage chatMessage = new ChatMessage(_profile_id,contact_id,"",timestamp,
                direction,type,text,resourceUri,read,
                status);

        return chatMessage;
    }

    public ChatMessage newChatMessageInstance(String contact_id, String direction, int type,
                                              String text, String resourceUri, String id,
                                              long timestamp, String status, String read)
    {
        ChatMessage chatMessage = new ChatMessage(_profile_id,contact_id,"",timestamp,
            direction,type,text,resourceUri,read,
                status, id);

        return chatMessage;
    }

    //INSERTS

    public boolean insertChatMessage (ChatMessage newChatMessage, Realm realm)
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
            Chat chat = getChatByContactId(newChatMessage.getContact_id(), mRealm);
            chat.setLastMessage_id(newChatMessage.getId());

            String lastText;
            if(newChatMessage.getType()==Constants.CHAT_MESSAGE_TYPE_TEXT)
                lastText = newChatMessage.getText();
            else lastText = mContext.getString(R.string.image);

            if (newChatMessage.getDirection().equals(Constants.CHAT_MESSAGE_DIRECTION_SENT))
                chat.setLastMessage(mContext.getResources().getString(R.string.chat_me_text) + lastText);
            else chat.setLastMessage(lastText);

            chat.setLastMessageTime(newChatMessage.getTimestamp());
            mRealm.commitTransaction();

            return true;
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmChatTransactions.insertChatMessage: ",e);
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

    public void setChatMessageReceivedAsRead (ChatMessage chatMessage, Realm realm)
    {
        //Sets a received message as read
        if(chatMessage==null) return;

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            mRealm.beginTransaction();
            chatMessage.setRead(Constants.CHAT_MESSAGE_READ);
            mRealm.commitTransaction();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmChatTransactions.setChatMessageReceivedAsRead: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public void setContactAllChatMessagesReceivedAsRead (String contactId, Realm realm)
    {
        //Sets all received messages of a contact as read
        if(contactId==null) return;

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
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_CONTACT_ID, contactId)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_GROUP_ID, "")
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
            Log.e(Constants.TAG, "RealmChatTransactions.setContactAllChatMessagesReceivedAsRead: ", e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public boolean setChatMessageStatus (String id, String status, Realm realm){
        //Sets a received message as read
        if(id==null || status==null) return false;
        boolean changed = false;

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            mRealm.beginTransaction();
            //Update associated Chat with new last message
            ChatMessage chatMessage = getChatMessageById(id, mRealm);
            if(chatMessage==null) return false;

            if(chatMessage.getDirection().compareTo(Constants.CHAT_MESSAGE_DIRECTION_SENT)==0) {
                if (XMPPTransactions.getXMPPStatusOrder(chatMessage.getStatus()) <
                        XMPPTransactions.getXMPPStatusOrder(status)) {
                    chatMessage.setStatus(status);
                    changed = true;
                }
            }
            else if (XMPPTransactions.getXMPPStatusOrder(status)==
                    XMPPTransactions.getXMPPStatusOrder(Constants.CHAT_MESSAGE_STATUS_READ)){
                chatMessage.setRead(Constants.CHAT_MESSAGE_READ);
                changed = true;
            }
            mRealm.commitTransaction();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmChatTransactions.setChatMessageSentStatus: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
            changed = false;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
        return changed;
    }

    //GETS

    public ArrayList<ChatMessage> getAllChatMessages(String contact_id, Realm realm)
    {
        if(_profile_id==null || contact_id==null) return null;

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
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_GROUP_ID, "")
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_CONTACT_ID, contact_id);

            RealmResults<ChatMessage> result1 = query.findAllSorted("timestamp");

            if (result1 != null) {
                int initialPoint = 0;
                if(result1.size()>50)
                    initialPoint = result1.size()-50;

                for (int i=initialPoint; i<result1.size(); i++) {
                    chatMessageArray.add(result1.get(i));
                }
            }
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmChatTransactions.getAllChatMessages: ",e);
            Crashlytics.logException(e);
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
        return chatMessageArray;
    }

    public ChatMessage getChatMessageById(String id, Realm realm){
        return getChatMessageById(id, false, realm);
    }

    public ChatMessage getChatMessageById(String id, boolean useProfile, Realm realm){
        ChatMessage chatMessage = null;

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            if(useProfile)
                query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id);
            query.equalTo(Constants.CHAT_MESSAGE_FIELD_ID, id);
            chatMessage = query.findFirst();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmChatTransactions.getChatMessageById: ",e);
            Crashlytics.logException(e);
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }

        return chatMessage;
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
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmChatTransactions.existsChatMessageById: ",e);
            Crashlytics.logException(e);
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
        return exists;
    }

    public ArrayList<ChatMessage> getNotReadReceivedContactChatMessages (String contactId, Realm
            realm)
    {
        //Sets all received messages of a contact as read
        if(contactId==null) return null;

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
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_CONTACT_ID, contactId)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_GROUP_ID, "")
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_DIRECTION, Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_READ, Constants.CHAT_MESSAGE_NOT_READ);

            RealmResults<ChatMessage> results = query.findAll();

            for (int i = 0; i<results.size(); i++)
            {
                if(messages==null) messages = new ArrayList<>();
                messages.add(results.get(i));
            }
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmChatTransactions.getNotReadReceivedContactChatMessages: ",e);
            Crashlytics.logException(e);
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
        return messages;
    }

    //DELETES

//    public void deleteChatMessageById(String id) {
//        try {
//            mRealm.beginTransaction();
//            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
//
//            query.equalTo(Constants.CHAT_MESSAGE_FIELD_ID, id);
//            RealmResults<ChatMessage> result1 = query.findAll();
//            if (result1 != null) {
//                result1.clear();
//            }
//            mRealm.commitTransaction();
//        } catch (Exception e) {
//            Log.e(Constants.TAG, "RealmChatTransactions.deleteChatMessageById: ",e);
//            mRealm.cancelTransaction();
//        }
//    }

//    public void deleteAllChatMessages(String contact_id) {
//        try {
//            mRealm.beginTransaction();
//            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
//            RealmResults<ChatMessage> result1 = query.findAll();
//            query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
//                    .equalTo(Constants.CHAT_MESSAGE_FIELD_CONTACT_ID, contact_id);
//
//            if (result1 != null) {
//                result1.clear();
//            }
//            mRealm.commitTransaction();
//        } catch (Exception e) {
//            Log.e(Constants.TAG, "RealmChatTransactions.deleteAllChatMessages: ",e);
//            mRealm.cancelTransaction();
//        }
//    }

    /*
     ******** CHAT Transactions *********
     */

    //CONSTRUCTORS

    public Chat newChatInstance(String contact_id)
    {
        Chat chat = new Chat(_profile_id, contact_id, "","",0);
        return chat;
    }

    public Chat updatedChatInstance(Chat chat, ChatMessage chatMsg) {
        Chat updatedChat = new Chat(_profile_id, chat.getContact_id(),
                chatMsg.getId(), chatMsg.getText(), chatMsg.getTimestamp());
        return updatedChat;
    }

    //INSERTS

    public void insertChat (Chat newChat, Realm realm){
        if(newChat==null) return;

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            mRealm.beginTransaction();
            mRealm.copyToRealmOrUpdate(newChat);
            mRealm.commitTransaction();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmChatTransactions.insertChat: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    //GETS

    public ArrayList<Chat> getAllChatsFromExistingContacts(Realm realm)
    {
        ArrayList<Chat> chatMessageArrayList = null;

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            chatMessageArrayList = new ArrayList<>();
            RealmQuery<Chat> query = mRealm.where(Chat.class);
            query.equalTo(Constants.CHAT_FIELD_PROFILE_ID, _profile_id);
            RealmResults<Chat> result1 = query.findAll();

            if (result1 != null) {
                result1.sort(Constants.CHAT_FIELD_LAST_MESSAGE_TIME, RealmResults.SORT_ORDER_DESCENDING);
                for (Chat chatMessageListItem : result1) {
                    if(contactTx.getContactById(chatMessageListItem.getContact_id(), mRealm)!=null)
                        chatMessageArrayList.add(chatMessageListItem);
                }
            }
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmChatTransactions.getAllChatsFromExistingContacts: ",e);
            Crashlytics.logException(e);
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
        return chatMessageArrayList;
    }

    public Chat getChatByContactId(String contact_id, Realm realm){
        Chat chatMessage = null;

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            RealmQuery<Chat> query = mRealm.where(Chat.class);
            query.equalTo(Constants.CHAT_FIELD_PROFILE_ID, _profile_id);
            query.equalTo(Constants.CHAT_FIELD_CONTACT_ID, contact_id);
            chatMessage = query.findFirst();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmChatTransactions.getChatByContactId: ",e);
            Crashlytics.logException(e);
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }

        return chatMessage;
    }

    public static long getChatPendingMessagesCount(String contact_id, Realm realm)
    {
        if(contact_id==null || _profile_id==null) return 0;

        long count = 0;

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            count = query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_CONTACT_ID, contact_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_GROUP_ID, "")
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_READ, Constants.CHAT_MESSAGE_NOT_READ)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_DIRECTION, Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)
                    .count();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmChatTransactions.getChatPendingMessagesCount: ", e);
            Crashlytics.logException(e);
        }
        finally
        {
            if(null == realm && null != mRealm)
                mRealm.close();
        }

        return count;
    }

    public long getAllChatPendingMessagesCount(Realm realm){
        if(_profile_id==null) return 0;

        long count = 0;

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            count = count + getSingleChatPendingMessagesCount(realm);
            count = count + getExistentGroupChatPendingMessagesCount(realm);
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmChatTransactions.getAllChatPendingMessagesCount: ",e);
            Crashlytics.logException(e);
        }
        finally
        {
            if(null == realm && null != mRealm)
                mRealm.close();
        }

        return count;
    }

    public long getSingleChatPendingMessagesCount(Realm realm){
        if(_profile_id==null) return 0;

        long count = 0;

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            count = count + query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_READ, Constants.CHAT_MESSAGE_NOT_READ)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_DIRECTION, Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_GROUP_ID, "")
                    .count();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmChatTransactions.getSingleChatPendingMessagesCount: ",e);
            Crashlytics.logException(e);
        }
        finally
        {
            if(null == realm && null != mRealm)
                mRealm.close();
        }

        return count;
    }

    public long getExistentGroupChatPendingMessagesCount(Realm realm){
        if(_profile_id==null) return 0;

        long count = 0;

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            RealmQuery<GroupChat> queryGroupChats = mRealm.where(GroupChat.class);
            queryGroupChats.equalTo(Constants.GROUP_CHAT_REALM_PROFILE_ID, _profile_id);
            RealmResults<GroupChat> result1 = queryGroupChats.findAll();

            for(GroupChat g : result1)
            {
                RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
                count = count + query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
                        .equalTo(Constants.CHAT_MESSAGE_FIELD_READ, Constants.CHAT_MESSAGE_NOT_READ)
                        .equalTo(Constants.CHAT_MESSAGE_FIELD_DIRECTION, Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)
                        .equalTo(Constants.CHAT_MESSAGE_FIELD_GROUP_ID, g.getId())
                        .count();
            }
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmChatTransactions.getExistentGroupChatPendingMessagesCount: ",e);
            Crashlytics.logException(e);
        }
        finally
        {
            if(null == realm && null != mRealm)
                mRealm.close();
        }

        return count;
    }

    //DELETES

//    public void deleteChatByFilter(String field, String filter)
//    {
//        try {
//            mRealm.beginTransaction();
//            RealmQuery<Chat> query = mRealm.where(Chat.class);
//
//            query.equalTo(field, filter);
//
//            RealmResults<Chat> result1 = query.findAll();
//            if (result1 != null) {
//                result1.clear();
//            }
//            mRealm.commitTransaction();
//        } catch (Exception e) {
//            Log.e(Constants.TAG, "RealmChatTransactions.deleteChatByFilter: ",e);
//            mRealm.cancelTransaction();
//        }
//    }

//    public void deleteChatById(String id) {
//        try {
//            mRealm.beginTransaction();
//            RealmQuery<Chat> query = mRealm.where(Chat.class);
//            query.equalTo(Constants.CHAT_FIELD_ID, id);
//            RealmResults<Chat> result1 = query.findAll();
//            Chat chat;
//
//            //Delete all associated chat messages
//            for(int i=0; i<result1.size(); i++)
//            {
//                chat = result1.get(i);
//                deleteAllChatMessages(chat.getContact_id());
//            }
//
//            if (result1 != null) {
//                result1.clear();
//            }
//            mRealm.commitTransaction();
//        } catch (Exception e) {
//            Log.e(Constants.TAG, "RealmChatTransactions.deleteChatById: ",e);
//            mRealm.cancelTransaction();
//        }
//    }

//    public void deleteAllChats() {
//        try {
//            mRealm.beginTransaction();
//            RealmQuery<Chat> query = mRealm.where(Chat.class);
//            RealmResults<Chat> result1 = query.findAll();
//
//            //Delete all associated chat messages
//            Chat chat;
//            for (int i = 0; i < result1.size(); i++) {
//                try {
//                    chat = result1.get(i);
//                    deleteAllChatMessages(chat.getContact_id());
//                } catch (Exception e) {
//                    Log.e(Constants.TAG, "RealmChatTransactions.deleteAllChats: ",e);
//                    mRealm.cancelTransaction();
//                }
//            }
//
//            //Delete chats
//            if (result1 != null) {
//                result1.clear();
//            }
//            mRealm.commitTransaction();
//        } catch (Exception e) {
//            Log.e(Constants.TAG, "RealmChatTransactions.deleteAllChats: ",e);
//            mRealm.cancelTransaction();
//        }
//    }

}
