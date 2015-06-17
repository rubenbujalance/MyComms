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
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;
import com.vodafone.mycomms.util.Utils;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import io.realm.Realm;
import model.ChatMessage;

/**
 * Created by str_rbm on 03/06/2015.
 */
public final class XMPPTransactions {
    private static XMPPTCPConnection _xmppConnection = null;
    private static Realm mRealm;
    private static RealmChatTransactions _chatTx;
    private static String accessToken;
    private static String _profile_id;
    private static boolean _isConnecting = false;
    private static Context _appContext;
    private static StanzaListener _packetListener;
    private static StanzaFilter _stanzaFilter;
    private static ConnectionListener _connectionListener;
    private static String _device_id;

    /*
     * Methods
     */

    public static void initializeMsgServerSession(Context appContext)
    {
        Log.i(Constants.TAG, "XMPPTransactions.initializeMsgServerSession: ");

        if(_isConnecting) return;

        if(_xmppConnection == null) {

            //Save context
            _appContext = appContext;

            //Get profile_id
            SharedPreferences sp = appContext.getSharedPreferences(
                    Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
            _profile_id = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);

            if (_profile_id == null) {
                Log.e(Constants.TAG, "ContactListMainActivity.initializeMsgServerSession: No profile_id found");
                return;
            }

            //Instantiate Realm and Transactions
            mRealm = Realm.getInstance(_appContext);
            _chatTx = new RealmChatTransactions(mRealm, _appContext);

            //Device ID
            _device_id = Utils.getDeviceId(_appContext.getContentResolver(),
                    (TelephonyManager) _appContext.getSystemService(Service.TELEPHONY_SERVICE));
        }

        if(_xmppConnection == null || _xmppConnection.isDisconnectedButSmResumptionPossible() || !_xmppConnection.isConnected()) {
            _isConnecting = true;
            //Configuration for the connection
            XMPPTCPConnectionConfiguration.Builder xmppConfigBuilder = XMPPTCPConnectionConfiguration.builder();

            accessToken = UserSecurity.getAccessToken(appContext);
            xmppConfigBuilder.setUsernameAndPassword(_profile_id, accessToken);
            xmppConfigBuilder.setServiceName(Constants.XMPP_PARAM_DOMAIN);
            xmppConfigBuilder.setHost(appContext.getString(R.string.xmpp_host));
            xmppConfigBuilder.setPort(Constants.XMPP_PARAM_PORT);
//        xmppConfigBuilder.setEnabledSSLProtocols(new String[]{"TLSv1.2"});
            xmppConfigBuilder.setDebuggerEnabled(false);
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

    private static String buildIQStanza(String type, String id, String contactId, String status)
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

    private static ChatMessage saveMessageToDB(Message msg)
    {
        if(msg==null) return null;

        Realm r = Realm.getInstance(_appContext);
        RealmChatTransactions chatTx = new RealmChatTransactions(r, _appContext);

        ChatMessage newChatMessage = chatTx.newChatMessageInstance(
                msg.getFrom().substring(0, msg.getFrom().indexOf("@")),
                Constants.CHAT_MESSAGE_DIRECTION_RECEIVED,
                Constants.CHAT_MESSAGE_TYPE_TEXT,
                msg.getBody(),
                "");

        chatTx.insertChatMessage(newChatMessage);
        r.close();

        return newChatMessage;
    }

    private static void notifyMessageReceived(ChatMessage chatMsg)
    {
        ChatsReceivedEvent event = new ChatsReceivedEvent();
        event.setMessage(chatMsg);
        BusProvider.getInstance().post(event);
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

                //Packet listener for incoming messages
                _packetListener = new StanzaListener() {
                    @Override
                    public void processPacket(Stanza packet)
                    {
                        Log.w(Constants.TAG, "XMPPTransactions.processPacket[PACKET LISTENER]: "+packet.getFrom());

//                    if(packet instanceof IQ) {
//                        IQ iq = (IQ)packet;
//                        Log.w(Constants.TAG, "XMPPTransactions.processPacket[packetListener] (IQ): Type-"+iq.getType()
//                                +"; From-"+iq.getFrom()+"; To-"+iq.getTo());
//                    }
//                    else {
//                        Message msg = (Message)packet;
//                        Log.w(Constants.TAG, "XMPPTransactions.processPacket[packetListener] (Message): Type-"+msg.getType()
//                                +"; From-"+msg.getFrom()+"; To-"+msg.getTo()+"; Text-"+msg.getBody());
//                    }
//                        if(msg.getFrom().substring(0, msg.getFrom().indexOf("@")).compareTo(
//                                _xmppConnection.getUser().substring(0, _xmppConnection.getUser().indexOf("@")))!=0) {
//
//                            ChatMessage chatMsg = saveMessageToDB(msg);
//                            notifyMessageReceived(chatMsg);
//                        }
                    }
                };

                _stanzaFilter = StanzaTypeFilter.MESSAGE;
                _xmppConnection.addAsyncStanzaListener(_packetListener, _stanzaFilter);

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

            //Add Message (extension) provider
//              //TODO RBM: Find Element and Namespace
//                _extensionProvider = new TestPacketExtension("message", "jabber:client","Prueba de extension");
//                ProviderManager.addExtensionProvider("message", "jabber:client", _extensionProvider);

            return _xmppConnection;
        }

        @Override
        protected void onPostExecute(XMPPTCPConnection xmppConnection) {
            _isConnecting = false;
            xmppConnectionCallback(xmppConnection);
        }

        @Override
        protected void onCancelled(XMPPTCPConnection xmppConnection) {
            super.onCancelled();
            _isConnecting = false;
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
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                Log.w(Constants.TAG, "XMPPTransactions.connectionClosedOnError");
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
            }
        };

        return _connectionListener;
    }

