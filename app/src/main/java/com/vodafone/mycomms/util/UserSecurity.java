package com.vodafone.mycomms.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by str_rbm on 13/05/2015.
 */
public final class UserSecurity {

    private static SharedPreferences _sharedPref;
    private static String _accessToken;
    private static String _refreshToken;
    private static long _expirationTimeMillis;

    private UserSecurity(){}

    private static boolean loadSharedPrefs(Context context)
    {
        try {
            _sharedPref = context.getSharedPreferences(
                    Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

            if(_sharedPref == null)
            {
                return false;
            }
            else
            {
                _accessToken = _sharedPref.getString(Constants.PROFILE_ACCESS_TOKEN, null);
                _refreshToken = _sharedPref.getString(Constants.PROFILE_REFRESH_TOKEN, null);
                _expirationTimeMillis = _sharedPref.getLong(Constants.PROFILE_EXPIRATION_TIME_MILLIS, 0);
            }

        } catch (Exception ex) {
            Log.e(Constants.TAG, "UserSecurity.loadSharedPrefs: \n" + ex.toString());
            return false;
        }

        return true;
    }

    public static boolean setTokens(String accessToken, String refreshToken, long expiresIn, Context context)
    {
        try {
            if (_sharedPref == null)
            {
                if(!loadSharedPrefs(context))
                    return false;
            }

            //Calculate expiration date/time
            long expirationTimeMilis = Calendar.getInstance().getTimeInMillis();
            expirationTimeMilis += expiresIn*1000;

            SharedPreferences.Editor editor = _sharedPref.edit();

            if(accessToken!=null) editor.putString(Constants.PROFILE_ACCESS_TOKEN, accessToken);
            if(refreshToken!=null) editor.putString(Constants.PROFILE_REFRESH_TOKEN, refreshToken);
            if(expirationTimeMilis!=0) editor.putLong(Constants.PROFILE_EXPIRATION_TIME_MILLIS, expirationTimeMilis);

            editor.commit();

            _accessToken = _sharedPref.getString(Constants.PROFILE_ACCESS_TOKEN, null);
            _refreshToken = _sharedPref.getString(Constants.PROFILE_REFRESH_TOKEN, null);
            _expirationTimeMillis = _sharedPref.getLong(Constants.PROFILE_EXPIRATION_TIME_MILLIS, 0);

        } catch (Exception ex) {
            Log.e(Constants.TAG, "UserSecurity.setTokens: \n" + ex.toString());
            return false;
        }

        return true;
    }

    public static String getAccessToken(Context context) {
        if (_sharedPref == null)
        {
            if(!loadSharedPrefs(context))
                return null;
        }

        return _accessToken;
    }

    public static String getRefreshToken(Context context) {
        if (_sharedPref == null)
        {
            if(!loadSharedPrefs(context))
                return null;
        }

        return _refreshToken;
    }

    public static long getExpirationTimeMilis(Context context) {
        if (_sharedPref == null)
        {
            if(!loadSharedPrefs(context))
                return 0;
        }

        return _expirationTimeMillis;
    }

    public static boolean isUserLogged(Context context)
    {
        if (_sharedPref == null)
        {
            if(!loadSharedPrefs(context))
                return false;
        }

        if(_accessToken != null && _refreshToken != null) return true;
        else return false;
    }

    public static boolean hasExpired(Context context)
    {
        if (_sharedPref == null)
        {
            if(!loadSharedPrefs(context))
                return false;
        }

        Calendar actualTime = Calendar.getInstance();
        Calendar expirationTime = Calendar.getInstance();
        expirationTime.setTimeInMillis(_expirationTimeMillis);

        return actualTime.compareTo(expirationTime) > 0;
    }

    public static boolean resetTokens(Context context)
    {
        try {
            if (_sharedPref == null)
            {
                if(!loadSharedPrefs(context))
                    return false;
            }

            SharedPreferences.Editor editor = _sharedPref.edit();

            editor.remove(Constants.PROFILE_ACCESS_TOKEN);
            editor.remove(Constants.PROFILE_REFRESH_TOKEN);
            editor.remove(Constants.PROFILE_EXPIRATION_TIME_MILLIS);

            editor.commit();

            _accessToken = null;
            _refreshToken = null;
            _expirationTimeMillis = 0;

        } catch (Exception ex) {
            Log.e(Constants.TAG, "UserSecurity.resetTokens: \n" + ex.toString());
            return false;
        }

        return true;
    }
}
