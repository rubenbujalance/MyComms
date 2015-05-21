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

import java.util.List;

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
        viewHolder.textViewOfficeLocation.setText(contact.getOfficeLocation());
        viewHolder.textViewTime.setText(Utils.getTimeFromMillis(contact.getLastSeen()));

        viewHolder.imageViewDayNight.setImageResource(R.drawable.icon_sun);
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
        TextView textViewOfficeLocation;
        ImageView imageViewDayNight;
        ImageView imageAvatar;
    }
}