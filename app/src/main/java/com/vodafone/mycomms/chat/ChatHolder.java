package com.vodafone.mycomms.chat;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.R;

public class ChatHolder extends RecyclerView.ViewHolder {
    protected TextView chatTextView;
    protected TextView chatSentTime;
    protected TextView chatSentText;
    protected ImageView chatAvatarImage;
    protected TextView chatAvatarText;
    protected ImageView chatImage;

    public ChatHolder(View view) {
        super(view);

        try {
            this.chatTextView = (TextView) view.findViewById(R.id.chat_text);
        } catch (Exception e){}
        try {
            this.chatImage = (ImageView) view.findViewById(R.id.chat_image);
        } catch (Exception e){}
        
        this.chatSentTime = (TextView) view.findViewById(R.id.sent_time);
        this.chatSentText = (TextView) view.findViewById(R.id.sent_text);
        this.chatAvatarImage = (ImageView) view.findViewById(R.id.companyLogo);
        this.chatAvatarText = (TextView) view.findViewById(R.id.avatarText);
    }
}
