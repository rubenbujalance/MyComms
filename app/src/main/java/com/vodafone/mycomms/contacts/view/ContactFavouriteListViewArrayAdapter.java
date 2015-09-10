package com.vodafone.mycomms.contacts.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.realm.RealmContactTransactions;
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

import io.realm.Realm;
import model.Contact;
import model.FavouriteContact;

/**
 * Created by str_vig on 28/04/2015.
 */
public class ContactFavouriteListViewArrayAdapter extends ArrayAdapter<FavouriteContact> {
    private Context mContext;
    private String profileId;
    private Realm realm;

    public ContactFavouriteListViewArrayAdapter(Context context, List<FavouriteContact> items,
                                                Realm realm) {
        super(context, R.layout.layout_list_item_contact, items);
        this.mContext = context;
        this.realm = realm;
        SharedPreferences sp = mContext.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.layout_list_item_contact, parent, false);
            viewHolder.imageAvatar = (ImageView) convertView.findViewById(R.id.companyLogo);
            viewHolder.textAvatar = (TextView) convertView.findViewById(R.id.avatarText);
            viewHolder.textViewCompany = (TextView) convertView.findViewById(R.id.list_item_content_company);
            viewHolder.textViewName = (TextView) convertView.findViewById(R.id.list_item_content_name);
            viewHolder.textViewPosition = (TextView) convertView.findViewById(R.id.list_item_content_position);
            viewHolder.textViewTime = (TextView) convertView.findViewById(R.id.list_item_status_local_time);
            viewHolder.textViewCountry = (TextView) convertView.findViewById(R.id.list_item_status_local_country);
            viewHolder.imageViewDayNight = (ImageView) convertView.findViewById(R.id.list_item_image_status_daynight);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // update the item view
        FavouriteContact contact = getItem(position);
        viewHolder.imageViewDayNight.setVisibility(View.VISIBLE);
        if(null != contact.getPlatform()
                && Constants.PLATFORM_SALES_FORCE.equals(contact.getPlatform()))
        {
            viewHolder.imageViewDayNight.setVisibility(View.INVISIBLE);
        }
        else if (null != contact.getPlatform()
                && Constants.PLATFORM_LOCAL.equals(contact.getPlatform()))
        {
            viewHolder.imageViewDayNight.setVisibility(View.INVISIBLE);
        }

        new RealmContactTransactions(profileId);
        Contact cont = RealmContactTransactions.getContactById(contact.getContactId(), realm);

        if(null != contact.getPlatform() && Constants.PLATFORM_SALES_FORCE.equals(contact.getPlatform()))
        {
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
                                        cont.getPlatform()
                                        , cont.getStringField1()
                                        , cont.getAvatar()
                                )
                );

        viewHolder.textViewCompany.setText(contact.getCompany());
        viewHolder.textViewName.setText(contact.getFirstName() + " " + contact.getLastName() );
        viewHolder.textViewPosition.setText(contact.getPosition());

        String country = "";
        try {
            if (contact.getCountry() != null && contact.getCountry().length() > 0)
                country = Utils.getCountryValue(contact.getCountry(), mContext);
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
                viewHolder.textViewCountry.setVisibility(View.VISIBLE);
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
            else
            {
                viewHolder.textViewCountry.setVisibility(View.GONE);
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
        TextView textAvatar;
    }
}