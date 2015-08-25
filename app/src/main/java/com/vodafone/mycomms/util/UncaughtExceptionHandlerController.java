package com.vodafone.mycomms.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;


/**
 * Created by str_oan on 25/08/2015.
 */
public class UncaughtExceptionHandlerController implements java.lang.Thread.UncaughtExceptionHandler
{
    private Activity mActivity;
    private Class<?> mClass;
    private Thread.UncaughtExceptionHandler androidDefaultUEH;

    public UncaughtExceptionHandlerController(Activity crashedActivity, Class<?> c)
    {
        mActivity = crashedActivity;
        mClass = c;
        androidDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex)
    {
        Log.e(Constants.TAG, "UncaughtExceptionHandlerController.uncaughtException: ", ex);
        //Crashlytics.logException(ex);
        if(isCrashedOnLoopActivity())
            androidDefaultUEH.uncaughtException(thread, ex);
        else
            startRecoverIntent();
    }

    private void startRecoverIntent()
    {
        Intent intent = new Intent(mActivity, mClass);
        intent.putExtra(Constants.IS_APP_CRASHED_EXTRA, true);
        mActivity.startActivity(intent);
        Process.killProcess(Process.myPid());
        System.exit(0);
    }
    private boolean isCrashedOnLoopActivity()
    {
        return mActivity.getClass().getSimpleName().equals(Constants.SPLASH_SCREEN_ACTIVITY)
                || mActivity.getClass().getSimpleName().equals(Constants.DASH_BOARD_ACTIVITY)
                || mActivity.getClass().getSimpleName().equals(Constants.MY_COMMS_APP);
    }
}
