package com.vodafone.mycomms.contacts.view;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import model.Contact;

public class ContactListViewArrayAdapter extends ArrayAdapter<Contact> {
    private Context mContext;

    public ContactListViewArrayAdapter(Context context, List<Contact> items) {
        super(context, R.layout.layout_list_item_contact, items);
        this.mContext = context;
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

        if(null != contact.getPlatform() && contact.getPlatform()
                .equals(Constants.PLATFORM_LOCAL))
        {
            viewHolder.imageCompanyLogo.setVisibility(View.GONE);
            viewHolder.imageViewDayNight.setVisibility(View.GONE);
        }
        else
        {
            viewHolder.imageCompanyLogo.setVisibility(View.VISIBLE);
            viewHolder.imageViewDayNight.setVisibility(View.VISIBLE);
        }

        //Image avatar
        final File avatarFile = new File(mContext.getFilesDir(), Constants.CONTACT_AVATAR_DIR +
                "avatar_"+contact.getContactId()+".jpg");


        //TODO RBM - Review download avatars
        if (!avatarFile.exists() && contact.getAvatar()!=null && contact.getAvatar().length()>0)
        {
            String filename = "avatar_" + contact.getContactId() + ".jpg";
            Log.i(Constants.TAG, "ContactListViewArrayAdapter.getView: AVATAR " + filename +
                    " does not exists");
            new DownloadAvatars().execute(contact.getAvatar(), filename);
        }
        if (contact.getAvatar()!=null &&
                contact.getAvatar().length()>0 &&
                contact.getAvatar().compareTo("")!=0 &&
                avatarFile.exists()) {

            viewHolder.textAvatar.setText(null);

            Picasso.with(mContext)
                    .load(avatarFile) // thumbnail url goes here
                    .placeholder(R.drawable.ic_circle_contact_photo)
                    .fit().centerCrop()
                    .into(viewHolder.imageAvatar, new Callback() {
                        @Override
                        public void onSuccess() {
                            Picasso.with(mContext)
                                    .load(avatarFile) // image url goes here
                                    .fit().centerCrop()
                                    .placeholder(viewHolder.imageAvatar.getDrawable())
                                    .into(viewHolder.imageAvatar);
                        }

                        @Override
                        public void onError() {
                        }
                    });
        }
        else
        {
            String initials = "";
            if(null != contact.getFirstName() && contact.getFirstName().length() > 0)
            {
                initials = contact.getFirstName().substring(0,1);

                if(null != contact.getLastName() && contact.getLastName().length() > 0)
                {
                    initials = initials + contact.getLastName().substring(0,1);
                }

            }

            viewHolder.imageAvatar.setImageResource(R.color.grey_middle);
            viewHolder.textAvatar.setText(initials);
        }

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
        try {
            icon = new JSONObject(contact.getPresence()).getString("icon");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(icon.compareTo("dnd")==0) viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_notdisturb);
        else if(icon.compareTo("vacation")==0) viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_vacation);
        else if(icon.compareTo("moon")==0) viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_moon);
        else viewHolder.imageViewDayNight.setImageResource(R.mipmap.ico_sun);

        //viewHolder.textViewTime.setText(Utils.getTimeFromMillis(contact.getLastSeen()));

        //Local time
        String presenceDetail = "";

        try {
            presenceDetail = new JSONObject(contact.getPresence()).getString("detail");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            assert null != presenceDetail;
            if (presenceDetail.equals("#LOCAL_TIME#")) {
                TimeZone tz = TimeZone.getTimeZone(contact.getTimezone());
                Calendar currentCal = Calendar.getInstance();

                SimpleDateFormat sourceFormat = new SimpleDateFormat("HH:mm");
                sourceFormat.setTimeZone(currentCal.getTimeZone());

                Date parsed = sourceFormat.parse(currentCal.get(Calendar.HOUR_OF_DAY) + ":" + currentCal.get(Calendar.MINUTE));

                SimpleDateFormat destFormat = new SimpleDateFormat("HH:mm");
                destFormat.setTimeZone(tz);

                String result = destFormat.format(parsed);

                viewHolder.textViewTime.setText(result);
            } else {
                viewHolder.textViewTime.setText(presenceDetail);
            }
        } catch (ParseException e) {
            e.printStackTrace();
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

    class DownloadAvatars extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... aurl) {
            try {
                Log.i(Constants.TAG, "ContactArrayAdapter.DownloadAvatars.doInBackground: ");
                URL url = new URL(aurl[0]);
                String avatarFileName = aurl[1];
                String dir = Constants.CONTACT_AVATAR_DIR;

                File file = new File(mContext.getFilesDir() + dir);
                if (file.exists()) {
                    file.delete();
                }
                file.mkdirs();
                if (downloadFile(String.valueOf(url),dir,avatarFileName)){
                    String avatarFile = mContext.getFilesDir() + dir + avatarFileName;
                    Log.i(Constants.TAG, "ContactArrayAdapter.DownloadAvatars.doInBackground: avatarFile: " + avatarFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(Constants.TAG, "ContactArrayAdapter.DownloadAvatars.doInBackground: " + e.toString());
            }
            return null;

        }

        public boolean downloadFile(final String path, String dir, String avatarFileName) {
            try {
                URL url = new URL(path);

                URLConnection ucon = url.openConnection();
                ucon.setReadTimeout(5000);
                ucon.setConnectTimeout(10000);

                InputStream is = ucon.getInputStream();
                BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);

                File file = new File(mContext.getFilesDir() + dir + avatarFileName);

                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();

                FileOutputStream outStream = new FileOutputStream(file);
                byte[] buff = new byte[5 * 1024];

                int len;
                while ((len = inStream.read(buff)) != -1) {
                    outStream.write(buff, 0, len);
                }
                outStream.flush();
                outStream.close();
                inStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(Constants.TAG, "ContactArrayAdapter.DownloadAvatars.downloadFile: " + e.toString());
                return false;
            }

            return true;
        }
    }

}