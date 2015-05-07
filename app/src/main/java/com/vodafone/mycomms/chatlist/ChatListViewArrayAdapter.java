package com.vodafone.mycomms.chatlist;

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
import model.DummyChatItem;

/**
 * Created by str_vig on 28/04/2015.
 */
public class ChatListViewArrayAdapter extends ArrayAdapter<DummyChatItem> {

    public ChatListViewArrayAdapter(Context context, List<DummyChatItem> items) {
        super(context, R.layout.layout_list_item_chat, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.layout_list_item_chat, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.textViewName = (TextView) convertView.findViewById(R.id.list_item_content_name);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
              viewHolder = (ViewHolder) convertView.getTag();
        }

        // update the item view
        Contact contact = getItem(position).getContact();
         viewHolder.textViewName.setText(contact.getFirstName() + " " + contact.getLastName() );

        return convertView;
    }

    /**
     * The view holder design pattern
     */
    private static class ViewHolder {
        TextView textViewName;

    }
}