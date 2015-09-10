package com.vodafone.mycomms.chatlist.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
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
import com.squareup.picasso.Callback;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatgroup.ComposedChat;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import io.realm.Realm;
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
    private Realm realm;

    public ChatListRecyclerViewAdapter(Context context, ArrayList<ComposedChat> composedChats,
                                       Realm realm)
    {
        mContext = context;
        this.realm = realm;
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
                int sdk = Build.VERSION.SDK_INT;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (sdk < Build.VERSION_CODES.JELLY_BEAN)
                        v.setBackgroundDrawable(mContext.getResources().getDrawable(R.color.grey_light));
                    else
                        v.setBackground(mContext.getResources().getDrawable(R.color.grey_light));
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP
                        ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {
                    if (sdk < Build.VERSION_CODES.JELLY_BEAN)
                        v.setBackground(mContext.getResources().getDrawable(R.drawable.simple_border));
                    else
                        v.setBackground(mContext.getResources().getDrawable(R.drawable.simple_border));
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
                .getChat().getLastMessageTime(), mContext);
        chatListHolder.textViewTime.setText(timeDifference);

        //Show visibility to availability
        chatListHolder.chat_availability.setVisibility(View.VISIBLE);

        long amountUnreadMessages = _chatTx.getChatPendingMessagesCount(getComposedChat(i)
                .getChat().getContact_id(), realm);

        if(amountUnreadMessages > 0) {
            chatListHolder.badgeUnread.setVisibility(View.VISIBLE);
            chatListHolder.badgeUnread.setText(Long.toString(amountUnreadMessages));
            if(amountUnreadMessages>99)
            {
                chatListHolder.badgeUnread.setText("99+");
            }
        }
        else
            chatListHolder.badgeUnread.setVisibility(View.GONE);

        Contact contact = mContactTransactions.getContactById(getComposedChat(i).getChat()
                .getContact_id(), realm);

        chatListHolder.top_left_avatar_text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        loadComposedAvatar
                (
                        contact
                        , chatListHolder.top_left_avatar
                        , chatListHolder.top_left_avatar_text
                );
    }

    private String getChatMemberName(String contactId)
    {
        Contact contact = mContactTransactions.getContactById(contactId, realm);
        String name = contact.getFirstName() + " " + contact.getLastName();
        return  name;
    }

    private String getChatMemberInitials(String contactId)
    {
        Contact contact = mContactTransactions.getContactById(contactId, realm);
        String name = contact.getFirstName().substring(0,1) + contact.getLastName().substring(0,1);
        return  name;
    }


    private ArrayList<Contact> loadContactsFromIds
            (
                    ArrayList<String> ids
            )
    {
        ArrayList<Contact> contacts = new ArrayList<>();
        RealmContactTransactions realmContactTransactions =
                new RealmContactTransactions(profileId);
        UserProfile userProfile = RealmContactTransactions.getUserProfile(realm, profileId);
        Contact contact = new Contact();
        contact.setAvatar(userProfile.getAvatar());
        contact.setFirstName(userProfile.getFirstName());
        contact.setLastName(userProfile.getLastName());
        contact.setContactId(userProfile.getId());
        contact.setPlatform(userProfile.getPlatform());
        contacts.add(contact);
        for(String id : ids)
        {
            if(!id.equals(userProfile.getId()))
            {
                contact = realmContactTransactions.getContactById(id,realm);
                if(null != contact)
                    contacts.add(contact);
            }
        }
        return contacts;
    }

    private void loadGroupChat(final ChatListHolder chatListHolder, final int i)
    {
        HashMap<ImageView, TextView> hashMapImageText = new HashMap<>();
        hashMapImageText.put(chatListHolder.top_left_avatar, chatListHolder.top_left_avatar_text);
        hashMapImageText.put(chatListHolder.top_right_avatar, chatListHolder.top_right_avatar_text);
        hashMapImageText.put(chatListHolder.bottom_left_avatar,chatListHolder
                .bottom_left_avatar_text);
        hashMapImageText.put(chatListHolder.bottom_right_avatar, chatListHolder
                .bottom_right_avatar_text);


        //Show visibility to availability
        chatListHolder.chat_availability.setVisibility(View.GONE);

        //Loads avatar visibility for group chat mode
        ArrayList<ImageView> images = new ArrayList<>();
        images.add(chatListHolder.top_left_avatar);
        images.add(chatListHolder.bottom_left_avatar);
        images.add(chatListHolder.bottom_right_avatar);

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
        }

        chatListHolder.bottom_left_chat_availability.setVisibility(View.VISIBLE);
        chatListHolder.bottom_right_chat_availability.setVisibility(View.VISIBLE);
        chatListHolder.top_left_chat_availability.setVisibility(View.VISIBLE);
        chatListHolder.top_right_chat_availability.setVisibility(View.VISIBLE);

        ArrayList<String> contactIds = new ArrayList<>();
        Collections.addAll(contactIds, membersArray);

        ArrayList<Contact> contacts = loadContactsFromIds(contactIds);

        String contactName = null;
        int count = 0;
        for(ImageView image : images)
        {
            if(contacts.size() > count)
            {
                Contact contact = contacts.get(count);

                if(null != contact)
                {
                    hashMapImageText.get(image).setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

                    try
                    {
                        if (null == contactName)
                            contactName = contact.getFirstName();
                        else
                            contactName = contactName + ", " + contact.getFirstName();

                        loadComposedAvatar
                                (
                                        contact
                                        , images.get(count)
                                        , hashMapImageText.get(image)
                                );
                    } catch (Exception e){
                        Log.e(Constants.TAG, "ChatListRecyclerViewAdapter.loadGroupChat: Error getting contact by Id");
                        Crashlytics.logException(e);
                    }
                }
                count++;
            }

        }
        chatListHolder.textViewName.setText(contactName);
        chatListHolder.textViewMessage.setText(groupChat.getLastMessage());
        String timeDifference = Utils.getStringChatTimeDifference(groupChat.getLastMessageTime(), mContext);
        chatListHolder.textViewTime.setText(timeDifference);

        //Load unread messages
        long amountUnreadMessages = groupChatTransactions.getGroupChatPendingMessagesCount(
                getComposedChat(i).getGroupChat().getId(), realm);

        if(amountUnreadMessages > 0) {
            chatListHolder.badgeUnread.setVisibility(View.VISIBLE);
            chatListHolder.badgeUnread.setText(Long.toString(amountUnreadMessages));
            if(amountUnreadMessages>99)
            {
                chatListHolder.badgeUnread.setText("99+");
            }
        }
        else
            chatListHolder.badgeUnread.setVisibility(View.GONE);
    }

    private void loadComposedAvatar
            (
                    Contact contact
                    , final ImageView image
                    , final TextView text
            )
    {
        Utils.loadContactAvatar
                (
                        contact.getFirstName()
                        , contact.getLastName()
                        , image
                        , text
                        , contact.getAvatar()
                );
    }

    public ComposedChat getComposedChat(int position) {
        return (null != composedChat ? composedChat.get(position) : null);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }
}
