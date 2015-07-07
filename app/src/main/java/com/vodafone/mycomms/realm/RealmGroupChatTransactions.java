package com.vodafone.mycomms.realm;

import android.content.Context;
import android.util.Log;

import com.vodafone.mycomms.util.Constants;

import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import model.GroupChat;

/**
 * Created by str_oan on 01/07/2015.
 */
public class RealmGroupChatTransactions {
    private Realm mRealm;
    private String _profile_id;
    private Context mContext;
    private String LOG_TAG = RealmGroupChatTransactions.class.getSimpleName();

    public RealmGroupChatTransactions(Realm realm, Context context, String profile_id)
    {
        mContext = context;
        mRealm = realm;
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

    public ArrayList<GroupChat> getGroupChatByMembers(String composedMembersId)
    {
        try
        {
            RealmQuery<GroupChat> query = mRealm.where(GroupChat.class);
            query.equalTo(Constants.GROUP_CHAT_REALM_MEMBERS, composedMembersId);
            RealmResults<GroupChat> result = query.findAll();

            ArrayList<GroupChat> groupChats = new ArrayList<>();

            for(GroupChat g : result)
            {
                groupChats.add(g);
            }

            return groupChats;

        }catch (Exception e )
        {
            Log.e(Constants.TAG, LOG_TAG+".getGroupChatByMembers: " , e);
            return  null;
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
            Log.e(Constants.TAG, LOG_TAG+".getGroupChatById: " , e);
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



}
