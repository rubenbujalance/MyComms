package com.vodafone.mycomms.chat;

import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.Constants;

import java.util.regex.Pattern;

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
            setInternalURLPatterns(chatTextView);
        } catch (Exception e){}
        try {
            this.chatImage = (ImageView) view.findViewById(R.id.chat_image);
        } catch (Exception e){}
        
        this.chatSentTime = (TextView) view.findViewById(R.id.sent_time);
        this.chatSentText = (TextView) view.findViewById(R.id.sent_text);
        this.chatAvatarImage = (ImageView) view.findViewById(R.id.companyLogo);
        this.chatAvatarText = (TextView) view.findViewById(R.id.avatarText);
    }


    private void setInternalURLPatterns(TextView textView)
    {
        Pattern pattern = Pattern.compile(Constants.INTERNAL_URL_PATTERN_PREFIX
                                +Constants.INTERNAL_URL_PATTERN_NEWS+"[0-9]*");
        String scheme = "http://www.google.com";
        Linkify.addLinks(textView, pattern, scheme);
    }
}
