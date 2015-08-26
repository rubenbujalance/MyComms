package com.vodafone.mycomms.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Process;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import java.io.PrintWriter;
import java.io.StringWriter;


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

    public UncaughtExceptionHandlerController()
    {
        mActivity = null;
        mClass = null;
        androidDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex)
    {
        Thread t = thread;
        Throwable tr = ex;
        Log.e(Constants.TAG, "UncaughtExceptionHandlerController.uncaughtException: ", ex);
        Crashlytics.logException(ex);
        String errorMessage = getStringFromThrowable(ex);
        if(null == mClass || null == mActivity)
            androidDefaultUEH.uncaughtException(thread, ex);
        else
        {
            if(null != errorMessage)
                startRecoverIntent("Exception reference: \n"+errorMessage);
            else
                startRecoverIntent("Exception reference: \n"+ex.toString());
        }
    }

    private void startRecoverIntent(String errorMessage)
    {
        Intent intent = new Intent(mActivity, mClass);
        intent.putExtra(Constants.IS_APP_CRASHED_EXTRA, true);
        intent.putExtra(Constants.APP_CRASH_MESSAGE, errorMessage);
        mActivity.startActivity(intent);
        Process.killProcess(Process.myPid());
        System.exit(0);
    }

    private String getStringFromThrowable(Throwable ex)
    {
        try
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            return sw.toString(); // stack trace as a string
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "UncaughtExceptionHandlerController.getStringFromThrowable: ", e);
            Crashlytics.logException(e);
            return null;
        }

    }
}
