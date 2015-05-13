package com.vodafone.mycomms.contacts.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.R;

import java.util.List;

import model.Contact;
import model.RecentItem;

/**
 * Created by str_vig on 28/04/2015.
 */
public class RecentListViewArrayAdapter extends ArrayAdapter<RecentItem> {
    public RecentListViewArrayAdapter(Context context, List<RecentItem> items) {
        super(context, R.layout.layout_list_item_recent, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RecentViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.layout_list_item_recent, parent, false);

            viewHolder = new RecentViewHolder();
            viewHolder.textViewCompany = (TextView) convertView.findViewById(R.id.list_item_content_company);
            viewHolder.textViewName = (TextView) convertView.findViewById(R.id.list_item_content_name);
            viewHolder.textViewOccupation = (TextView) convertView.findViewById(R.id.list_item_content_occupation);
            viewHolder.textViewTime = (TextView) convertView.findViewById(R.id.list_item_recent_info_time);
            viewHolder.imageViewRecentType = (ImageView) convertView.findViewById(R.id.list_item_recent_type_image);
            viewHolder.textViewRecentItemTime = (TextView) convertView.findViewById(R.id.list_item_recent_info_time);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
              viewHolder = (RecentViewHolder) convertView.getTag();
        }

        // update the item view
        RecentItem recentItem = getItem(position);
        Contact contact = getItem(position).getContact();
        viewHolder.textViewCompany.setText(contact.getCompany());
        viewHolder.textViewName.setText(contact.getFirstName() + " " + contact.getLastName() );
        //viewHolder.textViewOccupation.setText(contact.getOccupation());
        viewHolder.textViewRecentItemTime.setText(recentItem.getRecentEventTime());
        //viewHolder.textViewTime.setText(contact.getTime());

        if(recentItem.getItemType() == RecentItem.RecentItemType.MAIL){
            viewHolder.imageViewRecentType.setImageResource(R.drawable.btn_more_invite);
        }else if(recentItem.getItemType() == RecentItem.RecentItemType.CALL){
            viewHolder.imageViewRecentType.setImageResource(R.drawable.icon_recent_phone);
        } else if (recentItem.getItemType() == RecentItem.RecentItemType.CHAT){
            viewHolder.imageViewRecentType.setImageResource(R.drawable.icon_recent_message);
        }



        return convertView;
    }

    /**
     * The view holder design pattern
     */
    private static class RecentViewHolder {
        TextView textViewName;
        TextView textViewOccupation;
        TextView textViewCompany;
        TextView textViewTime;
        TextView textViewRecentItemTime;
        ImageView imageViewRecentType;
    }
}