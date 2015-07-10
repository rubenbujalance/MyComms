package com.vodafone.mycomms.xmpp;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chatgroup.DownloadAndSaveGroupChatAsyncTask;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.events.MessageSentStatusChanged;
import com.vodafone.mycomms.events.XMPPConnectingEvent;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.realm.RealmGroupChatTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;
import com.vodafone.mycomms.util.Utils;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;

import model.Chat;
import model.ChatMessage;
import model.GroupChat;

/**
 * Created by str_rbm on 03/06/2015.
 */
public final class XMPPTransactions {
    private static XMPPTCPConnection _xmppConnection = null;
    private static String accessToken;
    private static String _profile_id;
    private static Context _appContext;
    private static ConnectionListener _connectionListener;
    private static String _device_id;

    //Control to no retry consecutive connections
    private static boolean _isConnecting = false;

    //Last ChatMessage sent, to control unexpected server disconnection
    private static String _lastChatMessageSent;
    private static long _lastChatMessageSentTimestamp;

    /*
     * Methods
     */

    public static void initializeMsgServerSession(Context appContext)
    {
        if(_isConnecting &&
                Calendar.getInstance().getTimeInMillis() > _lastChatMessageSentTimestamp+10000)
            _isConnecting = false;

        if(_isConnecting) return;

        //Check if haven't received confirmation of last message sent
        //Force reconnection if necessary
        boolean force = false;
        if(_lastChatMessageSent != null &&
                Calendar.getInstance().getTimeInMillis() > _lastChatMessageSentTimestamp+5000 &&
                !_isConnecting)
        {
            //Disconnect and force manual reconnection
            try {
                if(_xmppConnection != null) _xmppConnection.disconnect();
                _xmppConnection = null;
            } catch (Exception e) {
                Log.e(Constants.TAG, "XMPPTransactions.forceReconnection: ", e);
            }

            force = true;
        }

        //Start connection
        if(_xmppConnection == null || _xmppConnection.isDisconnectedButSmResumptionPossible() ||
                !_xmppConnection.isConnected() || force) {

            //Save context and reset connection
            _xmppConnection = null;
            _appContext = appContext;

            //Check profile id exists
            if(_profile_id==null) {
                //Get profile_id
                SharedPreferences sp = appContext.getSharedPreferences(
                        Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
                _profile_id = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);
            }

            if (_profile_id == null) {
                Log.e(Constants.TAG, "XMPPTransactions.initializeMsgServerSession: No profile_id found");
                return;
            }

            _isConnecting = true;
            notifyXMPPConnecting(true);

            //Device ID
            _device_id = Utils.getDeviceId(_appContext.getContentResolver(),
                    (TelephonyManager) _appContext.getSystemService(Service.TELEPHONY_SERVICE));

            Log.i(Constants.TAG, "XMPPTransactions.initializeMsgServerSession: ");

            //Configuration for the connection
            XMPPTCPConnectionConfiguration.Builder xmppConfigBuilder = XMPPTCPConnectionConfiguration.builder();

            accessToken = UserSecurity.getAccessToken(appContext);
            xmppConfigBuilder.setUsernameAndPassword(_profile_id, accessToken);
            xmppConfigBuilder.setServiceName(Constants.XMPP_PARAM_DOMAIN);
            xmppConfigBuilder.setHost(appContext.getString(R.string.xmpp_host));
            xmppConfigBuilder.setPort(Constants.XMPP_PARAM_PORT);
            xmppConfigBuilder.setEnabledSSLProtocols(new String[]{"TLSv1.2"});
            xmppConfigBuilder.setDebuggerEnabled(true);
            xmppConfigBuilder.setSendPresence(false);
//            xmppConfigBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
            xmppConfigBuilder.setCompressionEnabled(false);

            //new XMPPOpenConnectionTask().execute(xmppConfigBuilder);
            XMPPOpenConnectionTask xmppOpenConnectionTask = new XMPPOpenConnectionTask();
            xmppOpenConnectionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, xmppConfigBuilder);
        }
    }

    public static boolean disconnectMsgServerSession()
    {
        try {
            _xmppConnection.disconnect();

        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.disconnectMsgServerSession: ", e);
            return false;
        }

        Log.w(Constants.TAG, "XMPPTransactions.disconnectMsgServerSession: XMPP Server DISCONNECTED");
        return true;
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
        }
        catch (SmackException.NotConnectedException e) {
            Log.e(Constants.TAG, "ChatMainActivity.sendText: Error sending message", e);
            return false;
        }

        return true;
    }

    public static boolean sendImage(String contact_id, String type, String id, String fileUrl)
    {
        final String stanzaStr = buildImageStanza(type, id, contact_id, fileUrl);

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
            Log.e(Constants.TAG, "ChatMainActivity.sendText: Error sending message", e);
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
            Log.e(Constants.TAG, "ChatMainActivity.sendText: Error sending message", e);
        }

        return true;
    }

    private static String buildIQStanza(String type, String id, String contactId, final String status)
    {
        String iq = "<"+Constants.XMPP_ELEMENT_IQ+" "+Constants.XMPP_ATTR_TYPE+"=\""+type+"\" " +
                Constants.XMPP_ATTR_TO+"=\""+contactId+"@"+Constants.XMPP_PARAM_DOMAIN+"\" " +
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

    private static String buildImageStanza(String type, String id, String contactId, String fileUrl)
    {
        String message = "<"+Constants.XMPP_ELEMENT_MESSAGE+" "+Constants.XMPP_ATTR_TYPE+"=\""+type+"\" " +
                Constants.XMPP_ATTR_ID+"=\""+id+"\" " +
                Constants.XMPP_ATTR_TO+"=\""+contactId+"@"+Constants.XMPP_PARAM_DOMAIN+"\" " +
                Constants.XMPP_ATTR_FROM+"=\""+_profile_id+"@"+Constants.XMPP_PARAM_DOMAIN+"/"+_device_id+"\" " +
                Constants.XMPP_ATTR_MEDIATYPE+"=\""+Constants.XMPP_MESSAGE_MEDIATYPE_IMAGE+"\" " +
                Constants.XMPP_ATTR_FILEURL+"=\""+fileUrl+"\"/>";

        return message;
    }

    private static void xmppConnectionCallback(XMPPTCPConnection xmppConnection)
    {
        if(xmppConnection != null && xmppConnection.isConnected()) {
            Log.w(Constants.TAG, "XMPPTransactions.xmppConnectionCallback: XMPP Connection established with user " + xmppConnection.getUser());
        }
        else {
            Log.e(Constants.TAG, "XMPPTransactions.xmppConnectionCallback: XMPP Connection NOT established");
            return;
        }
    }

    public static boolean saveAndNotifyStanzaReceived(XmlPullParser parser)
    {
        try {
            String from = parser.getAttributeValue("", Constants.XMPP_ATTR_FROM);
            String to = parser.getAttributeValue("", Constants.XMPP_ATTR_TO);
            String id = parser.getAttributeValue("", Constants.XMPP_ATTR_ID);
            String type = parser.getAttributeValue("", Constants.XMPP_ATTR_TYPE);

            if (from == null || id == null ||
                    to==null || type==null) return false;

            if(to.indexOf("@")<0) return false;
            to = to.substring(0, to.indexOf("@"));

            if((type.compareTo(Constants.XMPP_STANZA_TYPE_CHAT)!=0
                        && type.compareTo(Constants.XMPP_STANZA_TYPE_GROUPCHAT)!=0))
                return false;

            if (parser.getName().compareTo(Constants.XMPP_ELEMENT_MESSAGE) == 0
                    && type.compareTo(Constants.XMPP_STANZA_TYPE_CHAT) == 0)
            {
                String mediaType = parser.getAttributeValue("", Constants.XMPP_ATTR_MEDIATYPE);

                if(mediaType == null ||
                        mediaType.compareTo(Constants.XMPP_MESSAGE_MEDIATYPE_TEXT)==0)
                    return saveAndNotifyMessageReceived(parser);
                else if(mediaType.compareTo(Constants.XMPP_MESSAGE_MEDIATYPE_IMAGE)==0)
                    return saveAndNotifyImageReceived(parser);
                else return false;
            }
            else if (parser.getName().compareTo(Constants.XMPP_ELEMENT_MESSAGE) == 0
                    && type.compareTo(Constants.XMPP_STANZA_TYPE_GROUPCHAT) == 0)
            {
                String mediaType = parser.getAttributeValue("", Constants.XMPP_ATTR_MEDIATYPE);

                if(mediaType == null ||
                        mediaType.compareTo(Constants.XMPP_MESSAGE_MEDIATYPE_TEXT)==0)
                    return saveAndNotifyGroupMessageReceived(parser);
                else if(mediaType.compareTo(Constants.XMPP_MESSAGE_MEDIATYPE_IMAGE)==0)
                    return saveAndNotifyGroupImageReceived(parser);
                else return false;
            }
            else if (parser.getName().compareTo(Constants.XMPP_ELEMENT_IQ) == 0)
            {
                return saveAndNotifyIQReceived(parser);
            }
            else {return false;}

        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.saveAndNotifyStanzaReceived: ",e);
            return false;
        }
    }

    private static boolean saveAndNotifyIQReceived(XmlPullParser parser)
    {
        try {
            //Check the stanza
            String id = parser.getAttributeValue("", Constants.XMPP_ATTR_ID);
            String status = parser.getAttributeValue("", Constants.XMPP_ATTR_STATUS);

            RealmChatTransactions chatTx = new RealmChatTransactions(_appContext);

            //Save to DB
            boolean changed = chatTx.setChatMessageSentStatus(id, status);

            if(changed) {
                //Notify to app
                MessageSentStatusChanged statusEvent = new MessageSentStatusChanged();
                statusEvent.setId(id);
                statusEvent.setStatus(status);
                BusProvider.getInstance().post(statusEvent);

                //Mark last message as sent (for connection control)
                if(_lastChatMessageSent!=null && id.compareTo(_lastChatMessageSent)==0)
                    _lastChatMessageSent = null;
            }

        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.saveMessageToDB: ", e);
            return false;
        }

        return true;
    }

    private static boolean saveAndNotifyMessageReceived(XmlPullParser parser)
    {
        try {
            String from = parser.getAttributeValue("", Constants.XMPP_ATTR_FROM);
            String to = parser.getAttributeValue("", Constants.XMPP_ATTR_TO);
            String id = parser.getAttributeValue("", Constants.XMPP_ATTR_ID);

            int event = parser.next();
            if (event != XmlPullParser.START_TAG) return false;

            String text = parser.nextText();
            if(from.contains("@")) from = from.substring(0, from.indexOf("@"));
            if(to.contains("@")) to = to.substring(0, to.indexOf("@"));

            RealmChatTransactions chatTx = new RealmChatTransactions(_appContext);

            //Check if chat message has already been received
            if(chatTx.existsChatMessageById(id))
                return false;

            ChatMessage newChatMessage = null;
            String contactId = null;

            if(to.compareTo(_profile_id)==0) {
                newChatMessage = chatTx.newChatMessageInstance(from,
                        Constants.CHAT_MESSAGE_DIRECTION_RECEIVED,
                        Constants.CHAT_MESSAGE_TYPE_TEXT,
                        text, "", id);

                contactId = from;
            }
            else if(from.compareTo(_profile_id)==0){
                newChatMessage = chatTx.newChatMessageInstance(to,
                        Constants.CHAT_MESSAGE_DIRECTION_SENT,
                        Constants.CHAT_MESSAGE_TYPE_TEXT,
                        text, "", id);

                contactId = to;
            }

            if(newChatMessage == null) return false;

            //Load chat and create if it didn't exist
            Chat chat = chatTx.getChatByContactId(contactId);
            if(chat==null) chat = chatTx.newChatInstance(contactId);
            chat = chatTx.updatedChatInstance(chat, newChatMessage);

            chatTx.insertChat(chat);
            chatTx.insertChatMessage(newChatMessage);

            ChatsReceivedEvent chatEvent = new ChatsReceivedEvent();
            chatEvent.setMessage(newChatMessage);
            BusProvider.getInstance().post(chatEvent);

            if(to.compareTo(_profile_id)==0) {
                notifyIQMessageStatus(newChatMessage.getId(), newChatMessage.getContact_id(),
                        Constants.CHAT_MESSAGE_STATUS_DELIVERED);
            }

        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.saveMessageToDB: ", e);
            return false;
        }

        return true;
    }

    private static boolean saveAndNotifyImageReceived(XmlPullParser parser)
    {
        try {
            String from = parser.getAttributeValue("", Constants.XMPP_ATTR_FROM);
            String to = parser.getAttributeValue("", Constants.XMPP_ATTR_TO);
            String id = parser.getAttributeValue("", Constants.XMPP_ATTR_ID);
            String url = parser.getAttributeValue("", Constants.XMPP_ATTR_FILEURL);

            if(url==null) return false;

            RealmChatTransactions chatTx = new RealmChatTransactions(_appContext);

            //Check if chat message has already been received
            if(chatTx.existsChatMessageById(id))
                return false;

            if(from.contains("@")) from = from.substring(0, from.indexOf("@"));
            if(to.contains("@")) to = from.substring(0, to.indexOf("@"));

            ChatMessage newChatMessage = null;
            String contactId = null;

            if(to.compareTo(_profile_id)==0) {
                newChatMessage = chatTx.newChatMessageInstance(from,
                        Constants.CHAT_MESSAGE_DIRECTION_RECEIVED,
                        Constants.CHAT_MESSAGE_TYPE_IMAGE,
                        "", url, id);

                contactId = from;
            }
            else if(from.compareTo(_profile_id)==0) {
                newChatMessage = chatTx.newChatMessageInstance(to,
                        Constants.CHAT_MESSAGE_DIRECTION_SENT,
                        Constants.CHAT_MESSAGE_TYPE_IMAGE,
                        "", url, id);

                contactId = to;
            }

            if(newChatMessage == null) return false;

            //Load chat and create if it didn't exist
            Chat chat = chatTx.getChatByContactId(contactId);

            if(chat==null) chat = chatTx.newChatInstance(contactId);
            chat = chatTx.updatedChatInstance(chat, newChatMessage);

            chatTx.insertChat(chat);
            chatTx.insertChatMessage(newChatMessage);

            //Send IQ
            if(to.compareTo(_profile_id)==0) {
                notifyIQMessageStatus(newChatMessage.getId(), newChatMessage.getContact_id(),
                        Constants.CHAT_MESSAGE_STATUS_DELIVERED);
            }

            //Download to file
            if(!downloadToChatFile(url, id))
                return false;

            ChatsReceivedEvent chatEvent = new ChatsReceivedEvent();
            chatEvent.setMessage(newChatMessage);
            BusProvider.getInstance().post(chatEvent);

        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.saveMessageToDB: ", e);
            return false;
        }

        return true;
    }

    private static boolean saveAndNotifyGroupMessageReceived(XmlPullParser parser)
    {
        try {
            String from = parser.getAttributeValue("", Constants.XMPP_ATTR_FROM);
            String groupId = parser.getAttributeValue("", Constants.XMPP_ATTR_TO);
            String id = parser.getAttributeValue("", Constants.XMPP_ATTR_ID);
            String receiver = parser.getAttributeValue("", Constants.XMPP_ATTR_RECEIVER);

            int event = parser.next();
            if (event != XmlPullParser.START_TAG) return false;

            String text = parser.nextText();
            if(groupId.contains("@")) groupId = groupId.substring(0, groupId.indexOf("@"));
            if(from.contains("@")) from = from.substring(0, from.indexOf("@"));
            if(receiver.contains("@")) receiver = receiver.substring(0, receiver.indexOf("@"));

            if(receiver.compareTo(_profile_id)!=0) return false;

            RealmGroupChatTransactions groupTx =
                    new RealmGroupChatTransactions(_appContext, _profile_id);

            //Check if chat message has already been received
            if(groupTx.existsChatMessageById(id))
                return false;

            ChatMessage newChatMessage = null;
            String contactId = null;

            if(from.compareTo(_profile_id)!=0) {
                newChatMessage = groupTx.newGroupChatMessageInstance(groupId, from,
                        Constants.CHAT_MESSAGE_DIRECTION_RECEIVED,
                        Constants.CHAT_MESSAGE_TYPE_TEXT,
                        text, "", id);
            }
            else if(from.compareTo(_profile_id)==0){
                newChatMessage = groupTx.newGroupChatMessageInstance(groupId, "",
                        Constants.CHAT_MESSAGE_DIRECTION_SENT,
                        Constants.CHAT_MESSAGE_TYPE_TEXT,
                        text, "", id);
            }

            if(newChatMessage == null) return false;

            //Load chat and create if it didn't exist
            GroupChat chat = groupTx.getGroupChatById(groupId);

            //Save ChatMessage to DB
            groupTx.insertGroupChatMessage(groupId, newChatMessage);

            if(chat==null) {
                //Get group from API in background, and save to Realm
                new DownloadAndSaveGroupChatAsyncTask()
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                                _appContext, Constants.GROUP_CHAT_API, groupId, id);
            }
            else {
                chat = groupTx.updatedGroupChatInstance(chat, newChatMessage);
                groupTx.insertOrUpdateGroupChat(chat);
            }

            ChatsReceivedEvent chatEvent = new ChatsReceivedEvent();
            chatEvent.setMessage(newChatMessage);
            BusProvider.getInstance().post(chatEvent);

            if(from.compareTo(_profile_id)!=0) {
                notifyIQMessageStatus(newChatMessage.getId(), newChatMessage.getContact_id(),
                        Constants.CHAT_MESSAGE_STATUS_DELIVERED);
            }

        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.saveMessageToDB: ", e);
            return false;
        }

        return true;
    }

    private static boolean saveAndNotifyGroupImageReceived(XmlPullParser parser)
    {
        if(true) return true;

        try {
            String from = parser.getAttributeValue("", Constants.XMPP_ATTR_FROM);
            String to = parser.getAttributeValue("", Constants.XMPP_ATTR_TO);
            String id = parser.getAttributeValue("", Constants.XMPP_ATTR_ID);
            String url = parser.getAttributeValue("", Constants.XMPP_ATTR_FILEURL);

            if(url==null) return false;

            RealmChatTransactions chatTx = new RealmChatTransactions(_appContext);

            //Check if chat message has already been received
            if(chatTx.existsChatMessageById(id))
                return false;

            if(from.contains("@")) from = from.substring(0, from.indexOf("@"));
            if(to.contains("@")) to = from.substring(0, to.indexOf("@"));

            ChatMessage newChatMessage = null;
            String contactId = null;

            if(to.compareTo(_profile_id)==0) {
                newChatMessage = chatTx.newChatMessageInstance(from,
                        Constants.CHAT_MESSAGE_DIRECTION_RECEIVED,
                        Constants.CHAT_MESSAGE_TYPE_IMAGE,
                        "", url, id);

                contactId = from;
            }
            else if(from.compareTo(_profile_id)==0) {
                newChatMessage = chatTx.newChatMessageInstance(to,
                        Constants.CHAT_MESSAGE_DIRECTION_SENT,
                        Constants.CHAT_MESSAGE_TYPE_IMAGE,
                        "", url, id);

                contactId = to;
            }

            if(newChatMessage == null) return false;

            //Load chat and create if it didn't exist
            Chat chat = chatTx.getChatByContactId(contactId);

            if(chat==null) chat = chatTx.newChatInstance(contactId);
            chat = chatTx.updatedChatInstance(chat, newChatMessage);

            chatTx.insertChat(chat);
            chatTx.insertChatMessage(newChatMessage);

            //Send IQ
            if(to.compareTo(_profile_id)==0) {
                notifyIQMessageStatus(newChatMessage.getId(), newChatMessage.getContact_id(),
                        Constants.CHAT_MESSAGE_STATUS_DELIVERED);
            }

            //Download to file
            if(!downloadToChatFile(url, id))
                return false;

            ChatsReceivedEvent chatEvent = new ChatsReceivedEvent();
            chatEvent.setMessage(newChatMessage);
            BusProvider.getInstance().post(chatEvent);

        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.saveMessageToDB: ", e);
            return false;
        }

        return true;
    }

    public static boolean downloadToChatFile(String urlStr, String chatMessageId) {
        try {
            String dirStr = _appContext.getFilesDir() + Constants.CONTACT_CHAT_FILES;
            String fileStr = "file_" + chatMessageId + ".jpg";

            URL url = new URL(urlStr);

            URLConnection ucon = url.openConnection();
            ucon.setReadTimeout(Constants.HTTP_READ_FILE_TIMEOUT);
            ucon.setConnectTimeout(10000);

            InputStream is = ucon.getInputStream();
            BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);

            File dir = new File(dirStr);
            dir.mkdirs();

            File file = new File(dirStr, fileStr);
            if(file.exists()) file.delete();
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
            Log.e(Constants.TAG, "XMPPTransactions.downloadToChatFile: ",e);
            return false;
        }

        return true;
    }

    private static void notifyXMPPConnecting(boolean isConnecting)
    {
        XMPPConnectingEvent event = new XMPPConnectingEvent();
        event.setConnecting(isConnecting);
        BusProvider.getInstance().post(event);
    }

    public static boolean notifyIQMessageStatus(final String msgId, final String msgContactId,
                                                final String status)
    {
        //Sends the IQ stanza to notify
        try {
            Stanza st = new Stanza() {
                @Override
                public CharSequence toXML() {
                    String message = buildIQStanza(Constants.XMPP_STANZA_TYPE_CHAT,
                            msgId, msgContactId, status);

                    return message;
                }
            };

            _xmppConnection.sendStanza(st);
        }
        catch (SmackException.NotConnectedException e) {
            Log.e(Constants.TAG, "ChatMainActivity.sendText: Error sending message", e);
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

    public static void sendReadIQReceivedMessagesList(ArrayList<ChatMessage> messages)
    {
        for(int i=0; i<messages.size(); i++)
            notifyIQMessageStatus(messages.get(i).getId(), messages.get(i).getContact_id(),
                    Constants.CHAT_MESSAGE_STATUS_READ);
    }

    /*
     * Classes
     */

    static final class XMPPOpenConnectionTask extends AsyncTask<XMPPTCPConnectionConfiguration.Builder, Void, XMPPTCPConnection> {
        @Override
        protected XMPPTCPConnection doInBackground(XMPPTCPConnectionConfiguration.Builder[] params)
        {
            Log.i(Constants.TAG, "XMPPOpenConnectionTask.doInBackground: ");

            try {
                XMPPTCPConnectionConfiguration.Builder xmppConfigBuilder = params[0];
                _xmppConnection = new XMPPTCPConnection(xmppConfigBuilder.build());

                // Connect to the server
                _xmppConnection.addConnectionListener(getConnectionListener());
                _xmppConnection.connect();
                //Log into the server
                _xmppConnection.login();

            } catch (Exception e) {
                Log.e(Constants.TAG, "XMPPOpenConnection.doInBackground: Error opening XMPP server connection", e);
                _xmppConnection = null;
                return null;
            }

            //**********************
            //CONNECTION SUCCESSFUL
            //**********************

            //Reconnection manager
            ReconnectionManager reconnectionMgr = ReconnectionManager.getInstanceFor(_xmppConnection);
            reconnectionMgr.enableAutomaticReconnection();
            reconnectionMgr.setReconnectionPolicy(ReconnectionManager.ReconnectionPolicy.RANDOM_INCREASING_DELAY);

            //Check if there is a stanza not sent
            if(_lastChatMessageSent!=null) {
                sendStanzaStr(_lastChatMessageSent);
                _lastChatMessageSent = null;
            }

            return _xmppConnection;
        }

        @Override
        protected void onPostExecute(XMPPTCPConnection xmppConnection) {
            _isConnecting = false;
            notifyXMPPConnecting(false);
            xmppConnectionCallback(xmppConnection);
        }

        @Override
        protected void onCancelled(XMPPTCPConnection xmppConnection) {
            super.onCancelled();
            _isConnecting = false;
            notifyXMPPConnecting(false);
            _xmppConnection = null;
            xmppConnectionCallback(xmppConnection);
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

                _xmppConnection = null;
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                Log.w(Constants.TAG, "XMPPTransactions.connectionClosedOnError");

                _xmppConnection = null;
            }

            @Override
            public void reconnectionSuccessful() {
                //Check if there is a stanza not sent
                if(_lastChatMessageSent!=null) {
                    sendStanzaStr(_lastChatMessageSent);
                    _lastChatMessageSent = null;
                }

                _isConnecting = false;
                notifyXMPPConnecting(false);
                Log.w(Constants.TAG, "XMPPTransactions.reconnectionSuccessful");
            }

            @Override
            public void reconnectingIn(int seconds) {
                _isConnecting = true;
                Log.w(Constants.TAG, "XMPPTransactions.reconnectingIn: " + seconds + " seg.");
            }

            @Override
            public void reconnectionFailed(Exception e) {
                _isConnecting = false;
                notifyXMPPConnecting(false);
                Log.w(Constants.TAG, "XMPPTransactions.reconnectionFailed: Trying manually");

                _xmppConnection = null;
            }
        };

        return _connectionListener;
    }

    public static void closeRealm()
    {
//        _chatTx.closeRealm();
//        _groupChatTx.closeRealm();
    }

    /*
     * Getters and Setters
     */

    public static XMPPTCPConnection getXmppConnection() {
        return _xmppConnection;
    }

}
