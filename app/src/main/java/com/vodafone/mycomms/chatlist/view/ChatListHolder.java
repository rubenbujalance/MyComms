package com.vodafone.mycomms.chatlist.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vodafone.mycomms.R;

public class ChatListHolder extends RecyclerView.ViewHolder {
    public TextView textViewName;
    public TextView textViewMessage;
    public TextView textViewTime;

    public ImageView top_left_avatar;
    public ImageView top_right_avatar;
    public ImageView bottom_left_avatar;
    public ImageView bottom_right_avatar;
    public ImageView bottom_right_chat_availability;
    public ImageView bottom_left_chat_availability;
    public ImageView top_right_chat_availability;
    public ImageView top_left_chat_availability;
    public ImageView chat_availability;

    public TextView top_left_avatar_text;
    public TextView top_right_avatar_text;
    public TextView bottom_left_avatar_text;
    public TextView bottom_right_avatar_text;

    public LinearLayout lay_top_right_image_hide;
    public LinearLayout lay_bottom_both_image_hide;
    public LinearLayout lay_top_left_image;

    public TextView badgeUnread;
    public RelativeLayout layContainer;


    public ChatListHolder(View view)
    {
        super(view);
        view.setClickable(true);
        this.textViewName = (TextView) view.findViewById(R.id.list_item_content_name);
        this.textViewMessage = (TextView) view.findViewById(R.id.list_item_content_position);
        this.textViewTime = (TextView) view.findViewById(R.id.chat_list_item_last_event_day);
        this.badgeUnread = (TextView) view.findViewById(R.id.chat_list_item_badge_unread);
        this.layContainer = (RelativeLayout) view.findViewById(R.id.front);

        this.top_left_avatar = (ImageView) view.findViewById(R.id.top_left_avatar);
        this.top_right_avatar = (ImageView) view.findViewById(R.id.top_right_avatar);
        this.bottom_left_avatar = (ImageView) view.findViewById(R.id.bottom_left_avatar);
        this.bottom_right_avatar = (ImageView) view.findViewById(R.id.bottom_right_avatar);

        this.top_left_avatar_text = (TextView) view.findViewById(R.id.top_left_avatar_text);
        this.top_right_avatar_text = (TextView) view.findViewById(R.id.top_right_avatar_text);
        this.bottom_left_avatar_text = (TextView) view.findViewById(R.id.bottom_left_avatar_text);
        this.bottom_right_avatar_text = (TextView) view.findViewById(R.id.bottom_right_avatar_text);

        this.lay_top_right_image_hide = (LinearLayout) view.findViewById(R.id.lay_top_right_image_hide);
        this.lay_bottom_both_image_hide = (LinearLayout) view.findViewById(R.id.lay_bottom_both_image_hide);

        this.lay_top_left_image = (LinearLayout) view.findViewById(R.id.lay_top_left_image);

        this.chat_availability = (ImageView) view.findViewById(R.id.chat_availability);

        this.bottom_right_chat_availability = (ImageView) view.findViewById(R.id.bottom_right_chat_availability);
        this.bottom_left_chat_availability = (ImageView) view.findViewById(R.id.bottom_left_chat_availability);
        this.top_right_chat_availability = (ImageView) view.findViewById(R.id.top_right_chat_availability);
        this.top_left_chat_availability = (ImageView) view.findViewById(R.id.top_left_chat_availability);
    }
}
