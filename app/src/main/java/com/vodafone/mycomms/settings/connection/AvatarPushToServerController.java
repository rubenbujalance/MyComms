package com.vodafone.mycomms.settings.connection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;

import java.io.File;

/**
 * Created by str_oan on 12/06/2015.
 */
public class AvatarPushToServerController extends BaseController
{

    private final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private Context mContext;
    private File file_to_send;
    private String authorization = "Authorization";
    private String version_token = "x-mycomms-version";
    private String ACCESS_TOKEN = "Bearer ";

    private OkHttpClient client;
    private RequestBody requestBody;
    private Request request;
    private Response response;

    public AvatarPushToServerController(Activity activity)
    {
        super(activity);
        this.mContext = activity;
    }

    public void sendImageRequest()
    {
        client = new OkHttpClient();

        requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart(Constants.CONTACT_AVATAR, file_to_send.getName(),
                        RequestBody.create(MEDIA_TYPE_PNG, file_to_send))
                .build();

        request = new Request.Builder()
                .addHeader(authorization, ACCESS_TOKEN+UserSecurity.getAccessToken
                        (mContext))
                .addHeader(version_token, getVersionName())
                .url(Constants.CONTACT_API_POST_AVATAR)
                .post(requestBody)
                .build();
    }

    public void sendSimpleFileRequest()
    {

    }

    public String executeRequest()
    {
        try
        {
            response = client.newCall(request).execute();
            return convertResponseToString();
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "AvatarPushToServerController.executeRequest: ERROR "+e.toString());
            return null;
        }

    }

    public String convertResponseToString()
    {
        String output = "Request: "+response.request().toString()+"\n" +
                "Body: "+response.body().toString()+"" +
                "Headers: "+response.headers().toString()+"" +
                "Message: "+response.message();

        return output;
    }

    private String getVersionName()
    {
        try
        {
            PackageInfo pinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            int versionCode = pinfo.versionCode;
            String versionName = pinfo.versionName;

            return "android/"+versionName+"."+versionCode;
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "AvatarPushToServerController.getVersionName: ERROR "+e.toString());
            return "";
        }
    }


    public void showResponse(String response)
    {
        new AlertDialog.Builder(mContext)
                .setTitle("Response")
                .setMessage(response)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public Response getResponse()
    {
        return this.response;
    }

    public void setResponse(Response response)
    {
        this.response = response;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public void setClient(OkHttpClient client) {
        this.client = client;
    }

    public RequestBody getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
    }

    public File getFile_to_send() {
        return file_to_send;
    }

    public void setFile_to_send(File file_to_send) {
        this.file_to_send = file_to_send;
    }
}


