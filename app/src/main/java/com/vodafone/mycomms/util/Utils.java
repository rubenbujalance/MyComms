package com.vodafone.mycomms.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.squareup.picasso.Callback;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by str_rbm on 16/04/2015.
 */
public final class Utils extends Activity {

    private static HashMap<String, HashMap<String, String>> _countries = null;
    private static String _userAgent = null;
    private static String _gcmToken = null;

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

    public static String getShortStringTimeDifference(long millis){
        long timeDiffMilis = millis;
        long minutes = timeDiffMilis / 60000;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = weeks / 4;
        long years = months / 12;

        String difference = "";
        if(years >= 1) difference = years + "y ";
        else if(months >= 1) difference = months + "m ";
        else if(weeks >= 1) difference = weeks + "w ";
        else if(days >= 1) difference = days + "d ";
        else if(hours >=1) difference = hours + "h ";
        else if(minutes >=1) difference = minutes + "m";

        difference += " ago";

        return difference;
    }
    /*
    * Returns the unique device ID: IMEI for GSM and the MEID or ESN for CDMA phones.
    */
    public static String getImei(TelephonyManager telephonyManager) {
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
    public static String getHardWareId() {
        String hwID = android.os.SystemProperties.get("ro.serialno", "unknown");

        return hwID;
    }

    /*
    * Returns the unique device ID: SerialNumber
    */
    public static String getSerialId() {
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
    public static String getAndroidId(ContentResolver contentResolver) {
        String androidId = Settings.Secure.getString(contentResolver,
                Settings.Secure.ANDROID_ID);

        return androidId;
    }

    /*
    * Returns the unique DeviceID
    */
    public static String getDeviceId(ContentResolver contentResolver, TelephonyManager telephonyManager) {
        if (getImei(telephonyManager) != null) {
            return getImei(telephonyManager);
        } else if (getSerialId() != null) {
            return getSerialId();
        } else {
            return getAndroidId(contentResolver);
        }
    }

    public static String getStringChatTimeDifference(long millis, Context context){
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long currentTime = System.currentTimeMillis();
        long currentHours = TimeUnit.MILLISECONDS.toHours(currentTime);
        long currentDays = TimeUnit.MILLISECONDS.toDays(currentTime);
        if( (currentHours-hours) < 24 && (days - currentDays == 0)){
            return context.getString(R.string.today) + " " + getTimeFromMillis(millis);
        } else if ( (currentDays - days) <= 7){
            return dayStringFormat(millis) + " " + getTimeFromMillis(millis);
        } else{
            return getDateFromMillis(millis) + " " + getTimeFromMillis(millis);
        }
    }

    private static String setAMOrPM(String time)
    {
        String hourFirstPart = time.substring(0, 1);
        String hourSecondPart = time.substring(1, 2);
        if(hourFirstPart.equals("0"))
            return " a.m.";
        else if(hourFirstPart.equals(1)
                &&
                (hourSecondPart.equals(0)
                    ||hourSecondPart.equals(1)
                    ||hourSecondPart.equals(2)))
            return " a.m.";
        else
            return " p.m.";
    }


    public static String getElementFromJsonArrayString(String jsonArrayString, String key){
        Log.d(Constants.TAG, "Utils.getElementFromJsonArrayString: " + jsonArrayString + ", key=" + key);
        JSONObject jsonObject = null;
        String result = null;
        try {
            JSONArray jsonArray = new JSONArray(jsonArrayString);
            for(int i = 0; i < jsonArray.length() ; i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (!jsonObject.isNull(key)) {
                    result = jsonObject.getString(key);
                }
            }

            Log.d(Constants.TAG, "Utils.getElementFromJsonArrayString: " + jsonObject != null ? jsonObject.toString() : "null" );
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "Utils.getElementFromJsonArrayString: " ,e);
        }
        return result;
    }

    public static boolean validateStringHashMap(HashMap hashMapToValidate){
        Iterator it = hashMapToValidate.entrySet().iterator();
        while (it.hasNext()) {
            try {
                Map.Entry pair = (Map.Entry) it.next();
                String key = (String)pair.getKey();
                String value = (String) pair.getValue();
                if(key == null || key.length() <= 0 || value == null || value.length() <= 0){
                    return false;
                }
            }catch(Exception e){
                return false;
            }
        }
        return true;
    }

    public static String getHttpHeaderVersion(Context context) {
        String versionHeader = "android/";

        PackageInfo pinfo = null;
        try {
            pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.wtf(Constants.TAG, "Utils.getHttpHeaderVersion: Couldn't get application version:", e);
            return versionHeader+"0.1.0";
        }

        int versionCode = pinfo.versionCode;
        String versionName = pinfo.versionName;
        versionHeader += versionName + "." + versionCode;

        return versionHeader;
    }

    public static String getHttpHeaderAuth(Context context) {
        return "Bearer "+UserSecurity.getAccessToken(context);
    }

    public static String getHttpHeaderContentType() {
        return "application/json; charset=utf-8";
    }


    public static Bitmap resizeBitmapToStandardValue(Bitmap bitmap, int maxWidthOrHeight)
    {
        if(bitmap.getWidth() >= maxWidthOrHeight)
            return Bitmap.createScaledBitmap(bitmap,maxWidthOrHeight,maxWidthOrHeight, true);
        else
            return bitmap;
    }

    public static Bitmap adjustBitmapAsSquare(Bitmap bm)
    {
        if (bm.getWidth() >= bm.getHeight()){

            return Bitmap.createBitmap(
                    bm,
                    bm.getWidth()/2 - bm.getHeight()/2,
                    0,
                    bm.getHeight(),
                    bm.getHeight()
            );

        }
        else if(bm.getWidth() < bm.getHeight())
        {

            return Bitmap.createBitmap(
                    bm,
                    0,
                    bm.getHeight()/2 - bm.getWidth()/2,
                    bm.getWidth(),
                    bm.getWidth()
            );
        }
        else
            return bm;
    }

    public static String normalizeStringNFD(String inputString)
    {
        if(null != inputString)
        {
            String normalizedString = Normalizer.normalize(inputString, Normalizer.Form.NFD);
            normalizedString = normalizedString.replaceAll("[^\\p{ASCII}]", "");
            return normalizedString;
        }
        else
            return "";

    }

    public static int clearCacheFolder(final File dir, final int numDays) {

        int deletedFiles = 0;
        if (dir!= null && dir.isDirectory()) {
            try {
                for (File child:dir.listFiles()) {

                    //first delete subdirectories recursively
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, numDays);
                    }

                    //then delete the files and subdirectories in this dir
                    //only empty directories can be deleted, so subdirs have been done first
                    if (child.lastModified() < new Date().getTime() - numDays * DateUtils.DAY_IN_MILLIS) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            }
            catch(Exception e) {
                Log.e(Constants.TAG, "Utils.clearCacheFolder: ", e);
            }
        }
        return deletedFiles;
    }

