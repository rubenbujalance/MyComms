package com.vodafone.mycomms.chatgroup.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.util.AvatarSFController;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import model.Contact;
import model.UserProfile;


public class GroupMembersViewAdapter extends RecyclerView.Adapter<GroupHolder>{

    private ArrayList<Contact> groupContactList = new ArrayList<>();
    private Context mContext;
    private RealmChatTransactions _chatTx;
    private String profileId;

    public GroupMembersViewAdapter(Context context, ArrayList<Contact> groupListItem,
                                   UserProfile profile) {
        mContext = context;
        profileId = profile.getId();

        _chatTx = new RealmChatTransactions(mContext);

        if (groupListItem!=null){
            for (Contact groupListItems : groupListItem) {
                groupContactList.add(groupListItems);
            }
        }
    }

    @Override
    public int getItemCount() {
        return groupContactList.size();
    }

    @Override
    public GroupHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_list_item_contact, null);
        return new GroupHolder(view);
    }

    @Override
    public void onBindViewHolder(final GroupHolder groupHolder, int i)
    {
        // update the item view
        Contact contact = groupContactList.get(i);

        groupHolder.imageViewDayNight.setVisibility(View.VISIBLE);
        if (null != contact.getPlatform()
                && Constants.PLATFORM_LOCAL.equals(contact.getPlatform()))
        {
            groupHolder.imageViewDayNight.setVisibility(View.INVISIBLE);
        }

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
                        , groupHolder.imageAvatar
                        , groupHolder.textAvatar
                        , Utils.getAvatarURL
                                (
                                        contact.getPlatform()
                                        , contact.getStringField1()
                                        , contact.getAvatar()
                                )
                );

        groupHolder.textViewCompany.setText(contact.getCompany());
        groupHolder.textViewName.setText(contact.getFirstName() + " " + contact.getLastName() );
        groupHolder.textViewPosition.setText(contact.getPosition());
        
        groupHolder.chatAvailability.setVisibility(View.VISIBLE);

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

        groupHolder.textViewCountry.setText(country);

        //Icon
        String icon = "";
        try
        {
            if(null != contact.getPresence())
            {
                JSONObject jsonObject =  new JSONObject(contact.getPresence());
                if(!jsonObject.isNull("icon"))
                {
                    icon = new JSONObject(contact.getPresence()).getString("icon");
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(icon.compareTo("dnd")==0) groupHolder.imageViewDayNight.setImageResource(R.mipmap.ico_notdisturb);
        else if(icon.compareTo("vacation")==0) groupHolder.imageViewDayNight.setImageResource(R.mipmap.ico_vacation);
        else if(icon.compareTo("moon")==0) groupHolder.imageViewDayNight.setImageResource(R.mipmap.ico_moon);
        else if(icon.compareTo("sun")==0) groupHolder.imageViewDayNight.setImageResource(R.mipmap.ico_sun);
        else groupHolder.imageViewDayNight.setVisibility(View.INVISIBLE);

        //Local time
        String presenceDetail = "";
        groupHolder.textViewTime.setVisibility(View.VISIBLE);

        try {
            presenceDetail = new JSONObject(contact.getPresence()).getString("detail");
            if (presenceDetail.equals("#LOCAL_TIME#"))
            {
                if(null != contact.getTimezone())
                {
                    TimeZone tz = TimeZone.getTimeZone(contact.getTimezone());
                    Calendar c = Calendar.getInstance(tz);
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                    format.setTimeZone(c.getTimeZone());
                    Date parsed = format.parse(c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE));
                    String result = format.format(parsed);
                    groupHolder.textViewTime.setText(result);
                }
                else
                {
                    groupHolder.textViewTime.setText(" ");
                }

            } else {
                groupHolder.textViewTime.setText(presenceDetail);
            }
        } catch (Exception e) {
            Log.i(Constants.TAG, "ContactListViewArrayAdapter.getView: No presence found");
            groupHolder.textViewTime.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }
    
}
