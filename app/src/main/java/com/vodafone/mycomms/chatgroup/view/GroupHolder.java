package com.vodafone.mycomms.chatgroup.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.R;

public class GroupHolder extends RecyclerView.ViewHolder {
    protected TextView textViewName;
    protected TextView textViewPosition;
    protected TextView textViewCompany;
    protected TextView textViewTime;
    protected TextView textViewCountry;
    protected ImageView imageViewDayNight;
    protected ImageView imageAvatar;
    protected ImageView chatAvailability;
    protected TextView textAvatar;

    public GroupHolder(View view) {
        super(view);

        imageAvatar = (ImageView) view.findViewById(R.id.companyLogo);
        textAvatar = (TextView) view.findViewById(R.id.avatarText);
        textViewCompany = (TextView) view.findViewById(R.id.list_item_content_company);
        textViewName = (TextView) view.findViewById(R.id.list_item_content_name);
        textViewPosition = (TextView) view.findViewById(R.id.list_item_content_position);
        textViewTime = (TextView) view.findViewById(R.id.list_item_status_local_time);
        textViewCountry = (TextView) view.findViewById(R.id.list_item_status_local_country);
        imageViewDayNight = (ImageView) view.findViewById(R.id.list_item_image_status_daynight);
        chatAvailability = (ImageView) view.findViewById(R.id.chat_availability);
    }
    public TextView getTextViewName()
    {
        return this.textViewName;
    }



}
