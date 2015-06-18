package com.vodafone.mycomms.xmpp;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.events.MessageSentStatusChanged;
import com.vodafone.mycomms.events.XMPPConnectingEvent;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;
import com.vodafone.mycomms.util.Utils;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.xmlpull.v1.XmlPullParser;

import io.realm.Realm;
import model.Chat;
import model.ChatMessage;

/**
 * Created by str_rbm on 03/06/2015.
 */
public final class XMPPTransactions {
    private static XMPPTCPConnection _xmppConnection = null;
    private static String accessToken;
    private static String _profile_id;
    private static boolean _isConnecting = false;
    private static Context _appContext;
    private static ConnectionListener _connectionListener;
    private static String _device_id;
    private static Realm _realm;

    /*
     * Methods
     */

    public static void initializeMsgServerSession(Context appContext)
    {
        if(_isConnecting) return;

        if(_xmppConnection == null) {

            //Save context
            _appContext = appContext;
            _realm = Realm.getInstance(_appContext);

            //Get profile_id
            SharedPreferences sp = appContext.getSharedPreferences(
                    Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
            _profile_id = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);

            if (_profile_id == null) {
                Log.e(Constants.TAG, "ContactListMainActivity.initializeMsgServerSession: No profile_id found");
                return;
            }

            //Device ID
            _device_id = Utils.getDeviceId(_appContext.getContentResolver(),
                    (TelephonyManager) _appContext.getSystemService(Service.TELEPHONY_SERVICE));
        }

        if(_xmppConnection == null || _xmppConnection.isDisconnectedButSmResumptionPossible() || !_xmppConnection.isConnected()) {
            _isConnecting = true;
            Log.i(Constants.TAG, "XMPPTransactions.initializeMsgServerSession: ");
            notifyXMPPConnecting(true);

            //Configuration for the connection
            XMPPTCPConnectionConfiguration.Builder xmppConfigBuilder = XMPPTCPConnectionConfiguration.builder();

            accessToken = UserSecurity.getAccessToken(appContext);
            xmppConfigBuilder.setUsernameAndPassword(_profile_id, accessToken);
            xmppConfigBuilder.setServiceName(Constants.XMPP_PARAM_DOMAIN);
            xmppConfigBuilder.setHost(appContext.getString(R.string.xmpp_host));
            xmppConfigBuilder.setPort(Constants.XMPP_PARAM_PORT);
//        xmppConfigBuilder.setEnabledSSLProtocols(new String[]{"TLSv1.2"});
            xmppConfigBuilder.setDebuggerEnabled(true);
            xmppConfigBuilder.setSendPresence(false);
            xmppConfigBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
            xmppConfigBuilder.setCompressionEnabled(false);

            new XMPPOpenConnectionTask().execute(xmppConfigBuilder);
        }
    }

    public static boolean disconnectMsgServerSession()
    {
        try {
            _xmppConnection.disconnect();
            if(_realm != null)_realm.close();

        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.disconnectMsgServerSession: ", e);
            return false;
        }

        Log.w(Constants.TAG, "XMPPTransactions.disconnectMsgServerSession: XMPP Server DISCONNECTED");
        return true;
    }

