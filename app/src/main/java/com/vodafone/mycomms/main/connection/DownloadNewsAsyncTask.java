package com.vodafone.mycomms.main.connection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.vodafone.mycomms.util.Constants;

import java.util.ArrayList;

import model.News;

public class DownloadNewsAsyncTask extends AsyncTask<Context, Void, Void> implements INewsConnectionCallback {
    private NewsController mNewsController;
    private String apiCall;
    private  Context mContext;

    @Override
    protected Void doInBackground(Context... params) {
        mContext = params[0];
        return null;
    }

    @Override
    protected void onPostExecute(Void _void) {
        super.onPostExecute(_void);
        Log.i(Constants.TAG, "DownloadNewsAsyncTask.doInBackground: ");

        mNewsController = new NewsController(mContext);
        apiCall = Constants.NEWS_API_GET;
        mNewsController.getNewsList(apiCall);
        mNewsController.setConnectionCallback(this);
    }

    @Override
    public void onNewsResponse(ArrayList<News> newsList, boolean morePages, int offsetPaging) {
        Log.i(Constants.TAG, "DownloadNewsAsyncTask.onNewsResponse: " + apiCall);

        apiCall = Constants.NEWS_API_GET;

        if (morePages){
            mNewsController.getNewsList(apiCall + "&o=" + offsetPaging);
        }
    }

    @Override
    public void onConnectionNotAvailable() {
        Log.e(Constants.TAG, "DownloadNewsAsyncTask.onConnectionNotAvailable: ");
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.e(Constants.TAG, "DownloadNewsAsyncTask.onCancelled: ");
    }
}

