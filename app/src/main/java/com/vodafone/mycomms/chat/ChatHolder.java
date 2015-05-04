package com.vodafone.mycomms.chat;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.vodafone.mycomms.R;

public class ChatHolder extends RecyclerView.ViewHolder {
    protected TextView chatTextView;
    protected TextView chatSentTime;
    protected TextView chatSentText;

    public ChatHolder(View view) {
        super(view);
        this.chatTextView = (TextView) view.findViewById(R.id.chat_text);
        this.chatSentTime = (TextView) view.findViewById(R.id.sent_time);
        this.chatSentText = (TextView) view.findViewById(R.id.sent_text);
    }
}
