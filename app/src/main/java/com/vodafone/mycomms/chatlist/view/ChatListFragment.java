package com.vodafone.mycomms.chatlist.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.squareup.otto.Subscribe;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatgroup.ComposedChat;
import com.vodafone.mycomms.chatgroup.GroupChatActivity;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.events.GroupChatCreatedEvent;
import com.vodafone.mycomms.events.MessageStatusChanged;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import io.realm.Realm;
import model.Chat;
import model.GroupChat;

public class ChatListFragment extends Fragment{

    private LinearLayout mSwipeRefreshLayout;
    protected Handler handler = new Handler();

    private RecyclerView mRecyclerView;
    private ChatListRecyclerViewAdapter mChatRecyclerViewAdapter;
    private RealmChatTransactions mChatTransactions;
    private RealmGroupChatTransactions mGroupChatTransactions;
    private Realm realm;

    public static ChatListFragment newInstance(int index, String param2) {
        Log.d(Constants.TAG, "ContactListFragment.newInstance: " + index);
        ChatListFragment fragment = new ChatListFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_fragment_chat_list, container, false);
        mSwipeRefreshLayout = (LinearLayout) inflater.inflate(R.layout.layout_fragment_chat_list, container,
                false);
        mRecyclerView = (RecyclerView) mSwipeRefreshLayout.findViewById(R.id.recycler_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        mChatRecyclerViewAdapter = new ChatListRecyclerViewAdapter(
                getActivity(), getComposedChat(), this.realm);
        mRecyclerView.setAdapter(mChatRecyclerViewAdapter);

        mRecyclerView.addOnItemTouchListener(new ChatListRecyclerItemClickListener(getActivity(),
                mRecyclerView, new ChatListRecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ComposedChat composedChat = mChatRecyclerViewAdapter.getComposedChat(position);
                if (null != composedChat) {
                    if (null != composedChat.getChat()) {
                        startChatActivity(composedChat.getChat());
                    } else if (null != composedChat.getGroupChat()) {
                        startGroupChatActivity(composedChat.getGroupChat());
                    }
                }
            }
        }));

        return mSwipeRefreshLayout;
    }

    private void startChatActivity(Chat chat)
    {
        Intent in = new Intent(getActivity(), GroupChatActivity.class);
        in.putExtra(Constants.CHAT_FIELD_CONTACT_ID, chat.getContact_id());
        in.putExtra(Constants.IS_GROUP_CHAT, false);
        in.putExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY, Constants.CHAT_LIST_ACTIVITY);
        startActivity(in);
    }

    private void startGroupChatActivity(GroupChat chat)
    {
        Intent in = new Intent(getActivity(), GroupChatActivity.class);
        in.putExtra(Constants.GROUP_CHAT_ID, chat.getId());
        in.putExtra(Constants.IS_GROUP_CHAT, true);
        in.putExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY, Constants.CHAT_LIST_ACTIVITY);
        startActivity(in);
    }

    private ArrayList<ComposedChat> getComposedChat()
    {
        ArrayList<Chat> chatList = new ArrayList<>();
        ArrayList<GroupChat> groupChats = new ArrayList<>();
        ArrayList<ComposedChat> composedChats = new ArrayList<>();
        chatList = mChatTransactions.getAllChatsFromExistingContacts(realm);
        groupChats = mGroupChatTransactions.getAllGroupChats(realm);
        for(Chat c : chatList)
        {
            ComposedChat composedChat = new ComposedChat(c,null);
            composedChats.add(composedChat);
        }
        for(GroupChat g : groupChats)
        {
            ComposedChat composedChat = new ComposedChat(null,g);
            composedChats.add(composedChat);
        }

        return composedChats;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Constants.TAG, "ChatListFragment.onCreate: ");
        this.realm = Realm.getDefaultInstance();
        mChatTransactions = RealmChatTransactions.getInstance(getActivity());
        SharedPreferences sp = getActivity().getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        String profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
        mGroupChatTransactions = RealmGroupChatTransactions.getInstance(getActivity(),profileId);
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
    }

    public void refreshAdapter()
    {
        try {
            ArrayList<ComposedChat> composedChatList = getComposedChat();
            Collections.sort(composedChatList, new Comparator<ComposedChat>() {
                @Override
                public int compare(ComposedChat lhs, ComposedChat rhs) {
                    long lhsTime;
                    long rhsTime;

                    if (lhs.getChat() == null)
                        lhsTime = lhs.getGroupChat().getLastMessageTime();
                    else lhsTime = lhs.getChat().getLastMessageTime();

                    if (rhs.getChat() == null)
                        rhsTime = rhs.getGroupChat().getLastMessageTime();
                    else rhsTime = rhs.getChat().getLastMessageTime();

                    if (lhsTime > rhsTime) return -1;
                    else return 1;
                }
            });

            mChatRecyclerViewAdapter = new ChatListRecyclerViewAdapter(getActivity(),
                    composedChatList, this.realm);
            mRecyclerView.setAdapter(mChatRecyclerViewAdapter);
        } catch (Exception e) {
            Log.e(Constants.TAG, "ChatListFragment.refreshAdapter: ",e);
        }
    }

//    @Subscribe
//    public void onEventMessageSent(MessageSentEvent event) {
//        refreshAdapter();
//    }

    @Subscribe
    public void onEventChatsReceived(ChatsReceivedEvent event){
        refreshAdapter();
    }

    @Subscribe
    public void onEventMessageStatusChanged(MessageStatusChanged event){
        refreshAdapter();
    }

    @Subscribe
    public void onEventGroupChatCreated(GroupChatCreatedEvent event){
        refreshAdapter();
    }

}
