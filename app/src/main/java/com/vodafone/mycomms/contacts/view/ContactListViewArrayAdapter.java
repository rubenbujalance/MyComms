package com.vodafone.mycomms.contacts.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.AvatarSFController;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import model.Contact;

public class ContactListViewArrayAdapter extends ArrayAdapter<Contact> {
    private Context mContext;
    private String profileId;

    public ContactListViewArrayAdapter(Context context, List<Contact> items) {
        super(context, R.layout.layout_list_item_contact, items);
        this.mContext = context;
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        this.profileId = sharedPreferences.getString(Constants.PROFILE_ID_SHARED_PREF, null);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.layout_list_item_contact, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.imageAvatar = (ImageView) convertView.findViewById(R.id.companyLogo);
            viewHolder.textAvatar = (TextView) convertView.findViewById(R.id.avatarText);
            viewHolder.textViewCompany = (TextView) convertView.findViewById(R.id.list_item_content_company);
            viewHolder.textViewName = (TextView) convertView.findViewById(R.id.list_item_content_name);
            viewHolder.textViewPosition = (TextView) convertView.findViewById(R.id.list_item_content_position);
            viewHolder.textViewTime = (TextView) convertView.findViewById(R.id.list_item_status_local_time);
            viewHolder.textViewCountry = (TextView) convertView.findViewById(R.id.list_item_status_local_country);
            viewHolder.imageViewDayNight = (ImageView) convertView.findViewById(R.id.list_item_image_status_daynight);
            viewHolder.imageCompanyLogo = (ImageView) convertView.findViewById(R.id.list_item_content_companylogo);

            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
              viewHolder = (ViewHolder) convertView.getTag();
        }

        // update the item view
        Contact contact = getItem(position);

        viewHolder.imageViewDayNight.setVisibility(View.VISIBLE);
        if(null != contact.getPlatform()
                && Constants.PLATFORM_SALES_FORCE.equals(contact.getPlatform()))
        {
            viewHolder.imageCompanyLogo.setImageResource(R.drawable.btn_sales_force);
        }
        else if (null != contact.getPlatform()
                && Constants.PLATFORM_MY_COMMS.equals(contact.getPlatform()))
        {
            viewHolder.imageCompanyLogo.setImageResource(R.drawable.icon_mycomms);
        }
        else if (null != contact.getPlatform()
                && Constants.PLATFORM_LOCAL.equals(contact.getPlatform()))
        {
            viewHolder.imageViewDayNight.setVisibility(View.INVISIBLE);
            viewHolder.imageCompanyLogo.setImageResource(R.drawable.icon_local_contacts);
        }

        //Image avatar
        if (null != contact.getPlatform() && contact.getPlatform().equalsIgnoreCase(Constants.PLATFORM_SALES_FORCE))
        {
            AvatarSFController avatarSFController = new AvatarSFController
                    (
                            mContext
                            , contact.getContactId()
                            , this.profileId
                            , true
                            , false
                    );
            avatarSFController.getSFAvatar(contact.getAvatar());
        }

        //Image avatar
        Utils.loadContactAvatar
                (
                    contact.getFirstName()
                    , contact.getLastName()
                    , viewHolder.imageAvatar
                    , viewHolder.textAvatar
                    , Utils.getAvatarURL(
                            contact.getPlatform()
                            , contact.getStringField1()
                            , contact.getAvatar())
                );

        viewHolder.textViewCompany.setText(contact.getCompany());
        viewHolder.textViewName.setText(contact.getFirstName() + " " + contact.getLastName() );
        viewHolder.textViewPosition.setText(contact.getPosition());

        String country = "";

        try {
            if (contact.getCountry() != null && contact.getCountry().length() > 0) {
                if (Utils.getCountry(contact.getCountry(), mContext).get("is_special") != null) {
                    if (Utils.getCountry(contact.getCountry(), mContext).get("is_special").equals("Yes")) {
                        country = Utils.getCountry(contact.getCountry(), mContext).get("FIPS");
                    } else {
                        country = Utils.getCountry(contact.getCountry(), mContext).get("name");
                    }
                } else {
                    country = Utils.getCountry(contact.getCountry(), mContext).get("name");
                }
            }
        } catch (NullPointerException e){
            Log.e(Constants.TAG, "ContactListViewArrayAdapter.getView: NullPointerException " + e);
        }

        viewHolder.textViewCountry.setText(country);

