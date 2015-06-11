package com.vodafone.mycomms.main.connection;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.framework.library.connection.HttpConnection;
import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.main.connection.NewsConnection;
import com.vodafone.mycomms.main.connection.INewsConnectionCallback;

import com.vodafone.mycomms.util.Constants;

import org.json.JSONObject;

public class NewsController extends BaseController {
    private Context mContext;
    private NewsConnection newsConnection;
    private INewsConnectionCallback newsConnectionCallback;

    private String apiCall;

    private int offsetPaging = 0;

    public NewsController(Activity activity) {
        super(activity);
        this.mContext = activity;
    }

    public void getNewsList(String api){
        Log.i(Constants.TAG, "NewsController.getNewsList: ");
        if(newsConnection != null){
            newsConnection.cancel();
        }
        apiCall = api;
        newsConnection = new NewsConnection(getContext(), this, apiCall);
        newsConnection.request();
    }

    @Override
    public void onConnectionComplete(ConnectionResponse response) {
        super.onConnectionComplete(response);
        Log.i(Constants.TAG, "NewsController.onConnectionComplete: ");
        /*String apiCall = Constants.NEWS_API_GET;
        NewsController newsController = new NewsController(getActivity());
        newsController.getNewsList(apiCall);*/
        String result = response.getData().toString();
    }
}
