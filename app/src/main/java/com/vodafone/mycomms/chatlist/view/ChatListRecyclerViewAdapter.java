package com.vodafone.mycomms.chatlist.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import java.io.File;
import java.util.ArrayList;

import model.Chat;

public class ChatListRecyclerViewAdapter extends RecyclerView.Adapter<ChatListHolder>{

    private ArrayList<Chat> mChat = new ArrayList<>();
    private Context mContext;

    public ChatListRecyclerViewAdapter(Context context, ArrayList<Chat> Chat) {
        mContext = context;
        mChat = Chat;
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    @Override
    public ChatListHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_list_item_chat, null);
        ChatListHolder chatHolder = new ChatListHolder(view);
        return chatHolder;
    }

    @Override
    public void onBindViewHolder(ChatListHolder chatListHolder, int i) {
        chatListHolder.textViewName.setText(mChat.get(i).getContactName() + " " + mChat.get(i).getContactSurname());
        chatListHolder.textViewMessage.setText(mChat.get(i).getLastMessage());
        chatListHolder.textViewTime.setText(Utils.getTimeFromMillis(mChat.get(i).getLastMessageTime()));

        //Image avatar
        File avatarFile = new File(mContext.getFilesDir(), Constants.CONTACT_AVATAR_DIR + "avatar_"+mChat.get(i).getContact_id()+".jpg");

        if (mChat.get(i).getContact_id()!=null &&
                mChat.get(i).getContact_id().length()>0 &&
                mChat.get(i).getContact_id().compareTo("")!=0 &&
                avatarFile.exists()) {

            chatListHolder.textAvatar.setText(null);

            Picasso.with(mContext)
                    .load(avatarFile)
                    .into(chatListHolder.imageAvatar);

        } else{
            String initials = mChat.get(i).getContactName().substring(0,1)
                    + mChat.get(i).getContactSurname().substring(0,1);

            chatListHolder.imageAvatar.setImageResource(R.color.grey_middle);
            chatListHolder.textAvatar.setText(initials);
        }
    }
    public Chat getChat(int position) {
        return (null != mChat ? mChat.get(position) : null);
    }

}
