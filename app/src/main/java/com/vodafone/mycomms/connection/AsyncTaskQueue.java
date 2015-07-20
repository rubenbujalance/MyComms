package com.vodafone.mycomms.connection;

import android.os.AsyncTask;
import android.util.Log;

import com.vodafone.mycomms.util.Constants;

import java.util.HashMap;

/**
 * Created by str_rbm on 02/07/2015.
 */
public class AsyncTaskQueue {
    private HashMap<String, AsyncTask> asyncTaskQueue;
//    private static ArrayList<String> sortedKeys = new ArrayList<>();

    public AsyncTaskQueue()
    {
        asyncTaskQueue = new HashMap<>();
    }

    public void putConnection(String key, AsyncTask task)
    {
        synchronized (AsyncTaskQueue.class) {
            try {
                if(asyncTaskQueue.containsKey(key)) {
                    asyncTaskQueue.get(key).cancel(false);
//                    Log.i(Constants.TAG, "AsyncTaskQueue.putConnection: Task canceled "+key);
                }

                asyncTaskQueue.put(key, task);
//                Log.i(Constants.TAG, "AsyncTaskQueue.putConnection: Task added " + key);
            } catch (Exception e) {
                Log.e(Constants.TAG, "ConnectionsQueue.putConnection: ", e);
            }
        }
    }

    public void removeConnection(String key)
    {
        synchronized (AsyncTaskQueue.class) {
            try {
                if(asyncTaskQueue.containsKey(key)) {
                    asyncTaskQueue.remove(key);
//                    Log.i(Constants.TAG, "AsyncTaskQueue.removeConnection: Task removed " + key);
                }
            } catch (Exception e) {
                Log.e(Constants.TAG, "ConnectionsQueue.removeConnection: ", e);
            }
        }
    }

    public boolean hasConnections()
    {
        return asyncTaskQueue.isEmpty();
    }

    public boolean isConnectionQueued(String key) {
        return asyncTaskQueue.containsKey(key);
    }

    public void cancelConnection(String key)
    {
        synchronized (AsyncTaskQueue.class) {
            try {
                if(asyncTaskQueue.containsKey(key)) {
                    asyncTaskQueue.get(key).cancel(true);
                    asyncTaskQueue.remove(key);
                }
            } catch (Exception e) {
                Log.e(Constants.TAG, "ConnectionsQueue.cancelConnection: ", e);
            }
        }
    }

    //Commented due to concurrency problems
//    public void cancelAllConnections()
//    {
//        synchronized (AsyncTaskQueue.class) {
//            try {
//                Set<String> keys = asyncTaskQueue.keySet();
//                Iterator<String> keysIt = keys.iterator();
//                String key;
//
//                while (keysIt.hasNext()) {
//                    key = keysIt.next();
//                    asyncTaskQueue.get(key).cancel(true);
//                    asyncTaskQueue.remove(key);
//                }
//
//            } catch (Exception e) {
//                Log.e(Constants.TAG, "ConnectionsQueue.cancelAllConnections: ", e);
//            }
//        }
//    }
}
