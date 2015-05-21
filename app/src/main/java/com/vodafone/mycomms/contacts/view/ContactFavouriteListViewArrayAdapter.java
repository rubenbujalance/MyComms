package com.vodafone.mycomms.contacts.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
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
        ViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.layout_list_item_contact, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.imageAvatar = (ImageView) convertView.findViewById(R.id.companyLogo);
            viewHolder.textViewCompany = (TextView) convertView.findViewById(R.id.list_item_content_company);
            viewHolder.textViewName = (TextView) convertView.findViewById(R.id.list_item_content_name);
            viewHolder.textViewPosition = (TextView) convertView.findViewById(R.id.list_item_content_position);
            viewHolder.textViewTime = (TextView) convertView.findViewById(R.id.list_item_status_local_time);
            viewHolder.imageViewDayNight = (ImageView) convertView.findViewById(R.id.list_item_image_status_daynight);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
              viewHolder = (ViewHolder) convertView.getTag();
        }

        // update the item view
        FavouriteContact contact = getItem(position);
        if (!contact.getAvatar().equals("")) {
            Picasso.with(mContext).load(contact.getAvatar())
                    .error(R.drawable.cartoon_round_contact_image_example)
                    .into(viewHolder.imageAvatar);
        } else{
            viewHolder.imageAvatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cartoon_round_contact_image_example));
        }
        viewHolder.textViewCompany.setText(contact.getCompany());
        viewHolder.textViewName.setText(contact.getFirstName() + " " + contact.getLastName() );
        viewHolder.textViewPosition.setText(contact.getPosition());
        //Icon
        String icon = null;
        try {
            icon = new JSONObject(contact.getPresence()).getString("icon");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(icon.compareTo("dnd")==0) viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_notdisturb);
        else if(icon.compareTo("vacation")==0) viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_vacation);
        else if(icon.compareTo("moon")==0) viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_moon);
        else viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_sun);

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
            if (presenceDetail.equals("#LOCAL_TIME#")) {
                TimeZone tz = TimeZone.getTimeZone(contact.getTimezone());
                Calendar currentCal = Calendar.getInstance();

                SimpleDateFormat sourceFormat = new SimpleDateFormat("HH:mm");
                sourceFormat.setTimeZone(currentCal.getTimeZone());

                Date parsed = sourceFormat.parse(currentCal.get(Calendar.HOUR_OF_DAY) + ":" + currentCal.get(Calendar.MINUTE));

                SimpleDateFormat destFormat = new SimpleDateFormat("HH:mm");
                destFormat.setTimeZone(tz);

                String result = destFormat.format(parsed);

                viewHolder.textViewTime.setText(result);
            } else {
                viewHolder.textViewTime.setText(presenceDetail);
            }
        } catch (ParseException e) {
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
        ImageView imageViewDayNight;
        ImageView imageAvatar;
    }
}