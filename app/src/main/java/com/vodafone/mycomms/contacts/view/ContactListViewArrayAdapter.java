package com.vodafone.mycomms.contacts.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.contacts.connection.ContactsController;
import com.vodafone.mycomms.util.AvatarSFController;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import model.Contact;

public class ContactListViewArrayAdapter extends ArrayAdapter<Contact> {
    private Context mContext;
    private String profileId;
    private ContactsController mContactsController;
    private String userProfileEmails;

    public ContactListViewArrayAdapter(Context context, List<Contact> items) {
        super(context, R.layout.layout_list_item_contact, items);
        this.mContext = context;
        SharedPreferences sp = mContext.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
        mContactsController = new ContactsController(profileId, mContext);
        userProfileEmails = mContactsController.getUserProfileEmails();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.layout_list_item_contact, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.imageAvatar = (ImageView) convertView.findViewById(R.id.companyLogo);
            viewHolder.textAvatar = (TextView) convertView.findViewById(R.id.avatarText);
            viewHolder.textViewCompany = (TextView) convertView.findViewById(R.id.list_item_content_company);
            viewHolder.textViewName = (TextView) convertView.findViewById(R.id.list_item_content_name);
            viewHolder.textViewPosition = (TextView) convertView.findViewById(R.id.list_item_content_position);
            viewHolder.textViewTime = (TextView) convertView.findViewById(R.id.list_item_status_local_time);
            viewHolder.textViewCountry = (TextView) convertView.findViewById(R.id.list_item_status_local_country);
            viewHolder.imageViewDayNight = (ImageView) convertView.findViewById(R.id.list_item_image_status_daynight);
            viewHolder.imageCompanyLogo = (ImageView) convertView.findViewById(R.id.list_item_content_companylogo);
            viewHolder.platformLabel = (TextView) convertView.findViewById(R.id.platform_label);
            viewHolder.chatAvailability = (ImageView) convertView.findViewById(R.id.chat_availability);
            viewHolder.layInviteMyComms = (LinearLayout) convertView.findViewById(R.id.lay_invite_mycomms);

            convertView.setTag(viewHolder);
        } else
        {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.imageAvatar.setImageDrawable(null);
        }

        // update the item view
        final Contact contact = getItem(position);
        String noRecordFound = mContext.getResources().getString(R.string.no_search_records);

        //Check if contact can be invited to MyComms

        if(mContactsController.isContactCanBeInvited(contact, userProfileEmails))
            viewHolder.layInviteMyComms.setVisibility(View.VISIBLE);
        else
            viewHolder.layInviteMyComms.setVisibility(View.GONE);

