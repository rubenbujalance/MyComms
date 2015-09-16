package com.vodafone.mycomms.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.CalendarContract;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Callback;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.main.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by str_rbm on 16/04/2015.
 */
public final class Utils extends MainActivity {

    private static HashMap<String, HashMap<String, String>> _countries = null;
    private static String _userAgent = null;
    private static String _gcmToken = null;
    private static String _appVersion = null;


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

        context.startActivity(intent);
    }

    public static void launchSupportEmail(Activity activity, String subject, String text, String emailAddress, int resultCode)
    {
        String email = emailAddress;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        activity.startActivityForResult(intent, resultCode);
    }

    public static void launchSms(String phone, Context context)
    {
        String url = "sms:"+phone;
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse(url));
//                        sendIntent.putExtra("sms_body", x);
        context.startActivity(sendIntent);
    }

    public static void launchCalendar(String name, Context context){
        Calendar beginTime = Calendar.getInstance();
        beginTime.setTimeInMillis(System.currentTimeMillis());
        Calendar endTime = Calendar.getInstance();
        endTime.setTimeInMillis(System.currentTimeMillis() + 3600000); //plus one day

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, context.getResources().getString(R.string.new_meeting) + " " + name)
                .putExtra(CalendarContract.Events.DESCRIPTION, "My Comms")
