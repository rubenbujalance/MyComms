package com.vodafone.mycomms.xmpp;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.events.AllPendingMessagesReceivedEvent;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.events.GroupChatCreatedEvent;
import com.vodafone.mycomms.events.MessageStatusChanged;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.OKHttpWrapper;
import com.vodafone.mycomms.util.UserSecurity;
import com.vodafone.mycomms.util.Utils;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.ping.PingManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import io.realm.Realm;
import model.Chat;
import model.ChatMessage;
import model.GroupChat;

/**
 * Created by str_rbm on 03/06/2015.
 */
public final class XMPPTransactions {
    private static XMPPTCPConnection _xmppConnection = null;
    private static ReconnectionManager _reconnectionMgr = null;
    private static XMPPTCPConnectionConfiguration.Builder _xmppConfigBuilder;
    private static String accessToken;
    private static String _profile_id;
    private static Context _appContext;
    private static ConnectionListener _connectionListener;
    private static String _device_id;

    //Control to not retry consecutive connections
    private static boolean _isConnecting = false;
    private static long _isConnectingTime;

    //Control of pings to server
    private static Thread pingThread = null;
    private static String pingWaitingID = null;
    private static boolean isPinging = false;
    private static PingManager _pingManager = null;

    //Control of sleep when app in background
    private static Thread sleepThread = null;

    //Last ChatMessage sent, to control unexpected server disconnection
    private static String _lastChatMessageSent;
    private static long _lastChatMessageSentTimestamp;

    //Time between pings to server
    private static final int PINGING_TIME_MILLIS = 30000;

    //Pending messages handling
    private static int _pendingMessages;

    /*
     * Methods
     */

