package com.vodafone.mycomms.chatlist.view;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.readystatesoftware.viewbadger.BadgeView;
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import java.io.File;
import java.util.ArrayList;

import io.realm.Realm;
import model.Chat;

public class ChatListRecyclerViewAdapter extends RecyclerView.Adapter<ChatListHolder>{

    private ArrayList<Chat> mChat = new ArrayList<>();
    private Context mContext;
    private Realm _realm;
    private RealmChatTransactions _chatTx;

    public ChatListRecyclerViewAdapter(Context context, ArrayList<Chat> Chat) {
        mContext = context;
        _realm = Realm.getInstance(mContext);
        _chatTx = new RealmChatTransactions(_realm, mContext);
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
        String timeDifference = Utils.getStringChatTimeDifference(mChat.get(i).getLastMessageTime());
        chatListHolder.textViewTime.setText(timeDifference);

        long count = _chatTx.getChatPendingMessagesCount(getChat(i).getContact_id());

        if(count > 0) {
            BadgeView badge = new BadgeView(mContext, chatListHolder.badgeUnread);
            badge.setText(String.valueOf(count));
            badge.setBadgePosition(BadgeView.POSITION_CENTER);
            badge.setBadgeBackgroundColor(Color.parseColor("#0071FF"));
            badge.show();
        }

        //Image avatar
        File avatarFile = new File(mContext.getFilesDir(), Constants.CONTACT_AVATAR_DIR + "avatar_"+mChat.get(i).getContact_id()+".jpg");

        if (mChat.get(i).getContact_id()!=null &&
                mChat.get(i).getContact_id().length()>0 &&
                mChat.get(i).getContact_id().compareTo("")!=0 &&
                avatarFile.exists()) {

            chatListHolder.textAvatar.setText(null);

            Picasso.with(mContext)
                    .load(avatarFile)
                    .fit().centerCrop()
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

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        if(_realm!=null)
            _realm.close();
    }
}