        viewHolder.layInviteMyComms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                mContactsController.createInviteAlertWithEvents(contact);
            }
        });

        //validate if separator is needed
        boolean showSeparator;
        if (null != contact.getPlatform()) {
            if (position == 0)
                showSeparator = true;
            else {
                if (!contact.getPlatform().equals(getItem(position - 1).getPlatform()))
                    showSeparator = true;
                else
                    showSeparator = false;
            }
            if (showSeparator) {
                viewHolder.platformLabel.setVisibility(View.VISIBLE);
                viewHolder.platformLabel.setText(Utils.getPlaformName(contact.getPlatform(), getContext()));
            } else {
                viewHolder.platformLabel.setVisibility(View.GONE);
            }
        }

        //validate if it's a no resuld found record from the search
        if(null != contact.getPlatform()
                && contact.getFirstName().equals(noRecordFound))
        {
            MycommsApp.picasso
                    .load(R.drawable.ic_no_results)
                    .noFade()
                    .fit().centerCrop()
                    .into(viewHolder.imageAvatar);
            viewHolder.textViewName.setText("");
            viewHolder.textViewCompany.setText("");
            viewHolder.textViewCountry.setText("");
            viewHolder.textViewTime.setText("");
            viewHolder.textAvatar.setText("");
            viewHolder.imageCompanyLogo.setVisibility(View.GONE);
            viewHolder.imageViewDayNight.setVisibility(View.GONE);
            viewHolder.textViewPosition.setText(contact.getFirstName());
            viewHolder.textViewPosition.setGravity(Gravity.CENTER);
            viewHolder.textViewPosition.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            viewHolder.textViewPosition.setTextColor(mContext.getResources().getColor(R.color.contact_very_soft_grey));
            viewHolder.imageCompanyLogo.setVisibility(View.GONE);
            viewHolder.chatAvailability.setVisibility(View.GONE);
        }
        else
        {
            viewHolder.textViewPosition.setGravity(Gravity.START);
            viewHolder.textViewPosition.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            viewHolder.textViewPosition.setTextColor(mContext.getResources().getColor(R.color.contact_soft_grey));
            viewHolder.imageViewDayNight.setVisibility(View.VISIBLE);
            if (null != contact.getPlatform()
                    && Constants.PLATFORM_SALES_FORCE.equals(contact.getPlatform())) {
                viewHolder.imageCompanyLogo.setImageResource(R.drawable.btn_sales_force);
            } else if (null != contact.getPlatform()
                    && Constants.PLATFORM_MY_COMMS.equals(contact.getPlatform())) {
                viewHolder.imageCompanyLogo.setImageResource(R.drawable.icon_mycomms);
            } else if (null != contact.getPlatform()
                    && Constants.PLATFORM_LOCAL.equals(contact.getPlatform())) {
                viewHolder.imageViewDayNight.setVisibility(View.INVISIBLE);
                viewHolder.imageCompanyLogo.setImageResource(R.drawable.icon_local_contacts);
            } else if (null != contact.getPlatform()
                    && Constants.PLATFORM_GLOBAL_CONTACTS.equals(contact.getPlatform())) {
                viewHolder.imageViewDayNight.setVisibility(View.INVISIBLE);
                viewHolder.imageCompanyLogo.setImageResource(R.drawable.ic_add_vodafone);
            }

            if (null != contact.getPlatform() && Constants.PLATFORM_SALES_FORCE.equals(contact.getPlatform())) {
                AvatarSFController avatarSFController = new AvatarSFController
                        (
                                mContext, contact.getContactId(), profileId
                        );
                avatarSFController.getSFAvatar(contact.getAvatar());
            }

            Utils.loadContactAvatar
                    (
                            contact.getFirstName()
                            , contact.getLastName()
                            , viewHolder.imageAvatar
                            , viewHolder.textAvatar
                            , Utils.getAvatarURL
                                    (
                                            contact.getPlatform()
                                            , contact.getStringField1()
                                            , contact.getAvatar()
                                    )
                    );



            viewHolder.textViewCompany.setText(contact.getCompany());
            viewHolder.textViewName.setText(contact.getFirstName() + " " + contact.getLastName());
            viewHolder.textViewPosition.setText(contact.getPosition());
            String country = "";

            try {
                if (contact.getCountry() != null && contact.getCountry().length() > 0) {
                    if (Utils.getCountry(contact.getCountry(), mContext).get("is_special") != null) {
                        if (Utils.getCountry(contact.getCountry(), mContext).get("is_special").equals("Yes")) {
                            country = Utils.getCountry(contact.getCountry(), mContext).get("FIPS");
                        } else {
                            country = Utils.getCountry(contact.getCountry(), mContext).get("name");
                        }
                    } else {
                        country = Utils.getCountry(contact.getCountry(), mContext).get("name");
                    }
                }
            } catch (NullPointerException e) {
                Log.e(Constants.TAG, "ContactListViewArrayAdapter.getView: NullPointerException " + e);
            }

            viewHolder.textViewCountry.setText(country);

            //Icon
            String icon = "";
            try {
                if (null != contact.getPresence() && contact.getPresence().length() > 0) {
                    JSONObject jsonObject = new JSONObject(contact.getPresence());
                    if (!jsonObject.isNull("icon")) {
                        icon = new JSONObject(contact.getPresence()).getString("icon");
                    }
                }
            } catch (JSONException e) {
                Log.e(Constants.TAG, "ContactListViewArrayAdapter.getView: ", e);
            }

            if (icon.compareTo("dnd") == 0)
                viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_notdisturb);
            else if (icon.compareTo("vacation") == 0)
                viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_vacation);
            else if (icon.compareTo("moon") == 0)
                viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_moon);
            else if (icon.compareTo("sun") == 0)
                viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_sun);
            else viewHolder.imageViewDayNight.setVisibility(View.INVISIBLE);

            //Local time
            String presenceDetail = "";
            viewHolder.textViewTime.setVisibility(View.VISIBLE);

            try {
                presenceDetail = new JSONObject(contact.getPresence()).getString("detail");
                if (presenceDetail.equals("#LOCAL_TIME#"))
                {
                    viewHolder.textViewCountry.setVisibility(View.VISIBLE);
                    if (null != contact.getTimezone()) {
                        TimeZone tz = TimeZone.getTimeZone(contact.getTimezone());
                        Calendar c = Calendar.getInstance(tz);
                        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                        format.setTimeZone(c.getTimeZone());
                        Date parsed = format.parse(c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE));
                        String result = format.format(parsed);
                        viewHolder.textViewTime.setText(result);
                    } else {
                        viewHolder.textViewTime.setText(" ");
                    }

                } else {
                    viewHolder.textViewCountry.setVisibility(View.GONE);
                    viewHolder.textViewTime.setText(presenceDetail);
                }
            } catch (Exception e) {
                Log.i(Constants.TAG, "ContactListViewArrayAdapter.getView: No presence found");
                viewHolder.textViewTime.setVisibility(View.INVISIBLE);
            }
        }

        return convertView;
    }

    /**
     * The view holder design pattern
     */
    public static class ViewHolder {
        TextView textViewName;
        TextView textViewPosition;
        TextView textViewCompany;
        TextView textViewTime;
        TextView textViewCountry;
        ImageView imageViewDayNight;
        ImageView imageAvatar;
        ImageView imageCompanyLogo;
        TextView textAvatar;
        TextView platformLabel;
        ImageView chatAvailability;
        LinearLayout layInviteMyComms;
    }
}