    public static boolean sendText(final String contact_id, final String type, final String id,
                                   final String mediaType, final String text)
    {
        try {
            Stanza st = new Stanza() {
                @Override
                public CharSequence toXML() {
                    String message = buildMessageStanza(type, id, contact_id, mediaType, text);
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

    private static String buildIQStanza(String type, String id, String contactId, final String status)
    {
        String iq = "<iq type=\""+type+"\" " +
                "to=\""+contactId+"@my-comms.com\" " +
                "from=\""+_profile_id+"@my-comms.com/"+_device_id+"\" " +
                "id=\""+id+"\" " +
                "status=\""+status+"\"></iq>";

        return iq;
    }

    private static String buildMessageStanza(String type, String id, String contactId,
                                             String mediaType, String text)
    {
        String message = "<message type=\""+type+"\" id=\""+id+"\" " +
                    "to=\""+contactId+"@my-comms.com\" " +
                    "from=\""+_profile_id+"@my-comms.com/"+_device_id+"\" " +
                    "mediaType=\""+mediaType+"\" >" +
                    "<body>"+text+"</body></message>";

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
            String from = parser.getAttributeValue("", "from");
            String to = parser.getAttributeValue("", "to");
            String id = parser.getAttributeValue("", "id");

            if (from == null || id == null || to==null) return false;

            to = to.substring(0, to.indexOf("@"));
            if(to.compareTo(_profile_id)!=0) return false;

            if (parser.getName().compareTo("message") == 0)
                return saveAndNotifyMessageReceived(parser);
//            else if (parser.getName().compareTo("iq") == 0)
//                return saveAndNotifyIQReceived(parser);
            else return false;

        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.saveAndNotifyStanzaReceived: ",e);
        }

        return true;
    }

    private static boolean saveAndNotifyIQReceived(XmlPullParser parser)
    {
        Realm realm = null;

        try {
            //Check the stanza
            String from = parser.getAttributeValue("", "from");
            String to = parser.getAttributeValue("", "to");
            String id = parser.getAttributeValue("", "id");
            String status = parser.getAttributeValue("", "status");

            to = to.substring(0, to.indexOf("@"));
            from = from.substring(0, from.indexOf("@"));

            String text = parser.nextText();

            //Instantiate Realm
            realm = Realm.getInstance(_appContext);
            RealmChatTransactions chatTx = new RealmChatTransactions(realm, _appContext);

            //Save to DB
            boolean changed = chatTx.setChatMessageSentStatus(id, status);
            realm.close();

            if(changed) {
                //Notify to app
                MessageSentStatusChanged statusEvent = new MessageSentStatusChanged();
                statusEvent.setId(id);
                statusEvent.setStatus(status);
                BusProvider.getInstance().post(statusEvent);
            }

        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.saveMessageToDB: ", e);
            if(realm!=null) realm.close();
            return false;
        }

        return true;
    }

    private static boolean saveAndNotifyMessageReceived(XmlPullParser parser)
    {
        Realm realm = null;

        try {
            String from = parser.getAttributeValue("", "from");
            String to = parser.getAttributeValue("", "to");
            String id = parser.getAttributeValue("", "id");

            if (from == null || id == null || to==null) return false;

            to = to.substring(0, to.indexOf("@"));
            if(to.compareTo(_profile_id)!=0 ||
                    parser.getName().compareTo("message") != 0) return false;

            int event = parser.next();
            if (event != XmlPullParser.START_TAG) return false;

            String text = parser.nextText();
            from = from.substring(0, from.indexOf("@"));

            realm = Realm.getInstance(_appContext);
            RealmChatTransactions chatTx = new RealmChatTransactions(realm, _appContext);

            ChatMessage newChatMessage = chatTx.newChatMessageInstance(from,
                    Constants.CHAT_MESSAGE_DIRECTION_RECEIVED,
                    Constants.CHAT_MESSAGE_TYPE_TEXT,
                    text, "", id);

            //Load chat and create if it didn't exist
            Chat chat = chatTx.getChatById(from);
            if(chat==null) chat = chatTx.newChatInstance(from);
            chat = chatTx.updatedChatInstance(chat, newChatMessage);

            chatTx.insertChat(chat);
            chatTx.insertChatMessage(newChatMessage);

            realm.close();

            ChatsReceivedEvent chatEvent = new ChatsReceivedEvent();
            chatEvent.setMessage(newChatMessage);
            BusProvider.getInstance().post(chatEvent);

        } catch (Exception e) {
            Log.e(Constants.TAG, "XMPPTransactions.saveMessageToDB: ", e);
            if(realm!=null) realm.close();
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

    public static boolean notifyIQMessageStatus(final ChatMessage chatMsg, final String status)
    {
        //Sends the IQ stanza to notify
        try {
            Stanza st = new Stanza() {
                @Override
                public CharSequence toXML() {
                    String message = buildIQStanza(Constants.XMPP_IQ_TYPE_CHAT,
                            chatMsg.getId(), chatMsg.getContact_id(), status);

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

//            //Add IQ Provider
//            ProviderManager.addIQProvider("iq", "jabber:client", new MyIQProvider());

            //Reconnection manager
            ReconnectionManager reconnectionMgr = ReconnectionManager.getInstanceFor(_xmppConnection);
            reconnectionMgr.enableAutomaticReconnection();
            reconnectionMgr.setReconnectionPolicy(ReconnectionManager.ReconnectionPolicy.RANDOM_INCREASING_DELAY);

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
                if(_realm != null)_realm.close();
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                Log.w(Constants.TAG, "XMPPTransactions.connectionClosedOnError");

                _xmppConnection = null;
                if(_realm != null)_realm.close();
            }

            @Override
            public void reconnectionSuccessful() {
                Log.w(Constants.TAG, "XMPPTransactions.reconnectionSuccessful");
            }

            @Override
            public void reconnectingIn(int seconds) {
                Log.w(Constants.TAG, "XMPPTransactions.reconnectingIn: " + seconds + " seg.");
            }

            @Override
            public void reconnectionFailed(Exception e) {
                Log.w(Constants.TAG, "XMPPTransactions.reconnectionFailed: Trying manually");

                _xmppConnection = null;
                if(_realm != null)_realm.close();
            }
        };

        return _connectionListener;
    }

//    //IQ extended class
//    static class MyIQ extends IQ
//    {
//        public static final String IQ_STATUS_SENT = "sent";
//        public static final String IQ_STATUS_DELIVERED = "delivered";
//        public static final String IQ_STATUS_READ = "read";
//
//        private int pendingMessages = 0;
//        private String status;
//
//        public MyIQ(IQ iq) {super(iq);}
//        protected MyIQ(String childElementName) {
//            super(childElementName);
//        }
//
//        public MyIQ(String id, String from, String to, String status) {
//            super("iq");
//            setStanzaId(id);
//            setFrom(from);
//            setTo(to);
//            setStatus(status);
//        }
//
//        @Override
//        protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
//            return null;
//        }
//
//        public int getPendingMessages() {return pendingMessages;}
//        public void setPendingMessages(int pendingMessages) {this.pendingMessages = pendingMessages;}
//
//        public String getStatus() {return status;}
//        public void setStatus(String status) {this.status = status;}
//    }
//
//    //Provider for IQ Packets
//    static class MyIQProvider extends IQProvider<MyIQ> {
//        @Override
//        public MyIQ parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {
//            Log.e(Constants.TAG, "MyIQProvider.parse: "+parser.getText());
//
//            MyIQ iq = new MyIQ(parser.getName());
//            iq.setType(IQ.Type.fromString(parser.getAttributeValue("", "type")));
//            iq.setFrom(parser.getAttributeValue("", "from"));
//            iq.setTo(parser.getAttributeValue("", "to"));
//
//            try {
//                iq.setPendingMessages(Integer.valueOf(parser.getAttributeValue("", "pending")));
//            } catch (Exception e) {}
//
//            return iq;
//        }
//    }

    /*
     * Getters and Setters
     */

    public static XMPPTCPConnection getXmppConnection() {
        return _xmppConnection;
    }

}
