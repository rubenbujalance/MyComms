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
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

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
    private int responseCode;

    public FilePushToServerController(Context context)
    {
        super(context);
        this.mContext = context;
    }

    /**
     * Prepare request for send image via multipart file with OKHTTP
     * @author str_oan
     * @param URL (String) -> request URL
     * @param multipartFileName (String) -> multipart file name for the request
     * @param fileToSend (File) -> image file which will be sent
     * @param mediaType (MediaType) -> media type of the file (e.x. image/jpg)
     */
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
                .addHeader(Constants.API_HTTP_HEADER_VERSION,
                        Utils.getHttpHeaderVersion(mContext))
                .addHeader(Constants.API_HTTP_HEADER_CONTENTTYPE,
                        Utils.getHttpHeaderContentType())
                .addHeader(Constants.API_HTTP_HEADER_AUTHORIZATION,
                        Utils.getHttpHeaderAuth(mContext))
                .url("https://" + EndpointWrapper.getBaseURL() + URL)
                .post(requestBody)
                .build();
    }

    public void prepareRequestForPushAvatar(String URL, String multipartFileName, MediaType
            mediaType, String profileId)
    {
        client = new OkHttpClient();

        String avatarStoragePath = mContext.getFilesDir() + Constants.CONTACT_AVATAR_DIR + "avatar_" + profileId + ".jpg";
        File sdAvatarStorageDir = new File(avatarStoragePath);

        requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart(multipartFileName, sdAvatarStorageDir.getName(),
                        RequestBody.create(mediaType, sdAvatarStorageDir))
                .build();

        request = new Request.Builder()
                .addHeader(Constants.API_HTTP_HEADER_VERSION,
                        Utils.getHttpHeaderVersion(mContext))
                .addHeader(Constants.API_HTTP_HEADER_CONTENTTYPE,
                        Utils.getHttpHeaderContentType())
                .addHeader(Constants.API_HTTP_HEADER_AUTHORIZATION,
                        Utils.getHttpHeaderAuth(mContext))
                .url("https://" + EndpointWrapper.getBaseURL() + URL)
                .post(requestBody)
                .build();
    }

    /**
     * Execute the request
     * @author str_oan
     * @return (String) -> response after request execution
     */
    public String executeRequest()
    {
        try
        {
            response = client.newCall(request).execute();
            this.responseCode = response.code();
            return responseToString();
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "FilePushToServerController.executeRequest: ERROR ",e);
            return null;
        }

    }

    /**
     * Converts response to readable string
     * @author str_oan
     * @return (String) -> response as a string
     */
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

    /**
     * Gets version name of the APP
     * @author str_oan
     * @return (String) -> version name as a string
     */
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

    /**
     * Returns stored avatar URL from given result
     * @author str_oan
     * @param result (String) -> result in JSON string format (name:value)
     * @return (String) -> URL as a string
     */
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

    /**
     * Prepare file to send taking values from bitmap. In this case file is image
     * @author str_oan
     * @param fileBitmap (Bitmap) -> image in bitmap format
     * @param multipartName (String) -> file name used for multipart request
     * @param profileId (String) -> profile Id of user
     * @return (File) -> file stored in correct directory depending of type (avatar/simple image
     * file)
     */
    public File prepareFileToSend(Bitmap fileBitmap, String
            multipartName, String profileId)
    {
        if(null != fileBitmap)
        {
            try
            {
                File inputFile, dir;

                if(null != multipartName && multipartName.equals(Constants.MULTIPART_AVATAR)) {
                    inputFile = new File(mContext.getFilesDir() + Constants.CONTACT_AVATAR_DIR, "avatar_" + profileId + ".jpg");
                    dir = new File(mContext.getFilesDir() + Constants.CONTACT_AVATAR_DIR);
                }
                else
                {
                    inputFile = new File(mContext.getFilesDir() + Constants.CONTACT_AVATAR_DIR, "file_" + profileId + ".jpg");
                    dir = new File(mContext.getFilesDir() + Constants.CONTACT_AVATAR_DIR);

                }

                dir.mkdirs();

                inputFile.delete();
                inputFile.createNewFile();

                //write the bytes in file
                FileOutputStream fos = new FileOutputStream(inputFile);
                fileBitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
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
        else
            return null;
    }

    public boolean storeProfileAvatar(Bitmap imageData, String profileId) {
        //get path to external storage (SD card)
        String avatarStoragePath = mContext.getFilesDir() + Constants.CONTACT_AVATAR_DIR;
        File sdAvatar = new File(avatarStoragePath, "avatar_" + profileId + ".jpg");
        try
        {
            if(sdAvatar.exists())
            {
                boolean isDeleted = sdAvatar.delete();
                Log.i(Constants.TAG, "FilePushToServerController.prepareFileToSend: is file " +
                        "deleted? " +
                        "" + isDeleted);

                boolean isDirCreated = sdAvatar.getParentFile().mkdirs();
                Log.i(Constants.TAG, "FilePushToServerController.prepareFileToSend: is dir " +
                        "created?" + isDirCreated);

                boolean isFileCreated = sdAvatar.createNewFile();
                Log.i(Constants.TAG, "FilePushToServerController.prepareFileToSend: is file " +
                        "created?" + isFileCreated);
            }
            else
            {
                boolean isDirCreated = sdAvatar.getParentFile().mkdirs();
                Log.i(Constants.TAG, "FilePushToServerController.prepareFileToSend: is dir " +
                        "created?" + isDirCreated);

                boolean isFileCreated = sdAvatar.createNewFile();
                Log.i(Constants.TAG, "FilePushToServerController.prepareFileToSend: is file " +
                        "created?" + isFileCreated);
            }

            OutputStream fileOutputStream = new FileOutputStream(sdAvatar);
            //choose another format if PNG doesn't suit you
            imageData.compress(Bitmap.CompressFormat.JPEG, 75, fileOutputStream);

            fileOutputStream.flush();
            fileOutputStream.close();
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "FilePushToServerController.prepareFileToSend: ERROR ",e);
            return false;
        }

        return true;
    }

    /**
     * Copy of file from src to destination
     * @author str_oan
     * @param src (File) -> source file
     * @param dst (File) -> destination file
     * @throws IOException -> if is not possible to read file
     */
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

    public String getResponseCode() {
        return Integer.toString(responseCode);
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}


