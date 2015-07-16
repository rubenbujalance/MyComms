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
import java.util.Iterator;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import model.Chat;
import model.ChatMessage;

public class RealmChatTransactions {
    private Realm mRealm;
    private String _profile_id;
    private Context mContext;
    private RealmContactTransactions contactTx;

    public RealmChatTransactions(Context context) {

        mContext = context;
        mRealm = Realm.getDefaultInstance();

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
                                              int type, String text, String resourceUri)
    {
        long timestamp = Calendar.getInstance().getTimeInMillis();

        ChatMessage chatMessage = new ChatMessage(_profile_id,contact_id,"",timestamp,
                direction,type,text,resourceUri,Constants.CHAT_MESSAGE_NOT_READ,
                Constants.CHAT_MESSAGE_STATUS_NOT_SENT);

        return chatMessage;
    }

    public ChatMessage newChatMessageInstance(String contact_id, String direction, int type,
                                              String text, String resourceUri, String id)
    {
        long timestamp = Calendar.getInstance().getTimeInMillis();

        ChatMessage chatMessage = new ChatMessage(_profile_id,contact_id,"",timestamp,
                direction,type,text,resourceUri,Constants.CHAT_MESSAGE_NOT_READ,
                Constants.CHAT_MESSAGE_STATUS_NOT_SENT, id);

        return chatMessage;
    }

    public ChatMessage newChatMessageInstance(String contact_id, String direction, int type,
                                              String text, String resourceUri, String id,
                                              long timestamp)
    {
        ChatMessage chatMessage = new ChatMessage(_profile_id,contact_id,"",timestamp,
                direction,type,text,resourceUri,Constants.CHAT_MESSAGE_NOT_READ,
                Constants.CHAT_MESSAGE_STATUS_NOT_SENT, id);

        return chatMessage;
    }

    //INSERTS

