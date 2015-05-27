package com.vodafone.mycomms.chatlist.view;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.Constants;

import java.util.ArrayList;

import model.ChatListItem;

public class ChatListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private SwipeRefreshLayout mSwipeRefreshLayout;
    protected Handler handler = new Handler();

    private RecyclerView mRecyclerView;
    private ChatListRecyclerViewAdapter mChatRecyclerViewAdapter;

    public static ChatListFragment newInstance(int index, String param2) {
        Log.d(Constants.TAG, "ContactListFragment.newInstance: " + index);
        ChatListFragment fragment = new ChatListFragment();
        return fragment;
    }

   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
       View v = inflater.inflate(R.layout.layout_fragment_chat_list, container, false);
       mSwipeRefreshLayout = (SwipeRefreshLayout) inflater.inflate(R.layout.layout_fragment_chat_list, container, false);
       mSwipeRefreshLayout.setOnRefreshListener(this);
       mRecyclerView = (RecyclerView) mSwipeRefreshLayout.findViewById(R.id.recycler_view);
       final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
       mRecyclerView.setLayoutManager(layoutManager);

       mChatRecyclerViewAdapter = new ChatListRecyclerViewAdapter(getActivity(), loadFakeData());
       //TODO: GET CHAT LIST
       mRecyclerView.setAdapter(mChatRecyclerViewAdapter);
       return mSwipeRefreshLayout;
   }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Constants.TAG, "ChatListFragment.onCreate: ");
    }

    private ArrayList<ChatListItem> loadFakeData() {
        ArrayList<ChatListItem> mChatListItemList = new ArrayList<>();
        mChatListItemList.add(new ChatListItem("mc_5535b2ac13be4b7975c51600", "mc_555a0792121ef1695cc7c1c3", "Bruce Banner", "Ruben Bujalance", "Oooolaaaa", "00:50"));
        mChatListItemList.add(new ChatListItem("mc_5535d627c729d4430b9722e9", "mc_555a0792121ef1695cc7c1c3", "Darth Vader", "Ruben Bujalance", "Ola ke ase", "dl"));
        mChatListItemList.add(new ChatListItem("mc_5536597eed882c9348ec77bf", "mc_555a0792121ef1695cc7c1c3", "Janos Big", "Ruben Bujalance", "Te aburre o ke ase", "dm"));
        mChatListItemList.add(new ChatListItem("mc_553913669c2c1aaa5c794455", "mc_555a0792121ef1695cc7c1c3", "Jie Lee", "Ruben Bujalance", "Xatea o ke ase", "dc"));
        mChatListItemList.add(new ChatListItem("mc_5535b16c13be4b7975c515fe", "mc_555a0792121ef1695cc7c1c3", "Nick Fury", "Ruben Bujalance", "Me porculea o ke ase", "dj"));
        mChatListItemList.add(new ChatListItem("mc_5535b2ac13be4b7975c51600", "mc_555a0792121ef1695cc7c1c3", "Bruce Banner", "Ruben Bujalance", "Oooolaaaa", "00:50"));
        mChatListItemList.add(new ChatListItem("mc_5535d627c729d4430b9722e9", "mc_555a0792121ef1695cc7c1c3", "Darth Vader", "Ruben Bujalance", "Ola ke ase", "dl"));
        mChatListItemList.add(new ChatListItem("mc_5536597eed882c9348ec77bf", "mc_555a0792121ef1695cc7c1c3", "Janos Big", "Ruben Bujalance", "Te aburre o ke ase", "dm"));
        mChatListItemList.add(new ChatListItem("mc_553913669c2c1aaa5c794455", "mc_555a0792121ef1695cc7c1c3", "Jie Lee", "Ruben Bujalance", "Xatea o ke ase", "dc"));
        mChatListItemList.add(new ChatListItem("mc_5535b16c13be4b7975c515fe", "mc_555a0792121ef1695cc7c1c3", "Nick Fury", "Ruben Bujalance", "Me porculea o ke ase", "dj"));
        return mChatListItemList;
    }

    @Override
    public void onRefresh() {
        handler.postDelayed(testIsGood, 5000);
    }

    public Runnable testIsGood = new Runnable() {
        @Override
        public void run() {
            mSwipeRefreshLayout.setRefreshing(false);
            Log.wtf(Constants.TAG, "ChatListFragment.run: TEEEEESTINGGGG");
        }
    };

}
