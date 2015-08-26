package com.vodafone.mycomms.main;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler
                (
                        new UncaughtExceptionHandlerController(this, null)
                );
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
