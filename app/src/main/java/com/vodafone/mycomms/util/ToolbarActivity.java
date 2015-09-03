package com.vodafone.mycomms.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vodafone.mycomms.ContactListMainActivity;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatgroup.GroupChatListActivity;
import com.vodafone.mycomms.chatlist.view.ChatListActivity;
import com.vodafone.mycomms.main.DashBoardActivity;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.settings.SettingsMainActivity;

import io.realm.Realm;
import model.Contact;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ToolbarActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Toolbar mFooter;
    private int foregroundActivity; //0-Contacts,1-Dashboard,2-Inbox
    private String profileId;
    private RealmChatTransactions realmChatTransactions;
    private RealmGroupChatTransactions realmGroupChatTransactions;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realmChatTransactions = new RealmChatTransactions(this);
        this.realm = Realm.getDefaultInstance();
        SharedPreferences sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
        realmGroupChatTransactions = new RealmGroupChatTransactions(ToolbarActivity.this,
                profileId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != this.realm)
            this.realm.close();
    }

    public void setForegroundActivity(int activity)
    {
        foregroundActivity = activity;
    }

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

    public Toolbar activateContactListToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        Toolbar goneToolbar = (Toolbar) findViewById(R.id.app_inbox);
        Toolbar goneToolbar2 = (Toolbar) findViewById(R.id.app_group_chat);
        if(mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            mToolbar.setVisibility(View.VISIBLE);
            goneToolbar.setVisibility(View.GONE);
            goneToolbar2.setVisibility(View.GONE);
        }
        return mToolbar;
    }

    public Toolbar activateChatListToolbar(final Context context)
    {
        mToolbar = (Toolbar) findViewById(R.id.app_inbox);
        Toolbar goneToolbar = (Toolbar) findViewById(R.id.app_bar);
        Toolbar goneToolbar2 = (Toolbar) findViewById(R.id.app_group_chat);
        if(mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            mToolbar.setVisibility(View.VISIBLE);
            goneToolbar.setVisibility(View.GONE);
            goneToolbar2.setVisibility(View.GONE);
        }
        setUserProfileListener(context);
        return mToolbar;
    }

    public Toolbar activateGroupChatListToolbar()
    {
        mToolbar = (Toolbar) findViewById(R.id.app_group_chat);
        Toolbar goneToolbar = (Toolbar) findViewById(R.id.app_bar);
        Toolbar goneToolbar2 = (Toolbar) findViewById(R.id.app_inbox);
        if(mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            mToolbar.setVisibility(View.VISIBLE);
            goneToolbar.setVisibility(View.GONE);
            goneToolbar2.setVisibility(View.GONE);
        }
        return mToolbar;
    }

    public Toolbar activateGroupChatToolbar()
    {
        mToolbar = (Toolbar) findViewById(R.id.app_group_chat);
        Toolbar goneToolbar = (Toolbar) findViewById(R.id.app_bar);
        Toolbar goneToolbar2 = (Toolbar) findViewById(R.id.app_inbox);
        if(mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            mToolbar.setVisibility(View.VISIBLE);
            goneToolbar.setVisibility(View.GONE);
            goneToolbar2.setVisibility(View.GONE);
        }
        return mToolbar;
    }



    protected Toolbar activateFooter() {
        if(mFooter == null) {
            mFooter = (Toolbar) findViewById(R.id.app_footer);
            if(mFooter != null) {
                setSupportActionBar(mFooter);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                checkUnreadChatMessages();
            }
        }
        return mFooter;
    }

    protected Toolbar hideFooter() {
        if(mFooter == null) {
            mFooter = (Toolbar) findViewById(R.id.app_footer);
            RelativeLayout footerLay = (RelativeLayout) findViewById(R.id.footer);
            if(mFooter != null) {
                mFooter.setVisibility(View.GONE);
                footerLay.setVisibility(View.GONE);
            }
        }
        return mFooter;
    }

    public void checkUnreadChatMessages()
    {
        if (mFooter!=null) {
            TextView unreadMessagesText = (TextView) mFooter.findViewById(R.id.unread_messages);
            long unreadMessages = realmChatTransactions.getAllChatPendingMessagesCount(realm);
            if (unreadMessages > 0)
            {
                unreadMessagesText.setVisibility(View.VISIBLE);
                if (unreadMessages > 99) {
                    unreadMessagesText.setTextSize(Constants.CHAT_UNREAD_MORE_THAN_99_SIZE);
                    unreadMessagesText.setText(R.string.unread_messages_more_than_99);
                } else {
                    unreadMessagesText.setTextSize(Constants.CHAT_UNREAD_REGULAR_SIZE);
                    unreadMessagesText.setText(String.valueOf(unreadMessages));
                }
            }
            else
                unreadMessagesText.setVisibility(View.GONE);
        }
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
        LinearLayout layoutInbox = (LinearLayout) findViewById(R.id.footer_inbox_layout);

        SharedPreferences sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();

        layoutContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(foregroundActivity==0) return;
                Log.i(Constants.TAG, "ToolbarActivity.onClick: footerContacts");
                editor.putBoolean(Constants.IS_TOOLBAR_CLICKED, true);
                editor.apply();
                // set an exit transition
                Intent in = new Intent(context, ContactListMainActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                //in.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(in);
                overridePendingTransition(0, 0);
                //overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        });

        layoutDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(foregroundActivity==1) return;
                editor.putBoolean(Constants.IS_TOOLBAR_CLICKED, true);
                editor.apply();
                Log.i(Constants.TAG, "ToolbarActivity.onClick: footerDasboard");
                Intent in = new Intent(context, DashBoardActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(in);
            }
        });

        layoutInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (foregroundActivity == 2) return;
                editor.putBoolean(Constants.IS_TOOLBAR_CLICKED, true);
                editor.apply();
                Log.i(Constants.TAG, "ToolbarActivity.onClick: footerInbox");
                Intent in = new Intent(context, ChatListActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                //in.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(in);
            }
        });
    }

    protected void setContactsListeners(final Context context){
        LinearLayout contactsProfile = (LinearLayout) findViewById(R.id.lay_contacts_profile);
        contactsProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(context, SettingsMainActivity.class);
                startActivity(in);
                //overridePendingTransition(R.anim.pull_in_down, R.anim.push_out_up);
            }
        });

        ImageView contactsAdd = (ImageView) findViewById(R.id.contacts_add);
        contactsAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent in = new Intent(context, GroupChatActivity.class);
                //startActivity(in);
            }
        });
    }

    private void setUserProfileListener(final Context context) {
        LinearLayout contactsProfile = (LinearLayout) findViewById(R.id.lay_user_profile);
        contactsProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(context, SettingsMainActivity.class);
                startActivity(in);
            }
        });
    }

    protected void setChatListListeners(final Context context){
        ImageView contactsAdd = (ImageView) mToolbar.findViewById(R.id.chat_add);
        contactsAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(context, GroupChatListActivity.class);
                in.putExtra(Constants.GROUP_CHAT_PREVIOUS_ACTIVITY, GroupChatListActivity.class.getSimpleName());
                startActivity(in);
            }
        });
    }

    protected void setGroupChatListListeners(final Context context){
        TextView contactsProfile = (TextView) mToolbar.findViewById(R.id.chat_group_cancel);
        contactsProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ((Activity)context).finish();
            }
        });
    }

    public void activateFooterSelected(int selected){
        LinearLayout layoutContacts = (LinearLayout) findViewById(R.id.footer_contacts_layout);
        LinearLayout layoutDashboard = (LinearLayout) findViewById(R.id.footer_dashboard_layout);
        LinearLayout layoutRecents = (LinearLayout) findViewById(R.id.footer_inbox_layout);

        ImageView footerContacts = (ImageView) findViewById(R.id.footer_contacts);
        ImageView footerHome = (ImageView) findViewById(R.id.footer_dashboard);
        ImageView footerRecents = (ImageView) findViewById(R.id.footer_recents);

        TextView tvContacts = (TextView) findViewById(R.id.tv_contacts);
        TextView tvChat = (TextView) findViewById(R.id.tv_chat);

        switch (selected){
            case Constants.TOOLBAR_CONTACTS:
//                layoutContacts.setBackgroundColor(getResources().getColor(R.color.toolbar_selected_item));
//                layoutDashboard.setBackgroundColor(getResources().getColor(R.color.transparent));
//                layoutRecents.setBackgroundColor(getResources().getColor(R.color.transparent));

                tvContacts.setTextColor(getResources().getColor(R.color.white));
                tvChat.setTextColor(getResources().getColor(R.color.grey_middle));

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    footerRecents.setImageResource(R.drawable.chat);
                    footerHome.setImageResource(R.drawable.land);
                    footerContacts.setImageResource(R.drawable.btnuser_on);
                } else{
                    footerRecents.setImageResource(R.drawable.chat);
                    footerHome.setImageResource(R.drawable.land);
                    footerContacts.setImageResource(R.drawable.btnuser_on);
                }
                break;
            case Constants.TOOLBAR_DASHBOARD:
//                layoutContacts.setBackgroundColor(getResources().getColor(R.color.transparent));
//                layoutRecents.setBackgroundColor(getResources().getColor(R.color.transparent));

                tvContacts.setTextColor(getResources().getColor(R.color.grey_middle));
                tvChat.setTextColor(getResources().getColor(R.color.grey_middle));

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    footerRecents.setImageResource(R.drawable.chat);
                    footerHome.setImageResource(R.drawable.landselected);
                    footerContacts.setImageResource(R.drawable.btnuser);
                } else{
                    footerRecents.setImageResource(R.drawable.chat);
                    footerHome.setImageResource(R.drawable.landselected);
                    footerContacts.setImageResource(R.drawable.btnuser);
                }
                break;
            case Constants.TOOLBAR_RECENTS:
