package com.vodafone.mycomms.chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

import java.io.File;
import java.util.ArrayList;

import io.realm.Realm;
import model.ChatMessage;
import model.Contact;
import model.UserProfile;


public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatHolder>{

    private String LOG_TAG = ChatRecyclerViewAdapter.class.getSimpleName();
    private ArrayList<ChatMessage> chatList = new ArrayList<>();
    private Context mContext;
    private Contact _contact;
    private UserProfile _profile;
    private Realm _realm;
    private RealmChatTransactions _chatTx;

    public ChatRecyclerViewAdapter(Context context, ArrayList<ChatMessage> chatListItem, UserProfile profile, Contact contact) {
        mContext = context;
        _realm = Realm.getInstance(mContext);
        _chatTx = new RealmChatTransactions(_realm, mContext);

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
        if(chatList.get(i).getStatus().compareTo(Constants.CHAT_MESSAGE_STATUS_NOT_SENT)==0)
            chatHolder.chatSentText.setText(mContext.getString(R.string.status_not_sent));
        else if(chatList.get(i).getStatus().compareTo(Constants.CHAT_MESSAGE_STATUS_SENT)==0)
            chatHolder.chatSentText.setText(mContext.getString(R.string.status_sent));
        else if(chatList.get(i).getStatus().compareTo(Constants.CHAT_MESSAGE_STATUS_DELIVERED)==0)
            chatHolder.chatSentText.setText(mContext.getString(R.string.status_delivered));
        else if(chatList.get(i).getStatus().compareTo(Constants.CHAT_MESSAGE_STATUS_READ)==0)
            chatHolder.chatSentText.setText(mContext.getString(R.string.status_read));
        else chatHolder.chatSentText.setText("");

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
        String avatar;
        String contactId;
        String firstName;
        String lastName;

        if(chatHolder.getItemViewType() == Constants.LEFT_CHAT)
        {
            avatar = _contact.getAvatar();
            contactId = _contact.getContactId();
            firstName = _contact.getFirstName();
            lastName = _contact.getLastName();
        }
        else
        {
            avatar = _profile.getAvatar();
            contactId = _profile.getId();
            firstName = _profile.getFirstName();
            lastName = _profile.getLastName();
        }

        File avatarFile = new File(mContext.getFilesDir(), Constants.CONTACT_AVATAR_DIR + "avatar_"+contactId+".jpg");

        if (avatar!=null &&
                avatar.length()>0 &&
                avatar.compareTo("")!=0 &&
                avatarFile.exists()) {

            chatHolder.chatAvatarText.setText(null);

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
        }

        //Set message as read
        ChatMessage chatMsg = chatList.get(i);

        if(chatMsg.getRead().compareTo("0")==0 &&
                chatMsg.getDirection().compareTo(Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)==0)
        {
            if(XMPPTransactions.getXmppConnection()!=null &&
                    XMPPTransactions.getXmppConnection().isConnected()) {
                XMPPTransactions.notifyIQMessageStatus(chatMsg.getId(), chatMsg.getContact_id(),
                        Constants.CHAT_MESSAGE_STATUS_READ);
                _chatTx.setChatMessageReceivedAsRead(chatMsg);
            }
        }
    }

    @Override
     public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        if(_realm!=null)
            _realm.close();
    }
}
