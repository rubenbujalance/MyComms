package com.vodafone.mycomms.chatlist.view;

import android.content.Intent;
import android.os.Bundle;
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
import com.vodafone.mycomms.chat.ChatMainActivity;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.events.MessageSentEvent;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.util.Constants;

import java.util.ArrayList;

import io.realm.Realm;
import model.Chat;

public class ChatListFragment extends Fragment{

    private RecyclerView mRecyclerView;
    private ChatListRecyclerViewAdapter mChatRecyclerViewAdapter;
    private Realm mRealm;
    private RealmChatTransactions mChatTransactions;

    public static ChatListFragment newInstance(int index, String param2) {
        Log.d(Constants.TAG, "ContactListFragment.newInstance: " + index);
        ChatListFragment fragment = new ChatListFragment();
        return fragment;
    }

   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
//       View v = inflater.inflate(R.layout.layout_fragment_chat_list, container, false);
       LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.layout_fragment_chat_list, container, false);

       mRecyclerView = (RecyclerView) linearLayout.findViewById(R.id.recycler_view);
       final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
       mRecyclerView.setLayoutManager(layoutManager);

       refreshAdapter();

       mRecyclerView.addOnItemTouchListener(new ChatListRecyclerItemClickListener(getActivity(),
               mRecyclerView, new ChatListRecyclerItemClickListener.OnItemClickListener() {
           @Override
           public void onItemClick(View view, int position)
           {
               Intent in = new Intent(getActivity(), ChatMainActivity.class);
               in.putExtra(Constants.CHAT_FIELD_CONTACT_ID, mChatRecyclerViewAdapter.getChat(position).getContact_id());
               in.putExtra(Constants.CHAT_PREVIOUS_VIEW, Constants.CHAT_VIEW_CHAT_LIST);
               startActivity(in);

           }
       }));

       return linearLayout;
   }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Constants.TAG, "ChatListFragment.onCreate: ");
        mRealm = Realm.getInstance(getActivity());
        mChatTransactions = new RealmChatTransactions(mRealm, getActivity());
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mRecyclerView.setAdapter(mChatRecyclerViewAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRealm!=null)
            mRealm.close();
        BusProvider.getInstance().unregister(this);
    }

    public void refreshAdapter() {
        ArrayList<Chat> chatList = mChatTransactions.getAllChatsFromExistingContacts();
        mChatRecyclerViewAdapter = new ChatListRecyclerViewAdapter(getActivity(), chatList);
        mRecyclerView.setAdapter(mChatRecyclerViewAdapter);
    }

    @Subscribe
    public void onEventMessageSent(MessageSentEvent event) {
        refreshAdapter();
    }

    @Subscribe
    public void onEventChatsReceived(ChatsReceivedEvent event){
        refreshAdapter();
    }

}
