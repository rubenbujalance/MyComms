package com.vodafone.mycomms.chatlist.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.readystatesoftware.viewbadger.BadgeView;
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatgroup.ComposedChat;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import java.io.File;
import java.util.ArrayList;

import model.Contact;
import model.GroupChat;
import model.UserProfile;

public class ChatListRecyclerViewAdapter extends RecyclerView.Adapter<ChatListHolder>{

    private ArrayList<ComposedChat> composedChat = new ArrayList<>();
    private Context mContext;
    private RealmChatTransactions _chatTx;
    private RealmGroupChatTransactions groupChatTransactions;
    private RealmContactTransactions mContactTransactions;
    private String profileId;

    public ChatListRecyclerViewAdapter(Context context, ArrayList<ComposedChat> composedChats)
    {
        mContext = context;
        _chatTx = new RealmChatTransactions(mContext);
        this.composedChat = composedChats;
        SharedPreferences sp = mContext.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
        groupChatTransactions = new RealmGroupChatTransactions(mContext, profileId);
        mContactTransactions = new RealmContactTransactions(profileId);
    }

    @Override
    public int getItemCount() {
        return composedChat.size();
    }

    @Override
    public void onViewRecycled(ChatListHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public ChatListHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_list_item_group_chat, null);
        ChatListHolder chatHolder = new ChatListHolder(view);
        return chatHolder;
    }

    @Override
    public void onBindViewHolder(final ChatListHolder chatListHolder, final int i)
    {
        chatListHolder.layContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackground(mContext.getResources().getDrawable(R.color.grey_light));
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP
                        ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {
                    v.setBackground(mContext.getResources().getDrawable(R.drawable.simpleborder));
                    return true;
                } else return false;
            }
        });

        if(null != composedChat.get(i).getChat())
            loadChat(chatListHolder, i);
        else if(null != composedChat.get(i).getGroupChat())
            loadGroupChat(chatListHolder, i);
    }

    private void loadChat(final ChatListHolder chatListHolder, final int i)
    {
        //Show visibility to avatars
        chatListHolder.lay_top_right_image_hide.setVisibility(View.GONE);
        chatListHolder.lay_bottom_both_image_hide.setVisibility(View.GONE);
        chatListHolder.lay_top_left_image.setLayoutParams
                (
                        new LinearLayout.LayoutParams
                                (
                                        LinearLayout.LayoutParams.MATCH_PARENT
                                        , LinearLayout.LayoutParams.MATCH_PARENT
                                )
                );

        chatListHolder.textViewName.setText
                (
                        getChatMemberName(composedChat.get(i).getChat().getContact_id())
                );

        chatListHolder.textViewMessage.setText(composedChat.get(i).getChat().getLastMessage());
        String timeDifference = Utils.getStringChatTimeDifference(composedChat.get(i)
                .getChat().getLastMessageTime());
        chatListHolder.textViewTime.setText(timeDifference);

        long amountUnreadMessages = _chatTx.getChatPendingMessagesCount(getComposedChat(i).getChat().getContact_id());

        if(amountUnreadMessages > 0) {
            BadgeView badge = new BadgeView(mContext, chatListHolder.badgeUnread);
            badge.setText(String.valueOf(amountUnreadMessages));
            badge.setBadgePosition(BadgeView.POSITION_CENTER);
            badge.setBadgeBackgroundColor(Color.parseColor("#0071FF"));
            badge.show();
        }

        //Image avatar
        File avatarFile = new File
                (
                        mContext.getFilesDir()
                        , Constants.CONTACT_AVATAR_DIR + "avatar_"+composedChat.get(i)
                        .getChat().getContact_id()+".jpg"
                );

        if (composedChat.get(i).getChat().getContact_id()!=null &&
                composedChat.get(i).getChat().getContact_id().length()>0 &&
                composedChat.get(i).getChat().getContact_id().compareTo("")!=0 &&
                avatarFile.exists()) {

            chatListHolder.top_left_avatar_text.setText(null);

            Picasso.with(mContext)
                    .load(avatarFile)
                    .fit().centerCrop()
                    .into(chatListHolder.top_left_avatar);

        } else{
            String initials = getChatMemberInitials(composedChat.get(i).getChat().getContact_id());


            chatListHolder.top_left_avatar.setImageResource(R.color.grey_middle);
            chatListHolder.top_left_avatar_text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            chatListHolder.top_left_avatar_text.setText(initials);
        }
    }

    private String getChatMemberName(String contactId)
    {
        Contact contact = mContactTransactions.getContactById(contactId);
        String name = contact.getFirstName() + " " + contact.getLastName();
        return  name;
    }

    private String getChatMemberInitials(String contactId)
    {
        Contact contact = mContactTransactions.getContactById(contactId);
        String name = contact.getFirstName().substring(0,1) + contact.getLastName().substring(0,1);
        return  name;
    }


    private void loadGroupChat(final ChatListHolder chatListHolder, final int i)
    {
        //Loads avatar visibility for group chat mode
        ArrayList<ImageView> images = new ArrayList<>();
        images.add(chatListHolder.top_left_avatar);
        images.add(chatListHolder.bottom_left_avatar);
        images.add(chatListHolder.bottom_right_avatar);

        ArrayList<TextView> texts = new ArrayList<>();
        texts.add(chatListHolder.top_left_avatar_text);
        texts.add(chatListHolder.bottom_left_avatar_text);
        texts.add(chatListHolder.bottom_right_avatar_text);



        GroupChat groupChat = composedChat.get(i).getGroupChat();
        String members = groupChat.getMembers();
        String[] membersArray = members.split("@");

        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30,
                mContext.getResources().getDisplayMetrics());
        chatListHolder.lay_top_left_image.setLayoutParams
                (
                        new LinearLayout.LayoutParams(width,width)
                );
        chatListHolder.lay_bottom_both_image_hide.setVisibility(View.VISIBLE);

        if(membersArray.length > 3)
        {
            chatListHolder.lay_top_right_image_hide.setVisibility(View.VISIBLE);
            images.add(chatListHolder.top_right_avatar);
            texts.add(chatListHolder.top_right_avatar_text);
        }

        String contactName = null;
        int count = 0;
        for(String id : membersArray)
        {
            if(id.equals(profileId))
            {
                UserProfile userProfile = mContactTransactions.getUserProfile();
                if(null == contactName)
                    contactName = userProfile.getFirstName();
                else
                    contactName = contactName + ", " + userProfile.getFirstName();

                Contact contact = new Contact();
                contact.setContactId(id);
                contact.setFirstName(userProfile.getFirstName());
                contact.setLastName(userProfile.getLastName());
                contact.setAvatar(userProfile.getAvatar());
                if(count < 4)
                {
                    loadComposedAvatar
                            (
                                    count
                                    , id
                                    , contact
                                    , images
                                    , texts
                            );
                    count++;
                }
            }
            else
            {
                try {
                    Contact contact = mContactTransactions.getContactById(id);
                    String contactFirstName = null;
                    if (null == contact) {
                        contactFirstName = "Unknown";
                    } else {
                        contactFirstName = contact.getFirstName();
                    }
                    if (null == contactName)
                        contactName = contactFirstName;
                    else
                        contactName = contactName + ", " + contactFirstName;
                    if (count < 4) {
                        loadComposedAvatar
                                (
                                        count
                                        , id
                                        , contact
                                        , images
                                        , texts
                                );
                        count++;
                    }
                } catch (Exception e){
                    Log.e(Constants.TAG, "ChatListRecyclerViewAdapter.loadGroupChat: Error getting contact by Id");
                    Crashlytics.logException(e);
                }
            }

        }
        chatListHolder.textViewName.setText(contactName);
        chatListHolder.textViewMessage.setText(groupChat.getLastMessage());
        String timeDifference = Utils.getStringChatTimeDifference(groupChat.getLastMessageTime());
        chatListHolder.textViewTime.setText(timeDifference);

        //Load unread messages
        long amountUnreadMessages = groupChatTransactions.getGroupChatPendingMessagesCount(
                getComposedChat(i).getGroupChat().getId());

        if(amountUnreadMessages > 0) {
            BadgeView badge = new BadgeView(mContext, chatListHolder.badgeUnread);
            badge.setText(String.valueOf(amountUnreadMessages));
            badge.setBadgePosition(BadgeView.POSITION_CENTER);
            badge.setBadgeBackgroundColor(Color.parseColor("#0071FF"));
            badge.show();
        }
    }

    private void loadComposedAvatar
            (
                    int count
                    , String id
                    , Contact contact
                    , ArrayList<ImageView> images
                    , ArrayList<TextView> texts
            )
    {
        File avatarFile = new File(mContext.getFilesDir(), Constants.CONTACT_AVATAR_DIR +
                "avatar_"+id+".jpg");

        if (contact.getAvatar()!=null &&
                contact.getAvatar().length()>0 &&
                contact.getAvatar().compareTo("")!=0 &&
                avatarFile.exists())
        {

            Picasso.with(mContext)
                    .load(avatarFile)
                    .fit().centerCrop()
                    .into(images.get(count));

        } else{
            String initials = "";
            if(null != contact.getFirstName() && contact.getFirstName().length() > 0)
            {
                initials = contact.getFirstName().substring(0,1);

                if(null != contact.getLastName() && contact.getLastName().length() > 0)
                {
                    initials = initials + contact.getLastName().substring(0,1);
                }
            }
            images.get(count).setImageResource(R.color.grey_middle);
            texts.get(count).setText(initials);
        }
    }

    public ComposedChat getComposedChat(int position) {
        return (null != composedChat ? composedChat.get(position) : null);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        _chatTx.closeRealm();
        mContactTransactions.closeRealm();
    }
}
