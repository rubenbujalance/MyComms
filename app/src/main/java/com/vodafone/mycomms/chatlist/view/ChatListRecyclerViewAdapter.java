package com.vodafone.mycomms.chatlist.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.Constants;

import java.io.File;
import java.util.ArrayList;

import model.ChatListItem;

public class ChatListRecyclerViewAdapter extends RecyclerView.Adapter<ChatListHolder>{

    private ArrayList<ChatListItem> mChatListItem = new ArrayList<>();
    private Context mContext;

    public ChatListRecyclerViewAdapter(Context context, ArrayList<ChatListItem> chatListItem) {
        mContext = context;
        mChatListItem = chatListItem;
    }

    @Override
    public int getItemCount() {
        return mChatListItem.size();
    }

    @Override
    public ChatListHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_list_item_chat, null);
        ChatListHolder chatHolder = new ChatListHolder(view);
        return chatHolder;
    }

    @Override
    public void onBindViewHolder(ChatListHolder chatListHolder, int i) {
        chatListHolder.textViewName.setText(mChatListItem.get(i).getChatSenderName());
        chatListHolder.textViewMessage.setText(mChatListItem.get(i).getLastMessage());
        chatListHolder.textViewTime.setText(mChatListItem.get(i).getLastMessageTime());

        //Image avatar
        File avatarFile = new File(mContext.getFilesDir(), Constants.CONTACT_AVATAR_DIR + "avatar_"+mChatListItem.get(i).getChatSenderId()+".jpg");

        if (mChatListItem.get(i).getChatSenderId()!=null &&
                mChatListItem.get(i).getChatSenderId().length()>0 &&
                mChatListItem.get(i).getChatSenderId().compareTo("")!=0 &&
                avatarFile.exists()) {

            chatListHolder.textAvatar.setText(null);

            Picasso.with(mContext)
                    .load(avatarFile)
                    .into(chatListHolder.imageAvatar);

        } else{
            String initials = mChatListItem.get(i).getChatSenderName().substring(0,1);
                    //+ contact.getLastName().substring(0,1);

            chatListHolder.imageAvatar.setImageResource(R.color.grey_middle);
            chatListHolder.textAvatar.setText(initials);
        }
    }
    public ChatListItem getChatListItem(int position) {
        return (null != mChatListItem ? mChatListItem.get(position) : null);
    }

}
