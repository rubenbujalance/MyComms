package com.vodafone.mycomms.main.connection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.NewsReceivedEvent;
import com.vodafone.mycomms.realm.RealmNewsTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import model.News;

public class NewsController{
    private Context mContext;
    private ArrayList<News> newsList;
    private RealmNewsTransactions realmNewsTransactions;

    public NewsController(Context context) {
        mContext = context;
        newsList = new ArrayList<>();
        realmNewsTransactions = new RealmNewsTransactions();
    }

    public void getNewsList(String api) {
        Log.i(Constants.TAG, "NewsController.getNewsList: ");
        new NewsListAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                (String)Constants.NEWS_API_GET);
    }

    private ArrayList<News> loadNews(JSONObject jsonObject) {

        try {
            Log.i(Constants.TAG, "NewsController.loadNews: ");
            JSONArray jsonArray = jsonObject.getJSONArray(Constants.NEWS_DATA);
            News news;

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                news = mapNews(jsonObject);
                newsList.add(news);
            }
            realmNewsTransactions.insertNewsList(newsList);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "NewsController.showNews: " + e.toString());
            return null;
        }
        return newsList;
    }

    public static News mapNews(JSONObject jsonObject) {
        News news = new News();
        try {
            if (!jsonObject.isNull(Constants.NEWS_UUID)) news.setUuid(jsonObject.getString(Constants.NEWS_UUID));
            if (!jsonObject.isNull(Constants.NEWS_TITLE)) news.setTitle(jsonObject.getString(Constants.NEWS_TITLE));
            if (!jsonObject.isNull(Constants.NEWS_HTML)) news.setHtml(jsonObject.getString(Constants.NEWS_HTML));
            if (!jsonObject.isNull(Constants.NEWS_IMAGE)) news.setImage(jsonObject.getString(Constants.NEWS_IMAGE));
            if (!jsonObject.isNull(Constants.NEWS_LINK)) news.setLink(jsonObject.getString(Constants.NEWS_LINK));
            if (!jsonObject.isNull(Constants.NEWS_AUTHOR_NAME)) news.setAuthor_name(jsonObject.getString(Constants.NEWS_AUTHOR_NAME));
            if (!jsonObject.isNull(Constants.NEWS_AUTHOR_AVATAR)) news.setAuthor_avatar(jsonObject.getString(Constants.NEWS_AUTHOR_AVATAR));
            if (!jsonObject.isNull(Constants.NEWS_CREATED_AT)) news.setCreated_at(jsonObject.getLong(Constants.NEWS_CREATED_AT));
            if (!jsonObject.isNull(Constants.NEWS_UPDATED_AT)) news.setUpdated_at(jsonObject.getLong(Constants.NEWS_UPDATED_AT));
            if (!jsonObject.isNull(Constants.NEWS_PUBLISHED_AT)) news.setPublished_at(jsonObject.getLong(Constants.NEWS_PUBLISHED_AT));
        }catch (JSONException e){
            e.printStackTrace();
            Log.e(Constants.TAG, "NewsAPIController.mapNews: " + e.toString());
        }
        return news;
    }

    public class NewsListAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Log.e(Constants.TAG, "NewsAsyncTask.doInBackground: START");

            Response response;
            String json = null;

            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://" + EndpointWrapper.getBaseNewsURL() +
                                params[0])
                        .addHeader(Constants.API_HTTP_HEADER_VERSION,
                                Utils.getHttpHeaderVersion(mContext))
                        .addHeader(Constants.API_HTTP_HEADER_CONTENTTYPE,
                                Utils.getHttpHeaderContentType())
                        .addHeader(Constants.API_HTTP_HEADER_AUTHORIZATION,
                                Utils.getHttpHeaderAuth(mContext))
                        .build();

                response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    if (response.code()<500) {
                        Log.i(Constants.TAG, "NewsAsyncTask.isSuccessful");
                        json = response.body().string();
                    } else {
                        Log.e(Constants.TAG, "NewsAsyncTask.ErrorCode " + response.code());
                        json = null;
                    }
                } else {
                    Log.e(Constants.TAG, "NewsAsyncTask.isNOTSuccessful");
                    json = null;
                }

            } catch (Exception e) {
                Log.e(Constants.TAG, "NewsAsyncTask.doInBackground: ",e);
            }

            Log.e(Constants.TAG, "NewsAsyncTask.doInBackground: END");

            return json;
        }

        @Override
        protected void onPostExecute(String json) {
            if (json!=null){
                newsListCallback(json);
            } else {
                NewsReceivedEvent event = new NewsReceivedEvent();
                event.setNews(null);
                BusProvider.getInstance().post(event);
            }
        }

        public void newsListCallback(String json) {
            Log.i(Constants.TAG, "NewsController.newsListCallback");
            JSONObject jsonResponse;

            if (json != null && json.trim().length()>0) {
                try {
                    jsonResponse = new JSONObject(json);
                    newsList = loadNews(jsonResponse);
                    NewsReceivedEvent event = new NewsReceivedEvent();
                    event.setNews(newsList);
                    BusProvider.getInstance().post(event);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i(Constants.TAG, "NewsController.newsListCallback: NO NEWS GOOD NEWS");
            }
        }
    }

    public void closeRealm()
    {
        realmNewsTransactions.closeRealm();
    }
}
