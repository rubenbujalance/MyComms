package com.vodafone.mycomms.settings.connection;

import android.content.Context;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by str_oan on 12/06/2015.
 */
public class FilePushToServerController extends BaseController
{

    private Context mContext;
    private final String authorization = "Authorization";
    private final String version_token = "x-mycomms-version";
    private final String ACCESS_TOKEN = "Bearer ";

    private OkHttpClient client;
    private RequestBody requestBody;
    private Request request;
    private Response response;

    public FilePushToServerController(Context context)
    {
        super(context);
        this.mContext = context;
    }

    public void sendImageRequest(String URL, String multipartFileName, File fileToSend, MediaType
            mediaType)
    {
        client = new OkHttpClient();

        requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart(multipartFileName, fileToSend.getName(),
                        RequestBody.create(mediaType, fileToSend))
                .build();

        request = new Request.Builder()
                .addHeader(authorization, ACCESS_TOKEN+UserSecurity.getAccessToken
                        (mContext))
                .addHeader(version_token, getVersionName())
                .url(URL)
                .post(requestBody)
                .build();
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
            Log.e(Constants.TAG, "FilePushToServerController.executeRequest: ERROR ",e);
            return null;
        }

    }

    public String responseToString()
    {
        try
        {
            return response.body().string();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "FilePushToServerController.convertResponseToString: ERROR ",e);
            return null;
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
            Log.e(Constants.TAG, "FilePushToServerController.getVersionName: ERROR ",e);
            return "";
        }
    }

    public String getAvatarURL(String result)
    {
        try
        {
            return result.replace("{","").replace("}","").replace("\"","").replace("avatar:","");
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "FilePushToServerController.getAvatarURL: ERROR ",e);
            return null;
        }

    }

    public File prepareFileToSend(Bitmap fileBitmap, String
            multipartName, String profileId)
    {
        try
        {
            File inputFile;
            String profId = "new_profile";
            if(null != profileId) profId = profileId;

            File dir;

            if(null != multipartName && multipartName.equals(Constants.MULTIPART_AVATAR)) {
                dir = new File(mContext.getFilesDir() + Constants.CONTACT_AVATAR_DIR);
                inputFile = new File(dir, "avatar_" + profId + ".jpg");
            }
            else {
                dir = new File(mContext.getFilesDir() + Constants.CONTACT_CHAT_FILES);
                inputFile = new File(dir, "file_" + profId + ".jpg");
            }

            dir.mkdirs();
            inputFile.createNewFile();

            //Convert bitmap to byte array

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            fileBitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();


            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(inputFile);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();

            return inputFile;
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "FilePushToServerController.prepareFileToSend: ERROR ",e);
            return null;
        }

    }

    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
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
}


