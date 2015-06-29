package com.vodafone.mycomms.chatlist.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.readystatesoftware.viewbadger.BadgeView;
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import java.io.File;
import java.util.ArrayList;

import io.realm.Realm;
import model.Chat;
import model.Contact;

public class ChatListRecyclerViewAdapter extends RecyclerView.Adapter<ChatListHolder>{

    private ArrayList<Chat> mChat = new ArrayList<>();
    private Context mContext;
    private Realm _realm;
    private RealmChatTransactions _chatTx;
    private String _profileId;


    public ChatListRecyclerViewAdapter(Context context, ArrayList<Chat> Chat) {
        mContext = context;
        _realm = Realm.getInstance(mContext);
        _chatTx = new RealmChatTransactions(_realm, mContext);
        mChat = Chat;

        SharedPreferences sp = mContext.getSharedPreferences(Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        _profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    @Override
    public void onViewRecycled(ChatListHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public ChatListHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_list_item_chat, null);
        ChatListHolder chatHolder = new ChatListHolder(view);
        return chatHolder;
    }

    @Override
    public void onBindViewHolder(final ChatListHolder chatListHolder, final int i)
    {
        chatListHolder.layContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    v.setBackground(mContext.getResources().getDrawable(R.color.grey_light));
                    return true;
                }
                else if(event.getAction() == MotionEvent.ACTION_UP
                        ||
                        event.getAction() == MotionEvent.ACTION_CANCEL)
                {
                    v.setBackground(mContext.getResources().getDrawable(R.drawable.simpleborder));
                    return true;
                }
                else return false;

            }
        });

        //Get Contact
        RealmContactTransactions realmContactTransactions = new RealmContactTransactions(_realm, _profileId);
        Contact contact = realmContactTransactions.getContactById(mChat.get(i).getContact_id());

        if(contact != null)
            chatListHolder.textViewName.setText(contact.getFirstName() + " " + contact.getLastName());

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
            String initials = "";

            if(contact != null)
                initials = contact.getFirstName().substring(0, 1)
                        + contact.getLastName().substring(0,1);

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
