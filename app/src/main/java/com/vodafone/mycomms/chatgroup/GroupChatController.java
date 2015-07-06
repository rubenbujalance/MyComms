package com.vodafone.mycomms.chatgroup;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by str_oan on 01/07/2015.
 */
public class GroupChatController
{
    private Context mContext;

    private OkHttpClient client;
    private Request request;
    private RequestBody requestBody;
    private Response response;

    private final String authorization = "Authorization";
    private final String version_token = "x-mycomms-version";
    private final String ACCESS_TOKEN = "Bearer ";
    private final String CONTENT_TYPE = "Content_Type";
    private final String ContentTypeContent = "application/json; charset=utf-8";

    private String chatId;
    private String chatName;
    private String chatAvatar;
    private String chatAbout;
    private String chatCreator;
    private ArrayList<String> chatMembers;
    private String LOG_TAG = GroupChatController.class.getSimpleName();
    private String jsonRequest;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String URL_CREATE_GROUP_CHAT = "https://" + EndpointWrapper.getBaseURL() +
            Constants.GROUP_CHAT_API;
    private String URL_UPDATE_GROUP_CHAT = "https://" + EndpointWrapper.getBaseURL() +
            Constants.GROUP_CHAT_API_SET_MEMBERS;

    public GroupChatController(Context context)
    {
        this.mContext = context;
    }

    public boolean isCreatedURLForCreateGroupChat()
    {
        try
        {
            String jsonRequest = "{";
            if(null != chatName)
                jsonRequest = jsonRequest + "\"name\":\""+this.chatName+"\",";
            if(null != chatAvatar)
                jsonRequest = jsonRequest + "\"avatar\":\""+this.chatAvatar+"\",";
            if(null != chatAbout)
                jsonRequest = jsonRequest + "\"about\":\""+this.chatAbout+"\",";
            if(null != chatCreator)
                jsonRequest = jsonRequest + "\"creator\":\""+this.chatCreator+"\",";
            if(null != chatMembers && !this.chatMembers.isEmpty())
            {
                jsonRequest = jsonRequest + "\"members\":[";
                for(String member : this.chatMembers)
                {
                    if(member.equals(this.chatCreator))
                        jsonRequest = jsonRequest + "{\"id\":\""+member+"\",\"owner\": true},";
                    else
                        jsonRequest = jsonRequest + "{\"id\":\""+member+"\"},";
                }
                jsonRequest = jsonRequest.substring(0,jsonRequest.length()-1);
                jsonRequest = jsonRequest + "]}";
                this.jsonRequest = jsonRequest;
                return true;
            }
            return false;
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, LOG_TAG + ".isCreatedRequestURL: ERROR ", e);
            return false;
        }
    }

    public boolean isCreatedURLForUpdateGroupChat()
    {
        try
        {
            String jsonRequest = "{";
            if(null != chatName)
                jsonRequest = jsonRequest + "\"name\":\""+this.chatName+"\",";
            if(null != chatAvatar)
                jsonRequest = jsonRequest + "\"avatar\":\""+this.chatAvatar+"\",";
            if(null != chatAbout)
                jsonRequest = jsonRequest + "\"about\":\""+this.chatAbout+"\",";
            if(null != chatMembers && !this.chatMembers.isEmpty())
            {
                jsonRequest = jsonRequest + "\"members\":[";
                for(String member : this.chatMembers)
                {
                    jsonRequest = jsonRequest + "{\"id\":\""+member+"\"},";
                }
                jsonRequest = jsonRequest.substring(0,jsonRequest.length()-1);
                jsonRequest = jsonRequest + "]}";
                this.jsonRequest = jsonRequest;
                return true;
            }
            return false;
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, LOG_TAG + ".isCreatedRequestURL: ERROR ", e);
            return false;
        }
    }

    public void createGroupChatRequestForCreation()
    {
        try
        {
            client = new OkHttpClient();
            requestBody = RequestBody.create(JSON, this.jsonRequest);
            request = new Request.Builder()
                    .addHeader(authorization, ACCESS_TOKEN + UserSecurity.getAccessToken(mContext))
                    .addHeader(version_token, getVersionName())
                    .url(URL_CREATE_GROUP_CHAT)
                    .post(this.requestBody)
                    .build();
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, LOG_TAG + ".createGroupChatRequest: ERROR ", e);
        }
    }

    public void createGroupChatRequestForUpdate(String groupChatId)
    {
        try
        {
            client = new OkHttpClient();
            requestBody = RequestBody.create(JSON, this.jsonRequest);
            URL_UPDATE_GROUP_CHAT = URL_UPDATE_GROUP_CHAT + "/" + groupChatId + "/";
            request = new Request.Builder()
                    .addHeader(authorization, ACCESS_TOKEN + UserSecurity.getAccessToken(mContext))
                    .addHeader(version_token, getVersionName())
                    .url(URL_UPDATE_GROUP_CHAT)
                    .post(this.requestBody)
                    .build();
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, LOG_TAG + ".createGroupChatRequest: ERROR ", e);
        }
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
            Log.e(Constants.TAG, LOG_TAG + ".executeRequest: ERROR ", e);
            return null;
        }

    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public String getChatAvatar() {
        return chatAvatar;
    }

    public void setChatAvatar(String chatAvatar) {
        this.chatAvatar = chatAvatar;
    }

    public String getChatAbout() {
        return chatAbout;
    }

    public void setChatAbout(String chatAbout) {
        this.chatAbout = chatAbout;
    }

    public String getChatCreator() {
        return chatCreator;
    }

    public void setChatCreator(String chatCreator) {
        this.chatCreator = chatCreator;
    }

    public ArrayList<String> getChatMembers() {
        return chatMembers;
    }

    public void setChatMembers(ArrayList<String> chatMembers) {
        this.chatMembers = chatMembers;
    }

    public String responseToString()
    {
        try
        {
            return response.body().string();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, LOG_TAG+".convertResponseToString: " + "ERROR ",e);
            return null;
        }
    }

    public String getCreatedGroupChatId(String response)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getString("id");
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, LOG_TAG+".convertResponseToString: " + "ERROR ",e);
            return null;
        }
    }

    public String getUpdatedGroupChatId(String response)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getString("id");
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, LOG_TAG+".convertResponseToString: " + "ERROR ",e);
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
            Log.e(Constants.TAG, LOG_TAG+".getVersionName: ERROR ",e);
            return "";
        }
    }



}
