package com.vodafone.mycomms.settings;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;

public class AboutActivity  extends ToolbarActivity {

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
    }

    @Override
    public void onStart()
    {
        super.onStart();
        MycommsApp.activityStarted();
    }

    @Override
    public void onStop()
    {
        MycommsApp.activityStopped();
        super.onStop();
    }
}
