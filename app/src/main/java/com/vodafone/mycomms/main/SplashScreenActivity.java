package com.vodafone.mycomms.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.login.LoginSignupActivity;
import com.vodafone.mycomms.util.APIWrapper;
import com.vodafone.mycomms.util.SystemUiHider;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class SplashScreenActivity extends Activity {

    ProgressDialog mProgress;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_screen);
        mContext = this;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if(!APIWrapper.isConnected(this))
        {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.no_internet_connection));
            builder.setMessage(getString(R.string.no_internet_connection_is_available));
            builder.setCancelable(false);

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Launch download and install
                    dialog.dismiss();
                    finish();
                }
            });

            builder.create();
            builder.show();
        }
        else {
            new CheckVersionApi().execute(new HashMap<String, Object>(), null);
        }
    }

    private void callBackVersionCheck(final String result)
    {
        if(result == null) { //TODO - Remove before pushing to Git
        //if(result != null) {
        /*
         * New version detected! Show an alert and start the update...
         */
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.new_version_available));
            builder.setMessage(getString(R.string.must_update_to_last_application_version));
            builder.setCancelable(false);

            builder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Launch download and install
                    new UpdateVersion().execute(result);
                    dialog.dismiss();
                }
            });

            builder.create();
            builder.show();

        }
        else
        {
            Intent in = new Intent(SplashScreenActivity.this, LoginSignupActivity.class);
            startActivity(in);
            finish();
        }
    }

    private class CheckVersionApi extends AsyncTask<HashMap<String,Object>, Void, HashMap<String,Object>> {

        @Override
        protected HashMap<String,Object> doInBackground(HashMap<String,Object>[] params) {

            HashMap<String,Object> response = null;
            HashMap<String,Object> hashParams = params[0];
            HashMap<String,Object> hashHeaders = params[1];

//            //Build the JSONObject params
//            Iterator<String> it = hashParams.keySet().iterator();
//            String key,value = null;
//
//            JSONObject httpParams = new JSONObject();
//
//            try {
//                while (it.hasNext()) {
//                    key = it.next();
//                    value = hashParams.get(key);
//                    httpParams.put(key, value);
//                }
//            } catch (Exception ex) { ex.printStackTrace(); }

            response = APIWrapper.httpPostAPI("/version",hashParams,hashHeaders, SplashScreenActivity.this);

            return response;
        }

        @Override
        protected void onPostExecute(HashMap<String,Object> result) {
            JSONObject json = null;
            String text = null;
            String status = null;

            status = (String)result.get("status");

            if(result.containsKey("json")) json = (JSONObject)result.get("json");
            else if(result.containsKey("text")) text = (String)result.get("text");

            try {
                if (status.compareTo("400") == 0 &&
                        json.get("err") != null &&
                        json.get("err").toString().compareTo("invalid_version") == 0) {

                    callBackVersionCheck(json.get("data").toString());
                }
                else
                {
                    callBackVersionCheck(null);
                }
            } catch(Exception ex) {
                ex.printStackTrace();
                finish();
            }
        }
    }

    public class UpdateVersion extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String[] params) {
            boolean response = downloadAndInstall(params[0]);
            return response;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Show the progress
            mProgress = new ProgressDialog(mContext);

            mProgress.setTitle("Update " + getString(R.string.app_name));
            mProgress.setMessage("Download in progress ...");
            mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgress.setIndeterminate(true);
            mProgress.setCancelable(false);
            mProgress.setProgressNumberFormat("%d");
            mProgress.setProgressPercentFormat(null);
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            mProgress.dismiss();
        }

        public boolean downloadAndInstall(String apkurl)
        {
            try {
                String PATH = Environment.getExternalStorageDirectory() + "/download/";
                File file = new File(PATH);
                file.mkdirs();
                // Create a file on the external storage under download
                File outputFile = new File(file, "mycomms.apk");
                FileOutputStream fos = new FileOutputStream(outputFile);

                HttpGet m_httpGet = null;
                HttpResponse m_httpResponse = null;

                // Create a http client with the parameters
                HttpClient m_httpClient = new DefaultHttpClient();
                HttpConnectionParams.setConnectionTimeout(m_httpClient.getParams(), 10000);
                String result = null;

                try {
                    // Create a get object
                    m_httpGet = new HttpGet(apkurl);

                    // Execute the html request
                    m_httpResponse = m_httpClient.execute(m_httpGet);
                    HttpEntity entity = m_httpResponse.getEntity();

                    // See if we get a response
                    if (entity != null) {

                        InputStream instream = entity.getContent();

                        byte[] buffer = new byte[1024];

                        // Write out the file
                        int len1 = 0;
                        while ((len1 = instream.read(buffer)) != -1) {
                            fos.write(buffer, 0, len1);
//                            publishProgress((int)(downloaded/size)*100);
                        }

                        fos.close();
                        instream.close();// till here, it works fine - .apk is download to my sdcard in download file
                    }

                } catch (ConnectTimeoutException cte) {
                    cte.printStackTrace();
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    m_httpClient.getConnectionManager().closeExpiredConnections();
                }

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(
                        Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/download/" + "mycomms.apk"),
                        "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (Exception e1) {
                e1.printStackTrace();
                return false;
            }

            return true;
        }
    }
}
