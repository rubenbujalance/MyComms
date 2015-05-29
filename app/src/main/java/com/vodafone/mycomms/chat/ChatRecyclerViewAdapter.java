package com.vodafone.mycomms.chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vodafone.mycomms.R;
import model.ChatListItem;
import com.vodafone.mycomms.util.Constants;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import model.ChatMessage;


public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatHolder>{

    private String LOG_TAG = ChatRecyclerViewAdapter.class.getSimpleName();
    private ArrayList<ChatMessage> chatList = new ArrayList<>();
    private Context mContext;

    public ChatRecyclerViewAdapter(Context context, ArrayList<ChatMessage> chatListItem) {
        mContext = context;
        /*fakeDataLoad();
        if (chatListItem != null) {
            for (int i=0;i<chatListItem.size();i++){
                chatList.add(chatListItem.get(i));
            }
        }*/
        Realm realm = Realm.getInstance(mContext);
        RealmQuery<ChatMessage> query = realm.where(ChatMessage.class);

        // Execute the query:
        RealmResults<ChatMessage> result1 = query.findAll();
        if (result1!=null){
            for (ChatMessage chatListItems : result1) {
                chatList.add(chatListItems);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(chatList.get(position).getDirection().compareTo(Constants.CHAT_MESSAGE_DIRECTION_SENT)==0)
            return Constants.RIGHT_CHAT;
        else return Constants.LEFT_CHAT;
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public ChatHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        int layout;
        if (viewType == Constants.LEFT_CHAT)
            layout = R.layout.chat_msg_left;
        else
            layout = R.layout.chat_msg_right;
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(layout, null);
        ChatHolder chatHolder = new ChatHolder(view);
        return chatHolder;
    }

    @Override
    public void onBindViewHolder(ChatHolder chatHolder, int i) {
        chatHolder.chatTextView.setText(chatList.get(i).getText());
        chatHolder.chatSentTime.setVisibility(View.VISIBLE);

//        //Fake data show
//        if (i==3 || i==9 || i==15 || i==17){
//            chatHolder.chatSentTime.setVisibility(View.VISIBLE);
//        }
//        if (i==chatList.size()-1){
//            chatHolder.chatSentText.setVisibility(View.VISIBLE);
//        }
    }

}
