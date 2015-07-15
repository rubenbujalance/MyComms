package com.vodafone.mycomms.chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

import java.io.File;
import java.util.ArrayList;

import model.ChatMessage;
import model.Contact;
import model.UserProfile;


public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatHolder>{

    private String LOG_TAG = ChatRecyclerViewAdapter.class.getSimpleName();
    private ArrayList<ChatMessage> chatList = new ArrayList<>();
    private Context mContext;
    private UserProfile _profile;
    private RealmChatTransactions _chatTx;
    private RealmContactTransactions _contactTx;

    public ChatRecyclerViewAdapter(Context context, ArrayList<ChatMessage> chatListItem, UserProfile profile) {
        mContext = context;
        this._profile = profile;

        _chatTx = new RealmChatTransactions(mContext);
        _contactTx = new RealmContactTransactions(_profile.getId());

        if (chatListItem!=null){
            for (ChatMessage chatListItems : chatListItem) {
                chatList.add(chatListItems);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        int test = position;
        if(chatList.get(position).getDirection().compareTo(Constants.CHAT_MESSAGE_DIRECTION_SENT)==0)
        {
            if(chatList.get(position).getType()==Constants.CHAT_MESSAGE_TYPE_TEXT)
                return Constants.RIGHT_CHAT;
            else if(chatList.get(position).getType()==Constants.CHAT_MESSAGE_TYPE_IMAGE)
                return Constants.RIGHT_IMAGE_CHAT;
        }
        else
        {
            if(chatList.get(position).getType()==Constants.CHAT_MESSAGE_TYPE_TEXT)
                return Constants.LEFT_CHAT;
            else if(chatList.get(position).getType()==Constants.CHAT_MESSAGE_TYPE_IMAGE)
                return Constants.LEFT_IMAGE_CHAT;
        }

        return Constants.RIGHT_CHAT;
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
        else if (viewType == Constants.RIGHT_CHAT)
            layout = R.layout.chat_msg_right;
        else if (viewType == Constants.RIGHT_IMAGE_CHAT)
            layout = R.layout.chat_msg_image_right;
        else if (viewType == Constants.LEFT_IMAGE_CHAT)
            layout = R.layout.chat_msg_image_left;
        else layout = R.layout.chat_msg_right;

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(layout, null);
        ChatHolder chatHolder = new ChatHolder(view);
        return chatHolder;
    }

    @Override
    public void onBindViewHolder(ChatHolder chatHolder, int i)
    {
        int test = i;
        String test2 = chatList.get(i).getContact_id();
        int test3 = chatHolder.getItemViewType();
        Contact contact = _contactTx.getContactById(chatList.get(i).getContact_id());

        if(chatList.get(i).getType()==Constants.CHAT_MESSAGE_TYPE_IMAGE)
        {
            String dirStr = mContext.getFilesDir() + Constants.CONTACT_CHAT_FILES;
            String fileStr = "file_" + chatList.get(i).getId() + ".jpg";

            File image = new File(dirStr, fileStr);

            Picasso.with(mContext)
                    .load(image)
                    .fit().centerCrop()
                    .into(chatHolder.chatImage);
        }
        else
        {
            chatHolder.chatTextView.setText(chatList.get(i).getText());
        }

        if(chatList.get(i).getDirection().compareTo(Constants.CHAT_MESSAGE_DIRECTION_SENT)==0) {
            //Decide if show status or not
            ChatMessage nextSentMessage = getNextSentChatMessage(i);
            String status = null;

            if((i==chatList.size()-1 || nextSentMessage==null) ||
                    (XMPPTransactions.getXMPPStatusOrder(chatList.get(i).getStatus())==0) ||
                    (XMPPTransactions.getXMPPStatusOrder(nextSentMessage.getStatus()) <
                            XMPPTransactions.getXMPPStatusOrder(chatList.get(i).getStatus())))
                status = chatList.get(i).getStatus();

            //Set text status
            if(status == null)
                chatHolder.chatSentText.setVisibility(View.GONE);
            else if (chatList.get(i).getStatus().compareTo(Constants.CHAT_MESSAGE_STATUS_NOT_SENT) == 0)
                chatHolder.chatSentText.setText(mContext.getString(R.string.status_not_sent));
            else if (chatList.get(i).getStatus().compareTo(Constants.CHAT_MESSAGE_STATUS_SENT) == 0)
                chatHolder.chatSentText.setText(mContext.getString(R.string.status_sent));
            else if (chatList.get(i).getStatus().compareTo(Constants.CHAT_MESSAGE_STATUS_DELIVERED) == 0)
                chatHolder.chatSentText.setText(mContext.getString(R.string.status_delivered));
            else if (chatList.get(i).getStatus().compareTo(Constants.CHAT_MESSAGE_STATUS_READ) == 0)
                chatHolder.chatSentText.setText(mContext.getString(R.string.status_read));
            else chatHolder.chatSentText.setVisibility(View.GONE);
        }

        //Set message time
        long currentTimestamp = chatList.get(i).getTimestamp();
        long previousTimestamp = 0;
        if(i>0) previousTimestamp = chatList.get(i-1).getTimestamp();

        long thirtyMinutesDiff = 30 * 60 * 1000;

        if(currentTimestamp - previousTimestamp > thirtyMinutesDiff ||
                previousTimestamp == 0)
        {
            chatHolder.chatSentTime.setVisibility(View.VISIBLE);
            chatHolder.chatSentTime.setText(Utils.getStringChatTimeDifference(currentTimestamp,mContext));
        }
        else
        {
            chatHolder.chatSentTime.setVisibility(View.GONE);
        }

        //Set message avatar
        String avatar;
        String contactId;
        String firstName;
        String lastName;

        if(chatHolder.getItemViewType() == Constants.LEFT_CHAT ||
                chatHolder.getItemViewType() == Constants.LEFT_IMAGE_CHAT)
        {
            avatar = contact.getAvatar();
            contactId = contact.getContactId();
            firstName = contact.getFirstName();
            lastName = contact.getLastName();
        }
        else
        {
            avatar = _profile.getAvatar();
            contactId = _profile.getId();
            firstName = _profile.getFirstName();
            lastName = _profile.getLastName();
        }

        File avatarFile = new File(mContext.getFilesDir(), Constants.CONTACT_AVATAR_DIR + "avatar_"+contactId+".jpg");

//        if(XMPPTransactions.getXMPPStatusOrder(chatList.get(i).getStatus())==0 &&
//                (chatHolder.getItemViewType() == Constants.RIGHT_CHAT ||
//                    chatHolder.getItemViewType() == Constants.RIGHT_IMAGE_CHAT) &&
//                i!=chatList.size()-1) {
//            chatHolder.chatAvatarImage.setImageResource(R.color.red_action);
//            chatHolder.chatAvatarText.setVisibility(View.INVISIBLE);
//            chatHolder.chatAvatarText.setVisibility(View.VISIBLE);
//        }
//        else if (avatar!=null &&

        if (avatar!=null &&
                avatar.length()>0 &&
                avatar.compareTo("")!=0 &&
                avatarFile.exists()) {

            chatHolder.chatAvatarText.setVisibility(View.INVISIBLE);

            Picasso.with(mContext)
                    .load(avatarFile)
                    .fit().centerCrop()
                    .into(chatHolder.chatAvatarImage);

        } else{
            String initials = "";
            if(null != firstName && firstName.length() > 0)
            {
                initials = firstName.substring(0, 1);

                if(null != lastName && lastName.length() > 0)
                {
                    initials = initials + lastName.substring(0,1);
                }

            }

            chatHolder.chatAvatarImage.setImageResource(R.color.grey_middle);
            chatHolder.chatAvatarText.setText(initials);
            chatHolder.chatAvatarText.setVisibility(View.VISIBLE);
        }

        //Set message as read
        if(chatList.get(i).getRead().compareTo("0")==0 &&
                chatList.get(i).getDirection().compareTo(Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)==0)
        {
            XMPPTransactions.notifyIQMessageStatus(chatList.get(i).getId(),
                    chatList.get(i).getContact_id(),
                    Constants.CHAT_MESSAGE_STATUS_READ);
            _chatTx.setChatMessageReceivedAsRead(chatList.get(i));
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        _chatTx.closeRealm();
    }

    private ChatMessage getNextSentChatMessage(int position)
    {
        for(int i=position+1; i<chatList.size(); i++)
        {
            if(chatList.get(i).getDirection().compareTo(Constants.CHAT_MESSAGE_DIRECTION_SENT)==0)
                return chatList.get(i);
        }

        return null;
    }
}
