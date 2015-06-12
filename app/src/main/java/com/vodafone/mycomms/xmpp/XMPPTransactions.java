package com.vodafone.mycomms.xmpp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.jivesoftware.smackx.bytestreams.ibb.provider.DataPacketProvider;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;

import io.realm.Realm;
import model.ChatMessage;

/**
 * Created by str_rbm on 03/06/2015.
 */
public class XMPPTransactions {
    private static XMPPTCPConnection _xmppConnection = null;
    private static Realm mRealm;
    private static RealmChatTransactions _chatTx;
    private static Context mContext;
    private static ProviderManager provManager;
    private static String accessToken;
    private static String profile_id;

    /*
     * Methods
     */

    public static boolean initializeMsgServerSession(Context appContext)
    {
        Log.i(Constants.TAG, "XMPPTransactions.initializeMsgServerSession: ");
        if(mContext!=null) return true;

        //Save context
        mContext = appContext;

        //Instantiate Realm and Transactions
        mRealm = Realm.getInstance(mContext);
        _chatTx = new RealmChatTransactions(mRealm, mContext);

        //Get profile_id
        SharedPreferences sp = appContext.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        String profile_id = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);

        if(profile_id == null)
        {
            Log.e(Constants.TAG, "ContactListMainActivity.initializeMsgServerSession: No profile_id found");
            return false;
        }

        //Configuration for the connection
        XMPPTCPConnectionConfiguration.Builder xmppConfigBuilder = XMPPTCPConnectionConfiguration.builder();

        accessToken = UserSecurity.getAccessToken(appContext);
        xmppConfigBuilder.setUsernameAndPassword(profile_id, accessToken);
        xmppConfigBuilder.setServiceName(appContext.getString(R.string.xmpp_host));
        xmppConfigBuilder.setHost(appContext.getString(R.string.xmpp_host));
        xmppConfigBuilder.setPort(Constants.XMPP_PARAM_PORT);
        xmppConfigBuilder.setEnabledSSLProtocols(new String[]{"TLSv1.2"});
        xmppConfigBuilder.setDebuggerEnabled(true);
//        xmppConfigBuilder.setSendPresence(true);
        xmppConfigBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        xmppConfigBuilder.setCompressionEnabled(false);

        new XMPPOpenConnectionTask().execute(xmppConfigBuilder);

        return true;
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

