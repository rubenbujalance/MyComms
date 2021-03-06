package com.vodafone.mycomms.contacts.view;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.util.AvatarSFController;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import model.Contact;
import model.GroupChat;
import model.RecentContact;
import model.UserProfile;

public class RecentListViewArrayAdapter extends ArrayAdapter<RecentContact>
{
    private Context mContext;
    private String _profile_id;
    private Realm realm;

    public RecentListViewArrayAdapter(Context context, List<RecentContact> items, String
            profileId, Realm realm)
    {
        super(context, R.layout.layout_list_item_recent, items);
        this.mContext = context;
        this.realm = realm;
        this._profile_id = profileId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final RecentViewHolder viewHolder;

        if(convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.layout_list_item_recent, parent, false);

            viewHolder = new RecentViewHolder();
            viewHolder.textViewCompany = (TextView) convertView.findViewById(R.id.list_item_content_company);
            viewHolder.textViewName = (TextView) convertView.findViewById(R.id.list_item_content_name);
            viewHolder.textViewOccupation = (TextView) convertView.findViewById(R.id.list_item_content_position);
            viewHolder.textViewTime = (TextView) convertView.findViewById(R.id.list_item_recent_info_time);
            viewHolder.imageViewRecentType = (ImageView) convertView.findViewById(R.id.list_item_recent_type_image);
            viewHolder.textViewRecentItemTime = (TextView) convertView.findViewById(R.id.list_item_recent_info_time);

            viewHolder.top_left_avatar = (ImageView) convertView.findViewById(R.id.top_left_avatar);
            viewHolder.top_right_avatar = (ImageView) convertView.findViewById(R.id.top_right_avatar);
            viewHolder.bottom_left_avatar = (ImageView) convertView.findViewById(R.id.bottom_left_avatar);
            viewHolder.bottom_right_avatar = (ImageView) convertView.findViewById(R.id.bottom_right_avatar);

            viewHolder.top_left_avatar_text = (TextView) convertView.findViewById(R.id.top_left_avatar_text);
            viewHolder.top_right_avatar_text = (TextView) convertView.findViewById(R.id.top_right_avatar_text);
            viewHolder.bottom_left_avatar_text = (TextView) convertView.findViewById(R.id.bottom_left_avatar_text);
            viewHolder.bottom_right_avatar_text = (TextView) convertView.findViewById(R.id.bottom_right_avatar_text);

            viewHolder.lay_top_right_image_hide = (LinearLayout) convertView.findViewById(R.id.lay_top_right_image_hide);
            viewHolder.lay_bottom_both_image_hide = (LinearLayout) convertView.findViewById(R.id.lay_bottom_both_image_hide);

            viewHolder.lay_top_left_image = (LinearLayout) convertView.findViewById(R.id.lay_top_left_image);

            viewHolder.bottom_right_chat_availability = (ImageView) convertView.findViewById(R.id.bottom_right_chat_availability);
            viewHolder.bottom_left_chat_availability = (ImageView) convertView.findViewById(R.id.bottom_left_chat_availability);
            viewHolder.top_right_chat_availability = (ImageView) convertView.findViewById(R.id.top_right_chat_availability);
            viewHolder.top_left_chat_availability = (ImageView) convertView.findViewById(R.id.top_left_chat_availability);
            convertView.setTag(viewHolder);
        }
        else
        {
            // recycle the already inflated view
              viewHolder = (RecentViewHolder) convertView.getTag();
        }

        // update the item view
        RecentContact contact = getItem(position);

        if(contact.getId().startsWith("mg_"))
            loadGroupChatView(contact, viewHolder);
        else
            loadContactView(contact,viewHolder);

