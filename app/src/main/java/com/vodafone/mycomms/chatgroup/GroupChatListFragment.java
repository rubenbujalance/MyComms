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
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
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
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.contacts.connection.ContactListController;
import com.vodafone.mycomms.contacts.connection.IContactsRefreshConnectionCallback;
import com.vodafone.mycomms.contacts.connection.RecentContactController;
import com.vodafone.mycomms.contacts.view.ContactListViewArrayAdapter;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ReloadAdapterEvent;
import com.vodafone.mycomms.events.SetContactListAdapterEvent;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.search.SearchBarController;
import com.vodafone.mycomms.search.SearchController;
import com.vodafone.mycomms.util.Constants;

import java.io.File;
import java.util.ArrayList;

import model.Contact;
import model.GroupChat;

public class GroupChatListFragment extends ListFragment implements
        SwipeRefreshLayout.OnRefreshListener, IContactsRefreshConnectionCallback {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    protected Handler handler = new Handler();

    private ListView listView;
    private SearchController mSearchController;
    private SharedPreferences sp;
    private ArrayList<Contact> contactList;
    private String profileId;
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
    private RealmGroupChatTransactions mGroupChatTransactions;
    private RealmContactTransactions mContactTransactions;
    private GroupChatController mGroupChatController;
    private RecentContactController mRecentContactController;

    private ArrayList<String> selectedContacts;
    private ArrayList<String> ownersIds;
    private ContactListController contactListController;

    private final String LOG_TAG = GroupChatListActivity.class.getSimpleName();

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

        sp = getActivity().getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

        if(sp==null){
            Log.e(Constants.TAG, "contactListFragment.onCreate: error loading Shared Preferences");
            profileId = "";
        }else{
            profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
        }

        mSearchController = new SearchController(getActivity(), profileId);
        mGroupChatTransactions = new RealmGroupChatTransactions(getActivity(),profileId);
        mContactTransactions = new RealmContactTransactions(profileId);
        mGroupChatController = new GroupChatController(getActivity(), profileId);
        contactListController = new ContactListController(getActivity(), profileId);
        mRecentContactController = new RecentContactController(getActivity(),profileId);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRefresh() {
        handler.postDelayed(testIsGood, 5000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        contactListController.closeRealm();
        mSearchController.closeRealm();
        mGroupChatTransactions.closeRealm();
        mContactTransactions.closeRealm();
        contactListController.closeRealm();
        contactListController.closeRealm();
        mRecentContactController.closeRealm();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        Contact contact = (Contact)l.getAdapter().getItem(position);
        if(!selectedContacts.contains(contact.getContactId()))
        {
            addContactToChat(contact);
        }
    }

    @Subscribe
    public void reloadAdapterEvent(ReloadAdapterEvent event){
        Log.i(Constants.TAG, "ContactListPagerFragment.reloadAdapterEvent: ");
        this.contactList = mSearchBarController.getContactList();
        reloadAdapter();
    }


    public Runnable testIsGood = new Runnable() {
        @Override
        public void run() {
            mSwipeRefreshLayout.setRefreshing(false);
            Log.wtf(Constants.TAG, "ChatListFragment.run: TEEEEESTINGGGG");
        }
    };

    private View loadViews(LayoutInflater inflater, ViewGroup container)
    {
        View v = inflater.inflate(R.layout.layout_fragment_group_chat_list, container, false);
        listView = (ListView) v.findViewById(android.R.id.list);
        chatContactsContainer = (LinearLayout) v.findViewById(R.id
                .list_group_chat_contacts);

        layGroupChatHeader = (LinearLayout) v.findViewById(R.id
                .group_chat_header);

        layGroupChatHeader.setVisibility(View.GONE);
        txtNumberParticipants = (TextView) v.findViewById(R.id.txt_participants);
        txtWrite = (TextView) v.findViewById(R.id.txt_write);
        selectedContacts = new ArrayList<>();
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.contacts_swipe_refresh_layout);
        return v;
    }

    private void addListeners()
    {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });

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
        in.putExtra(Constants.GROUP_CHAT_ID, groupChat.getId());
        in.putExtra(Constants.CHAT_PREVIOUS_VIEW, Constants.GROUP_CHAT_LIST_ACTIVITY);
        in.putExtra(Constants.IS_GROUP_CHAT, true);
        startActivity(in);

        getActivity().finish();
    }

    private void startActivityInChatMode()
    {
        Intent in = new Intent(getActivity(), GroupChatActivity.class);
        in.putExtra(Constants.CHAT_FIELD_CONTACT_ID, selectedContacts.get(0));
        in.putExtra(Constants.CHAT_PREVIOUS_VIEW, Constants.GROUP_CHAT_LIST_ACTIVITY);
        in.putExtra(Constants.IS_GROUP_CHAT, false);
        startActivity(in);

        getActivity().finish();
    }

    private void loadExtras()
    {
        Intent in = getActivity().getIntent();
        if(in.getStringExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY).equals(GroupChatListActivity.class.getSimpleName()))
            isNewGroupChat = true;
        else if(in.getStringExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY).equals(GroupChatActivity.class.getSimpleName()))
        {
            this.isNewGroupChat = false;
            this.groupChat = mGroupChatTransactions.getGroupChatById(in.getStringExtra(Constants
                    .GROUP_CHAT_ID));

            this.ownersIds = new ArrayList<>();
            String[] ids = this.groupChat.getOwners().split("@");
            for(String id : ids)
            {
                this.ownersIds.add(id);
            }
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
                Contact contact = mContactTransactions.getContactById(id);
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
            contactList = mContactTransactions.getAllContacts();
        }
        else
        {
            contactList = mSearchController.getContactsByKeyWordWithoutLocals(keyWord);
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
        adapter = new ContactListViewArrayAdapter(getActivity().getApplicationContext(), contactList);
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
                adapter = new ContactListViewArrayAdapter(getActivity().getApplicationContext(), contactList);
                setListAdapter(adapter);
                if (state!= null)
                    listView.onRestoreInstanceState(state);
            }
        }
    }


    private void refreshContent()
    {
        contactListController.getContactList(Constants.CONTACT_API_GET_CONTACTS);
        contactListController.setConnectionCallback(this);
    }

    private void loadSearchBarEventsAndControllers(View v)
    {
        mSearchBarController = new SearchBarController
                (
                        getActivity()
                        , mContactTransactions
                        , contactList
                        , mSearchController
                        , adapter
                        , Constants.CONTACTS_ALL
                        , listView
                        , true
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

        ImageView contactAvatar = (ImageView) contactChild.findViewById(R.id.group_chat_avatar);
        File avatarFile = new File(getActivity().getFilesDir(), Constants.CONTACT_AVATAR_DIR +
                "avatar_"+contact.getContactId()+".jpg");

        if (avatarFile.exists()) {
            Picasso.with(getActivity())
                    .load(avatarFile)
                    .fit().centerCrop()
                    .into(contactAvatar);
        } else {
            String initials = "";
            if(null != contact.getFirstName() && contact.getFirstName().length() > 0)
            {
                initials = contact.getFirstName().substring(0,1);

                if(null != contact.getLastName() && contact.getLastName().length() > 0)
                {
                    initials = initials + contact.getLastName().substring(0,1);
                }

            }
            TextView avatarText = (TextView) contactChild.findViewById(R.id.avatarText);
            contactAvatar.setImageResource(R.color.grey_middle);
            avatarText.setText(initials);
        }

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

    @Override
    public void onContactsRefreshResponse(ArrayList<Contact> contactList, boolean morePages, int offsetPaging)
    {
        if (morePages){
            Log.i(Constants.TAG, "ContactListFragment.onContactsRefreshResponse: ");
            apiCall = Constants.CONTACT_API_GET_CONTACTS;

            contactListController.getContactList(apiCall + "&o=" + offsetPaging);
            contactListController.setConnectionCallback(this);
        } else {
            Log.i(Constants.TAG, "ContactListFragment.onContactsRefreshResponse: FINISH");
            mSwipeRefreshLayout.setRefreshing(false);
            BusProvider.getInstance().post(new SetContactListAdapterEvent());
        }
    }

    @Override
    public void onFavouritesRefreshResponse()
    {

    }

    @Override
    public void onRecentsRefreshResponse()
    {

    }

    @Override
    public void onConnectionNotAvailable()
    {
        Log.d(Constants.TAG, "GroupChatListFragment.onConnectionNotAvailable: ");
    }

    private class CreateGroupChatTask extends AsyncTask<Void, Void, Boolean>
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
        protected Boolean doInBackground(Void... params) {
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
        protected void onPostExecute(Boolean isGroupChatCreated)
        {
            super.onPostExecute(isGroupChatCreated);
            if(pdia.isShowing()) pdia.dismiss();
            if(!isGroupChatCreated)
            {
                Log.e(Constants.TAG, LOG_TAG+".CreateGroupChatTask -> onPostExecute: ERROR. " +
                        "Impossible Create Group Chat!!!");
            }
            else
            {
                String chatToString = groupChat.getId() + groupChat.getProfileId()
                        + groupChat.getMembers();
                Log.i(Constants.TAG, LOG_TAG + ".CreateGroupChatTask -> Created chat is: " + chatToString);
                mGroupChatTransactions.insertOrUpdateGroupChat(groupChat);

                String action = Constants.CONTACTS_ACTION_SMS;
                String id = groupChat.getId();
                mRecentContactController.insertRecentOKHttp(id, action);

                startActivityInGroupChatMode();
            }
        }
    }


    private class  UpdateGroupChatTask extends AsyncTask<String, Void, String>
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
        protected String doInBackground(String... params) {
            try
            {
                if(updateGroupChat(params[0]))
                    return params[0];
                else
                    return null;
            }
            catch (Exception e)
            {
                Log.e(Constants.TAG, LOG_TAG+".UpdateGroupChatTask -> doInBackground: ERROR ",e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String groupId)
        {
            super.onPostExecute(groupId);
            if(pdia.isShowing()) pdia.dismiss();
            if(null == groupId)
            {
                Log.e(Constants.TAG, LOG_TAG+".UpdateGroupChatTask -> onPostExecute: ERROR. " +
                        "Impossible Update Group Chat!!!");
            }
            else
            {

                String chatToString = groupChat.getId() + groupChat.getProfileId()
                        + groupChat.getMembers();
                Log.i(Constants.TAG, LOG_TAG + ".UpdateGroupChatTask -> Updated chat is: " +
                        chatToString);

                GroupChat updatedGroupChat = new GroupChat(groupChat);
                updatedGroupChat.setMembers(generateComposedMembersId(selectedContacts));
                updatedGroupChat.setOwners(generateComposedMembersId(ownersIds));
                mGroupChatTransactions.insertOrUpdateGroupChat(updatedGroupChat);
                startActivityInGroupChatMode();
            }
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

    private boolean createGroupChat()
    {
        this.ownersIds = new ArrayList<>();
        this.ownersIds.add(profileId);

        this.selectedContacts.add(profileId);
        mGroupChatController.setChatMembers(this.selectedContacts);
        mGroupChatController.setChatCreator(this.profileId);
        mGroupChatController.setChatOwners(this.ownersIds);
        if(mGroupChatController.isCreatedJSONBodyForCreateGroupChat())
        {
            mGroupChatController.createRequest(mGroupChatController.URL_CREATE_GROUP_CHAT,"post");
            String response = mGroupChatController.executeRequest();
            if(null != response)
            {
                String id = mGroupChatController.getCreatedGroupChatId(response);

                this.groupChat = mGroupChatTransactions.newGroupChatInstance
                        (
                                id
                                , this.profileId
                                , this.selectedContacts
                                , this.ownersIds
                                , ""
                                , ""
                                , ""
                        );

                return true;
            }
        }
        return false;
    }

    private boolean updateGroupChat(String groupChatId)
    {
        this.selectedContacts.add(profileId);
        mGroupChatController.setChatMembers(this.selectedContacts);
        mGroupChatController.setChatCreator(this.profileId);
        mGroupChatController.setChatOwners(this.ownersIds);
        if(mGroupChatController.isCreatedJSONBodyForUpdateGroupChat())
        {
            mGroupChatController.createRequest(mGroupChatController.URL_UPDATE_GROUP_CHAT, "put", groupChatId);

            String response = mGroupChatController.executeRequest();
            if(null != response)
            {
                return true;
            }
        }
        return false;
    }
}