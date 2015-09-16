package com.vodafone.mycomms.chatgroup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.contacts.view.ContactListViewArrayAdapter;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ReloadAdapterEvent;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.search.SearchBarController;
import com.vodafone.mycomms.search.SearchController;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import io.realm.Realm;
import model.Contact;
import model.GroupChat;

public class GroupChatListFragment extends ListFragment
{

    protected Handler handler = new Handler();

    private ListView listView;
    private SearchController mSearchController;
    private SharedPreferences sp;
    private ArrayList<Contact> contactList;
    private String profileId;
    private String name = "";
    private String about = "";
    private String avatarURL = "";
    private ContactListViewArrayAdapter adapter;
    private Parcelable state;
    private SearchBarController mSearchBarController;
    private boolean isRemoveAllViewNeeded = true;
    private LinearLayout chatContactsContainer;
    private LinearLayout layGroupChatHeader;
    private int numberOfAddedContactsToGroupChat = 0;
    private TextView txtNumberParticipants;
    private TextView txtWrite;
    private String apiCall;
    private boolean isNewGroupChat;
    private GroupChat groupChat;
    private String groupChatId;
    private RealmGroupChatTransactions mGroupChatTransactions;
    private RealmContactTransactions mContactTransactions;
    private GroupChatController mGroupChatController;
    private RecentContactController mRecentContactController;

    private ArrayList<String> selectedContacts;
    private ArrayList<String> ownersIds;

    private final String LOG_TAG = GroupChatListActivity.class.getSimpleName();

    private Realm realm;

    public static GroupChatListFragment newInstance(int index, String param2) {
        Log.d(Constants.TAG, "ContactListFragment.newInstance: " + index);
        GroupChatListFragment fragment = new GroupChatListFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        View v = loadViews(inflater, container);
        loadExtras();
        addListeners();
        loadAllContactsFromDB();
        reloadAdapter();
        loadSearchBarEventsAndControllers(v);

        if(!isNewGroupChat)
        {
            loadGroupChatMembers();
        }

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Constants.TAG, "ChatListFragment.onCreate: ");
        BusProvider.getInstance().register(this);
        this.realm = Realm.getDefaultInstance();
        this.realm.setAutoRefresh(true);

        sp = getActivity().getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        if(sp==null){
            Log.e(Constants.TAG, "contactListFragment.onCreate: error loading Shared Preferences");
            profileId = "";
        }else{
            profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
        }

