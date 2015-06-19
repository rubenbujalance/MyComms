package com.vodafone.mycomms.settings.connection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by str_oan on 12/06/2015.
 */
public class FilePushToServerController extends BaseController
{

    private final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private Context mContext;
    private String authorization = "Authorization";
    private String version_token = "x-mycomms-version";
    private String ACCESS_TOKEN = "Bearer ";

    private OkHttpClient client;
    private RequestBody requestBody;
    private Request request;
    private Response response;

    public FilePushToServerController(Activity activity)
    {
        super(activity);
        this.mContext = activity;
    }

    public void sendImageRequest(String URL, String multipartFileName, File fileToSend)
    {
        client = new OkHttpClient();

        requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart(multipartFileName, fileToSend.getName(),
                        RequestBody.create(MEDIA_TYPE_PNG, fileToSend))
                .build();

        request = new Request.Builder()
                .addHeader(authorization, ACCESS_TOKEN+UserSecurity.getAccessToken
                        (mContext))
                .addHeader(version_token, getVersionName())
                .url(URL)
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
            return responseToString();
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "FilePushToServerController.executeRequest: ERROR "+e.toString());
            return null;
        }

    }

    public String responseToString()
    {
        try
        {
            return "Body: "+response.body().string();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "FilePushToServerController.convertResponseToString: ERROR "+e.toString());
            return "Wrong response!";
        }

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
            Log.e(Constants.TAG, "FilePushToServerController.getVersionName: ERROR "+e.toString());
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

    public String getAvatarURL()
    {
        try
        {
            return response.body().string();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "FilePushToServerController.getAvatarURL: ERROR "+e.toString());
            return "Wrong BODY!";
        }

    }

    public File prepareFileToSend(File inputFile, Bitmap fileBitmap, Context context, String
            multipartName, String profileId)
    {
        try
        {
            inputFile = new File(context.getCacheDir(), multipartName);
            inputFile.createNewFile();

            File avatarFile = new File(getActivity().getFilesDir(), Constants.CONTACT_AVATAR_DIR +
                    "avatar_"+profileId+".jpg");

            //Convert bitmap to byte array
            Bitmap bitmap = fileBitmap;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(inputFile);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();

            FileOutputStream fos2 = new FileOutputStream(avatarFile);
            fos2.write(bitmapdata);
            fos2.flush();
            fos2.close();

            return inputFile;
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "FilePushToServerController.prepareFileToSend: ERROR "+e.toString());
            return inputFile;
        }

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

}


