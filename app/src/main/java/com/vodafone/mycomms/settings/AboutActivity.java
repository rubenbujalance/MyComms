package com.vodafone.mycomms.settings;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

public class AboutActivity  extends ToolbarActivity {
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private LinearLayout noConnectionLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(Constants.TAG, "AboutActivity.onCreate: ");
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler
                (
                        new UncaughtExceptionHandlerController(this, SplashScreenActivity.class)
                );
        BusProvider.getInstance().register(this);
        setContentView(R.layout.layout_about);
        ;
        noConnectionLayout = (LinearLayout) findViewById(R.id.no_connection_layout);
        TextView textVersion = (TextView) findViewById(R.id.text_version);
        textVersion.setText(getResources().getString(R.string.about_version) + BuildConfig.VERSION_NAME);
        TextView textBuild = (TextView) findViewById(R.id.text_build);
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        textBuild.setText(getResources().getString(R.string.about_build) + versionCode);
    }

    //Prevent of going from main screen back to login
    @Override
    public void onBackPressed() {
        //moveTaskToBack(true);
        finish();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);

        // Disconnect from the XMPP server
        XMPPTransactions.disconnectMsgServerSession();
    }

}
