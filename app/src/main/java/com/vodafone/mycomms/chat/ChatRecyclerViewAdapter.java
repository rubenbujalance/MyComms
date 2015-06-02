package com.vodafone.mycomms.chat;

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

import model.ChatMessage;
import model.Contact;


public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatHolder>{

    private String LOG_TAG = ChatRecyclerViewAdapter.class.getSimpleName();
    private ArrayList<ChatMessage> chatList = new ArrayList<>();
    private Context mContext;
    private Contact _contact;
    private Contact _profile;

    public ChatRecyclerViewAdapter(Context context, ArrayList<ChatMessage> chatListItem, Contact profile, Contact contact) {
        mContext = context;
        this._contact = contact;
        this._profile = profile;

        if (chatListItem!=null){
            for (ChatMessage chatListItems : chatListItem) {
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

        //Set text message
        chatHolder.chatTextView.setText(chatList.get(i).getText());

        //Set text status
        if(chatList.get(i).getStatus().compareTo("0")==0)
            chatHolder.chatSentText.setText(mContext.getString(R.string.status_not_sent));
        else if(chatList.get(i).getStatus().compareTo("1")==0)
            chatHolder.chatSentText.setText(mContext.getString(R.string.status_sent));
        else if(chatList.get(i).getStatus().compareTo("2")==0)
            chatHolder.chatSentText.setText(mContext.getString(R.string.status_delivered));
        else if(chatList.get(i).getStatus().compareTo("3")==0)
            chatHolder.chatSentText.setText(mContext.getString(R.string.status_read));

        //Set message time
        long currentTimestamp = chatList.get(i).getTimestamp();
        long previousTimestamp = 0;
        if(i>0) previousTimestamp = chatList.get(i-1).getTimestamp();

        long thirtyMinutesDiff = 30 * 60 * 1000;

        if(currentTimestamp - previousTimestamp > thirtyMinutesDiff ||
            previousTimestamp == 0)
        {
            chatHolder.chatSentTime.setVisibility(View.VISIBLE);
            chatHolder.chatSentTime.setText(Utils.getStringChatTimeDifference(currentTimestamp));
        }
        else
        {
            chatHolder.chatSentTime.setVisibility(View.GONE);
        }

        //Set message avatar
        Contact contact;

        if(chatHolder.getItemViewType() == Constants.LEFT_CHAT)
            contact = _contact;
        else contact = _profile;

        File avatarFile = new File(mContext.getFilesDir(), Constants.CONTACT_AVATAR_DIR + "avatar_"+contact.getId()+".jpg");

        if (contact.getAvatar()!=null &&
                contact.getAvatar().length()>0 &&
                contact.getAvatar().compareTo("")!=0 &&
                avatarFile.exists()) {

            chatHolder.chatAvatarText.setText(null);

            Picasso.with(mContext)
                    .load(avatarFile)
                    .into(chatHolder.chatAvatarImage);

        } else{
            String initials = contact.getFirstName().substring(0,1) +
                    contact.getLastName().substring(0,1);

            chatHolder.chatAvatarImage.setImageResource(R.color.grey_middle);
            chatHolder.chatAvatarText.setText(initials);
        }
    }

}