    public static void loadContactAvatar(String firstName, String lastName, final ImageView
            imageAvatar, final TextView textAvatar, String avatarURL, float textAvatarSize)
    {
        //Image avatar
        String initials = "";
        if(null != firstName && firstName.length() > 0)
        {
            initials = firstName.substring(0, 1);

            if(null != lastName && lastName.length() > 0)
            {
                initials = initials + lastName.substring(0, 1);
            }

        }

        final String finalInitials = initials;

        imageAvatar.setImageResource(R.color.grey_middle);
        textAvatar.setVisibility(View.VISIBLE);
        textAvatar.setText(finalInitials);
        if(textAvatarSize != 0)
            textAvatar.setTextSize(TypedValue.COMPLEX_UNIT_SP, textAvatarSize);

        if (avatarURL!=null && avatarURL.length()>0)
        {
            MycommsApp.picasso
                    .load(avatarURL)
                    .placeholder(R.color.grey_middle)
                    .noFade()
                    .fit().centerCrop()
                    .into(imageAvatar, new Callback() {
                        @Override
                        public void onSuccess() {
                            textAvatar.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onError() {
                            imageAvatar.setImageResource(R.color.grey_middle);
                            textAvatar.setVisibility(View.VISIBLE);
                            textAvatar.setText(finalInitials);
                        }
                    });
        }
        else
        {
            imageAvatar.setImageResource(R.color.grey_middle);
            textAvatar.setText(initials);
        }
    }

    public static void loadContactAvatar(String firstName, String lastName, final ImageView
            imageAvatar, final TextView textAvatar, String avatarURL)
    {
        loadContactAvatar(firstName, lastName, imageAvatar, textAvatar, avatarURL, 0);
    }

    public static String getAvatarURL(String platform, String stringFiledAvatar, String avatar)
    {
        String avatarURL;
        if(null != platform && Constants.PLATFORM_SALES_FORCE.equals(platform))
        {
            if(null != stringFiledAvatar && stringFiledAvatar.length() > 0)
                avatarURL = stringFiledAvatar;
            else
                avatarURL = null;
        }
        else
            avatarURL = avatar;
        return avatarURL;
    }

    public static boolean isMainThread() {
        return (Looper.getMainLooper().getThread() == Thread.currentThread());
    }

    public static String getGCMToken(Context context) {
        if(_gcmToken==null) {

            if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) ==
                    ConnectionResult.SUCCESS) {
                //Get deviceId
                try {
                    InstanceID instanceID = InstanceID.getInstance(context);
                    String token = instanceID.getToken(Constants.GCM_SENDER_ID,
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                    if (token != null && token.length() > 0)
                        _gcmToken = token;

                    Log.i(Constants.TAG, "Utils.getGCMToken: Device Token " + token);
                } catch (Exception e) {
                    Log.w(Constants.TAG, "Utils.getGCMToken: " +
                            "Error getting GCM Device Token", e);
                }
            } else {
                Log.i(Constants.TAG, "Utils.getGCMToken: Google Play Services not available");
            }
        }

        return _gcmToken;
    }

    public static String getUserAgent(Context context) {
        if(_userAgent==null) {
            _userAgent = new WebView(context).getSettings().getUserAgentString();
        }
        return _userAgent;
    }

}
