package com.vodafone.mycomms.chat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatgroup.FullscreenImageActivity;
import com.vodafone.mycomms.main.NewsDetailActivity;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmNewsTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

import java.io.File;
import java.util.ArrayList;

import model.ChatMessage;
import model.Contact;
import model.News;
import model.UserProfile;


public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatHolder>{

    private String LOG_TAG = ChatRecyclerViewAdapter.class.getSimpleName();
    private ArrayList<ChatMessage> chatList = new ArrayList<>();
    private Context mContext;
    private UserProfile _profile;
    private RealmChatTransactions _chatTx;
    private RealmContactTransactions _contactTx;
    private boolean isGroupChatMode;

    public ChatRecyclerViewAdapter(Context context, ArrayList<ChatMessage> chatListItem,
                                   UserProfile profile, boolean isGroupChatMode) {
        mContext = context;
        this._profile = profile;
        this.isGroupChatMode = isGroupChatMode;

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
    public void onBindViewHolder(final ChatHolder chatHolder, int i)
    {

        Contact contact = null;

        if(null != chatList.get(i).getContact_id() && chatList.get(i).getContact_id().length() >
                0 && chatList.get(i).getDirection().equals(Constants.CHAT_MESSAGE_DIRECTION_RECEIVED))
        {
            contact = _contactTx.getContactById(chatList.get(i).getContact_id());
        }
        else
        {
            if(null != _profile)
            {
                contact = new Contact();
                contact.setAvatar(_profile.getAvatar());
                contact.setFirstName(_profile.getFirstName());
                contact.setLastName(_profile.getLastName());
                contact.setContactId(_profile.getId());
                contact.setPlatform(_profile.getPlatform());
            }
        }

        if(chatList.get(i).getType()==Constants.CHAT_MESSAGE_TYPE_IMAGE)
        {
            String dirStr = mContext.getFilesDir() + Constants.CONTACT_CHAT_FILES;
            final String fileStr = "file_" + chatList.get(i).getId() + ".jpg";

            final File image = new File(dirStr, fileStr);

            MycommsApp.picasso
                    .load(image)
                    .fit().centerCrop()
                    .into(chatHolder.chatImage);

            chatHolder.chatImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in = new Intent(mContext, FullscreenImageActivity.class);
                    in.putExtra("imageFilePath", image.getAbsolutePath());
                    mContext.startActivity(in);
                }
            });
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

            //Hiding delivered message in group chat mode
            if(isGroupChatMode)
                chatHolder.chatSentText.setVisibility(View.GONE);
            else
            {
                //Set text status
                if (status == null)
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

        if(null != contact)
        {
            Utils.loadContactAvatar(contact.getFirstName(), contact.getLastName(), chatHolder
                    .chatAvatarImage, chatHolder.chatAvatarText, contact.getAvatar());
        }

        if(null != chatHolder.chatTextView)
            setTextListeners(chatHolder.chatTextView);
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

    private void setTextListeners(final TextView chatText)
    {
        chatText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String text = ((TextView)v).getText().toString();
                if(null != text && text.length() > 0)
                {
                    if(text.startsWith(Constants.INTERNAL_URL_PATTERN_NEWS))
                    {
                        String newsId = text.replace(Constants.INTERNAL_URL_PATTERN_NEWS, "");
                        News news = getNewsById(newsId);
                        if(null != news)
                            startNewsActivity(news);
                    }
                }
            }
        });
    }

    private void startNewsActivity(News news)
    {
        Intent in = new Intent(mContext, NewsDetailActivity.class);
        in.putExtra(Constants.NEWS_IMAGE, news.getImage());
        in.putExtra(Constants.NEWS_TITLE, news.getTitle());
        in.putExtra(Constants.NEWS_AUTHOR_AVATAR, news.getAuthor_avatar());
        in.putExtra(Constants.NEWS_AUTHOR_NAME, news.getAuthor_name());
        in.putExtra(Constants.NEWS_PUBLISHED_AT, news.getPublished_at());
        in.putExtra(Constants.NEWS_HTML, news.getHtml());
        in.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);


        mContext.startActivity(in);
    }

    private News getNewsById(String id)
    {
        RealmNewsTransactions realmNewsTransactions = new RealmNewsTransactions();
        News news = realmNewsTransactions.getNewById(id);
        realmNewsTransactions.closeRealm();
        return news;
    }

   
}
