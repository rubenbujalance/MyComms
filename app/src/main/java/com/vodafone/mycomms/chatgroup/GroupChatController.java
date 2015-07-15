package com.vodafone.mycomms.chatgroup;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import model.GroupChat;

/**
 * Created by str_oan on 01/07/2015.
 */
public class GroupChatController
{
    private Context mContext;
    private String profileId;

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
    private ArrayList<String> chatOwners;
    private ArrayList<String> chatMembers;
    private String LOG_TAG = GroupChatController.class.getSimpleName();
    private String jsonRequest;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private String responseCode = "";

    public static  String URL_CREATE_GROUP_CHAT = "https://" + EndpointWrapper.getBaseURL() +
            Constants.GROUP_CHAT_API;
    public static  String URL_UPDATE_GROUP_CHAT = "https://" + EndpointWrapper.getBaseURL() +
            Constants.GROUP_CHAT_API_SET_MEMBERS;
    public static  String URL_GET_ALL_GROUP_CHATS = "https://" + EndpointWrapper.getBaseURL() +
            Constants.GROUP_CHAT_API;

    public GroupChatController(Context context, String profileId)
    {
        this.mContext = context;
        this.profileId = profileId;
    }

    /**
     * Creates JSON body for crete group chat request
     * @author str_oan
     * @return true if JSON is created and false otherwise.
     */
    public boolean isCreatedJSONBodyForCreateGroupChat()
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
                    if(this.chatOwners.contains(member))
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
            Log.e(Constants.TAG, LOG_TAG + ".isCreatedJSONBodyForCreateGroupChat: ERROR ", e);
            Crashlytics.logException(e);
            return false;
        }
    }

    /**
     * Creates JSON body for UPDATE group chat request
     * @author str_oan
     * @return true if JSON is created and false otherwise.
     */
    public boolean isCreatedJSONBodyForUpdateGroupChat()
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
                    if(this.chatOwners.contains(member))
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
            Log.e(Constants.TAG, LOG_TAG + ".isCreatedJSONBodyForUpdateGroupChat: ERROR ", e);
            Crashlytics.logException(e);
            return false;
        }
    }

    /**
     * Creates request for any kind of request type and URL.
     * @param URL
     * @param requestType
     */
    public void createRequest(String URL, String requestType)
    {
        createRequest(URL, requestType, null);
    }
    public void createRequest(String URL, String requestType, String groupChatIdForUpdate)
    {
        try
        {
            client = new OkHttpClient();

            switch (requestType)
            {
                case "post":
                    requestBody = RequestBody.create(JSON, this.jsonRequest);
                    request = new Request.Builder()
                            .addHeader(authorization, ACCESS_TOKEN + UserSecurity.getAccessToken(mContext))
                            .addHeader(version_token, getVersionName())
                            .url(URL)
                            .post(this.requestBody)
                            .build();
                    break;

                case "put":
                    if (null != groupChatIdForUpdate)
                        URL = URL + "/" + groupChatIdForUpdate + "/members";

                    requestBody = RequestBody.create(JSON, this.jsonRequest);
                    request = new Request.Builder()
                            .addHeader(authorization, ACCESS_TOKEN + UserSecurity.getAccessToken(mContext))
                            .addHeader(version_token, getVersionName())
                            .url(URL)
                            .put(this.requestBody)
                            .build();
                    break;

                case "get":
                    request = new Request.Builder()
                            .addHeader(authorization, ACCESS_TOKEN + UserSecurity.getAccessToken(mContext))
                            .addHeader(version_token, getVersionName())
                            .url(URL)
                            .get()
                            .build();
                    break;
            }
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, LOG_TAG + ".createRequest: ERROR ", e);
            Crashlytics.logException(e);
        }
    }

    /**
     * Execute the request
     * @return (String) -> response body as string
     */
    public String executeRequest()
    {
        try
        {
            response = client.newCall(request).execute();
            this.responseCode = Integer.toString(response.code());
            return responseToString();
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, LOG_TAG + ".executeRequest: ERROR ", e);
            Crashlytics.logException(e);
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
            Log.e(Constants.TAG, LOG_TAG+".convertResponseToString: " + "ERROR ",e);
            Crashlytics.logException(e);
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
            Crashlytics.logException(e);
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
            Crashlytics.logException(e);
            return "";
        }
    }

    /**
     * Gets all group chats from server which belong to profile id as Array List.
     * If some error has been thrown or nothing to get, return will be empty list;
     * @author str_oan
     * @return (ArrayList) -> list of group chats is any.
     */
    public ArrayList<GroupChat> getAllGroupChatsFromAPI()
    {
        createRequest(URL_GET_ALL_GROUP_CHATS, "get");
        String response = executeRequest();
        return getResponseAsGroupChats(response);
    }

    /**
     * Inserts group chats into realm if they not exist
     * @param chats (ArrayList<GroupChats>) group chats to be inserted
     * @return (String) -> ids of inserted group chats separated by "@"
     */
    public String insertGroupChatsIntoRealmIfNotExist(ArrayList<GroupChat> chats)
    {
        RealmGroupChatTransactions realmGroupChatTransactions =
                new RealmGroupChatTransactions(mContext, profileId);
        String insertedGroupChatIds = null;
        for(GroupChat chat : chats)
        {
            if(null == realmGroupChatTransactions.getGroupChatById(chat.getId()))
            {
                realmGroupChatTransactions.insertOrUpdateGroupChat(chat);
                if(null == insertedGroupChatIds) insertedGroupChatIds = chat.getId();
                else insertedGroupChatIds = insertedGroupChatIds + "@" + chat.getId();
            }
        }
        return insertedGroupChatIds;
    }

    /**
     * Created new group chat from JSON object
     * @param object (JSONObject) -> object for create group chat
     * @return (GroupChat) -> created group chat
     * @throws Exception -> exception during creation
     */
    private GroupChat createNewGroupChat(JSONObject object) throws Exception
    {
        String[] membersAndOwners = getMembersAndOwners(object);
        if(null != membersAndOwners)
        {
            GroupChat groupChat = new GroupChat
                    (
                            object.getString("id")
                            , object.getString("creator")
                            , object.getString("creator")
                            , object.getString("name")
                            , object.getString("avatar")
                            , object.getString("about")
                            , membersAndOwners[0]
                            , membersAndOwners[1]
                            , Calendar.getInstance().getTimeInMillis()
                            , ""
                            , ""
                    );

            return groupChat;
        }
        else
        {
            return null;
        }
    }

    private String[] getMembersAndOwners(JSONObject object)
    {
        try
        {
            String[] membersAndOwners = new String[2];
            JSONArray array = object.getJSONArray("members");
            for(int i = 0; i < array.length(); i++)
            {
                JSONObject o = array.getJSONObject(i);
                if(null == membersAndOwners[0]) membersAndOwners[0] = o.getString("id");
                else membersAndOwners[0] = membersAndOwners[0] + "@" + o.getString("id");


                if(null == membersAndOwners[1])
                {
                    if(!o.isNull("owner"))
                        membersAndOwners[1] = o.getString("owner");
                }
                else
                {
                    if(!o.isNull("owner"))
                        membersAndOwners[1] = membersAndOwners[1] + o.getString("owner");
                }
            }
            return membersAndOwners;
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, LOG_TAG+".getMembersAndOwners: ERROR ",e);
            Crashlytics.logException(e);
            return null;
        }
    }

    private ArrayList<GroupChat> getResponseAsGroupChats(String jsonObject)
    {
        ArrayList<GroupChat> groupChats = new ArrayList<>();
        try
        {
            JSONArray jsonArray = new JSONArray(jsonObject);
            for(int i = 0; i < jsonArray.length(); i++)
            {
                GroupChat groupChat = createNewGroupChat(jsonArray.getJSONObject(i));
                if(null != groupChat)
                    groupChats.add(groupChat);
            }
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, LOG_TAG+".getResponseAsGroupChats: ERROR ",e);
            Crashlytics.logException(e);
        }

        return groupChats;
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

    public ArrayList<String> getChatOwners() {
        return chatOwners;
    }

    public void setChatOwners(ArrayList<String> chatOwners) {
        this.chatOwners = chatOwners;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
}
