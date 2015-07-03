package com.vodafone.mycomms.connection;

import android.util.Log;

import com.squareup.picasso.Target;
import com.vodafone.mycomms.util.Constants;

import java.util.HashMap;

/**
 * Created by str_rbm on 02/07/2015.
 */
public class ConnectionsQueue {
    private static HashMap<String, Target> connectionsQueue;
//    private static ArrayList<String> sortedKeys = new ArrayList<>();

    public static void putConnection(String key, Target target)
    {
        if(connectionsQueue==null) connectionsQueue = new HashMap<>();

        try {
            connectionsQueue.put(key, target);
//            sortedKeys.add(0,key);

//            if(sortedKeys.size()>20) {
//                Target cancelTarget = connectionsQueue.get(sortedKeys.get(20));
//                Picasso.cancelRequest(cancelTarget);
//            }
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
