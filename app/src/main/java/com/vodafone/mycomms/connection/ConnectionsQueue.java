package com.vodafone.mycomms.connection;

import android.util.Log;

import com.vodafone.mycomms.util.Constants;

import java.util.HashMap;

/**
 * Created by str_rbm on 02/07/2015.
 */
public class ConnectionsQueue {
    private static HashMap<String, Object> connectionsQueue;

    public static void putConnection(String key)
    {
        if(connectionsQueue==null) connectionsQueue = new HashMap<>();

        try {
            connectionsQueue.put(key, null);
        } catch (Exception e) {
            Log.e(Constants.TAG, "ConnectionsQueue.putConnection: ",e);
        }
    }

    public static void removeConnection(String key)
    {
        try {
            connectionsQueue.remove(key);
        } catch (Exception e) {
            Log.e(Constants.TAG, "ConnectionsQueue.removeConnection: ",e);
        }
    }

    public static boolean isConnectionAlive(String key)
    {
        if(connectionsQueue==null) connectionsQueue = new HashMap<>();
        return connectionsQueue.containsKey(key);
    }
}
