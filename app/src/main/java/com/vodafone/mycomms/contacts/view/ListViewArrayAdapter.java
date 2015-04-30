package com.vodafone.mycomms.contacts.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.vodafone.mycomms.R;

import java.util.List;

import model.Contact;

/**
 * Created by str_vig on 28/04/2015.
 */
public class ListViewArrayAdapter extends ArrayAdapter<Contact> {

    public ListViewArrayAdapter(Context context, List<Contact> items) {
        super(context, R.layout.layout_list_item, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.layout_list_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.textViewCompany = (TextView) convertView.findViewById(R.id.list_item_content_company);
            viewHolder.textViewName = (TextView) convertView.findViewById(R.id.list_item_content_name);
            viewHolder.textViewOccupation = (TextView) convertView.findViewById(R.id.list_item_content_occupation);
            viewHolder.textViewCountry = (TextView) convertView.findViewById(R.id.list_item_status_country);
            viewHolder.textViewTime = (TextView) convertView.findViewById(R.id.list_item_status_local_time);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
              viewHolder = (ViewHolder) convertView.getTag();
        }

        // update the item view
        Contact contact = getItem(position);

        viewHolder.textViewCompany.setText(contact.getCompany());
        viewHolder.textViewName.setText(contact.getFirstName() + " " + contact.getLastName() );
        viewHolder.textViewOccupation.setText(contact.getOccupation());
        viewHolder.textViewCountry.setText(contact.getCountry());
        viewHolder.textViewTime.setText(contact.getTime());
        return convertView;
    }

    /**
     * The view holder design pattern
     */
    private static class ViewHolder {
        TextView textViewName;
        TextView textViewOccupation;
        TextView textViewCompany;
        TextView textViewTime;
        TextView textViewCountry;
    }
}