    //IQ extended class
    static class MyIQ extends IQ
    {
        public static final String IQ_STATUS_SENT = "sent";
        public static final String IQ_STATUS_DELIVERED = "delivered";
        public static final String IQ_STATUS_READ = "read";

        private int pendingMessages = 0;
        private String status;

        public MyIQ(IQ iq) {super(iq);}
        protected MyIQ(String childElementName) {
            super(childElementName);
        }

        public MyIQ(String id, String from, String to, String status) {
            super("iq");
            setStanzaId(id);
            setFrom(from);
            setTo(to);
            setStatus(status);
        }

        @Override
        protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
            return null;
        }

        public int getPendingMessages() {return pendingMessages;}
        public void setPendingMessages(int pendingMessages) {this.pendingMessages = pendingMessages;}

        public String getStatus() {return status;}
        public void setStatus(String status) {this.status = status;}
    }

    //Provider for IQ Packets
    static class MyIQProvider extends IQProvider<MyIQ> {
        @Override
        public MyIQ parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {
            Log.e(Constants.TAG, "MyIQProvider.parse: "+parser.getText());

            MyIQ iq = new MyIQ(parser.getName());
            iq.setType(IQ.Type.fromString(parser.getAttributeValue("", "type")));
            iq.setFrom(parser.getAttributeValue("", "from"));
            iq.setTo(parser.getAttributeValue("", "to"));

            try {
                iq.setPendingMessages(Integer.valueOf(parser.getAttributeValue("", "pending")));
            } catch (Exception e) {}

            return iq;
        }
    }


    /*
     * Getters and Setters
     */

    public static XMPPTCPConnection getXmppConnection() {
        return _xmppConnection;
    }

//    static class SubscriptionProvider extends DataPacketProvider.PacketExtensionProvider {
//        public static NewSubscription parseExtension(XmlPullParser parser) throws Exception {
//            Log.i(Constants.TAG, "SubscriptionProvider.parseExtension: ");
//            String jid = parser.getAttributeValue(null, "jid");
//            String nodeId = parser.getAttributeValue(null, "node");
//            String subId = parser.getAttributeValue(null, "subid");
//            String state = parser.getAttributeValue(null, "subscription");
//
//            String type = parser.getAttributeValue(null, "type");
//            String to = parser.getAttributeValue(null, "to");
//            String id = parser.getAttributeValue(null, "id");
//            String mediaType = parser.getAttributeValue(null, "mediaType");
//            String from = parser.getAttributeValue(null, "from");
//            String status = parser.getAttributeValue(null, "status");
//            String sent = parser.getAttributeValue(null, "sent");//TODO: Careful, it's a long!
//            String xmlns_stream = parser.getAttributeValue(null, "xmlns:stream");
//
//
//            boolean isRequired = false;
//
//            int tag = parser.next();
//
//            if ((tag == XmlPullParser.START_TAG) && parser.getName().equals("subscribe-options")) {
//                tag = parser.next();
//
//                if ((tag == XmlPullParser.START_TAG) && parser.getName().equals("required"))
//                    isRequired = true;
//
//                while (parser.next() != XmlPullParser.END_TAG && parser.getName() != "subscribe-options");
//            }
//            while (parser.getEventType() != XmlPullParser.END_TAG) parser.next();
//            //return new Subscription(jid, nodeId, subId, (state == null ? null : Subscription.State.valueOf(state)), isRequired);
//            return new NewSubscription(type, to, id, mediaType, from, status, sent, xmlns_stream,
//                    jid, nodeId, subId, (mediaType == null ? null : Subscription.State.valueOf(state)), isRequired);
//        }
//    }

//    static class NewSubscription extends org.jivesoftware.smackx.pubsub.Subscription {
//        String type;
//        String to;
//        String id;
//        String mediaType;
//        String from;
//        String status;
//        String sent;
//        String xmlns_stream;
//        public NewSubscription(String type, String to, String id, String mediaType, String from, String status, String sent,
//                               String xmlns_stream, String jid, String nodeId, String subscriptionId, State state, boolean configRequired) {
//            super(jid, nodeId, subscriptionId, state, configRequired);
//        }
//
//        public String getType() {
//            return type;
//        }
//
//        public void setType(String type) {
//            this.type = type;
//        }
//
//        public String getTo() {
//            return to;
//        }
//
//        public void setTo(String to) {
//            this.to = to;
//        }
//
//        @Override
//        public String getId() {
//            return id;
//        }
//
//        public void setId(String id) {
//            this.id = id;
//        }
//
//        public String getMediaType() {
//            return mediaType;
//        }
//
//        public void setMediaType(String mediaType) {
//            this.mediaType = mediaType;
//        }
//
//        public String getFrom() {
//            return from;
//        }
//
//        public void setFrom(String from) {
//            this.from = from;
//        }
//
//        public String getStatus() {
//            return status;
//        }
//
//        public void setStatus(String status) {
//            this.status = status;
//        }
//
//        public String getSent() {
//            return sent;
//        }
//
//        public void setSent(String sent) {
//            this.sent = sent;
//        }
//
//        public String getXmlns_stream() {
//            return xmlns_stream;
//        }
//
//        public void setXmlns_stream(String xmlns_stream) {
//            this.xmlns_stream = xmlns_stream;
//        }
//    }

}
