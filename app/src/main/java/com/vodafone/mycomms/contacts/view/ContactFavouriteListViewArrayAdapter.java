package com.vodafone.mycomms.contacts.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import model.FavouriteContact;

/**
 * Created by str_vig on 28/04/2015.
 */
public class ContactFavouriteListViewArrayAdapter extends ArrayAdapter<FavouriteContact> {
    private Context mContext;

    public ContactFavouriteListViewArrayAdapter(Context context, List<FavouriteContact> items) {
        super(context, R.layout.layout_list_item_contact, items);
        this.mContext = context;
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
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
              viewHolder = (ViewHolder) convertView.getTag();
        }

        // update the item view
        FavouriteContact contact = getItem(position);

        if(null != contact.getPlatform() && contact.getPlatform()
                .equals(Constants.PLATFORM_LOCAL))
        {
            viewHolder.imageCompanyLogo.setVisibility(View.GONE);
            viewHolder.imageViewDayNight.setVisibility(View.GONE);
        }
        else
        {
            viewHolder.imageCompanyLogo.setVisibility(View.VISIBLE);
            viewHolder.imageViewDayNight.setVisibility(View.VISIBLE);
        }

        //Image avatar
        String initials = "";
        if(null != contact.getFirstName() && contact.getFirstName().length() > 0)
        {
            initials = contact.getFirstName().substring(0,1);

            if(null != contact.getLastName() && contact.getLastName().length() > 0)
            {
                initials = initials + contact.getLastName().substring(0,1);
            }

        }
        final String finalInitials = initials;

        if (contact.getAvatar()!=null &&
                contact.getAvatar().length()>0)
        {
            if (contact.getPlatform().equalsIgnoreCase(Constants.PLATFORM_MY_COMMS)
                    || contact.getPlatform().equalsIgnoreCase(Constants.PLATFORM_LOCAL)) {
                MycommsApp.picasso
                        .load(contact.getAvatar())
                        .placeholder(R.color.grey_middle)
                        .noFade()
                        .fit().centerCrop()
                        .into(viewHolder.imageAvatar, new Callback() {
                            @Override
                            public void onSuccess() {
                                viewHolder.textAvatar.setVisibility(View.INVISIBLE);
                            }

                            @Override
                            public void onError() {
                                viewHolder.imageAvatar.setImageResource(R.color.grey_middle);
                                viewHolder.textAvatar.setVisibility(View.VISIBLE);
                                viewHolder.textAvatar.setText(finalInitials);
                            }
                        });
            }
            else if (contact.getPlatform().equalsIgnoreCase(Constants.PLATFORM_SALES_FORCE)){
//                AvatarSFController avatarSFController = new AvatarSFController(mContext, viewHolder.top_left_avatar, viewHolder.bottom_left_avatar_text, contact.getContactId());
//                avatarSFController.getSFAvatar(contact.getAvatar());
                viewHolder.imageAvatar.setImageResource(R.color.grey_middle);
                viewHolder.textAvatar.setText(initials);
            }
        }
        else
        {
            viewHolder.imageAvatar.setImageResource(R.color.grey_middle);
            viewHolder.textAvatar.setText(initials);
        }

        viewHolder.textViewCompany.setText(contact.getCompany());
        viewHolder.textViewName.setText(contact.getFirstName() + " " + contact.getLastName() );
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
        } catch (NullPointerException e){
            Log.e(Constants.TAG, "ContactListViewArrayAdapter.getView: NullPointerException " + e);
        }
        viewHolder.textViewCountry.setText(country);

        //Icon
        String icon = null;
        try {
            icon = new JSONObject(contact.getPresence()).getString("icon");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(null != icon)
        {
            if(icon.compareTo("dnd")==0) viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_notdisturb);
            else if(icon.compareTo("vacation")==0) viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_vacation);
            else if(icon.compareTo("moon")==0) viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_moon);
            else viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_sun);
        }
        else
        {
            viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_sun);
        }

        //viewHolder.textViewTime.setText(Utils.getTimeFromMillis(contact.getLastSeen()));

        //Local time
        String presenceDetail = null;

        try {
            presenceDetail = new JSONObject(contact.getPresence()).getString("detail");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            assert null != presenceDetail;
            if (null != presenceDetail && presenceDetail.equals("#LOCAL_TIME#"))
            {
                if(null != contact.getTimezone())
                {
                    TimeZone tz = TimeZone.getTimeZone(contact.getTimezone());
                    Calendar c = Calendar.getInstance(tz);
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                    format.setTimeZone(c.getTimeZone());
                    Date parsed = format.parse(c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE));
                    String result = format.format(parsed);
                    viewHolder.textViewTime.setText(result);
                }
                else
                {
                    viewHolder.textViewTime.setText(" ");
                }
            }
            else {
                viewHolder.textViewTime.setText(presenceDetail);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    /**
     * The view holder design pattern
     */
    private static class ViewHolder {
        TextView textViewName;
        TextView textViewPosition;
        TextView textViewCompany;
        TextView textViewTime;
        TextView textViewCountry;
        ImageView imageViewDayNight;
        ImageView imageAvatar;
        ImageView imageCompanyLogo;
        TextView textAvatar;
    }
}