    public static void initializeMsgServerSession(Context appContext)
    {
        if(_isConnecting || MycommsApp.disconnectedProcess) return;

        //Start connection
//        if(_xmppConnection == null || _xmppConnection.isDisconnectedButSmResumptionPossible() ||
//                !_xmppConnection.isConnected() || force) {

        if (!MycommsApp.disconnectedProcess) {
            Log.i(Constants.TAG, "XMPPTransactions.initializeMsgServerSession: Connecting");

            //Save context and reset connection
            if (_appContext == null)
                _appContext = appContext;

            //Check profile id exists
            if (_profile_id == null) {
                //Get profile_id
                SharedPreferences sp = appContext.getSharedPreferences(
                        Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
                _profile_id = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);
            }
            else {
                Log.i(Constants.TAG, "XMPPTransactions.initializeMsgServerSession: PROFILE ID IS NOT NULL" + _profile_id);
            }

            //If at this point it is no profile loaded, it indicates there is no user profile logged
            if(_profile_id==null || _profile_id.length()==0) return;

            //Device ID
            if (_device_id == null) {
                _device_id = Utils.getDeviceId(_appContext.getContentResolver(),
                        (TelephonyManager) _appContext.getSystemService(Service.TELEPHONY_SERVICE));
            }

            //Configuration for the connection
            if (_xmppConfigBuilder == null) {
                loadConnectionConfig();
            }
            else if (_xmppConfigBuilder.build().getUsername()==null) {
                _xmppConfigBuilder = null;
                loadConnectionConfig();
                Log.e(Constants.TAG, "XMPPTransactions.initializeMsgServerSession: CONFIG BUILDER (CLEAN) AND NULL");
            }
            else {
                Log.e(Constants.TAG, "XMPPTransactions.initializeMsgServerSession: CONFIG BUILDER NOT NULL");
            }

            //Set a timer to check connection every 5 seconds
            intervalPinging(PINGING_TIME_MILLIS);

            //Connect to server
            XMPPOpenConnectionTask xmppOpenConnectionTask = new XMPPOpenConnectionTask();
            xmppOpenConnectionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        }
        }
    }

    private static void loadConnectionConfig()
    {
        _xmppConfigBuilder = XMPPTCPConnectionConfiguration.builder();

        accessToken = UserSecurity.getAccessToken(_appContext);
        _xmppConfigBuilder.setUsernameAndPassword(_profile_id, accessToken);
        _xmppConfigBuilder.setResource(_device_id);
        _xmppConfigBuilder.setServiceName(Constants.XMPP_PARAM_DOMAIN);
        _xmppConfigBuilder.setHost(EndpointWrapper.getXMPPHost());
        _xmppConfigBuilder.setPort(Constants.XMPP_PARAM_PORT);
        _xmppConfigBuilder.setEnabledSSLProtocols(new String[]{"TLSv1.2"});
        _xmppConfigBuilder.setDebuggerEnabled(true);
        _xmppConfigBuilder.setSendPresence(false);
        _xmppConfigBuilder.setCompressionEnabled(false);
    }

    private static void intervalPinging(final int miliseconds)
    {
        if(pingThread!=null) return;

        pingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(Constants.TAG, "XMPPTransactions.intervalPinging: START");
                while(!Thread.interrupted()) {
                    try {
                        Thread.sleep(miliseconds);
                        if(!_isConnecting && !isPinging && !MycommsApp.disconnectedProcess) {
                            isPinging = true;
                            sendPing();
                            Thread.sleep(2000);
                            if (pingWaitingID != null)
                                initializeMsgServerSession(_appContext);

                            isPinging = false;
                        }
                    }catch (Exception e) {
                        Log.e(Constants.TAG, "XMPPTransactions.intervalPinging: ",e);
                        Crashlytics.logException(e);
                        pingThread = null;
                        isPinging = false;
                    }
                }
                Log.i(Constants.TAG, "XMPPTransactions.intervalPinging: END");
                pingThread = null;
            }
        });

        pingThread.start();
    }

    public static void sleepXMPPAfterMilis(final int miliseconds)
    {
        if(sleepThread!=null)
            sleepThread.interrupt();

        sleepThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(Constants.TAG, "XMPPTransactions.sleepThread: Sleeping in "+miliseconds/1000+" seconds");
                //Divide the time in 10 seconds slots
                int tenSecSlots = miliseconds / 10000;
                int i = 1;

                try {
                    while(i<=tenSecSlots && !Thread.interrupted()) {
                        //Sleep during 10 seconds
                        Thread.sleep(10000);
                        i++;
                    }

                    //If have arrived to the end of counter, go to sleep
                    if(i==tenSecSlots) {
                        Log.i(Constants.TAG, "XMPPTransactions.sleepThread: Go to sleep");
                        _xmppConnection.disconnect();
                    }
                    else {
                        Log.e(Constants.TAG, "XMPPTransactions.sleepThread: Application awake detected");
                    }

                } catch (Exception e) {
                    Log.e(Constants.TAG, "XMPPTransactions.sleepThread: ", e);
                    Crashlytics.logException(e);
                }
            }
        });

        sleepThread.start();
    }

    public static void awakeXMPP()
    {
        if(sleepThread!=null) {
            sleepThread.interrupt();
            sleepThread = null;
        }
    }

    public static boolean disconnectMsgServerSession()
    {
        try {
            if (_xmppConnection != null) {
                _xmppConnection.disconnect();
            }

        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.disconnectMsgServerSession: ", e);
            Crashlytics.logException(e);
            return false;
        }

        _xmppConfigBuilder = null;
        _xmppConnection = null;
        _profile_id = null;

        Log.w(Constants.TAG, "XMPPTransactions.disconnectMsgServerSession: XMPP Server DISCONNECTED");
        return true;
    }

    public static boolean disconnectPingThreadSession()
    {
        try {
            if (pingThread != null)
                pingThread.interrupt();
            pingThread = null;
        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.disconnectPingThreadSession: ", e);
            Crashlytics.logException(e);
            return false;
        }

        Log.w(Constants.TAG, "XMPPTransactions.disconnectPingThreadSession: Ping Session DISCONNECTED");
        return true;
    }

    public static void checkAndReconnectXMPP(final Context context)
    {
        Log.i(Constants.TAG, "XMPPTransactions.checkAndReconnectXMPP _xmppConnection-" + _xmppConnection);

        if(_isConnecting &&
                Calendar.getInstance().getTimeInMillis() > _isConnectingTime+10000)
            _isConnecting = false;

        if(_isConnecting || isPinging) return;

        //If it's first time, initialize
        if(_xmppConnection==null) {
            initializeMsgServerSession(context);
            return;
        }

        new AsyncTask<Void,Void,Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                boolean isConnected = false;
                try {
//                    isConnected = _pingManager.pingMyServer();
                    isPinging = true;

                    sendPing();
                    Thread.sleep(3000);
                    isConnected = (pingWaitingID==null);

                    isPinging = false;

                } catch (Exception e) {
                    Log.i(Constants.TAG, "XMPPTransactions.checkAndReconnectXMPP: Pinging error caught > " + e.getMessage());
                }
                return isConnected;
            }
            @Override
            protected void onPostExecute(Boolean isConnected) {
                super.onPostExecute(isConnected);
                if(!isConnected) initializeMsgServerSession(context);

                if(isConnected)
                    Log.i(Constants.TAG, "XMPPTransactions.checkAndReconnectXMPP: Ping OK");
                else
                    Log.i(Constants.TAG, "XMPPTransactions.checkAndReconnectXMPP: Ping FAILED");
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static boolean sendText(boolean isGroup, String contactGroupId, String id, String text)
    {
        final String stanzaStr = buildMessageStanza(isGroup, id, contactGroupId, text);

        try {
            Stanza st = new Stanza() {
                @Override
                public CharSequence toXML() {
                    return stanzaStr;
                }
            };

            _xmppConnection.sendStanza(st);
            _lastChatMessageSent = stanzaStr;
            _lastChatMessageSentTimestamp = Calendar.getInstance().getTimeInMillis();

            //Check connection to keep it alive
            checkAndReconnectXMPP(_appContext);
        }
        catch (SmackException.NotConnectedException e) {
            Log.e(Constants.TAG, "XMPPTransactions.sendText: Error sending message", e);
            Crashlytics.logException(e);
            return false;
        }

        return true;
    }

    public static boolean sendPing()
    {
        String id = UUID.randomUUID().toString().toUpperCase().replaceAll("-","");

        final String stanzaStr = buildPingStanza(id);

        try {
            Stanza st = new Stanza() {
                @Override
                public CharSequence toXML() {
                    return stanzaStr;
                }
            };

            pingWaitingID = id;
            if (_xmppConnection!=null)
                _xmppConnection.sendStanza(st);
        }
        catch (SmackException.NotConnectedException e) {
            Log.e(Constants.TAG, "XMPPTransactions.sendPing: Error sending message", e);
            return false;
        }

        return true;
    }

    public static boolean sendImage(boolean isGroup, String destinationId,
                                    String id, String fileUrl)
    {
        final String stanzaStr = buildImageStanza(isGroup, id, destinationId, fileUrl);

        try {
            Stanza st = new Stanza() {
                @Override
                public CharSequence toXML() {
                    return stanzaStr;
                }
            };

            _xmppConnection.sendStanza(st);
            _lastChatMessageSent = stanzaStr;
            _lastChatMessageSentTimestamp = Calendar.getInstance().getTimeInMillis();
        }
        catch (SmackException.NotConnectedException e) {
            Log.e(Constants.TAG, "ChatMainActivity.sendImage: Error sending message", e);
            Crashlytics.logException(e);
            return false;
        }

        return true;
    }

    public static boolean sendStanzaStr(final String stanzaStr)
    {
        try {
            Stanza st = new Stanza() {
                @Override
                public CharSequence toXML() {
                    return stanzaStr;
                }
            };

            _xmppConnection.sendStanza(st);
            _lastChatMessageSent = stanzaStr;
            _lastChatMessageSentTimestamp = Calendar.getInstance().getTimeInMillis();
        }
        catch (SmackException.NotConnectedException e) {
            Log.e(Constants.TAG, "ChatMainActivity.sendStanzaStr: Error sending message", e);
            Crashlytics.logException(e);
        }

        return true;
    }

    private static String buildIQStanza(boolean isGroup, String id, String destinationId,
                                        final String status)
    {
        String type;
        if(isGroup) type = Constants.XMPP_STANZA_TYPE_GROUPCHAT;
        else type = Constants.XMPP_STANZA_TYPE_CHAT;

        String iq = "<"+Constants.XMPP_ELEMENT_IQ+" "+Constants.XMPP_ATTR_TYPE+"=\""+type+"\" " +
                Constants.XMPP_ATTR_TO+"=\""+destinationId+"@"+Constants.XMPP_PARAM_DOMAIN+"\" " +
                Constants.XMPP_ATTR_FROM+"=\""+_profile_id+"@"+Constants.XMPP_PARAM_DOMAIN+"/"+_device_id+"\" " +
                Constants.XMPP_ATTR_ID+"=\""+id+"\" " +
                Constants.XMPP_ATTR_STATUS+"=\""+status+"\"></"+Constants.XMPP_ELEMENT_IQ+">";

        return iq;
    }

    private static String buildMessageStanza(boolean isGroup, String id, String contactGroupId,
                                             String text)
    {
        String type;
        if(isGroup) type = Constants.XMPP_STANZA_TYPE_GROUPCHAT;
        else type = Constants.XMPP_STANZA_TYPE_CHAT;

        String message = "<"+Constants.XMPP_ELEMENT_MESSAGE+" "+
                    Constants.XMPP_ATTR_TYPE+"=\""+type+"\" " +
                    Constants.XMPP_ATTR_ID+"=\""+id+"\" " +
                    Constants.XMPP_ATTR_TO+"=\""+contactGroupId+"@"+Constants.XMPP_PARAM_DOMAIN+"\" " +
                    Constants.XMPP_ATTR_FROM+"=\""+_profile_id+"@"+Constants.XMPP_PARAM_DOMAIN+"/"+_device_id+"\" " +
                    Constants.XMPP_ATTR_MEDIATYPE+"=\""+Constants.XMPP_MESSAGE_MEDIATYPE_TEXT+"\" >" +
                    "<"+Constants.XMPP_ELEMENT_BODY+">"+text+"</"+Constants.XMPP_ELEMENT_BODY+">" +
                    "</"+Constants.XMPP_ELEMENT_MESSAGE+">";

        return message;
    }

    private static String buildPingStanza(String id)
    {
        String iq = "<"+Constants.XMPP_ELEMENT_IQ+" "+Constants.XMPP_ATTR_TYPE+"=\""+Constants.XMPP_STANZA_TYPE_GET+"\" " +
                Constants.XMPP_ATTR_TO+"=\""+Constants.XMPP_PARAM_DOMAIN+"\" " +
                Constants.XMPP_ATTR_FROM+"=\""+_profile_id+"@"+Constants.XMPP_PARAM_DOMAIN+"/"+_device_id+"\" " +
                Constants.XMPP_ATTR_ID+"=\""+id+"\"><"+Constants.XMPP_ELEMENT_PING+" xmlns=\"urn:xmpp:ping\"/>" +
                "</"+Constants.XMPP_ELEMENT_IQ+">";

        return iq;
    }

    private static String buildImageStanza(boolean isGroup, String id,
                                           String destinationId, String fileUrl)
    {
        String type;
        if(isGroup) type = Constants.XMPP_STANZA_TYPE_GROUPCHAT;
        else type = Constants.XMPP_STANZA_TYPE_CHAT;

        String message = "<"+Constants.XMPP_ELEMENT_MESSAGE+" "+Constants.XMPP_ATTR_TYPE+"=\""+type+"\" " +
                Constants.XMPP_ATTR_ID+"=\""+id+"\" " +
                Constants.XMPP_ATTR_TO+"=\""+destinationId+"@"+Constants.XMPP_PARAM_DOMAIN+"\" " +
                Constants.XMPP_ATTR_FROM+"=\""+_profile_id+"@"+Constants.XMPP_PARAM_DOMAIN+"/"+_device_id+"\" " +
                Constants.XMPP_ATTR_MEDIATYPE+"=\""+Constants.XMPP_MESSAGE_MEDIATYPE_IMAGE+"\" " +
                Constants.XMPP_ATTR_FILEURL+"=\""+fileUrl+"\"/>";

        return message;
    }

    public static boolean saveAndNotifyStanzaReceived(XmlPullParser parser)
    {
        try {
            String from = parser.getAttributeValue("", Constants.XMPP_ATTR_FROM);
            String to = parser.getAttributeValue("", Constants.XMPP_ATTR_TO);
            String id = parser.getAttributeValue("", Constants.XMPP_ATTR_ID);
            String type = parser.getAttributeValue("", Constants.XMPP_ATTR_TYPE);

//            if(type.compareTo(Constants.XMPP_STANZA_TYPE_CHAT)!=0
//                    && type.compareTo(Constants.XMPP_STANZA_TYPE_GROUPCHAT)!=0
//                    && type.compareTo(Constants.XMPP_STANZA_TYPE_PENDINGMESSAGES)!=0)
//                return false;

            if (type!=null && parser.getName().compareTo(Constants.XMPP_ELEMENT_MESSAGE) == 0
                    && type.compareTo(Constants.XMPP_STANZA_TYPE_CHAT) == 0)
            {
                if (from == null || id == null ||
                        to==null || type==null) return false;

                String mediaType = parser.getAttributeValue("", Constants.XMPP_ATTR_MEDIATYPE);

                if(mediaType == null ||
                        mediaType.compareTo(Constants.XMPP_MESSAGE_MEDIATYPE_TEXT)==0)
                    return saveAndNotifyMessageReceived(parser);
                else if(mediaType!=null &&
                        mediaType.compareTo(Constants.XMPP_MESSAGE_MEDIATYPE_IMAGE)==0)
                    return saveAndNotifyImageReceived(parser);
                else return false;
            }
            else if (type!=null && parser.getName().compareTo(Constants.XMPP_ELEMENT_MESSAGE) == 0
                    && type.compareTo(Constants.XMPP_STANZA_TYPE_GROUPCHAT) == 0)
            {
                String mediaType = parser.getAttributeValue("", Constants.XMPP_ATTR_MEDIATYPE);

                if(mediaType == null ||
                        mediaType.compareTo(Constants.XMPP_MESSAGE_MEDIATYPE_TEXT)==0)
                    return saveAndNotifyGroupMessageReceived(parser);
                else if(mediaType!=null &&
                        mediaType.compareTo(Constants.XMPP_MESSAGE_MEDIATYPE_IMAGE)==0)
                    return saveAndNotifyGroupImageReceived(parser);
                else return false;
            }
            else if (parser.getName().compareTo(Constants.XMPP_ELEMENT_IQ) == 0)
            {
                if(from.indexOf("@")>0) from = from.substring(0, from.indexOf("@"));
                if(to.indexOf("@")>0) to = to.substring(0, to.indexOf("@"));

                if(type.compareTo(Constants.XMPP_STANZA_TYPE_RESULT)==0 &&
                        from.compareTo(Constants.XMPP_PARAM_DOMAIN)==0 &&
                        to.compareTo(_profile_id) == 0 ) //It's a pong
                    return handlePongReceived(parser);
                else if(type!=null && (type.compareTo(Constants.XMPP_STANZA_TYPE_CHAT)==0 ||
                        type.compareTo(Constants.XMPP_STANZA_TYPE_GROUPCHAT)== 0))
                    return saveAndNotifyIQReceived(parser);
                else if(type!=null &&
                        type.compareTo(Constants.XMPP_STANZA_TYPE_PENDINGMESSAGES)==0)
                    return handlePendingMessages(parser);
                else return false;
            }
            else {return false;}

        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.saveAndNotifyStanzaReceived: ",e);
            Crashlytics.logException(e);
            return false;
        }
    }

    private static boolean saveAndNotifyIQReceived(XmlPullParser parser)
    {
        RealmChatTransactions chatTx = null;
        try {
            //Check the stanza
            String id = parser.getAttributeValue("", Constants.XMPP_ATTR_ID);
            String status = parser.getAttributeValue("", Constants.XMPP_ATTR_STATUS);

            chatTx = new RealmChatTransactions(_appContext);

            //Save to DB
            boolean changed = chatTx.setChatMessageStatus(id, status, null);

            if (changed) {
                //Notify to app
                MessageStatusChanged statusEvent = new MessageStatusChanged();
                statusEvent.setId(id);
                statusEvent.setStatus(status);
                BusProvider.getInstance().post(statusEvent);

                //Mark last message as sent (for connection control)
                if (_lastChatMessageSent != null && id.compareTo(_lastChatMessageSent) == 0)
                    _lastChatMessageSent = null;
            }

        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.saveMessageToDB: ", e);
            Crashlytics.logException(e);
            return false;
        }
        return true;
    }

    private static boolean handlePongReceived(XmlPullParser parser)
    {
        if(pingWaitingID==null) return false;

        try {
            String id = parser.getAttributeValue("", Constants.XMPP_ATTR_ID);
            String from = parser.getAttributeValue("", Constants.XMPP_ATTR_FROM);

            if(id==null || from==null) return false;

            if(from.compareTo(Constants.XMPP_PARAM_DOMAIN)==0 &&
                    id.compareTo(pingWaitingID)==0)
                pingWaitingID = null;

        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.handlePongReceived: ", e);
            return false;
        }

        return true;
    }

    private static boolean handlePendingMessages(XmlPullParser parser)
    {
        try {
            String to = parser.getAttributeValue("", Constants.XMPP_ATTR_TO);
            if(to==null || !to.contains("@")) return false;

            to = to.substring(0, to.indexOf("@"));
            if(to.compareTo(_profile_id)!=0) return false;

            String pendingStr = parser.getAttributeValue("", Constants.XMPP_ATTR_PENDING);
            int pending = 0;

            try {
                pending = Integer.parseInt(pendingStr);
            } catch (Exception e) {
                Log.i(Constants.TAG, "XMPPTransactions.handlePendingMessages: " +
                        "Pending messages not number");
                pending = 0;
            }

            if(pending>0) _pendingMessages = pending;
            else _pendingMessages = 0;

        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.handlePongReceived: ", e);
            Crashlytics.logException(e);
            return false;
        }

        return true;
    }

    private static boolean saveAndNotifyMessageReceived(XmlPullParser parser)
    {
        RealmChatTransactions chatTx = null;
        Realm realm = Realm.getDefaultInstance();
        try {
            String from = parser.getAttributeValue("", Constants.XMPP_ATTR_FROM);
            String to = parser.getAttributeValue("", Constants.XMPP_ATTR_TO);
            String id = parser.getAttributeValue("", Constants.XMPP_ATTR_ID);
            String sentTimeStr = parser.getAttributeValue("", Constants.XMPP_ATTR_SENT);
            String status = parser.getAttributeValue("", Constants.XMPP_ATTR_STATUS);

            if(status==null) status = Constants.CHAT_MESSAGE_STATUS_NOT_SENT;

            int event = parser.next();
            if (event != XmlPullParser.START_TAG) return false;

            String text = parser.nextText();
            if(from.contains("@")) from = from.substring(0, from.indexOf("@"));
            if(to.contains("@")) to = to.substring(0, to.indexOf("@"));

            if(to.compareTo(_profile_id)==0 && from.compareTo(_profile_id)==0)
                return false;

            long sentTime = Calendar.getInstance().getTimeInMillis();

            try {
                sentTime = Long.parseLong(sentTimeStr);
            } catch (Exception e) {
                Log.e(Constants.TAG, "XMPPTransactions.saveAndNotifyMessageReceived: " +
                        "Error parsing sent time");
            }

            chatTx = new RealmChatTransactions(_appContext);
            boolean changeStatus = false;

            //Check if chat message has already been received
            ChatMessage tempMsg = chatTx.getChatMessageById(id, true, realm);
            if(tempMsg!=null) {
                if(getXMPPStatusOrder(status) <= getXMPPStatusOrder(tempMsg.getStatus()))
                    return false;
                else changeStatus = true;
            }

            String read = Constants.CHAT_MESSAGE_NOT_READ;
            if(status.compareTo(Constants.CHAT_MESSAGE_STATUS_READ)==0)
                read = Constants.CHAT_MESSAGE_READ;

            ChatMessage newChatMessage = null;
            String contactId = null;

            if(to.compareTo(_profile_id)==0) {
                newChatMessage = chatTx.newChatMessageInstance(from,
                        Constants.CHAT_MESSAGE_DIRECTION_RECEIVED,
                        Constants.CHAT_MESSAGE_TYPE_TEXT,
                        text, "", id, sentTime, status, read);

                contactId = from;
            }
            else if(from.compareTo(_profile_id)==0){
                newChatMessage = chatTx.newChatMessageInstance(to,
                        Constants.CHAT_MESSAGE_DIRECTION_SENT,
                        Constants.CHAT_MESSAGE_TYPE_TEXT,
                        text, "", id, sentTime, status, read);

                contactId = to;
            }

            if(newChatMessage == null) return false;

            //Load chat and create if it didn't exist
            Chat chat = chatTx.getChatByContactId(contactId, realm);
            if(chat==null) chat = chatTx.newChatInstance(contactId);
            chat = chatTx.updatedChatInstance(chat, newChatMessage);

            chatTx.insertChat(chat, realm);
            chatTx.insertChatMessage(newChatMessage, realm);

            if(changeStatus) {
                MessageStatusChanged statusEvent = new MessageStatusChanged();
                statusEvent.setId(id);
                statusEvent.setStatus(status);
                BusProvider.getInstance().post(statusEvent);
            }
            else {
                //Chat received event
                ChatsReceivedEvent chatEvent = new ChatsReceivedEvent();
                chatEvent.setMessage(newChatMessage);
                chatEvent.setPendingMessages(_pendingMessages);
                BusProvider.getInstance().post(chatEvent);
            }

            //All pending messages received
            if(_pendingMessages>0) {
                _pendingMessages--;

                if(_pendingMessages==0)
                    BusProvider.getInstance().post(new AllPendingMessagesReceivedEvent());
            }

            //Send IQ if needed
            if(to.compareTo(_profile_id)==0
                    && newChatMessage.getDirection()
                        .compareTo(Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)==0
                    && (getXMPPStatusOrder(status) <
                        getXMPPStatusOrder(Constants.CHAT_MESSAGE_STATUS_DELIVERED))) {
                notifyIQMessageStatus(false, newChatMessage.getId(), newChatMessage.getContact_id(),
                        Constants.CHAT_MESSAGE_STATUS_DELIVERED);
            }

            return true;

        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.saveMessageToDB: ", e);
            Crashlytics.logException(e);
            return false;
        } finally {
            realm.close();
        }
    }

    private static boolean saveAndNotifyImageReceived(XmlPullParser parser)
    {
        RealmChatTransactions chatTx = null;
        Realm realm = Realm.getDefaultInstance();
        try {
            String from = parser.getAttributeValue("", Constants.XMPP_ATTR_FROM);
            String to = parser.getAttributeValue("", Constants.XMPP_ATTR_TO);
            String id = parser.getAttributeValue("", Constants.XMPP_ATTR_ID);
            String url = parser.getAttributeValue("", Constants.XMPP_ATTR_FILEURL);
            String sentTimeStr = parser.getAttributeValue("", Constants.XMPP_ATTR_SENT);

            String status = parser.getAttributeValue("", Constants.XMPP_ATTR_STATUS);
            if(status==null) status = Constants.CHAT_MESSAGE_STATUS_NOT_SENT;

            if(url==null) return false;

            chatTx = new RealmChatTransactions(_appContext);

            //Check if chat message has already been received
            ChatMessage tempMsg = chatTx.getChatMessageById(id, true, realm);
            if(tempMsg!=null) {
                if(getXMPPStatusOrder(status) <= getXMPPStatusOrder(tempMsg.getStatus()))
                    return false;
            }

            if(from.contains("@")) from = from.substring(0, from.indexOf("@"));
            if(to.contains("@")) to = to.substring(0, to.indexOf("@"));

            String read = Constants.CHAT_MESSAGE_NOT_READ;
            if(status.compareTo(Constants.CHAT_MESSAGE_STATUS_READ)==0)
                read = Constants.CHAT_MESSAGE_READ;

            ChatMessage newChatMessage = null;
            String contactId = null;

            long sentTime = Calendar.getInstance().getTimeInMillis();

            try {
                sentTime = Long.parseLong(sentTimeStr);
            } catch (Exception e) {
                Log.e(Constants.TAG, "XMPPTransactions.saveAndNotifyMessageReceived: " +
                        "Error parsing sent time");
            }

            if(to.compareTo(_profile_id)==0) {
                newChatMessage = chatTx.newChatMessageInstance(from,
                        Constants.CHAT_MESSAGE_DIRECTION_RECEIVED,
                        Constants.CHAT_MESSAGE_TYPE_IMAGE,
                        "", url, id, sentTime, status, read);

                contactId = from;
            }
            else if(from.compareTo(_profile_id)==0) {
                newChatMessage = chatTx.newChatMessageInstance(to,
                        Constants.CHAT_MESSAGE_DIRECTION_SENT,
                        Constants.CHAT_MESSAGE_TYPE_IMAGE,
                        "", url, id, sentTime, status, read);

                contactId = to;
            }

            if(newChatMessage == null) return false;

            //Load chat and create if it didn't exist
            Chat chat = chatTx.getChatByContactId(contactId, realm);

            if(chat==null) chat = chatTx.newChatInstance(contactId);
            chat = chatTx.updatedChatInstance(chat, newChatMessage);

            chatTx.insertChat(chat, realm);
            chatTx.insertChatMessage(newChatMessage, realm);

            //All pending messages received
            if(_pendingMessages>0) {
                _pendingMessages--;

                if(_pendingMessages==0)
                    BusProvider.getInstance().post(new AllPendingMessagesReceivedEvent());
            }

            //Send IQ
            if(to.compareTo(_profile_id)==0
                    && newChatMessage.getDirection()
                        .compareTo(Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)==0
                    && (getXMPPStatusOrder(status) <
                        getXMPPStatusOrder(Constants.CHAT_MESSAGE_STATUS_DELIVERED))) {
                notifyIQMessageStatus(false, newChatMessage.getId(), newChatMessage.getContact_id(),
                        Constants.CHAT_MESSAGE_STATUS_DELIVERED);
            }

            //Download to file
            downloadToChatFile(url, id);

            ChatsReceivedEvent chatEvent = new ChatsReceivedEvent();
            chatEvent.setMessage(newChatMessage);
            chatEvent.setPendingMessages(_pendingMessages);
            BusProvider.getInstance().post(chatEvent);

            return true;

        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.saveMessageToDB: ", e);
            Crashlytics.logException(e);
            return false;
        }
        finally {
            realm.close();
        }
    }

    private static boolean saveAndNotifyGroupMessageReceived(XmlPullParser parser)
    {
        RealmGroupChatTransactions groupTx = null;
        Realm realm = Realm.getDefaultInstance();
        try {
            String from = parser.getAttributeValue("", Constants.XMPP_ATTR_FROM);
            String groupId = parser.getAttributeValue("", Constants.XMPP_ATTR_TO);
            String id = parser.getAttributeValue("", Constants.XMPP_ATTR_ID);
            String receiver = parser.getAttributeValue("", Constants.XMPP_ATTR_RECEIVER);
            String sentTimeStr = parser.getAttributeValue("", Constants.XMPP_ATTR_SENT);

            String status = parser.getAttributeValue("", Constants.XMPP_ATTR_STATUS);
            if(status==null) status = Constants.CHAT_MESSAGE_STATUS_NOT_SENT;

            int event = parser.next();
            if (event != XmlPullParser.START_TAG) return false;

            String text = parser.nextText();
            if(groupId.contains("@")) groupId = groupId.substring(0, groupId.indexOf("@"));
            if(from.contains("@")) from = from.substring(0, from.indexOf("@"));
            if(receiver.contains("@")) receiver = receiver.substring(0, receiver.indexOf("@"));

            if(receiver.compareTo(_profile_id)!=0) return false;

            long sentTime = Calendar.getInstance().getTimeInMillis();

            try {
                sentTime = Long.parseLong(sentTimeStr);
            } catch (Exception e) {
                Log.e(Constants.TAG, "XMPPTransactions.saveAndNotifyGroupMessageReceived: " +
                        "Error parsing sent time");
            }

            groupTx = new RealmGroupChatTransactions(_appContext, _profile_id);

            //Check if chat message has already been received
            ChatMessage tempMsg = groupTx.getGroupChatMessageById(id, realm);
            if(tempMsg!=null) {
                if(getXMPPStatusOrder(status) <= getXMPPStatusOrder(tempMsg.getStatus()))
                    return false;
            }

            String read = Constants.CHAT_MESSAGE_NOT_READ;
            if(status.compareTo(Constants.CHAT_MESSAGE_STATUS_READ)==0)
                read = Constants.CHAT_MESSAGE_READ;

            ChatMessage newChatMessage = null;

            if(from.compareTo(_profile_id)!=0) {
                newChatMessage = groupTx.newGroupChatMessageInstance(groupId, from,
                        Constants.CHAT_MESSAGE_DIRECTION_RECEIVED,
                        Constants.CHAT_MESSAGE_TYPE_TEXT,
                        text, "", id, sentTime, status, read);
            }
            else if(from.compareTo(_profile_id)==0){
                newChatMessage = groupTx.newGroupChatMessageInstance(groupId, "",
                        Constants.CHAT_MESSAGE_DIRECTION_SENT,
                        Constants.CHAT_MESSAGE_TYPE_TEXT,
                        text, "", id, sentTime, status, read);
            }

            if(newChatMessage == null) return false;

            //Load chat and create if it didn't exist
            GroupChat chat = groupTx.getGroupChatById(groupId, realm);

            //Save ChatMessage to DB
            groupTx.insertGroupChatMessage(groupId, newChatMessage, null);

            if(chat==null) {
                //Get group from API in background, and save to Realm
                downloadAndSaveGroupChat(id, groupId, null);

            }
            else {
                chat = groupTx.updatedGroupChatInstance(chat, newChatMessage);
                groupTx.insertOrUpdateGroupChat(chat, realm);
            }

            //All pending messages received event
            if(_pendingMessages>0) {
                _pendingMessages--;

                if(_pendingMessages==0)
                    BusProvider.getInstance().post(new AllPendingMessagesReceivedEvent());
            }

            //Send IQ if needed
            if(from.compareTo(_profile_id)!=0
                    && newChatMessage.getDirection()
                        .compareTo(Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)==0
                    && (getXMPPStatusOrder(status) <
                        getXMPPStatusOrder(Constants.CHAT_MESSAGE_STATUS_DELIVERED))) {
                notifyIQMessageStatus(true, newChatMessage.getId(), newChatMessage.getGroup_id(),
                        Constants.CHAT_MESSAGE_STATUS_DELIVERED);
            }

            //Chats received event
            ChatsReceivedEvent chatEvent = new ChatsReceivedEvent();
            chatEvent.setMessage(newChatMessage);
            chatEvent.setPendingMessages(_pendingMessages);
            BusProvider.getInstance().post(chatEvent);

            return true;

        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.saveMessageToDB: ", e);
            Crashlytics.logException(e);
            return false;
        }
        finally
        {
            realm.close();
        }
    }

    public static void downloadAndSaveGroupChat(String chatMessageId, String groupId){
        downloadAndSaveGroupChat(chatMessageId, groupId, null);
    }

    public static void downloadAndSaveGroupChat(final String chatMessageId, final String groupId, Context appContext){
        Log.i(Constants.TAG, "XMPPTransactions.downloadAndSaveGroupChat: ");

        if(_appContext==null) _appContext = appContext;

        if (_profile_id == null) {
            //Get profile_id
            SharedPreferences sp = _appContext.getSharedPreferences(
                    Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
            _profile_id = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);
        }

        OKHttpWrapper.get(Constants.SINGLE_GROUP_CHAT_API + "/" + groupId, _appContext, new OKHttpWrapper.HttpCallback() {
            @Override
            public void onFailure(Response response, IOException e) {
                Log.i(Constants.TAG, "XMPPTransactions.downloadAndSaveGroupChat.onFailure:");
                //Notify app
                GroupChatCreatedEvent groupChatCreatedEvent = new GroupChatCreatedEvent();
                groupChatCreatedEvent.setGroupId(groupId);
                groupChatCreatedEvent.setSuccess(false);
                BusProvider.getInstance().post(groupChatCreatedEvent);
            }

            @Override
            public void onSuccess(Response response) {
                Log.i(Constants.TAG, "XMPPTransactions.downloadAndSaveGroupChat.onSuccess:");
                boolean success = false;
                Realm realm = Realm.getDefaultInstance();
                try {
                    String jsonStr;
                    JSONObject json;
                    if (response.isSuccessful()) {
                        jsonStr = response.body().string();
                        if (jsonStr != null && jsonStr.trim().length() > 0) {
                            try {
                                json = new JSONObject(jsonStr);
                                String groupId = json.getString("id");
                                String creator = json.getString("creator");
                                JSONArray members = json.getJSONArray("members");

                                ArrayList<String> membersIds = new ArrayList<>();
                                ArrayList<String> ownersIds = new ArrayList<>();
                                JSONObject member;
                                String id;

                                for (int i = 0; i < members.length(); i++) {
                                    member = members.getJSONObject(i);
                                    id = member.getString("id");

                                    membersIds.add(id);
                                    if (member.has("owner"))
                                        ownersIds.add(id);
                                }

                                RealmGroupChatTransactions groupTx =
                                        new RealmGroupChatTransactions(_appContext, _profile_id);

                                GroupChat chat = groupTx.newGroupChatInstance(
                                        groupId, _profile_id, membersIds, ownersIds, "", "", "");

                                //Set last message data
                                if (chatMessageId != null) {
                                    ChatMessage newChatMessage = groupTx.getGroupChatMessageById
                                            (chatMessageId, realm);
                                    if (newChatMessage != null) {
                                        chat.setLastMessage_id(newChatMessage.getId());

                                        String lastText;
                                        if (newChatMessage.getType() == Constants.CHAT_MESSAGE_TYPE_TEXT)
                                            lastText = newChatMessage.getText();
                                        else lastText = _appContext.getString(R.string.image);

                                        if (newChatMessage.getDirection().equals(Constants.CHAT_MESSAGE_DIRECTION_SENT))
                                            chat.setLastMessage(_appContext.getResources().getString(R.string.chat_me_text) + lastText);
                                        else chat.setLastMessage(lastText);

                                        chat.setLastMessageTime(newChatMessage.getTimestamp());
                                    }
                                }
                                groupTx.insertOrUpdateGroupChat(chat, realm);

                                success = true;

                            } catch (JSONException e) {
                                Log.e(Constants.TAG, "XMPPTransactions.downloadAndSaveGroupChat.onConnectionComplete: ", e);
                            }
                        }
                    } else {
                        Log.e(Constants.TAG, "XMPPTransactions.downloadAndSaveGroupChat.isNOTSuccessful");
                    }
                } catch (IOException e) {
                    Log.e(Constants.TAG, "XMPPTransactions.downloadAndSaveGroupChat.onSuccess: ", e);
                } finally {
                    realm.close();
                }

                //Notify app
                GroupChatCreatedEvent groupChatCreatedEvent = new GroupChatCreatedEvent();
                groupChatCreatedEvent.setGroupId(groupId);
                groupChatCreatedEvent.setSuccess(success);
                BusProvider.getInstance().post(groupChatCreatedEvent);
            }
        });
    }

    private static boolean saveAndNotifyGroupImageReceived(XmlPullParser parser)
    {
        RealmGroupChatTransactions groupTx = null;
        Realm realm = Realm.getDefaultInstance();
        try {
            String from = parser.getAttributeValue("", Constants.XMPP_ATTR_FROM);
            String groupId = parser.getAttributeValue("", Constants.XMPP_ATTR_TO);
            String id = parser.getAttributeValue("", Constants.XMPP_ATTR_ID);
            String receiver = parser.getAttributeValue("", Constants.XMPP_ATTR_RECEIVER);
            String sentTimeStr = parser.getAttributeValue("", Constants.XMPP_ATTR_SENT);
            String url = parser.getAttributeValue("", Constants.XMPP_ATTR_FILEURL);

            String status = parser.getAttributeValue("", Constants.XMPP_ATTR_STATUS);
            if(status==null) status = Constants.CHAT_MESSAGE_STATUS_NOT_SENT;

            if(url==null) return false;

            groupTx = new RealmGroupChatTransactions(_appContext, _profile_id);

            //Check if chat message has already been received
            ChatMessage tempMsg = groupTx.getGroupChatMessageById(id, realm);
            if(tempMsg!=null) {
                if(getXMPPStatusOrder(status) <= getXMPPStatusOrder(tempMsg.getStatus()))
                    return false;
            }

            if(groupId.contains("@")) groupId = groupId.substring(0, groupId.indexOf("@"));
            if(from.contains("@")) from = from.substring(0, from.indexOf("@"));
            if(receiver.contains("@")) receiver = receiver.substring(0, receiver.indexOf("@"));

            if(receiver.compareTo(_profile_id)!=0) return false;

            String read = Constants.CHAT_MESSAGE_NOT_READ;
            if(status.compareTo(Constants.CHAT_MESSAGE_STATUS_READ)==0)
                read = Constants.CHAT_MESSAGE_READ;

            long sentTime = Calendar.getInstance().getTimeInMillis();

            try {
                sentTime = Long.parseLong(sentTimeStr);
            } catch (Exception e) {
                Log.e(Constants.TAG, "XMPPTransactions.saveAndNotifyMessageReceived: " +
                        "Error parsing sent time");
            }

            ChatMessage newChatMessage = null;

            if(from.compareTo(_profile_id)!=0) {
                newChatMessage = groupTx.newGroupChatMessageInstance(groupId, from,
                        Constants.CHAT_MESSAGE_DIRECTION_RECEIVED,
                        Constants.CHAT_MESSAGE_TYPE_IMAGE,
                        "", url, id, sentTime, status, read);
            }
            else if(from.compareTo(_profile_id)==0){
                newChatMessage = groupTx.newGroupChatMessageInstance(groupId, "",
                        Constants.CHAT_MESSAGE_DIRECTION_SENT,
                        Constants.CHAT_MESSAGE_TYPE_IMAGE,
                        "", url, id, sentTime, status, read);
            }

            if(newChatMessage == null) return false;

            //Save ChatMessage to DB
            groupTx.insertGroupChatMessage(groupId, newChatMessage, null);

            //Load chat and create if it didn't exist
            GroupChat chat = groupTx.getGroupChatById(groupId, realm);

            if(chat==null) {
                //Get group from API in background, and save to Realm
                downloadAndSaveGroupChat(id, groupId);
            }
            else {
                chat = groupTx.updatedGroupChatInstance(chat, newChatMessage);
                groupTx.insertOrUpdateGroupChat(chat, realm);
            }

            //Send event and IQ if needed
            if(from.compareTo(_profile_id)!=0
                    && newChatMessage.getDirection()
                        .compareTo(Constants.CHAT_MESSAGE_DIRECTION_RECEIVED)==0
                    && (getXMPPStatusOrder(status) <
                        getXMPPStatusOrder(Constants.CHAT_MESSAGE_STATUS_DELIVERED))) {
                notifyIQMessageStatus(true, newChatMessage.getId(), newChatMessage.getGroup_id(),
                        Constants.CHAT_MESSAGE_STATUS_DELIVERED);
            }

            //Download to file
            downloadToChatFile(url, id);

            //All pending messages received event
            if(_pendingMessages>0) {
                _pendingMessages--;

                if(_pendingMessages==0)
                    BusProvider.getInstance().post(new AllPendingMessagesReceivedEvent());
            }

            //Chat received event
            ChatsReceivedEvent chatEvent = new ChatsReceivedEvent();
            chatEvent.setMessage(newChatMessage);
            chatEvent.setPendingMessages(_pendingMessages);
            BusProvider.getInstance().post(chatEvent);

            return true;

        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.saveMessageToDB: ", e);
            Crashlytics.logException(e);
            return false;
        }
        finally {
            realm.close();
        }
    }

    public static boolean downloadToChatFile(String urlStr, String chatMessageId) {
        try {
            Log.i(Constants.TAG, "XMPPTransactions.downloadToChatFile: ");
//            String dirStr = _appContext.getFilesDir() + Constants.CONTACT_CHAT_FILES;
            String fileStr = "file_" + chatMessageId + ".jpg";
            URL url = new URL(urlStr);
            downloadImage(url,fileStr,Constants.CONTACT_CHAT_FILES);


//            final File imageFile = new File(fileStr);
//            File imagesDir = new File(dirStr);
//            if(!imagesDir.exists()) imagesDir.mkdirs();
//
//            Target target = new Target() {
//                @Override
//                public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
//                    Log.i(Constants.TAG, "XMPPTransactions.onBitmapLoaded: ");
//                    SaveAndShowImageAsyncTask task =
//                            new SaveAndShowImageAsyncTask(
//                                    imageFile, bitmap);
//
//                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                }
//
//                @Override
//                public void onBitmapFailed(Drawable errorDrawable) {
//                    Log.i(Constants.TAG, "XMPPTransactions.onBitmapFailed: ");
//                    if(imageFile.exists()) imageFile.delete();
//                }
//
//                @Override
//                public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                }
//            };
//            Log.i(Constants.TAG, "XMPPTransactions.downloadToChatFile: pre Picasso");
//            Picasso.with(_appContext)
//                    .load(urlStr)
//                    .into(target);
//            Log.i(Constants.TAG, "XMPPTransactions.downloadToChatFile: post Picasso");

//            URL url = new URL(urlStr);
//            URLConnection ucon = url.openConnection();
//            ucon.setReadTimeout(Constants.HTTP_READ_FILE_TIMEOUT);
//            ucon.setConnectTimeout(10000);
//
//            InputStream is = ucon.getInputStream();
//            BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);
//
//            File dir = new File(dirStr);
//            dir.mkdirs();
//
//            File file = new File(dirStr, fileStr);
//            if(file.exists()) file.delete();
//            file.createNewFile();
//
//            FileOutputStream outStream = new FileOutputStream(file);
//            byte[] buff = new byte[5 * 1024];
//
//            int len;
//            while ((len = inStream.read(buff)) != -1) {
//                outStream.write(buff, 0, len);
//            }
//            outStream.flush();
//            outStream.close();
//            inStream.close();
        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.downloadToChatFile: ",e);
            Crashlytics.logException(e);
            return false;
        }

        return true;
    }

    private static boolean downloadImage(URL url, String fileName, String dir)
    {
        try {
            File file = new File(_appContext.getFilesDir() + dir);
            file.mkdirs();

            if (!okHttpDownloadFile(String.valueOf(url), dir, fileName)) {
                File badAvatar = new File(_appContext.getFilesDir() + dir, fileName);
                badAvatar.delete();
                return false;
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.downloadImage: ",e);
            Crashlytics.logException(e);
            return false;
        }

        return true;
    }

    public static boolean okHttpDownloadFile(final String path, String dir, String fileName){

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(path).build();
            Response response = client.newCall(request).execute();
            InputStream in = response.body().byteStream();
            BufferedInputStream inStream = new BufferedInputStream(in, 1024 * 5);
            File file = new File(_appContext.getFilesDir() + dir, fileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream outStream = new FileOutputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(inStream);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outStream);
            outStream.flush();
            outStream.close();
            inStream.close();
        } catch (IOException e){
            Log.e(Constants.TAG, "XMPPTransactions.okHttpDownloadFile: ",e);
            Crashlytics.logException(e);
            return false;
        }
        return true;
    }

    public static boolean notifyIQMessageStatus(final boolean isGroup, final String msgId,
                                                final String destinationId, final String status)
    {
        //Sends the IQ stanza to notify
        try {
            Stanza st = new Stanza() {
                @Override
                public CharSequence toXML() {
                    String message = buildIQStanza(isGroup,
                            msgId, destinationId, status);

                    return message;
                }
            };

            _xmppConnection.sendStanza(st);
        }
        catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.notifyIQMessageStatus: Error sending IQ", e);
            Crashlytics.logException(e);
            return false;
        }

        return true;
    }

    public static int getXMPPStatusOrder(String xmppStatus)
    {
        if(xmppStatus.compareTo(Constants.CHAT_MESSAGE_STATUS_NOT_SENT)==0) return 0;
        else if(xmppStatus.compareTo(Constants.CHAT_MESSAGE_STATUS_SENT)==0) return 1;
        else if(xmppStatus.compareTo(Constants.CHAT_MESSAGE_STATUS_DELIVERED)==0) return 2;
        else return 3; //Read
    }

    public static void sendReadIQReceivedMessagesList(boolean isGroup,
                                                      ArrayList<ChatMessage> messages)
    {
        for(int i=0; i<messages.size(); i++) {
            if(isGroup)
                notifyIQMessageStatus(isGroup, messages.get(i).getId(),
                        messages.get(i).getGroup_id(), Constants.CHAT_MESSAGE_STATUS_READ);
            else
                notifyIQMessageStatus(isGroup, messages.get(i).getId(),
                        messages.get(i).getContact_id(), Constants.CHAT_MESSAGE_STATUS_READ);
        }
    }

    /*
     * Classes
     */

    static final class XMPPOpenConnectionTask extends AsyncTask<Object, Void, XMPPTCPConnection> {
        @Override
        protected XMPPTCPConnection doInBackground(Object... params)
        {
            //Control to not do parallel connections
            if(_isConnecting) {
                return _xmppConnection;
            }

            Log.i(Constants.TAG, "XMPPOpenConnectionTask.doInBackground: ");
            boolean connectionCreated = false;

            try {
                //Save connection start time
                _isConnecting = true;
                _isConnectingTime = Calendar.getInstance().getTimeInMillis();

                if(_xmppConnection!=null && _xmppConnection.isConnected())
                    _xmppConnection.disconnect();

                _xmppConnection = new XMPPTCPConnection(_xmppConfigBuilder.build());
                _xmppConnection.addConnectionListener(getConnectionListener());
                connectionCreated = true;

                // Connect to the server
                _xmppConnection.connect();

                //Log into the server
                if(!_xmppConnection.isAuthenticated())
                    _xmppConnection.login();

            } catch (Exception e) {
                Log.e(Constants.TAG, "XMPPOpenConnection.doInBackground: Error opening XMPP server connection", e);
                _isConnecting = false;

                return null;
            }

            //**********************
            //CONNECTION SUCCESSFUL
            //**********************

            //Reconnection manager
            if(connectionCreated) {
                _reconnectionMgr = ReconnectionManager.getInstanceFor(_xmppConnection);
                _reconnectionMgr.enableAutomaticReconnection();
                _reconnectionMgr.setReconnectionPolicy(ReconnectionManager.ReconnectionPolicy.RANDOM_INCREASING_DELAY);

                _pingManager = PingManager.getInstanceFor(_xmppConnection);
//                _pingManager.setPingInterval(5);
//                _pingManager.registerPingFailedListener(new PingFailedListener() {
//                    @Override
//                    public void pingFailed() {
//                        Log.w(Constants.TAG, "XMPPOpenConnectionTask.pingFailed: " +
//                                "Ping failed, trying to reconnect");
//                        initializeMsgServerSession(_appContext, true);
//                    }
//                });
            }

            //Check if there is a stanza not sent
            if(_lastChatMessageSent!=null) {
                sendStanzaStr(_lastChatMessageSent);
                _lastChatMessageSent = null;
            }

            _isConnecting = false;

            return _xmppConnection;
        }

        @Override
        protected void onPostExecute(XMPPTCPConnection xmppConnection) {
            _isConnecting = false;
        }

        @Override
        protected void onCancelled(XMPPTCPConnection xmppConnection) {
            super.onCancelled();
            _isConnecting = false;
        }
    }

    private static ConnectionListener getConnectionListener()
    {
        _connectionListener = new ConnectionListener() {
            @Override
            public void connected(XMPPConnection connection) {
                Log.w(Constants.TAG, "XMPPTransactions.connected");
            }

            @Override
            public void authenticated(XMPPConnection connection, boolean resumed) {
                Log.w(Constants.TAG, "XMPPTransactions.authenticated");
            }

            @Override
            public void connectionClosed() {
                Log.w(Constants.TAG, "XMPPTransactions.connectionClosed");
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                Log.w(Constants.TAG, "XMPPTransactions.connectionClosedOnError");
            }

            @Override
            public void reconnectionSuccessful() {
                //Check if there is a stanza not sent
                if(_lastChatMessageSent!=null) {
                    sendStanzaStr(_lastChatMessageSent);
                    _lastChatMessageSent = null;
                }

                _isConnecting = false;
                Log.w(Constants.TAG, "XMPPTransactions.reconnectionSuccessful");
            }

            @Override
            public void reconnectingIn(int seconds) {
                //Save connection start time
                _isConnecting = true;
                _isConnectingTime = Calendar.getInstance().getTimeInMillis();
                Log.w(Constants.TAG, "XMPPTransactions.reconnectingIn: " + seconds + " seg.");
            }

            @Override
            public void reconnectionFailed(Exception e) {
                _isConnecting = false;
                Log.w(Constants.TAG, "XMPPTransactions.reconnectionFailed: Try it manually");
            }
        };

        return _connectionListener;
    }

    public static XMPPTCPConnection getXmppConnection() {
        return _xmppConnection;
    }

}
