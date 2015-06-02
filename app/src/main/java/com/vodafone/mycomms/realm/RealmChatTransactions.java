package com.vodafone.mycomms.realm;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.vodafone.mycomms.util.Constants;

import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import model.Chat;
import model.ChatMessage;

/**
 * Created by AMG on 12/05/2015.
 */
public class RealmChatTransactions {
    private Realm mRealm;
    private String _profile_id;

    public RealmChatTransactions(Realm realm, Context context) {

        mRealm = realm;
        _profile_id = null;

        SharedPreferences sp = context.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        if(sp==null) return;

        _profile_id = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);

        //TODO RBM - Remove after testing
        _profile_id = "mc_555a0792121ef1695cc7c1c3";
    }

    /*
     ******** CHAT MESSAGE Transactions *********
     */

    //CONSTRUCTORS

    public ChatMessage newChatMessageInstance(String contact_id, String direction,
                                       int type, String text, String resourceUri)
    {
        long timestamp = Calendar.getInstance().getTimeInMillis();

        ChatMessage chatMessage = new ChatMessage(_profile_id,contact_id,timestamp,
                                    direction,type,text,resourceUri,"0","0");

        return chatMessage;
    }

    //INSERTS

    public void insertChatMessage (ChatMessage newChatMessage){
        if(newChatMessage==null) return;

        try {
            //Insert new chat message
            mRealm.beginTransaction();
            newChatMessage.setProfile_id(_profile_id);
            mRealm.copyToRealmOrUpdate(newChatMessage);

            //Update associated Chat with new last message
            Chat chat = getChatById(newChatMessage.getContact_id());
            chat.setLastMessage_id(newChatMessage.getId());
            mRealm.commitTransaction();
//            insertChat(chat); //Begins and commits the transaction itself

        } catch (IllegalArgumentException e){
            Log.e(Constants.TAG, "RealmChatTransactions.insertChatMessage: ",e);
        }
    }

    public void insertChatMessageList (ArrayList<ChatMessage> chatMessageArrayList){
        if(chatMessageArrayList==null || chatMessageArrayList.size()==0) return;

        int size = chatMessageArrayList.size();
        try {
            mRealm.beginTransaction();
            for (int i = 0; i < size; i++) {
                mRealm.copyToRealmOrUpdate(chatMessageArrayList.get(i));
            }
            mRealm.commitTransaction();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            Log.e(Constants.TAG, "RealmChatTransactions.insertChatMessageList: ", e);
        }
    }

    //GETS

    public ArrayList<ChatMessage> getAllChatMessages(String contact_id)
    {
        if(_profile_id==null || contact_id==null) return null;

        ArrayList<ChatMessage> chatMessageArrayList = null;

        try {
            chatMessageArrayList = new ArrayList<>();
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_CONTACT_ID, contact_id);

            RealmResults<ChatMessage> result1 = query.findAll();
            if (result1 != null) {
                for (ChatMessage chatMessageListItem : result1) {
                    chatMessageArrayList.add(chatMessageListItem);
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.getAllChatMessages: ",e);
        }

        return chatMessageArrayList;
    }

    public ArrayList<ChatMessage> getFilteredChatMessages(String contact_id, String field, String filter)
    {
        if(_profile_id==null || contact_id==null) return null;

        ArrayList<ChatMessage> chatMessageArrayList = null;

        try {
            chatMessageArrayList = new ArrayList<>();
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_CONTACT_ID, contact_id)
                    .equalTo(field, filter);

            RealmResults<ChatMessage> result1 = query.findAll();
            if (result1 != null) {
                for (ChatMessage chatMessageListItem : result1) {
                    chatMessageArrayList.add(chatMessageListItem);
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.getFilteredChatMessages: ",e);
        }

        return chatMessageArrayList;
    }

    public ChatMessage getChatMessageById(String contact_id, long timestamp){
        ChatMessage chatMessage = null;

        try {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            String chatMessageId = _profile_id + "_" + contact_id + "_" + timestamp;
            query.equalTo(Constants.CHAT_MESSAGE_FIELD_ID, chatMessageId);
            chatMessage = query.findFirst();
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.getChatMessageById: ",e);
        }

        return chatMessage;
    }

    public ChatMessage getLastChatMessage(String contact_id){
        if(contact_id==null || _profile_id==null) return null;

        ChatMessage message = null;

        try {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            long timestamp = query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_CONTACT_ID, contact_id)
                    .maximumInt(Constants.CHAT_MESSAGE_FIELD_TIMESTAMP);

            message = getChatMessageById(contact_id, timestamp);

        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.getLastChatMessage: ",e);
        }

        return message;
    }

    //DELETES

    public void deleteChatMessageByFilter(String contact_id, String field, String filter)
    {
        if(_profile_id==null || contact_id==null) return;

        try {
            mRealm.beginTransaction();
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);

            query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_CONTACT_ID, contact_id)
                    .equalTo(field, filter);

            RealmResults<ChatMessage> result1 = query.findAll();
            if (result1 != null) {
                result1.clear();
            }
            mRealm.commitTransaction();
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.deleteChatMessageByFilter: ",e);
        }
    }

    public void deleteChatMessageById(String contact_id, long timestamp) {
        try {
            mRealm.beginTransaction();
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            String chatMessageId = _profile_id + "_" + contact_id + "_" + timestamp;

            query.equalTo(Constants.CHAT_MESSAGE_FIELD_ID, chatMessageId);
            RealmResults<ChatMessage> result1 = query.findAll();
            if (result1 != null) {
                result1.clear();
            }
            mRealm.commitTransaction();
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.deleteChatMessageById: ",e);
        }
    }

    public void deleteAllChatMessages(String contact_id) {
        try {
            mRealm.beginTransaction();
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            RealmResults<ChatMessage> result1 = query.findAll();
            query.equalTo(Constants.CHAT_MESSAGE_FIELD_PROFILE_ID, _profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_CONTACT_ID, contact_id);

            if (result1 != null) {
                result1.clear();
            }
            mRealm.commitTransaction();
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.deleteAllChatMessages: ",e);
        }
    }

    /*
     ******** CHAT Transactions *********
     */

    //CONSTRUCTORS

    public Chat newChatInstance(String contact_id)
    {
        Chat chat = new Chat(_profile_id, contact_id);
        return chat;
    }

    //INSERTS

    public void insertChat (Chat newChat){
        if(newChat==null) return;

        try {
            mRealm.beginTransaction();
            mRealm.copyToRealmOrUpdate(newChat);

            mRealm.commitTransaction();
        } catch (IllegalArgumentException e){
            Log.e(Constants.TAG, "RealmChatTransactions.insertChat: ",e);
        }
    }

    public void insertChatList (ArrayList<Chat> chatArrayList){
        if(chatArrayList==null || chatArrayList.size()==0) return;

        int size = chatArrayList.size();
        try {
            mRealm.beginTransaction();
            for (int i = 0; i < size; i++) {
                mRealm.copyToRealmOrUpdate(chatArrayList.get(i));
            }
            mRealm.commitTransaction();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            Log.e(Constants.TAG, "RealmChatTransactions.insertChatList: ", e);
        }
    }

    //GETS

    public ArrayList<Chat> getAllChats()
    {
        ArrayList<Chat> chatMessageArrayList = null;

        try {
            chatMessageArrayList = new ArrayList<>();
            RealmQuery<Chat> query = mRealm.where(Chat.class);
            query.equalTo(Constants.CHAT_FIELD_PROFILE_ID, _profile_id);
            RealmResults<Chat> result1 = query.findAll();

            if (result1 != null) {
                for (Chat chatMessageListItem : result1) {
                    chatMessageArrayList.add(chatMessageListItem);
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.getAllChats: ",e);
        }

        return chatMessageArrayList;
    }

    public ArrayList<Chat> getFilteredChats(String field, String filter)
    {
        ArrayList<Chat> chatMessageArrayList = null;

        try {
            chatMessageArrayList = new ArrayList<>();
            RealmQuery<Chat> query = mRealm.where(Chat.class);
            query.equalTo(field, filter);

            RealmResults<Chat> result1 = query.findAll();
            if (result1 != null) {
                for (Chat chatMessageListItem : result1) {
                    chatMessageArrayList.add(chatMessageListItem);
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.getFilteredChats: ",e);
        }

        return chatMessageArrayList;
    }

    public Chat getChatById(String contact_id){
        Chat chatMessage = null;

        try {
            RealmQuery<Chat> query = mRealm.where(Chat.class);
            String chatId = _profile_id + "_" + contact_id;
            query.equalTo(Constants.CHAT_FIELD_ID, chatId);
            chatMessage = query.findFirst();
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.getChatById: ",e);
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
                    .equalTo(Constants.CHAT_MESSAGE_FIELD_READ, Constants.CHAT_MESSAGE_NOT_READ)
                    .count();
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.getChatMessagesCount: ",e);
        }

        return count;
    }

    //DELETES

    public void deleteChatByFilter(String field, String filter)
    {
        try {
            mRealm.beginTransaction();
            RealmQuery<Chat> query = mRealm.where(Chat.class);

            query.equalTo(field, filter);

            RealmResults<Chat> result1 = query.findAll();
            if (result1 != null) {
                result1.clear();
            }
            mRealm.commitTransaction();
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.deleteChatByFilter: ",e);
        }
    }

    public void deleteChatById(String contact_id) {
        try {
            mRealm.beginTransaction();
            RealmQuery<Chat> query = mRealm.where(Chat.class);
            String chatId = _profile_id + "_" + contact_id;
            query.equalTo(Constants.CHAT_FIELD_ID, chatId);
            RealmResults<Chat> result1 = query.findAll();
            Chat chat;

            //Delete all associated chat messages
            for(int i=0; i<result1.size(); i++)
            {
                chat = result1.get(i);
                deleteAllChatMessages(chat.getContact_id());
            }

            if (result1 != null) {
                result1.clear();
            }
            mRealm.commitTransaction();
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.deleteChatById: ",e);
        }
    }

    public void deleteAllChats() {
        try {
            mRealm.beginTransaction();
            RealmQuery<Chat> query = mRealm.where(Chat.class);
            RealmResults<Chat> result1 = query.findAll();

            //Delete all associated chat messages
            Chat chat;
            for (int i = 0; i < result1.size(); i++) {
                try {
                    chat = result1.get(i);
                    deleteAllChatMessages(chat.getContact_id());
                } catch (Exception e) {
                    Log.e(Constants.TAG, "RealmChatTransactions.deleteAllChats: ",e);
                }
            }

            //Delete chats
            if (result1 != null) {
                result1.clear();
            }
            mRealm.commitTransaction();
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.deleteAllChats: ",e);
        }
    }
}

