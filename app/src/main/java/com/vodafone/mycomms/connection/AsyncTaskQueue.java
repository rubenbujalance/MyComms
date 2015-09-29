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
                }
                asyncTaskQueue.put(key, task);
            } catch (Exception e) {
                Log.e(Constants.TAG, "AsyncTaskQueue.putConnection: ", e);
            }
        }
    }

    public void removeConnection(String key)
    {
        synchronized (AsyncTaskQueue.class) {
            try {
                if(asyncTaskQueue.containsKey(key)) {
                    asyncTaskQueue.remove(key);
                }
            } catch (Exception e) {
                Log.e(Constants.TAG, "AsyncTaskQueue.removeConnection: ", e);
            }
        }
    }
}
