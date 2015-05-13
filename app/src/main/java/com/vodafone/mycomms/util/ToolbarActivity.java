package com.vodafone.mycomms.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vodafone.mycomms.ContactListMainActivity;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chat.ChatMainActivity;
import com.vodafone.mycomms.chatlist.ChatListActivity;
import com.vodafone.mycomms.contacts.detail.ContactDetailMainActivity;
import com.vodafone.mycomms.settings.SettingsMainActivity;

public class ToolbarActivity extends ActionBarActivity {

    private Toolbar mToolbar;
    private Toolbar mFooter;

    protected Toolbar activateToolbar() {
        if(mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.app_bar);
            if(mToolbar != null) {
                setSupportActionBar(mToolbar);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }
        return mToolbar;
    }

    protected Toolbar activateFooter() {
        if(mFooter == null) {
            mFooter = (Toolbar) findViewById(R.id.app_footer);
            if(mFooter != null) {
                setSupportActionBar(mFooter);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }
        return mFooter;
    }

    protected Toolbar activateToolbarWithHomeEnabled() {
        activateToolbar();
        if(mToolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            changeArrowColor(R.color.white);
        }
        return mToolbar;
    }

    protected void changeArrowColor(int color){
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        if (upArrow != null) {
            upArrow.setColorFilter(getResources().getColor(color), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }
    }

    protected Toolbar setToolbarBackground(int image) {
        if(mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.app_bar);
        }
        if(mToolbar != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
                mToolbar.setBackgroundDrawable(getResources().getDrawable(image));
            else
                mToolbar.setBackground(getResources().getDrawable(image));
        }
        return mToolbar;
    }

    protected void setToolbarTitle(String title){
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(title);
    }

    protected void setFooterListeners(final Context context){

        ImageView footerContacts = (ImageView) findViewById(R.id.footer_contacts);
        ImageView footerHome = (ImageView) findViewById(R.id.footer_dashboard);
        ImageView footerRecents = (ImageView) findViewById(R.id.footer_recents);

        LinearLayout layoutContacts = (LinearLayout) findViewById(R.id.footer_contacts_layout);
        LinearLayout layoutDashboard = (LinearLayout) findViewById(R.id.footer_dashboard_layout);
        LinearLayout layoutRecents = (LinearLayout) findViewById(R.id.footer_recents_layout);

        layoutContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(Constants.TAG, "ToolbarActivity.onClick: footerProfile");
                // set an exit transition
                Intent in = new Intent(context, ContactListMainActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                //in.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(in);
                //overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        });

        footerHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent in = new Intent(context, ContactListMainActivity.class);
                //startActivity(in);
            }
        });

        layoutRecents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(Constants.TAG, "ToolbarActivity.onClick: footerRecents");
                Intent in = new Intent(context, ChatListActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                //in.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(in);
                //overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        });
    }

    protected void setChatListeners(final Context context) {
        ImageView imageContact = (ImageView) findViewById(R.id.user_img);
        imageContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(context, ContactDetailMainActivity.class);
                startActivity(in);
                //overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        });
    }

    protected void setContactsListeners(final Context context){
        ImageView contactsProfile = (ImageView) findViewById(R.id.contacts_profile);
        contactsProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(context, SettingsMainActivity.class);
                startActivity(in);
                //overridePendingTransition(R.anim.pull_in_down, R.anim.push_out_up);
            }
        });

        ImageView contactsSearch = (ImageView) findViewById(R.id.contacts_search);
        contactsSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // set an exit transition
                Intent in = new Intent(context, ChatMainActivity.class);
                startActivity(in);
                //overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        });

        ImageView contactsAdd = (ImageView) findViewById(R.id.contacts_add);
        contactsAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(context, ChatMainActivity.class);
                startActivity(in);
            }
        });
    }

    public void activateFooterSelected(int selected){
        LinearLayout layoutContacts = (LinearLayout) findViewById(R.id.footer_contacts_layout);
        LinearLayout layoutDashboard = (LinearLayout) findViewById(R.id.footer_dashboard_layout);
        LinearLayout layoutRecents = (LinearLayout) findViewById(R.id.footer_recents_layout);

        ImageView footerContacts = (ImageView) findViewById(R.id.footer_contacts);
        ImageView footerHome = (ImageView) findViewById(R.id.footer_dashboard);
        ImageView footerRecents = (ImageView) findViewById(R.id.footer_recents);

        switch (selected){
            case Constants.CONTACTS:
                layoutContacts.setBackgroundColor(getResources().getColor(R.color.toolbar_selected_item));
                layoutRecents.setBackgroundColor(getResources().getColor(R.color.transparent));
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    footerRecents.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_create_event_date_time));
                    footerContacts.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_tabbar_contacts_selected));
                } else{
                    footerRecents.setBackground(getResources().getDrawable(R.drawable.icon_create_event_date_time));
                    footerContacts.setBackground(getResources().getDrawable(R.drawable.icon_tabbar_contacts_selected));
                }
                break;
            case Constants.DASHBOARD:
                break;
            case Constants.RECENTS:
                layoutContacts.setBackgroundColor(getResources().getColor(R.color.transparent));
                layoutRecents.setBackgroundColor(getResources().getColor(R.color.toolbar_selected_item));

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    footerContacts.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_tabbar_contacts_unselected));
                    footerRecents.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_create_event_date_time_selected));
                }else{
                    footerContacts.setBackground(getResources().getDrawable(R.drawable.icon_tabbar_contacts_unselected));
                    footerRecents.setBackground(getResources().getDrawable(R.drawable.icon_create_event_date_time_selected));
                }
                break;
        }
    }
}