//                .putExtra(CalendarContract.Events.EVENT_LOCATION, "Location")
//                .putExtra(Intent.EXTRA_EMAIL, "test@test.com")
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
        context.startActivity(intent);
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
        Log.i(Constants.TAG, "Utils.getElementFromJsonArrayString: " + jsonArrayString + ", key=" + key);
        JSONObject jsonObject = null;
        String result = null;
        try {
            JSONArray jsonArray = new JSONArray(jsonArrayString);
            for(int i = 0; i < jsonArray.length() ; i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.has(key)) {
                    result = jsonObject.getString(key);
                }
            }

            Log.i(Constants.TAG, "Utils.getElementFromJsonArrayString: " + jsonObject != null ? jsonObject.toString() : "null");
        } catch (JSONException e) {
            Log.e(Constants.TAG, "Utils.getElementFromJsonArrayString: " ,e);
        }
        return result;
    }

    public static String getElementFromJsonObjectString(String json, String key){
        JSONObject jsonObject;
        String result = "";
        try {
            jsonObject = new JSONObject(json);
            if (key.equals(Constants.CONTACT_PHONE)){
                //TODO: Pending show all telephone numbers of Contact
                if (!jsonObject.isNull(Constants.CONTACT_PHONE_WORK)){
                    result = jsonObject.getString(Constants.CONTACT_PHONE_WORK);
                } else if (!jsonObject.isNull(Constants.CONTACT_PHONE_HOME)){
                    result = jsonObject.getString(Constants.CONTACT_PHONE_HOME);
                } else if (!jsonObject.isNull(Constants.CONTACT_PHONE_MOBILE)){
                    result = jsonObject.getString(Constants.CONTACT_PHONE_MOBILE);
                } else if (!jsonObject.isNull(Constants.CONTACT_PHONE)){
                    result = jsonObject.getString(Constants.CONTACT_PHONE);
                }
            }
        } catch (JSONException e) {
            Log.e(Constants.TAG, "ContactDetailMainActivity.getElementFromJsonObjectString: " , e);
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
        return "android/"+Utils.getAppVersion(context);
    }

    public static String getAppVersion(Context context) {
        if(_appVersion==null) {
            PackageInfo pinfo;
            try {
                pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            } catch (Exception e) {
                Log.wtf(Constants.TAG, "Utils.getHttpHeaderVersion: Couldn't get application version:", e);
                return "0.0.0";
            }

            int versionCode = pinfo.versionCode;
            String versionName = pinfo.versionName;
            _appVersion = versionName + "." + versionCode;
        }

        return _appVersion;
    }

    public static String getHttpHeaderAuth(Context context) {
        return "Bearer "+UserSecurity.getAccessToken(context);
    }

    public static String getHttpHeaderContentType() {
        return "application/json; charset=utf-8";
    }


    public static Bitmap resizeBitmapToStandardValue(Bitmap bitmap, int maxWidthOrHeight)
    {

        if(null != bitmap)
        {
            if(bitmap.getWidth() > maxWidthOrHeight)
                return Bitmap.createScaledBitmap(bitmap,maxWidthOrHeight,maxWidthOrHeight, true);
            else
                return bitmap;
        }
        else
            return bitmap;
    }

    public static Bitmap adjustBitmapAsSquare(Bitmap bm)
    {
        Bitmap retBitmap = null;

        if(null != bm) {
            if (bm.getWidth() >= bm.getHeight()){
                retBitmap =  Bitmap.createBitmap(
                        bm,
                        bm.getWidth()/2 - bm.getHeight()/2,
                        0,
                        bm.getHeight(),
                        bm.getHeight()
                );
            }
            else
            {

                retBitmap = Bitmap.createBitmap(
                        bm,
                        0,
                        bm.getHeight()/2 - bm.getWidth()/2,
                        bm.getWidth(),
                        bm.getWidth()
                );
            }
        }

        return retBitmap;
    }

    public static Bitmap decodeFile(String path)
    {
        final int REQUIRED_SIZE = 90;
        final int MAX_LENGTH_IN_MB_BEFORE_REDUCE = 10;
        try
        {
            Bitmap avatar = null;
            File file = new File(path);
            //If file is more than 10 MB, we gonna reduce pixel density;
            if(file.length()/1024/1024 > MAX_LENGTH_IN_MB_BEFORE_REDUCE)
            {
                // Decode image size
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(new FileInputStream(path), null, o);

                // Find the correct scale value. It should be the power of 2.
                int scale = 1;
                while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                        o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                    scale *= 2;
                }

                // Decode with inSampleSize
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                avatar = BitmapFactory.decodeStream(new FileInputStream(path), null, o2);
                avatar = Utils.adjustBitmapAsSquare(avatar);
                avatar = Utils.resizeBitmapToStandardValue(avatar, Constants.MAX_AVATAR_WIDTH_OR_HEIGHT);
            }
            else
            {

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inDither = true;
                // Decode image size
                avatar = BitmapFactory.decodeFile(path, options);
                avatar = Utils.adjustBitmapAsSquare(avatar);
                avatar = Utils.resizeBitmapToStandardValue(avatar, Constants.MAX_AVATAR_WIDTH_OR_HEIGHT);
            }

            return avatar;
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "Utils.decodeFile: ERROR -> ",e);
            Crashlytics.logException(e);
        }
        return null;
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
            imageAvatar, final TextView textAvatar, final String avatarURL, float textAvatarSize)
    {
        //Image avatar
        String initials = "";
        if(null != firstName && firstName.length() > 0)
        {
            initials = firstName.substring(0, 1);
            if(null != lastName && lastName.length() > 0)
                initials = initials + lastName.substring(0, 1);
        }

        final String finalInitials = initials;

        if(textAvatarSize != 0)
            textAvatar.setTextSize(TypedValue.COMPLEX_UNIT_SP, textAvatarSize);

        imageAvatar.setImageResource(R.color.grey_middle);
        textAvatar.setVisibility(View.VISIBLE);
        textAvatar.setText(finalInitials);
        MycommsApp.picasso.cancelRequest(imageAvatar);

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
                            textAvatar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            imageAvatar.setImageResource(R.color.grey_middle);
                            textAvatar.setVisibility(View.VISIBLE);
                            textAvatar.setText(finalInitials);
                        }
                    });
        }
    }

    public static void loadContactAvatarDetail(String firstName, String lastName, final ImageView
            imageAvatar, final TextView textAvatar, String avatarURL)
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

        MycommsApp.picasso.cancelRequest(imageAvatar);

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
        try {
            if (_userAgent == null) {
                String version = Utils.getAppVersion(context);
                String appName = context.getString(R.string.app_name);
                String deviceManuf = Build.MANUFACTURER;
                String deviceModel = Build.MODEL;
                String sdkVersionInt = String.valueOf(Build.VERSION.SDK_INT);
                String sdkVersionStr = Build.VERSION.RELEASE;

                _userAgent = appName+"/"+version+" ("+Constants.DEVICE_DEFAULT_USER_AGENT+
                        "/"+sdkVersionInt+"; "+sdkVersionStr+"; "+deviceManuf+"/"+deviceModel+")";
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "Utils.getUserAgent: ");
            return Constants.DEVICE_DEFAULT_USER_AGENT + " " +
                    android.os.Build.VERSION.SDK_INT;
        }
        return _userAgent;
    }

    @SuppressLint("NewApi")
    public static void removeCookies()
    {
        final int API_LEVEL = android.os.Build.VERSION.SDK_INT;

        if(API_LEVEL >= 21)
            CookieManager.getInstance().removeSessionCookies(null);
        else
            CookieManager.getInstance().removeAllCookie();
    }

    @SuppressLint("NewApi")
    public static String getRealPathFromUri(Uri uri, Context context)
    {
        //check here to KITKAT or <span id="IL_AD8" class="IL_AD">new version</span>
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://<span id=\"IL_AD2\" class=\"IL_AD\">downloads</span>/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("<span id=\"IL_AD7\" class=\"IL_AD\">video</span>".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google <span id="IL_AD4" class="IL_AD">Photos</span>.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static void showKeyboard(final View view, final Context context)
    {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            public void run() {
                view.requestFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(
                        context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 500);
    }

    public static void hideKeyboard(final View view, final Context context)
    {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            public void run() {
                view.requestFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(
                        context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }, 300);
    }

    public static String timestampToFormatedString(long timestamp, String format) {
        String formatedDate = null;
        if(timestamp==0) return null;

        try {
            //Format in ISO
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setTimeZone(c.getTimeZone());
            formatedDate = sdf.format(c.getTime());


        } catch (Exception e) {
            Log.e(Constants.TAG, "Utils.timestampToFormatedString: ",e);
        }

        return formatedDate;
    }

    public static Date dateToUTC(Date date) {
        TimeZone tz = TimeZone.getDefault();
        Date ret = new Date( date.getTime() - tz.getRawOffset() );

        // if we are now in DST, back off by the delta.  Note that we are checking the GMT date, this is the KEY.
        if ( tz.inDaylightTime( ret )){
            Date dstDate = new Date( ret.getTime() - tz.getDSTSavings() );

            // check to make sure we have not crossed back into standard time
            // this happens when we are on the cusp of DST (7pm the day before the change for PDT)
            if ( tz.inDaylightTime( dstDate )){
                ret = dstDate;
            }
        }

        return ret;
    }

    public static String isoDateToTimezone(String date) {
        String result = "";

        try {
            SimpleDateFormat sourceFormat = new SimpleDateFormat(Constants.API_DATE_FULL_FORMAT);
//            sourceFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date parsed = sourceFormat.parse(date); // => Date is in UTC now

            SimpleDateFormat destFormat = new SimpleDateFormat(Constants.API_DATE_FULL_FORMAT);
            destFormat.setTimeZone(TimeZone.getDefault());

            result = destFormat.format(parsed);
        } catch (Exception e) {
            Log.e(Constants.TAG, "Utils.utcISOToTimezone: ");
        }

        return result;
    }

    public static String getProfileId(Context context){
        SharedPreferences sp = context.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        return sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
    }

    public static long setPlatformOrder(String platform) {
        long platformOrder;

        switch (platform) {
            case Constants.PLATFORM_MY_COMMS:
                platformOrder = Constants.ORDER_MYCOMMS;
                break;
            case Constants.PLATFORM_SALES_FORCE:
                platformOrder = Constants.ORDER_SALES_FORCE;
                break;
            case Constants.PLATFORM_GLOBAL_CONTACTS:
                platformOrder = Constants.ORDER_GLOBAL_CONTACTS;
                break;
            case Constants.PLATFORM_LOCAL:
                platformOrder = Constants.ORDER_LOCAL;
                break;
            default:
                platformOrder = 0;
        }

        return platformOrder;
    }

    public static String getPlaformName(String platform, Context context) {
        String platformName;

        switch (platform) {
            case Constants.PLATFORM_MY_COMMS:
                platformName = context.getResources().getString(R.string.mycomms);
                break;
            case Constants.PLATFORM_SALES_FORCE:
                platformName = context.getResources().getString(R.string.set_salesforce);
                break;
            case Constants.PLATFORM_GLOBAL_CONTACTS:
                platformName = context.getResources().getString(R.string.set_vodafone_global_list);
                break;
            case Constants.PLATFORM_LOCAL:
                platformName = context.getResources().getString(R.string.set_local_contacts);
                break;
            default:
                platformName = "";
        }

        return platformName;
    }

    /**
     * Gets custom alert dialog title view
     * @author str_oan
     * @return (LinearLayout) -> custom title
     */
    public static View getCustomAlertTitleView(Context context, int viewId)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(viewId, null);
    }

    //Check network connection
    public static boolean isConnected(Context context){
        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    public static boolean checkConnectionAndAlert(Context context){
        if(isConnected(context))
        {
            return true;
        }
        else
        {
            Utils.showAlert(
                    context,
                    context.getString(R.string.no_internet_connection),
                    context.getString(R.string.no_internet_connection_is_available));

            return false;
        }
    }

    public static HashMap<String,Object> okHttpResToHash(Response response)
    {
        if(response == null) return null;
        HashMap<String, Object> hash = new HashMap<>();
        String textEntity = null;
        try {
            if(response.body() != null)
                textEntity = response.body().string();

            String contentType;

            if(textEntity != null && textEntity.length() > 0) {
                contentType = response.header("Content-Type");

                if (contentType.compareTo("application/json") == 0) {
                    JSONObject json = new JSONObject(textEntity);
                    hash.put("json", json);
                } else {
                    hash.put("text", textEntity);
                }
            }

            hash.put("status", String.valueOf(response.code()));

        } catch(Exception ex) {
            Log.e(Constants.TAG, "APIWrapper.httpResToHash: \n" + ex.toString());
            return null;
        }

        return hash;
    }

    public static String getCountryValue(String countryCode, Context context) {
        String countryValue;
        HashMap countryHashMap = Utils.getCountry(countryCode, context);
        if (countryHashMap.get("is_special") != null) {
            if (countryHashMap.get("is_special").equals("Yes") || countryHashMap.get("name").toString().length() > 9) {
                countryValue = countryHashMap.get("FIPS").toString();
            } else {
                countryValue = countryHashMap.get("name").toString();
            }
        } else {
            if (countryHashMap.get("name").toString().length() > 9)
                countryValue = countryHashMap.get("FIPS").toString();
            else
                countryValue = countryHashMap.get("name").toString();
        }
        return countryValue;
    }
}
