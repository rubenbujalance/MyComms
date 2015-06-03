package com.vodafone.mycomms.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.vodafone.mycomms.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.lang.reflect.Method;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Created by str_rbm on 16/04/2015.
 */
public final class Utils extends Activity {

    private static HashMap<String, HashMap<String, String>> _countries = null;

    public static void showAlert(Context activityContext, String title, String subtitle)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
        builder.setTitle(title);

        if(subtitle != null) {
            LayoutInflater inflater = ((Activity)activityContext).getLayoutInflater();
            View view = inflater.inflate(R.layout.cv_title_subtitle, null);
            ((TextView) view.findViewById(R.id.tvTitle)).setText(title);
            ((TextView) view.findViewById(R.id.tvSubtitle)).setText(subtitle);
            builder.setCustomTitle(view);
        }
        else
        {
            builder.setTitle(title);
        }

        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builder.create();
        builder.show();
    }

    public static String getTimeFromMillis(long millis){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        return formatter.format(calendar.getTime());

    }

    public static String getDateFromMillis(long millis){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd");
        return formatter.format(calendar.getTime());

    }

    public static String dayStringFormat(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        SimpleDateFormat formatter = new SimpleDateFormat("EEE");
        return formatter.format(calendar.getTime());
    }

    private static boolean loadCountriesHash(Context context)
    {
        if(_countries != null) return true;

        //Get Data From Text Resource File Contains Json Data.
        InputStream inputStream = context.getResources().openRawResource(R.raw.countries_new);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int ctr;
        try {
            ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Utils.loadCountriesHash: ", e);
            return false;
        }

        HashMap<String,HashMap<String,String>> hash = null;

        try {
            JSONArray jArray = new JSONArray(byteArrayOutputStream.toString());
            hash = new HashMap<>();
            HashMap<String,String> map = new HashMap<>();
            JSONObject jObject;
            String key;
            Iterator it;

            for (int i = 0; i < jArray.length(); i++) {
                jObject = jArray.getJSONObject(i);
                it = jObject.keys();
                map = new HashMap<>();
                while (it.hasNext()) {
                    key = (String)it.next();
                    map.put(key, jObject.getString(key));
                }

                hash.put(map.get("ISO3166-1-Alpha-2"),map);
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "Utils.loadCountriesHash: ", e);
            return false;
        }

        _countries = hash;

        return true;
    }

    public static HashMap<String, HashMap<String,String>> getCountries(Context context)
    {
        if(!loadCountriesHash(context))
            return null;
        else return _countries;
    }

    public static HashMap<String,String> getCountry(String code, Context context)
    {
        if(!loadCountriesHash(context))
            return null;
        else return _countries.get(code);
    }

    public static void launchCall(String phone, Context context)
    {
        String url = "tel:"+phone;
        Intent in = new Intent(Intent.ACTION_CALL, Uri.parse(url));
        context.startActivity(in);
    }

    public static void launchEmail(String email, Context context)
    {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
//                        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
//                        intent.putExtra(Intent.EXTRA_TEXT, "I'm email body.");

        context.startActivity(intent);
    }

    public static void launchSms(String phone, Context context)
    {
        String url = "sms:"+phone;
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse(url));
//                        sendIntent.putExtra("sms_body", x);
        context.startActivity(sendIntent);
    }

    public static String getStringTimeDifference(int millis){
        long timeDiffMilis = millis;
        long minutes = timeDiffMilis / 60000;
        long hours = minutes / 60;
        long days = hours / 24;
        String difference = "";
        if(days >= 1) difference = days + " days ";
        else if(hours >=1) difference = hours + " hours ";
        else if(minutes >=1) difference = minutes + " min";

        return difference;
    }

    /*
    * Returns the unique device ID: IMEI for GSM and the MEID or ESN for CDMA phones.
    */
    public String getImei() {
        TelephonyManager   telephonyManager  =  ( TelephonyManager
                )getSystemService( Context.TELEPHONY_SERVICE );

        String imeistring = telephonyManager.getDeviceId();


        return imeistring;
    }

    /*
    * Returns the unique device ID: IMSI for a GSM phone.
    */
    public String getImsi() {
        TelephonyManager   telephonyManager  =  ( TelephonyManager
                )getSystemService( Context.TELEPHONY_SERVICE );

        String imsistring = telephonyManager.getSubscriberId();

        return imsistring;
    }

    /*
    * Returns the unique device ID: HardwareID
    */
    public String getHardWareId() {
        String hwID = android.os.SystemProperties.get("ro.serialno", "unknown");

        return hwID;
    }

    /*
    * Returns the unique device ID: SerialNumber
    */
    public String getSerialId() {
        String hwID = android.os.SystemProperties.get("ro.serialno", "unknown");

        String serialnum = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class );
            serialnum = (String)(   get.invoke(c, "ro.serialno", "unknown" )  );
        } catch (Exception ignored) {
            serialnum = "none";
        }

        return serialnum;
    }

    /*
    * Returns Settings.Secure.ANDROID_ID returns the unique DeviceID
    */
    public String getAndroidId() {
        String androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);

        return androidId;
    }

    /*
    * Returns the unique DeviceID
    */
    public String getDeviceId() {
        if (getImei() != null) {
            return getImei();
        } else if (getSerialId() != null) {
            return getSerialId();
        } else {
            return getAndroidId();
        }
    }

    public static String getStringChatTimeDifference(long millis){
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long currentTime = System.currentTimeMillis();
        long currentHours = TimeUnit.MILLISECONDS.toHours(currentTime);
        long currentDays = TimeUnit.MILLISECONDS.toDays(currentTime);
        if( (currentHours-hours) < 24){
            return getTimeFromMillis(millis);
        } else if ( (currentDays - days) <= 7){
            return dayStringFormat(millis) + " " + getTimeFromMillis(millis);
        } else{
            return getDateFromMillis(millis) + " " + getTimeFromMillis(millis);
        }
    }
}