        //Icon
        String icon = "";
        try
        {
            if(null != contact.getPresence())
            {
                JSONObject jsonObject =  new JSONObject(contact.getPresence());
                if(!jsonObject.isNull("icon"))
                {
                    icon = new JSONObject(contact.getPresence()).getString("icon");
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(icon.compareTo("dnd")==0) viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_notdisturb);
        else if(icon.compareTo("vacation")==0) viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_vacation);
        else if(icon.compareTo("moon")==0) viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_moon);
        else if(icon.compareTo("sun")==0) viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_sun);
        else viewHolder.imageViewDayNight.setVisibility(View.INVISIBLE);

        //Local time
        String presenceDetail = "";
        viewHolder.textViewTime.setVisibility(View.VISIBLE);

        try {
            presenceDetail = new JSONObject(contact.getPresence()).getString("detail");
            if (presenceDetail.equals("#LOCAL_TIME#"))
            {
                if(null != contact.getTimezone())
                {
                    TimeZone tz = TimeZone.getTimeZone(contact.getTimezone());
                    Calendar c = Calendar.getInstance(tz);
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                    format.setTimeZone(c.getTimeZone());
                    Date parsed = format.parse(c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE));
                    String result = format.format(parsed);
                    viewHolder.textViewTime.setText(result);
                }
                else
                {
                    viewHolder.textViewTime.setText(" ");
                }

            } else {
                viewHolder.textViewTime.setText(presenceDetail);
            }
        } catch (Exception e) {
            Log.i(Constants.TAG, "ContactListViewArrayAdapter.getView: No presence found");
            viewHolder.textViewTime.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    /**
     * The view holder design pattern
     */
    private static class ViewHolder {
        TextView textViewName;
        TextView textViewPosition;
        TextView textViewCompany;
        TextView textViewTime;
        TextView textViewCountry;
        ImageView imageViewDayNight;
        ImageView imageAvatar;
        ImageView imageCompanyLogo;
        TextView textAvatar;
    }

//public class OkHttpSaveBitmapToDiskAsyncTask extends AsyncTask<Void, Void, Void> {
//
//    ImageView avatarImage;
//    TextView avatarText;
//    File avatarFile;
//    Bitmap bitmap;
//
//    public OkHttpSaveBitmapToDiskAsyncTask(ImageView avatarImage, TextView avatarText,
//                                           File avatarFile, Bitmap bitmap) {
//        this.avatarImage = avatarImage;
//        this.avatarText = avatarText;
//        this.avatarFile = avatarFile;
//        this.bitmap = bitmap;
//    }
//
//    @Override
//    protected Void doInBackground(Void... params) {
//        Log.e(Constants.TAG, "OkHttpSaveBitmapToDiskAsyncTask.doInBackground: START "+avatarFile);
//
//        try {
//            //Descarga del archivo local
//            Log.e(Constants.TAG, "OkHttpSaveBitmapToDiskAsyncTask.doInBackground (INICIO DownloadAvatar by Picasso: "
//                    + avatarFile.toString());
//
//            avatarFile.createNewFile();
//            FileOutputStream ostream = new FileOutputStream(avatarFile);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, ostream);
//            ostream.close();
//
//            Log.e(Constants.TAG, "OkHttpSaveBitmapToDiskAsyncTask.doInBackground (FIN DownloadAvatar by Picasso: "
//                    + avatarFile.toString());
//
//        } catch (Exception e) {
//            Log.e(Constants.TAG, "DashBoardActivity.run(Picasso avatar Target): ", e);
//            if(avatarFile.exists())
//            {
//                avatarFile.delete();
//                bitmap = null;
//            }
//        }
//
//        Log.e(Constants.TAG, "OkHttpSaveBitmapToDiskAsyncTask.doInBackground: END "+avatarFile);
//
//        return null;
//    }
//
//    @Override
//    protected void onPostExecute(Void aVoid) {
//        Log.e(Constants.TAG, "OkHttpSaveBitmapToDiskAsyncTask.onPostExecute: "+avatarFile);
//
//        if(bitmap!=null) {
//            //Carga del bitmap
//            avatarText.setVisibility(View.INVISIBLE);
//
//            Picasso.with(mContext)
//                    .load(avatarFile)
//                    .fit().centerCrop()
//                    .into(avatarImage);
//        }
//    }
//}

//    class DownloadAvatars extends AsyncTask<String, String, String> {
//
//        @Override
//        protected String doInBackground(String... aurl) {
//            try {
//                Log.i(Constants.TAG, "ContactArrayAdapter.DownloadAvatars.doInBackground: ");
//                URL url = new URL(aurl[0]);
//                String avatarFileName = aurl[1];
//                String dir = Constants.CONTACT_AVATAR_DIR;
//
//                File file = new File(mContext.getFilesDir() + dir);
//                file.mkdirs();
//
//                if (downloadFile(String.valueOf(url),dir,avatarFileName)){
//                    String avatarFile = mContext.getFilesDir() + dir + avatarFileName;
//                    Log.i(Constants.TAG, "ContactArrayAdapter.DownloadAvatars.doInBackground: avatarFile: " + avatarFile);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                Log.e(Constants.TAG, "ContactArrayAdapter.DownloadAvatars.doInBackground: " + e.toString());
//            }
//            return null;
//        }
//
//        public boolean downloadFile(final String path, String dir, String avatarFileName) {
//            try {
//                URL url = new URL(path);
//
//                URLConnection ucon = url.openConnection();
//                ucon.setReadTimeout(Constants.HTTP_READ_AVATAR_TIMEOUT);
//                ucon.setConnectTimeout(10000);
//
//                InputStream is = ucon.getInputStream();
//                BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);
//
//                File file = new File(mContext.getFilesDir() + dir, avatarFileName);
//
//                if (file.exists()) {
//                    file.delete();
//                }
//                file.createNewFile();
//
//                FileOutputStream outStream = new FileOutputStream(file);
//                byte[] buff = new byte[5 * 1024];
//
//                int len;
//                while ((len = inStream.read(buff)) != -1) {
//                    outStream.write(buff, 0, len);
//                }
//                outStream.flush();
//                outStream.close();
//                inStream.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//                Log.e(Constants.TAG, "ContactArrayAdapter.DownloadAvatars.downloadFile: " + e.toString());
//                return false;
//            }
//
//            return true;
//        }
//    }

}