        return convertView;
    }

    private ArrayList<Contact> getGroupChatContacts(String groupChatId)
    {
        ArrayList<Contact> contacts = new ArrayList<>();
        new RealmGroupChatTransactions(mContext, _profile_id);

        GroupChat groupChat = RealmGroupChatTransactions.getGroupChatById(groupChatId, realm);
        //TODO: ERROR 'java.lang.String model.GroupChat.getMembers()' on a null object reference
        if(groupChat==null) return null;

        String[] ids = groupChat.getMembers().split("@");

        new RealmContactTransactions(_profile_id);

        UserProfile userProfile = RealmContactTransactions.getUserProfile(realm, _profile_id);
        Contact contact = new Contact();
        contact.setAvatar(userProfile.getAvatar());
        contact.setFirstName(userProfile.getFirstName());
        contact.setLastName(userProfile.getLastName());
        contact.setContactId(userProfile.getId());
        contact.setId(_profile_id + "_" + userProfile.getId());
        contacts.add(contact);

        for(String id : ids)
        {
            if(!id.equals(_profile_id))
            {
                contact = RealmContactTransactions.getContactById(id, realm);
                contacts.add(contact);
            }
        }

        return contacts;
    }

    private String getComposedName(ArrayList<Contact> contacts)
    {
        String composedName = null;
        for(Contact contact : contacts)
        {
            if(null != contact)
            {
                try {
                    String contactFirstName = null;
                    if (null == contact.getFirstName()) {
                        contactFirstName = "Unknown";
                    } else {
                        contactFirstName = contact.getFirstName();
                    }
                    if (null == composedName) composedName = contactFirstName;
                    else composedName = composedName + ", " + contactFirstName;
                } catch (Exception e){
                    Log.e(Constants.TAG, "RecentListViewArrayAdapter.getComposedName: contact first name null");
                    Crashlytics.logException(e);
                }
            }

        }
        return composedName;
    }

    private void loadContactView(RecentContact contact, final RecentViewHolder viewHolder)
    {
        viewHolder.bottom_right_chat_availability.setVisibility(View.GONE);
        viewHolder.bottom_left_chat_availability.setVisibility(View.GONE);
        viewHolder.top_right_chat_availability.setVisibility(View.GONE);
        viewHolder.top_left_chat_availability.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams params = viewHolder.top_left_chat_availability.getLayoutParams();
        params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25,
                mContext.getResources().getDisplayMetrics());
        params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25,
                mContext.getResources().getDisplayMetrics());
        viewHolder.top_left_chat_availability.setLayoutParams(params);

        viewHolder.textViewCompany.setVisibility(View.VISIBLE);

        viewHolder.lay_top_right_image_hide.setVisibility(View.GONE);
        viewHolder.lay_bottom_both_image_hide.setVisibility(View.GONE);
        viewHolder.lay_top_left_image.setLayoutParams
                (
                        new LinearLayout.LayoutParams
                                (
                                        LinearLayout.LayoutParams.MATCH_PARENT
                                        , LinearLayout.LayoutParams.MATCH_PARENT
                                )
                );

        viewHolder.top_left_avatar_text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);

        new RealmContactTransactions(_profile_id);
        Contact cont = RealmContactTransactions.getContactById(contact.getContactId(), realm);

        if(null != contact.getPlatform() && Constants.PLATFORM_SALES_FORCE.equals(contact.getPlatform()))
        {
            AvatarSFController avatarSFController = new AvatarSFController
                    (
                            mContext, contact.getContactId(), _profile_id
                    );
            avatarSFController.getSFAvatar(contact.getAvatar());
        }

        Utils.loadContactAvatar
                (
                        contact.getFirstName()
                        , contact.getLastName()
                        , viewHolder.top_left_avatar
                        , viewHolder.top_left_avatar_text
                        , Utils.getAvatarURL
                                (
                                        cont.getPlatform()
                                        , cont.getStringField1()
                                        , cont.getAvatar()
                                )
                );

        viewHolder.textViewName.setText(contact.getFirstName() + " " + contact.getLastName());
        viewHolder.textViewOccupation.setText(contact.getPosition());
        viewHolder.textViewCompany.setText(contact.getCompany());

        if(contact.getAction().equals("")) {
            viewHolder.imageViewRecentType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_error_tooltip));
        } else if (contact.getAction().equals(Constants.CONTACTS_ACTION_SMS)) {
            viewHolder.imageViewRecentType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_rec_chat_grey));
        } else if (contact.getAction().equals(Constants.CONTACTS_ACTION_EMAIL)) {
            viewHolder.imageViewRecentType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_rec_email_grey));
        } else if (contact.getAction().equals(Constants.CONTACTS_ACTION_CALL)) {
            viewHolder.imageViewRecentType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_rec_phone_grey));
        }
        viewHolder.textViewTime.setText(Utils.getStringChatTimeDifference(contact.getTimestamp(), mContext));
    }

    private void loadGroupChatView(RecentContact contact,RecentViewHolder viewHolder)
    {
        ArrayList<Contact> contacts = getGroupChatContacts(contact.getId());
        if(contacts==null) return;

        String composedName = getComposedName(contacts);
        if(contacts.size() > 3)
            viewHolder.lay_top_right_image_hide.setVisibility(View.VISIBLE);
        else
            viewHolder.lay_top_right_image_hide.setVisibility(View.GONE);

        viewHolder.lay_bottom_both_image_hide.setVisibility(View.VISIBLE);
        viewHolder.textViewCompany.setVisibility(View.GONE);
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30,
        mContext.getResources().getDisplayMetrics());
        viewHolder.lay_top_left_image.setLayoutParams
                (
                        new LinearLayout.LayoutParams(width, width)
                );

        ViewGroup.LayoutParams params = viewHolder.top_left_chat_availability.getLayoutParams();
        params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                mContext.getResources().getDisplayMetrics());
        params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                mContext.getResources().getDisplayMetrics());
        viewHolder.top_left_chat_availability.setLayoutParams(params);

        ArrayList<ImageView> images = new ArrayList<>();
        images.add(viewHolder.top_left_avatar);
        images.add(viewHolder.bottom_left_avatar);
        images.add(viewHolder.bottom_right_avatar);
        if(contacts.size() > 3)
            images.add(viewHolder.top_right_avatar);

        ArrayList<TextView> imagesText = new ArrayList<>();
        imagesText.add(viewHolder.top_left_avatar_text);
        imagesText.add(viewHolder.bottom_left_avatar_text);
        imagesText.add(viewHolder.bottom_right_avatar_text);
        if(contacts.size() > 3)
            imagesText.add(viewHolder.top_right_avatar_text);

        int i = 0;
        Contact cont;
        for(ImageView imageView : images)
        {

            cont = contacts.get(i);

            if(null != cont)
            {
                Utils.loadContactAvatar
                        (
                                cont.getFirstName()
                                , cont.getLastName()
                                , imageView
                                , imagesText.get(i)
                                , cont.getAvatar()
                        );
            }
            i++;
        }

        viewHolder.imageViewRecentType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_rec_chat_grey));
        viewHolder.textViewTime.setText(Utils.getStringChatTimeDifference(contact.getTimestamp(), mContext));
        viewHolder.textViewName.setText(composedName);
        viewHolder.textViewOccupation.setText(mContext.getString(R.string.group_chat));

        viewHolder.bottom_right_chat_availability.setVisibility(View.VISIBLE);
        viewHolder.bottom_left_chat_availability.setVisibility(View.VISIBLE);
        viewHolder.top_right_chat_availability.setVisibility(View.VISIBLE);
        viewHolder.top_left_chat_availability.setVisibility(View.VISIBLE);
    }

    /**
     * The view holder design pattern
     */
    private class RecentViewHolder {
        TextView textViewName;
        TextView textViewOccupation;
        TextView textViewCompany;
        TextView textViewTime;
        TextView textViewRecentItemTime;
        ImageView imageViewRecentType;

        ImageView bottom_right_chat_availability;
        ImageView bottom_left_chat_availability;
        ImageView top_right_chat_availability;
        ImageView top_left_chat_availability;

        ImageView top_left_avatar;
        ImageView top_right_avatar;
        ImageView bottom_left_avatar;
        ImageView bottom_right_avatar;

        TextView top_left_avatar_text;
        TextView top_right_avatar_text;
        TextView bottom_left_avatar_text;
        TextView bottom_right_avatar_text;

        LinearLayout lay_top_right_image_hide;
        LinearLayout lay_bottom_both_image_hide;
        LinearLayout lay_top_left_image;
    }
}