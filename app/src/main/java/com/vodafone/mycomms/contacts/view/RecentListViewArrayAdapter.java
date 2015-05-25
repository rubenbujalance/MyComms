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

import model.RecentContact;

/**
 * Created by str_vig on 28/04/2015.
 */
public class RecentListViewArrayAdapter extends ArrayAdapter<RecentContact> {
    private Context mContext;

    public RecentListViewArrayAdapter(Context context, List<RecentContact> items) {
        super(context, R.layout.layout_list_item_recent, items);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RecentViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.layout_list_item_recent, parent, false);

            viewHolder = new RecentViewHolder();
            viewHolder.imageAvatar = (ImageView) convertView.findViewById(R.id.companyLogo);
            viewHolder.textViewCompany = (TextView) convertView.findViewById(R.id.list_item_content_company);
            viewHolder.textViewName = (TextView) convertView.findViewById(R.id.list_item_content_name);
            viewHolder.textViewOccupation = (TextView) convertView.findViewById(R.id.list_item_content_position);
            viewHolder.textViewTime = (TextView) convertView.findViewById(R.id.list_item_recent_info_time);
            viewHolder.imageViewRecentType = (ImageView) convertView.findViewById(R.id.list_item_recent_type_image);
            viewHolder.textViewRecentItemTime = (TextView) convertView.findViewById(R.id.list_item_recent_info_time);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
              viewHolder = (RecentViewHolder) convertView.getTag();
        }

        // update the item view
        RecentContact contact = getItem(position);

        if (!contact.getAvatar().equals("")) {
            Picasso.with(mContext).load(contact.getAvatar())
                    .error(R.drawable.cartoon_round_contact_image_example)
                    .placeholder( R.drawable.progress_animation )
                    .into(viewHolder.imageAvatar);
        } else {
            viewHolder.imageAvatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cartoon_round_contact_image_example));
        }

        viewHolder.textViewName.setText(contact.getFirstName() + " " + contact.getLastName() );
        viewHolder.textViewOccupation.setText(contact.getPosition());
        viewHolder.textViewCompany.setText(contact.getCompany());

        if(contact.getAction().equals("")) {
            viewHolder.imageViewRecentType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_error_tooltip));
        } else if (contact.getAction().equals("sms")) {
            viewHolder.imageViewRecentType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_recent_message));
        } else if (contact.getAction().equals("email")) {
            viewHolder.imageViewRecentType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.img_verify_email));
        } else if (contact.getAction().equals("call")) {
            viewHolder.imageViewRecentType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_recent_phone));
        }
        viewHolder.textViewTime.setText(Utils.getTimeFromMillis(contact.getActionTimeStamp()));

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
        ImageView imageAvatar;
    }
}