package com.vodafone.mycomms.chatlist.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.R;

public class ChatListHolder extends RecyclerView.ViewHolder {
    protected TextView textViewName;
    protected TextView textViewMessage;
    protected TextView textViewTime;
    protected ImageView imageAvatar;
    protected TextView textAvatar;

    public ChatListHolder(View view) {
        super(view);
        this.textViewName = (TextView) view.findViewById(R.id.list_item_content_name);
        this.textViewMessage = (TextView) view.findViewById(R.id.list_item_content_position);
        this.textViewTime = (TextView) view.findViewById(R.id.chat_list_item_last_event_day);
        this.imageAvatar = (ImageView) view.findViewById(R.id.companyLogo);
        this.textAvatar = (TextView) view.findViewById(R.id.avatarText);
    }
}