        mSearchController = new SearchController(getActivity(),
                profileId, realm);
        mGroupChatTransactions = new RealmGroupChatTransactions(getActivity(),profileId);
        mContactTransactions = new RealmContactTransactions(profileId);
        mGroupChatController = new GroupChatController(getActivity(), profileId);
        mRecentContactController = new RecentContactController(getActivity(),profileId);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

//    @Override
//    public void onRefresh() {
//        handler.postDelayed(testIsGood, 5000);
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
        this.realm.close();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        mSearchBarController.hideKeyboard();
        Contact contact = (Contact)l.getAdapter().getItem(position);
        if(!selectedContacts.contains(contact.getContactId()))
            addContactToChat(contact);
    }

    @Subscribe
    public void reloadAdapterEvent(ReloadAdapterEvent event){
        Log.i(Constants.TAG, "ContactListPagerFragment.reloadAdapterEvent: ");
        ArrayList<Contact> compareList = mSearchBarController.getContactList();
        if(!compareList.equals(contactList))
        {
            contactList = compareList;
            reloadAdapter();
        }
    }

    private View loadViews(LayoutInflater inflater, ViewGroup container)
    {
        View v = inflater.inflate(R.layout.layout_fragment_group_chat_list, container, false);
        listView = (ListView) v.findViewById(android.R.id.list);
        chatContactsContainer = (LinearLayout) v.findViewById(R.id.list_group_chat_contacts);

        layGroupChatHeader = (LinearLayout) v.findViewById(R.id.group_chat_header);

        layGroupChatHeader.setVisibility(View.GONE);
        txtNumberParticipants = (TextView) v.findViewById(R.id.txt_participants);
        txtWrite = (TextView) v.findViewById(R.id.txt_write);
        selectedContacts = new ArrayList<>();
        return v;
    }

    private void addListeners()
    {
        txtWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectedContacts.isEmpty()) {
                    if (selectedContacts.size() == 1)
                        startActivityInChatMode();
                    else {
                        if (isNewGroupChat)
                            new CreateGroupChatTask().executeOnExecutor(AsyncTask
                                    .THREAD_POOL_EXECUTOR);
                        else
                            new UpdateGroupChatTask().executeOnExecutor(AsyncTask
                                    .THREAD_POOL_EXECUTOR, groupChat.getId());
                    }
                }
            }
        });
    }

    private void startActivityInGroupChatMode()
    {
        Intent in = new Intent(getActivity(), GroupChatActivity.class);
        in.putExtra(Constants.GROUP_CHAT_ID, groupChatId);
        in.putExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY, Constants.GROUP_CHAT_LIST_ACTIVITY);
        in.putExtra(Constants.IS_GROUP_CHAT, true);
        startActivity(in);

        getActivity().finish();
    }

    private void startActivityInChatMode()
    {
        Intent in = new Intent(getActivity(), GroupChatActivity.class);
        in.putExtra(Constants.CHAT_FIELD_CONTACT_ID, selectedContacts.get(0));
        in.putExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY, Constants.GROUP_CHAT_LIST_ACTIVITY);
        in.putExtra(Constants.IS_GROUP_CHAT, false);
        startActivity(in);

        getActivity().finish();
    }

    private void loadExtras()
    {
        Intent in = getActivity().getIntent();
        if(in.getStringExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY).equals(GroupChatListActivity.class.getSimpleName()))
            isNewGroupChat = true;
        else //if(in.getStringExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY).equals(GroupChatActivity.class.getSimpleName()))
        {
            this.isNewGroupChat = false;
            this.groupChat = mGroupChatTransactions.getGroupChatById(in.getStringExtra(Constants
                    .GROUP_CHAT_ID), realm);
            this.groupChatId = in.getStringExtra(Constants.GROUP_CHAT_ID);
            this.ownersIds = new ArrayList<>();
            String[] ids = this.groupChat.getOwners().split("@");
            Collections.addAll(ownersIds, ids);
        }
    }

    private void loadGroupChatMembers()
    {
        String membersIds = groupChat.getMembers();
        String[] ids = membersIds.split("@");
        this.selectedContacts = new ArrayList<>();
        for(String id : ids)
        {
            if(!id.equals(profileId))
            {
                Contact contact = RealmContactTransactions.getContactById(id, realm);
                addContactToChat(contact);
            }
        }
    }

    /**
     * Gets all contacts from Realm DB by given key word
     * @author str_oan
     * @param keyWord (String) -> key word for make search
     * empty list
     */
    private void loadAllContactsFromDB(String keyWord)
    {
        if(null == keyWord)
        {
            contactList = RealmContactTransactions.getAllContacts(realm, profileId);
        }
        else
        {
            contactList = mSearchController
                    .getContactsByKeyWordWithoutLocalsAndSalesForce(keyWord);
        }
    }

    private void loadAllContactsFromDB()
    {
        loadAllContactsFromDB(null);
    }

    /**
     * Reloads list adapter when data changes
     * @author ---
     */
    private void reloadAdapter()
    {
        adapter = new ContactListViewArrayAdapter(getActivity(), contactList);
        if (contactList!=null) {
            if (listView!=null)
                state = listView.onSaveInstanceState();
            if (adapter != null)
            {
                setListAdapter(adapter);
                if (state!=null)
                    listView.onRestoreInstanceState(state);
            }
            else
            {
                adapter = new ContactListViewArrayAdapter(getActivity(), contactList);
                setListAdapter(adapter);
                if (state!= null)
                    listView.onRestoreInstanceState(state);
            }
        }
    }

    private void loadSearchBarEventsAndControllers(View v)
    {
        mSearchBarController = new SearchBarController
                (
                        getActivity()
                        , mContactTransactions
                        , contactList
                        , mSearchController
                        , Constants.CONTACTS_ALL
                        , listView
                        , true
                        , realm
                        , null
                );

        mSearchBarController.initiateComponentsForSearchView(v);
        mSearchBarController.setSearchBarEvents();
    }


    private void addContactToChat(final Contact contact)
    {
        Log.i(Constants.TAG, "DashBoardActivity.addContactToChat: ");
        try
        {
            adaptViewForEmptyGroupChat();
            View contactChild = createContactViewChild(contact);
            addContactToChatGroupEvent(contact);
            contactChild.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    chatContactsContainer.removeView(v);
                    restContactToChatGroupEvent(contact);
                    adaptViewForGroupChat();
                }
            });
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "GroupChatListFragment.addContactToChat -> ERROR: ",e);
        }
    }

    private void adaptViewForGroupChat()
    {
        if (chatContactsContainer.getChildCount() <= 0)
        {
            adaptLayoutForEmptyContainer();
            isRemoveAllViewNeeded = true;
            layGroupChatHeader.setVisibility(View.GONE);
        }
    }

    private void adaptViewForEmptyGroupChat()
    {
        if(isRemoveAllViewNeeded)
        {
            adaptLayoutForGroupChatContainer();
            isRemoveAllViewNeeded = false;
            layGroupChatHeader.setVisibility(View.VISIBLE);
        }
    }

    private void adaptLayoutForEmptyContainer()
    {
        chatContactsContainer.removeAllViews();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams
                .MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        chatContactsContainer.setLayoutParams(params);
        TextView empty = new TextView(getActivity());
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams
            .WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params2.gravity = Gravity.CENTER;
        empty.setLayoutParams(params2);
        empty.setText(getActivity().getString(R.string.no_group_chat_records));
        empty.setTextColor(getActivity().getResources().getColor(R.color.grey_chat_name));
        empty.setTextAppearance(getActivity(), android.R.style.TextAppearance_Medium);

        chatContactsContainer.addView(empty);
    }

    private void adaptLayoutForGroupChatContainer()
    {
        chatContactsContainer.removeAllViews();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams
                .MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.LEFT;
        chatContactsContainer.setLayoutParams(params);
        chatContactsContainer.setGravity(Gravity.LEFT);
        chatContactsContainer.setHorizontalGravity(Gravity.LEFT);
        chatContactsContainer.setVerticalGravity(Gravity.TOP);
    }

    private View createContactViewChild(Contact contact)
    {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View contactChild = inflater.inflate(R.layout.layout_group_chat_selector,
                chatContactsContainer, false);

        chatContactsContainer.addView(contactChild);
        contactChild.setPadding(10, 20, 10, 20);

        final ImageView contactAvatar = (ImageView) contactChild.findViewById(R.id.group_chat_avatar);
        final TextView avatarText = (TextView) contactChild.findViewById(R.id.avatarText);
        avatarText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);


        //Image avatar
        Utils.loadContactAvatar
                (
                        contact.getFirstName()
                        , contact.getLastName()
                        , contactAvatar
                        , avatarText
                        , contact.getAvatar()
                );

        TextView firstName = (TextView) contactChild.findViewById(R.id.group_chat_first_name);
        TextView lastName = (TextView) contactChild.findViewById(R.id.group_chat_last_name);

        firstName.setText(contact.getFirstName());
        lastName.setText(contact.getLastName());

        return contactChild;
    }

    private void addContactToChatGroupEvent(Contact contact)
    {
        numberOfAddedContactsToGroupChat++;
        txtNumberParticipants.setText(getActivity().getString(R.string
                .group_chat_participants)+": "+numberOfAddedContactsToGroupChat);
        selectedContacts.add(contact.getContactId());

        if(selectedContacts.size()>1)
            txtWrite.setVisibility(View.VISIBLE);
    }

    private void restContactToChatGroupEvent(Contact contact)
    {
        numberOfAddedContactsToGroupChat--;
        txtNumberParticipants.setText(getActivity().getString(R.string
                .group_chat_participants) + ": " + numberOfAddedContactsToGroupChat);
        selectedContacts.remove(contact.getContactId());

        if(selectedContacts.size()<=1)
            txtWrite.setVisibility(View.INVISIBLE);
    }

    private class CreateGroupChatTask extends AsyncTask<Void, Void, String>
    {
        private ProgressDialog pdia;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pdia = new ProgressDialog(getActivity());
            pdia.setMessage(getActivity().getString(R.string.progress_dialog_creating_group_chat));
            pdia.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try
            {
                return createGroupChat();
            }
            catch (Exception e)
            {
                Log.e(Constants.TAG, LOG_TAG+".CreateGroupChatTask -> doInBackground: ERROR ",e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response)
        {
            super.onPostExecute(response);
            if(pdia.isShowing()) pdia.dismiss();
            if(!mGroupChatController.getResponseCode().startsWith("2") || null == response)
            {
                Log.e(Constants.TAG, LOG_TAG + ".CreateGroupChatTask -> onPostExecute: ERROR. " +
                        "Impossible Create Group Chat!!! -> code: " + mGroupChatController
                        .getResponseCode() + " Response -> " + response);
            }
            else
            {
                if(null != groupChat)
                {
                    String chatToString = groupChat.getId() + groupChat.getProfileId()
                            + groupChat.getMembers();
                    Log.i(Constants.TAG, LOG_TAG + ".CreateGroupChatTask -> Created chat is: " + chatToString);
                    mGroupChatTransactions.insertOrUpdateGroupChat(groupChat, null);

                    // Insert recent
                    String action = Constants.CONTACTS_ACTION_SMS;
                    String id = groupChat.getId();
                    mRecentContactController.insertRecentOKHttp(id, action);

                    startActivityInGroupChatMode();
                }
            }
        }
    }


    private class  UpdateGroupChatTask extends AsyncTask<String, Void, Boolean>
    {

        private ProgressDialog pdia;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pdia = new ProgressDialog(getActivity());
            pdia.setMessage(getActivity().getString(R.string.progress_dialog_updating_group_chat));
            pdia.show();
        }

        @Override
        protected Boolean doInBackground(String... params)
        {
            String response = updateGroupChat(params[0]);
            Realm realm = Realm.getDefaultInstance();
            try
            {
                if(!mGroupChatController.getResponseCode().startsWith("2"))
                {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.isNull("err")) {
                        Log.e(Constants.TAG, LOG_TAG + ".UpdateGroupChatTask -> onPostExecute: ERROR. " +
                                "Impossible Update Group Chat!!! -> " + jsonObject.getString("err"));
                    }
                    else
                    {
                        Log.e(Constants.TAG, LOG_TAG + ".UpdateGroupChatTask -> onPostExecute: ERROR. " +
                                "Impossible Update Group Chat!!! -> " + response);
                    }
                    return false;
                }
                else
                {
                    GroupChat groupChat = RealmGroupChatTransactions.getGroupChatById(groupChatId, realm);
                    if(null != groupChat)
                    {
                        mGroupChatTransactions.updateGroupChatInstance
                                (
                                        groupChat,
                                        generateComposedMembersId(selectedContacts),
                                        generateComposedMembersId(ownersIds),
                                        realm
                                );
                        return true;
                    }
                    return false;
                }
            }
            catch (Exception e)
            {
                Log.e(Constants.TAG, LOG_TAG + ".UpdateGroupChatTask -> onPostExecute: ERROR. " +
                        "Impossible Update Group Chat!!!", e);
                return false;
            }
            finally {
                realm.close();
            }

        }

        @Override
        protected void onPostExecute(Boolean isOK)
        {
            super.onPostExecute(isOK);
            if(pdia.isShowing()) pdia.dismiss();
            if(isOK)
                startActivityInGroupChatMode();
        }
    }

    private String generateComposedMembersId(ArrayList<String> contactsIds)
    {
        String composedId = null;
        for(String id : contactsIds)
        {
            if(null == composedId) composedId = id;
            else composedId = composedId + "@" + id;
        }
        return composedId;
    }

    private String createGroupChat()
    {
        String response = null;
        this.ownersIds = new ArrayList<>();
        this.ownersIds.add(profileId);

        this.selectedContacts.add(profileId);
        mGroupChatController.setChatMembers(this.selectedContacts);
        mGroupChatController.setChatCreator(this.profileId);
        mGroupChatController.setChatOwners(this.ownersIds);
        //Added new fields here
        mGroupChatController.setChatName("TestName");
        mGroupChatController.setChatAbout("TestAbout");
        mGroupChatController.setChatAvatar("http://www.google.com");

        if(mGroupChatController.isCreatedJSONBodyForCreateGroupChat())
        {
            mGroupChatController.createRequest(GroupChatController.URL_CREATE_GROUP_CHAT,"post");
            response = mGroupChatController.executeRequest();
            if(null != response && mGroupChatController.getResponseCode().startsWith("2"))
            {
                String id = mGroupChatController.getCreatedGroupChatId(response);
                this.groupChatId = id;
                if(null != id)
                {
                    this.groupChat = mGroupChatTransactions.newGroupChatInstance
                            (
                                    id
                                    , this.profileId
                                    , this.selectedContacts
                                    , this.ownersIds
                                    , this.name
                                    , this.about
                                    , this.avatarURL
                            );
                }
                else
                    response = null;
            }
        }
        return response;
    }

    private String updateGroupChat(String groupChatId)
    {
        this.selectedContacts.add(profileId);
        mGroupChatController.setChatMembers(this.selectedContacts);
        mGroupChatController.setChatCreator(this.profileId);
        mGroupChatController.setChatOwners(this.ownersIds);
        if(mGroupChatController.isCreatedJSONBodyForUpdateGroupChat())
        {
            mGroupChatController.createRequest(GroupChatController.URL_UPDATE_GROUP_CHAT, "put", groupChatId);
            return mGroupChatController.executeRequest();
        }
        else
            return null;
    }
}