    public static boolean sendText(String contact_id, String text)
    {
        //Check connection
        if(_xmppConnection == null || !_xmppConnection.isConnected())
            initializeMsgServerSession(mContext);

        //Send text to the server
        ChatManager chatmanager = ChatManager.getInstanceFor(XMPPTransactions.getXmppConnection());
        //org.jivesoftware.smack.chat.Chat newChat = chatmanager.createChat(contact_id + "@" + Constants.XMPP_PARAM_DOMAIN);
        /*
        att:
            type: "chat"
            to: "mc_user2Id@my-comms.com"
            id: "message1Id"
            mediaType: "text"
            from: "mc_user1Id@my-comms.com/device"
            xmlns:stream: "http://etherx.jabber.org/streams"
            body: "Hello world"
         */

        Chat newChat = chatmanager.createChat(contact_id + "@" + Constants.XMPP_PARAM_DOMAIN);

        try {
            newChat.sendMessage(text);
        }
        catch (SmackException.NotConnectedException e) {
            Log.e(Constants.TAG, "ChatMainActivity.sendText: Error sending message", e);
            return false;
        }

        return true;
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

        Realm r = Realm.getInstance(mContext);
        RealmChatTransactions chatTx = new RealmChatTransactions(r, mContext);

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

    private static void configure(AbstractXMPPConnection conn)
    {
//        //  Private Data Storage
//        pm.addIQProvider("query","jabber:iq:private", new PrivateDataManager.PrivateDataIQProvider());
//
//        //  Time
//        try {
//            pm.addIQProvider("query","jabber:iq:time", Class.forName("org.jivesoftware.smackx.packet.Time"));
//        } catch (ClassNotFoundException e) {
//            Log.w(Constants.TAG, "XMPPTransactions.configure: Can't load class for org.jivesoftware.smackx.packet.Time", e);
//        }
//
//        //  XHTML
//        pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im", new XHTMLExtensionProvider());

        //Roster Exchange
        Roster.getInstanceFor(conn);
//        ProviderManager.addExtensionProvider("x","jabber:x:roster", RosterPacketProvider.INSTANCE);
//        ProviderManager.addExtensionProvider("iq","jabber:iq:roster", RosterPacketProvider.INSTANCE);

//        //  Message Events
//        pm.addExtensionProvider("x","jabber:x:event", new MessageEventProvider());
//        //Chat State
//        pm.addExtensionProvider("active","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
//        pm.addExtensionProvider("composing","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
//        pm.addExtensionProvider("paused","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
//        pm.addExtensionProvider("inactive","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
//        pm.addExtensionProvider("gone","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
//
//        //   FileTransfer
//        pm.addIQProvider("si","http://jabber.org/protocol/si", new StreamInitiationProvider());
//        pm.addIQProvider("query","http://jabber.org/protocol/bytestreams", new BytestreamsProvider());
//        pm.addIQProvider("open","http://jabber.org/protocol/ibb", new OpenIQProvider());
//        pm.addIQProvider("close","http://jabber.org/protocol/ibb", new CloseIQProvider());
//        pm.addExtensionProvider("data","http://jabber.org/protocol/ibb", new DataPacketProvider());
//
//        //  Group Chat Invitations
//        pm.addExtensionProvider("x","jabber:x:conference", new GroupChatInvitation.Provider());
//        //  Service Discovery # Items
//        pm.addIQProvider("query","http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());
//        //  Service Discovery # Info
//        pm.addIQProvider("query","http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());
//        //  Data Forms
//        pm.addExtensionProvider("x","jabber:x:data", new DataFormProvider());
//        //  MUC User
//        pm.addExtensionProvider("x","http://jabber.org/protocol/muc#user", new MUCUserProvider());
//        //  MUC Admin
//        pm.addIQProvider("query","http://jabber.org/protocol/muc#admin", new MUCAdminProvider());
//        //  MUC Owner
//        pm.addIQProvider("query","http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());
//        //  Delayed Delivery
//        pm.addExtensionProvider("x","jabber:x:delay", new DelayInformationProvider());
//        //  Version
//        try {
//            pm.addIQProvider("query","jabber:iq:version", Class.forName("org.jivesoftware.smackx.packet.Version"));
//        } catch (ClassNotFoundException e) {
//            Log.w(Constants.TAG, "XMPPTransactions.configure: Can't load class for org.jivesoftware.smackx.packet.Version", e);
//        }
//        //  VCard
//        pm.addIQProvider("vCard","vcard-temp", new VCardProvider());
//        //  Offline Message Requests
//        pm.addIQProvider("offline","http://jabber.org/protocol/offline", new OfflineMessageRequest.Provider());
//        //  Offline Message Indicator
//        pm.addExtensionProvider("offline","http://jabber.org/protocol/offline", new OfflineMessageInfo.Provider());
//        //  Last Activity
//        pm.addIQProvider("query","jabber:iq:last", new LastActivity.Provider());
//        //  User Search
//        pm.addIQProvider("query","jabber:iq:search", new UserSearch.Provider());
//        //  SharedGroupsInfo
//        pm.addIQProvider("sharedgroup","http://www.jivesoftware.org/protocol/sharedgroup", new SharedGroupsInfo.Provider());
//        //  JEP-33: Extended Stanza Addressing
//        pm.addExtensionProvider("addresses","http://jabber.org/protocol/address", new MultipleAddressesProvider());
    }

    //Classes

    static final class XMPPOpenConnectionTask extends AsyncTask<XMPPTCPConnectionConfiguration.Builder, Void, XMPPTCPConnection> {
        @Override
        protected XMPPTCPConnection doInBackground(XMPPTCPConnectionConfiguration.Builder[] params)
        {
            XMPPTCPConnection conn = null;

            try {
                XMPPTCPConnectionConfiguration.Builder xmppConfigBuilder = params[0];
                conn = new XMPPTCPConnection(xmppConfigBuilder.build());

                //Register the listener
                //Register the listener for incoming messages
                StanzaListener packetListener = new StanzaListener() {
                    @Override
                    public void processPacket(Stanza packet)
                    {
                        if(packet instanceof IQ)
                        {
                            IQ iq = (IQ)packet;
                            Log.w(Constants.TAG, "XMPPTransactions.processPacket (IQ packet): Type-"+iq.getType()
                                    +"; From-"+iq.getFrom()+"; To-"+iq.getTo());
                        }
                        else
                        {
                            Message msg = (Message)packet;
                            Log.w(Constants.TAG, "XMPPTransactions.processPacket (IQ packet): Type-"+msg.getType()
                                    +"; From-"+msg.getFrom()+"; To-"+msg.getTo()+"; Text-"+msg.getBody());
                        }

//                        if(msg.getFrom().substring(0, msg.getFrom().indexOf("@")).compareTo(
//                                _xmppConnection.getUser().substring(0, _xmppConnection.getUser().indexOf("@")))!=0) {
//
//                            ChatMessage chatMsg = saveMessageToDB(msg);
//                            notifyMessageReceived(chatMsg);
//                        }
                    }
                };

                //TESTING: Versión alternativa del Incoming chat
                ChatManager chatmanager = ChatManager.getInstanceFor(conn);
                chatmanager.addChatListener(new ChatManagerListener() {
                    @Override
                    public void chatCreated(Chat chat, boolean createdLocally)
                    {
                        Log.i(Constants.TAG, "XMPPTransactions.chatCreated: ");
                        if (!createdLocally)
                            chat.addMessageListener(new ChatMessageListener(){
                                @Override
                                public void processMessage(Chat chat, Message message) {
                                    Log.i(Constants.TAG, "XMPPTransactions.processMessage: ");
                                }
                            });
                    }
                });

//        StanzaFilter packetFilter = new AndFilter(new StanzaTypeFilter(Message.class),
//                                    new OrFilter(MessageTypeFilter.CHAT));

                conn.addAsyncStanzaListener(packetListener, null);

                //Add IQ Provider
                ProviderManager.addIQProvider("iq", "jabber:client", new MyIQProvider());

                // Connect to the server
                configure(conn);
                conn.setPacketReplyTimeout(15000);
                conn.connect();
                //Log into the server
                conn.login();

            } catch (Exception e) {
                Log.e(Constants.TAG, "XMPPOpenConnection.doInBackground: Error opening XMPP server connection", e);
                return null;
            }

            //Save connection
            _xmppConnection = conn;

            return conn;
        }

        @Override
        protected void onPostExecute(XMPPTCPConnection xmppConnection) {
            xmppConnectionCallback(xmppConnection);
        }
    }

    private static ConnectionListener getConnectionListener()
    {
        ConnectionListener listener = new ConnectionListener() {
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

        return listener;
    }

    //IQ extended class
    static class MyIQ extends IQ
    {
        private int pendingMsgs = 0;

        public MyIQ(IQ iq) {
            super(iq);
        }
        protected MyIQ(String childElementName) {
            super(childElementName);
        }

        @Override
        protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
            return null;
        }

        public int getPendingMsgs() {return pendingMsgs;}
        public void setPendingMsgs(int pendingMsgs) {this.pendingMsgs = pendingMsgs;}
    }

    //Provider for IQ Packets
    static class MyIQProvider extends IQProvider<MyIQ> {
        @Override
        public MyIQ parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {
            Log.e(Constants.TAG, "MyIQProvider.parse: hola parserrrrrr");

            MyIQ iq = new MyIQ(parser.getName());
            iq.setType(IQ.Type.fromString(parser.getAttributeValue("", "type")));
            iq.setFrom(parser.getAttributeValue("", "from"));
            iq.setTo(parser.getAttributeValue("", "to"));

            try {
                iq.setPendingMsgs(Integer.valueOf(parser.getAttributeValue("","pending")));
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

    static class SubscriptionProvider extends DataPacketProvider.PacketExtensionProvider {
        public static NewSubscription parseExtension(XmlPullParser parser) throws Exception {
            Log.i(Constants.TAG, "SubscriptionProvider.parseExtension: ");
            String jid = parser.getAttributeValue(null, "jid");
            String nodeId = parser.getAttributeValue(null, "node");
            String subId = parser.getAttributeValue(null, "subid");
            String state = parser.getAttributeValue(null, "subscription");

            String type = parser.getAttributeValue(null, "type");
            String to = parser.getAttributeValue(null, "to");
            String id = parser.getAttributeValue(null, "id");
            String mediaType = parser.getAttributeValue(null, "mediaType");
            String from = parser.getAttributeValue(null, "from");
            String status = parser.getAttributeValue(null, "status");
            String sent = parser.getAttributeValue(null, "sent");//TODO: Careful, it's a long!
            String xmlns_stream = parser.getAttributeValue(null, "xmlns:stream");


            boolean isRequired = false;

            int tag = parser.next();

            if ((tag == XmlPullParser.START_TAG) && parser.getName().equals("subscribe-options")) {
                tag = parser.next();

                if ((tag == XmlPullParser.START_TAG) && parser.getName().equals("required"))
                    isRequired = true;

                while (parser.next() != XmlPullParser.END_TAG && parser.getName() != "subscribe-options");
            }
            while (parser.getEventType() != XmlPullParser.END_TAG) parser.next();
            //return new Subscription(jid, nodeId, subId, (state == null ? null : Subscription.State.valueOf(state)), isRequired);
            return new NewSubscription(type, to, id, mediaType, from, status, sent, xmlns_stream,
                    jid, nodeId, subId, (mediaType == null ? null : Subscription.State.valueOf(state)), isRequired);
        }
    }

    static class NewSubscription extends org.jivesoftware.smackx.pubsub.Subscription {
        String type;
        String to;
        String id;
        String mediaType;
        String from;
        String status;
        String sent;
        String xmlns_stream;
        public NewSubscription(String type, String to, String id, String mediaType, String from, String status, String sent,
                               String xmlns_stream, String jid, String nodeId, String subscriptionId, State state, boolean configRequired) {
            super(jid, nodeId, subscriptionId, state, configRequired);
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        @Override
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMediaType() {
            return mediaType;
        }

        public void setMediaType(String mediaType) {
            this.mediaType = mediaType;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getSent() {
            return sent;
        }

        public void setSent(String sent) {
            this.sent = sent;
        }

        public String getXmlns_stream() {
            return xmlns_stream;
        }

        public void setXmlns_stream(String xmlns_stream) {
            this.xmlns_stream = xmlns_stream;
        }
    }

}
