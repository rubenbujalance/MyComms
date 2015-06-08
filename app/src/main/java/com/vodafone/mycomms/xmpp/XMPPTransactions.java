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

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

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

    //Methods

    public static boolean initializeMsgServerSession(Context appContext)
    {
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

        //TODO RBM - Extensions configuration
//        XMPPConnection.configure(ProviderManager.getInstance());

        //Configuration for the connection
        XMPPTCPConnectionConfiguration.Builder xmppConfigBuilder = XMPPTCPConnectionConfiguration.builder();

        xmppConfigBuilder.setUsernameAndPassword(profile_id, UserSecurity.getAccessToken(appContext));
        xmppConfigBuilder.setServiceName(appContext.getString(R.string.xmpp_host));
        xmppConfigBuilder.setHost(appContext.getString(R.string.xmpp_host));
        xmppConfigBuilder.setPort(Constants.XMPP_PARAM_PORT);
        xmppConfigBuilder.setEnabledSSLProtocols(new String[]{"TLSv1.2"});
        xmppConfigBuilder.setSendPresence(true);

//        TEST CONFIGURATION (securejabber.me)
//        xmppConfigBuilder.setUsernameAndPassword(profile_id, "Stratesys123");
//        xmppConfigBuilder.setServiceName(Constants.XMPP_PARAM_SERVICE_NAME);
//        xmppConfigBuilder.setHost(Constants.XMPP_PARAM_HOST);
//        xmppConfigBuilder.setPort(5222);
//        xmppConfigBuilder.setEnabledSSLProtocols(new String[]{"TLSv1.2"});

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
        org.jivesoftware.smack.chat.Chat newChat = chatmanager.createChat(contact_id+"@"+Constants.XMPP_PARAM_DOMAIN);

        try {
            newChat.sendMessage(text);
        }
        catch (Exception e) {
            Log.e(Constants.TAG, "ChatMainActivity.sendText: Error sending message", e);
            return false;
        }

        return true;
    }

    private static void configure(ProviderManager pm)
    {
        /*
        //  Private Data Storage
        pm.addIQProvider("query","jabber:iq:private", new PrivateDataManager.PrivateDataIQProvider());

        //  Time
        try {
            pm.addIQProvider("query","jabber:iq:time", Class.forName("org.jivesoftware.smackx.packet.Time"));
        } catch (ClassNotFoundException e) {
            Log.w(Constants.TAG, "XMPPTransactions.configure: Can't load class for org.jivesoftware.smackx.packet.Time", e);
        }

        //  XHTML
        pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im", new XHTMLExtensionProvider());

//        //  Roster Exchange
//        pm.addExtensionProvider("x","jabber:x:roster", new RosterExchangeProvider());
//        //  Message Events
//        pm.addExtensionProvider("x","jabber:x:event", new MessageEventProvider());
        //  Chat State
        pm.addExtensionProvider("active","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("composing","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("paused","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("inactive","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("gone","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());

        //   FileTransfer
        pm.addIQProvider("si","http://jabber.org/protocol/si", new StreamInitiationProvider());
        pm.addIQProvider("query","http://jabber.org/protocol/bytestreams", new BytestreamsProvider());
        pm.addIQProvider("open","http://jabber.org/protocol/ibb", new OpenIQProvider());
        pm.addIQProvider("close","http://jabber.org/protocol/ibb", new CloseIQProvider());
        pm.addExtensionProvider("data","http://jabber.org/protocol/ibb", new DataPacketProvider());

        //  Group Chat Invitations
        pm.addExtensionProvider("x","jabber:x:conference", new GroupChatInvitation.Provider());
        //  Service Discovery # Items
        pm.addIQProvider("query","http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());
        //  Service Discovery # Info
        pm.addIQProvider("query","http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());
        //  Data Forms
        pm.addExtensionProvider("x","jabber:x:data", new DataFormProvider());
        //  MUC User
        pm.addExtensionProvider("x","http://jabber.org/protocol/muc#user", new MUCUserProvider());
        //  MUC Admin
        pm.addIQProvider("query","http://jabber.org/protocol/muc#admin", new MUCAdminProvider());
        //  MUC Owner
        pm.addIQProvider("query","http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());
        //  Delayed Delivery
        pm.addExtensionProvider("x","jabber:x:delay", new DelayInformationProvider());
        //  Version
        try {
            pm.addIQProvider("query","jabber:iq:version", Class.forName("org.jivesoftware.smackx.packet.Version"));
        } catch (ClassNotFoundException e) {
            Log.w(Constants.TAG, "XMPPTransactions.configure: Can't load class for org.jivesoftware.smackx.packet.Version", e);
        }
        //  VCard
        pm.addIQProvider("vCard","vcard-temp", new VCardProvider());
        //  Offline Message Requests
        pm.addIQProvider("offline","http://jabber.org/protocol/offline", new OfflineMessageRequest.Provider());
        //  Offline Message Indicator
        pm.addExtensionProvider("offline","http://jabber.org/protocol/offline", new OfflineMessageInfo.Provider());
        //  Last Activity
        pm.addIQProvider("query","jabber:iq:last", new LastActivity.Provider());
        //  User Search
        pm.addIQProvider("query","jabber:iq:search", new UserSearch.Provider());
        //  SharedGroupsInfo
        pm.addIQProvider("sharedgroup","http://www.jivesoftware.org/protocol/sharedgroup", new SharedGroupsInfo.Provider());
        //  JEP-33: Extended Stanza Addressing
        pm.addExtensionProvider("addresses","http://jabber.org/protocol/address", new MultipleAddressesProvider());
        */
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
                // Connect to the server
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

    private static void xmppConnectionCallback(XMPPTCPConnection xmppConnection)
    {
        if(xmppConnection != null && xmppConnection.isConnected()) {
            Log.w(Constants.TAG, "XMPPTransactions.xmppConnectionCallback: XMPP Connection established with user " + xmppConnection.getUser());
        }
        else {
            Log.e(Constants.TAG, "XMPPTransactions.xmppConnectionCallback: XMPP Connection NOT established");
            return;
        }

        //Register the listener for incoming messages
        StanzaListener packetListener = new StanzaListener() {
            @Override
            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                Log.w(Constants.TAG, "XMPPTransactions.processPacket: " + packet.toString());

                Message msg = (Message)packet;

                if(msg.getFrom().substring(0, msg.getFrom().indexOf("@")).compareTo(
                        _xmppConnection.getUser().substring(0, _xmppConnection.getUser().indexOf("@")))!=0) {

                    ChatMessage chatMsg = saveMessageToDB(msg);
                    notifyMessageReceived(chatMsg);
                }
            }
        };

        // Register the listener
        StanzaFilter packetFilter = new AndFilter(new StanzaTypeFilter(Message.class),
                                    new OrFilter(MessageTypeFilter.CHAT));
        _xmppConnection.addAsyncStanzaListener(packetListener, packetFilter);
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
                initializeMsgServerSession(mContext);
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
                initializeMsgServerSession(mContext);
            }
        };

        return listener;
    }

    //Getters and Setters
    public static XMPPTCPConnection getXmppConnection() {
        return _xmppConnection;
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
}
