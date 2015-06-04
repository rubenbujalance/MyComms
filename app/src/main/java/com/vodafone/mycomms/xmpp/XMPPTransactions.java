package com.vodafone.mycomms.xmpp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.realm.RealmChatTransactions;
import com.vodafone.mycomms.util.Constants;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import io.realm.Realm;
import model.ChatMessage;

/**
 * Created by str_rbm on 03/06/2015.
 */
public final class XMPPTransactions {
    private static XMPPTCPConnection _xmppConnection = null;
    private static Realm mRealm;
    private static RealmChatTransactions chatTx;

    //Methods

    public static boolean initializeMsgServerSession(Context context)
    {
        //Instantiate Realm and Transactions
        mRealm = Realm.getInstance(context);
        chatTx = new RealmChatTransactions(mRealm, context);

        //Get profile_id
        SharedPreferences sp = context.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        String profile_id = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);

        if(profile_id == null)
        {
            Log.e(Constants.TAG, "ContactListMainActivity.initializeMsgServerSession: No profile_id found");
            return false;
        }

        //Configuration for the connection
        XMPPTCPConnectionConfiguration.Builder xmppConfigBuilder = XMPPTCPConnectionConfiguration.builder();

        //TODO RBM - Use this config when integrating with mycomms.com
//            xmppConfigBuilder.setUsernameAndPassword(profile_id, UserSecurity.getAccessToken(this));
//            xmppConfigBuilder.setServiceName(Constants.XMPP_PARAM_SERVICE_NAME);
//            xmppConfigBuilder.setHost(getString(R.string.xmpp_host));
//            xmppConfigBuilder.setPort(Constants.XMPP_PARAM_PORT);

        xmppConfigBuilder.setUsernameAndPassword(profile_id, "Stratesys123");
        xmppConfigBuilder.setServiceName(Constants.XMPP_PARAM_SERVICE_NAME);
        xmppConfigBuilder.setHost(Constants.XMPP_PARAM_HOST);
        xmppConfigBuilder.setPort(5222);
        xmppConfigBuilder.setEnabledSSLProtocols(new String[]{"TLSv1.2"});

        new XMPPOpenConnectionTask().execute(xmppConfigBuilder);

        return true;
    }

    public static boolean sendText(String contact_id, String text)
    {
        //Send text to the server
        ChatManager chatmanager = ChatManager.getInstanceFor(XMPPTransactions.getXmppConnection());
        org.jivesoftware.smack.chat.Chat newChat = chatmanager.createChat(contact_id+Constants.XMPP_PARAM_DOMAIN);

        try {
            newChat.sendMessage(text);
        }
        catch (Exception e) {
            Log.e(Constants.TAG, "ChatMainActivity.sendText: Error sending message", e);
            return false;
        }

        return true;
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
        protected void onPostExecute(XMPPTCPConnection xmppTcpConnection) {
            xmppConnectionCallback(xmppTcpConnection);
        }
    }

    private static void xmppConnectionCallback(XMPPTCPConnection xmppTcpConnection)
    {
        if(xmppTcpConnection != null)
            Log.i(Constants.TAG, "XMPPOpenConnection.onPostExecute: XMPP Connection established");
        else Log.i(Constants.TAG, "XMPPOpenConnection.onPostExecute: XMPP Connection NOT established");

        //Register the listener for incoming messages
        StanzaListener packetListener = new StanzaListener() {
            @Override
            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                Log.e(Constants.TAG, "XMPPTransactions.processPacket: " + packet.toString());

                Message msg = (Message)packet;

                if(msg.getTo().substring(0, msg.getTo().indexOf("@")).compareTo(
                        _xmppConnection.getUser().substring(0, _xmppConnection.getUser().indexOf("@")))==0)
                    return;

                ChatMessage chatMsg = chatTx.newChatMessageInstance(
                        msg.getFrom(),
                        Constants.CHAT_MESSAGE_DIRECTION_RECEIVED,
                        Constants.CHAT_MESSAGE_TYPE_TEXT,
                        msg.getBody(),
                        "");

                saveMessageToDB(chatMsg);
                notifyMessageReceived(chatMsg);
            }
        };

        // Register the listener
        StanzaFilter packetFilter = new OrFilter(IQTypeFilter.GET,
                                    new StanzaTypeFilter(Message.class));
        _xmppConnection.addAsyncStanzaListener(packetListener, packetFilter);
    }

    //Getters and Setters
    public static XMPPTCPConnection getXmppConnection() {
        return _xmppConnection;
    }

    private static boolean saveMessageToDB(ChatMessage chatMsg)
    {
        if(chatMsg==null) return false;
        return chatTx.insertChatMessage(chatMsg);
    }

    private static void notifyMessageReceived(ChatMessage chatMsg)
    {
        ChatsReceivedEvent event = new ChatsReceivedEvent();
        event.setMessage(chatMsg);
        BusProvider.getInstance().post(event);
    }
}
