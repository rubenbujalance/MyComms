package com.vodafone.mycomms.realm;

import android.util.Log;

import com.vodafone.mycomms.util.Constants;

import java.util.ArrayList;

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

    public RealmChatTransactions(Realm realm) {
        mRealm = realm;
    }

    /*
     ******** CHAT MESSAGE Transactions *********
     */

    //INSERTS

    public void insertChatMessage (ChatMessage newChatMessage){
        if(newChatMessage==null) return;

        try {
            mRealm.beginTransaction();
            mRealm.copyToRealmOrUpdate(newChatMessage);

            mRealm.commitTransaction();
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

    public ArrayList<ChatMessage> getAllChatMessages(String profile_id, String contact_id)
    {
        if(profile_id==null || contact_id==null) return null;

        ArrayList<ChatMessage> chatMessageArrayList = null;

        try {
            chatMessageArrayList = new ArrayList<>();
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            query.equalTo(Constants.CHAT_MESSAGE_PROFILE_ID, profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_CONTACT_ID, contact_id);

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

    public ArrayList<ChatMessage> getFilteredChatMessages(String profile_id, String contact_id, String field, String filter)
    {
        if(profile_id==null || contact_id==null) return null;

        ArrayList<ChatMessage> chatMessageArrayList = null;

        try {
            chatMessageArrayList = new ArrayList<>();
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            query.equalTo(Constants.CHAT_MESSAGE_PROFILE_ID, profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_CONTACT_ID, contact_id)
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

    public ChatMessage getChatMessageById(String chatMessageId){
        ChatMessage chatMessage = null;

        try {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            query.equalTo(Constants.CHAT_MESSAGE_ID, chatMessageId);
            chatMessage = query.findFirst();
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.getChatMessageById: ",e);
        }

        return chatMessage;
    }

    public long getChatMessagesCount(String profile_id, String contact_id){
        if(contact_id==null || profile_id==null) return 0;

        long count = 0;

        try {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            count = query.equalTo(Constants.CHAT_MESSAGE_PROFILE_ID, profile_id)
                                    .equalTo(Constants.CHAT_MESSAGE_CONTACT_ID, contact_id)
                                    .count();
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.getChatMessageById: ",e);
        }

        return count;
    }

    public ChatMessage getLastChatMessage(String profile_id, String contact_id){
        if(contact_id==null || profile_id==null) return null;

        ChatMessage message = null;

        try {
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            long timestamp = query.equalTo(Constants.CHAT_MESSAGE_PROFILE_ID, profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_CONTACT_ID, contact_id)
                    .maximumInt(Constants.CHAT_MESSAGE_TIMESTAMP);
            String id = profile_id + "_" + contact_id + "_" + timestamp;
            message = getChatMessageById(id);

        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.getChatMessageById: ",e);
        }

        return message;
    }

    //DELETES

    public void deleteChatMessageByFilter(String profile_id, String contact_id, String field, String filter)
    {
        if(profile_id==null || contact_id==null) return;

        try {
            mRealm.beginTransaction();
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);

            query.equalTo(Constants.CHAT_MESSAGE_PROFILE_ID, profile_id)
                    .equalTo(Constants.CHAT_MESSAGE_CONTACT_ID, contact_id)
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

    public void deleteChatMessageById(String chatMessageId) {
        try {
            mRealm.beginTransaction();
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            query.equalTo(Constants.CHAT_MESSAGE_ID, chatMessageId);
            RealmResults<ChatMessage> result1 = query.findAll();
            if (result1 != null) {
                result1.clear();
            }
            mRealm.commitTransaction();
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.deleteChatMessageById: ",e);
        }
    }

    public void deleteAllChatMessages() {
        try {
            mRealm.beginTransaction();
            RealmQuery<ChatMessage> query = mRealm.where(ChatMessage.class);
            RealmResults<ChatMessage> result1 = query.findAll();
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

    public void insertChatList (ArrayList<Chat> chatMessageArrayList){
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

    public Chat getChatById(String chatMessageId){
        Chat chatMessage = null;

        try {
            RealmQuery<Chat> query = mRealm.where(Chat.class);
            query.equalTo(Constants.CHAT_ID, chatMessageId);
            chatMessage = query.findFirst();
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.getChatById: ",e);
        }

        return chatMessage;
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

    public void deleteChatById(String chatMessageId) {
        try {
            mRealm.beginTransaction();
            RealmQuery<Chat> query = mRealm.where(Chat.class);
            query.equalTo(Constants.CHAT_ID, chatMessageId);
            RealmResults<Chat> result1 = query.findAll();
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
            if (result1 != null) {
                result1.clear();
            }
            mRealm.commitTransaction();
        } catch (Exception e) {
            Log.e(Constants.TAG, "RealmChatTransactions.deleteAllChats: ",e);
        }
    }
}