    public boolean insertChatMessage (ChatMessage newChatMessage){
        if(newChatMessage==null) return false;

        try {
            //Insert new chat message
            mRealm.beginTransaction();
            newChatMessage.setProfile_id(_profile_id);
            mRealm.copyToRealmOrUpdate(newChatMessage);

            //Update associated Chat with new last message
            Chat chat = getChatByContactId(newChatMessage.getContact_id());
            chat.setLastMessage_id(newChatMessage.getId());

            String lastText;
            if(newChatMessage.getType()==Constants.CHAT_MESSAGE_TYPE_TEXT)
                lastText = newChatMessage.getText();
            else lastText = mContext.getString(R.string.image);

            if (newChatMessage.getDirection().equals(Constants.CHAT_MESSAGE_DIRECTION_SENT))
                chat.setLastMessage(mContext.getResources().getString(R.string.chat_me_text) + lastText);
            else chat.setLastMessage(lastText);

            chat.setLastMessageTime(newChatMessage.getTimestamp());

        } catch (Exception e){
            Log.e(Constants.TAG, "RealmChatTransactions.insertChatMessage: ", e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
            return false;
        } finally {
            mRealm.commitTransaction();
        }

        return true;
    }

    public void setChatMessageReceivedAsRead (ChatMessage chatMessage){
        //Sets a received message as read
        if(chatMessage==null) return;

        try {
            mRealm.beginTransaction();
            chatMessage.setRead(Constants.CHAT_MESSAGE_READ);
        } catch (Exception e){
            Log.e(Constants.TAG, "RealmChatTransactions.setChatMessageAsRead: ", e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        } finally {
            mRealm.commitTransaction();
        }
    }

    public void setContactAllChatMessagesReceivedAsRead (String contactId){
        //Sets all received messages of a contact as read
        if(contactId==null) return;

        try {
            mRealm.beginTransaction();
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_CONTACT_ID, contactId)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_GROUP_ID, "")
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_DIRECTION, Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_READ, Constants.CHAT_MESSAGE_NOT_READ);

            RealmResults<ChatMessage> results = query.findAll();

            Iterator<ChatMessage> it = results.iterator();
            ChatMessage msg;

            while(it.hasNext()) {
                msg = it.next();
                msg.setRead(Constants.CHAT_MESSAGE_READ);
            }


        } catch (Exception e){
            Log.e(Constants.TAG, "RealmChatTransactions.setContactAllChatMessagesReceivedAsRead: ", e);
            mRealm.cancelTransaction();
        } finally {
            mRealm.commitTransaction();
        }
    }

    public boolean setChatMessageSentStatus (String id, String status){
        //Sets a received message as read
        if(id==null || status==null) return false;
        boolean changed = false;

        try {
            mRealm.beginTransaction();
            //Update associated Chat with new last message
            ChatMessage chatMessage = getChatMessageById(id);
            if(chatMessage==null) return false;

            if(XMPPTransactions.getXMPPStatusOrder(chatMessage.getStatus()) <
                    XMPPTransactions.getXMPPStatusOrder(status)) {
                chatMessage.setStatus(status);
                changed = true;
            }

        } catch (Exception e){
            Log.e(Constants.TAG, "RealmChatTransactions.setChatMessageSentStatus: ", e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
            changed = false;
        } finally {
            mRealm.commitTransaction();
        }

        return changed;
    }

    //GETS

    public ArrayList<ChatMessage> getAllChatMessages(String contact_id)
    {
        if(_profile_id==null || contact_id==null) return null;

        ArrayList<ChatMessage> chatMessageArray = null;

        try {
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
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.getAllChatMessages: ", e);
            Crashlytics.logException(e);
        }

        return chatMessageArray;
    }

    public ChatMessage getChatMessageById(String id){
        ChatMessage chatMessage = null;

        try {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            query.equalTo(Constants.CHAT_MESSAGE_FIELD_ID, id);
            chatMessage = query.findFirst();
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.getChatMessageById: ", e);
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
            Log.e(Constants.TAG, "RealmChatTransactions.existsChatMessageById: ", e);
            Crashlytics.logException(e);
            return false;
        }

        return exists;
    }

    public ArrayList<ChatMessage> getNotReadReceivedContactChatMessages (String contactId){
        //Sets all received messages of a contact as read
        if(contactId==null) return null;

        ArrayList<ChatMessage> messages = null;

        try {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_CONTACT_ID, contactId)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_GROUP_ID, "")
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_DIRECTION, Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)
                    .notEqualTo(Constants.CHAT_MESSAGE_FIELD_STATUS, Constants.CHAT_MESSAGE_STATUS_READ);

            RealmResults<ChatMessage> results = query.findAll();

            for (int i = 0; i<results.size(); i++)
            {
                if(messages==null) messages = new ArrayList<>();
                messages.add(results.get(i));
            }

        } catch (Exception e){
            Log.e(Constants.TAG, "RealmChatTransactions.getNotReadReceivedContactChatMessages: ", e);
            Crashlytics.logException(e);
            return null;
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

    public void insertChat (Chat newChat){
        if(newChat==null) return;
        try {
            mRealm.beginTransaction();
            mRealm.copyToRealmOrUpdate(newChat);

        } catch (Exception e){
            Log.e(Constants.TAG, "RealmChatTransactions.insertChat: ",e);
            mRealm.cancelTransaction();
        } finally {
            mRealm.commitTransaction();
        }
    }

    //GETS

    public ArrayList<Chat> getAllChatsFromExistingContacts()
    {
        ArrayList<Chat> chatMessageArrayList = null;

        try {
            chatMessageArrayList = new ArrayList<>();
            RealmQuery<Chat> query = mRealm.where(Chat.class);
            query.equalTo(Constants.CHAT_FIELD_PROFILE_ID, _profile_id);
            RealmResults<Chat> result1 = query.findAll();

            if (result1 != null) {
                result1.sort(Constants.CHAT_FIELD_LAST_MESSAGE_TIME, RealmResults.SORT_ORDER_DESCENDING);
                for (Chat chatMessageListItem : result1) {
                    if(contactTx.getContactById(chatMessageListItem.getContact_id())!=null)
                        chatMessageArrayList.add(chatMessageListItem);
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.getAllChatsFromExistingContacts: ", e);
        }

        return chatMessageArrayList;
    }

    public Chat getChatByContactId(String contact_id){
        Chat chatMessage = null;

        try {
            RealmQuery<Chat> query = mRealm.where(Chat.class);
            query.equalTo(Constants.CHAT_FIELD_PROFILE_ID, _profile_id);
            query.equalTo(Constants.CHAT_FIELD_CONTACT_ID, contact_id);
            chatMessage = query.findFirst();
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.getChatById: ", e);
        }

        return chatMessage;
    }

    public long getChatPendingMessagesCount(String contact_id){
        if(contact_id==null || _profile_id==null) return 0;

        long count = 0;

        try {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            count = query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_CONTACT_ID, contact_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_GROUP_ID, "")
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_READ, Constants.CHAT_MESSAGE_NOT_READ)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_DIRECTION, Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)
                    .count();
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.getChatMessagesCount: ",e);
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
            Log.e(Constants.TAG, "RealmChatTransactions.getAllChatPendingMessagesCount: ",e);
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

    public void closeRealm() {
        if(mRealm!=null) mRealm.close();
        contactTx.closeRealm();
    }

}
