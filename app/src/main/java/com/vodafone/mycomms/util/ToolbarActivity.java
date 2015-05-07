package com.vodafone.mycomms.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.ContactListMainActivity;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chat.ChatMainActivity;
import com.vodafone.mycomms.chatlist.ChatListActivity;
import com.vodafone.mycomms.settings.ProfileMainActivity;

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
        ImageView footerHome = (ImageView) findViewById(R.id.footer_dashboard);
        footerHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent in = new Intent(context, ContactListMainActivity.class);
                startActivity(in);
            }
        });

        ImageView footerProfile = (ImageView) findViewById(R.id.footer_profile);
        footerProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // set an exit transition
                Intent in = new Intent(context, ContactListMainActivity.class);
                startActivity(in);
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        });

        ImageView footerComingsoon = (ImageView) findViewById(R.id.footer_coming_soon);
        footerComingsoon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(context, ChatListActivity.class);
                startActivity(in);
            }
        });
    }

    protected void setContactsListeners(final Context context){
        ImageView contactsProfile = (ImageView) findViewById(R.id.contacts_profile);
        contactsProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(context, ProfileMainActivity.class);
                startActivity(in);
                overridePendingTransition(R.anim.pull_in_down, R.anim.push_out_up);
            }
        });

        ImageView contactsSearch = (ImageView) findViewById(R.id.contacts_search);
        contactsSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // set an exit transition
                Intent in = new Intent(context, ChatMainActivity.class);
                startActivity(in);
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
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
}