//                layoutDashboard.setBackgroundColor(getResources().getColor(R.color.transparent));
//                layoutContacts.setBackgroundColor(getResources().getColor(R.color.transparent));
//                layoutRecents.setBackgroundColor(getResources().getColor(R.color.toolbar_selected_item));

                tvContacts.setTextColor(getResources().getColor(R.color.grey_middle));
                tvChat.setTextColor(getResources().getColor(R.color.white));

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    footerContacts.setImageResource(R.drawable.btnuser);
                    footerHome.setImageResource(R.drawable.land);
                    footerRecents.setImageResource(R.drawable.chat_on);

                }else{
                    footerContacts.setImageResource(R.drawable.btnuser);
                    footerHome.setImageResource(R.drawable.land);
                    footerRecents.setImageResource(R.drawable.chat_on);

                }
                break;
        }
    }

    public void enableToolbarIsClicked(boolean enabled){
        SharedPreferences sp = getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(Constants.IS_TOOLBAR_CLICKED, enabled);
        editor.apply();
    }


    public RealmChatTransactions getRealmChatTransactions() {
        return realmChatTransactions;
    }

    public void setRealmChatTransactions(RealmChatTransactions realmChatTransactions) {
        this.realmChatTransactions = realmChatTransactions;
    }

    public RealmGroupChatTransactions getRealmGroupChatTransactions() {
        return realmGroupChatTransactions;
    }

    public void setRealmGroupChatTransactions(RealmGroupChatTransactions realmGroupChatTransactions) {
        this.realmGroupChatTransactions = realmGroupChatTransactions;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
