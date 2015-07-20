package com.vodafone.mycomms.main.connection;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.Response;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.NewsReceivedEvent;
import com.vodafone.mycomms.realm.RealmNewsTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.OKHttpWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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

        final String[] json = {null};

        try {
            OKHttpWrapper.get(Constants.NEWS_API_GET, mContext, new OKHttpWrapper.HttpCallback() {
                @Override
                public void onFailure(Response response, IOException e) {
                    // handle failure
                    Log.i(Constants.TAG, "NewsController.onFailure:");
                }

                @Override
                public void onSuccess(Response response) {
                    if (response.isSuccessful()) {
                        Log.i(Constants.TAG, "NewsController.onSuccess");
                        if (response.code() < 500) {
                            try {
                                json[0] = response.body().string();
                                if (json[0] != null) {
                                    newsListCallback(json[0]);
                                } else {
                                    NewsReceivedEvent event = new NewsReceivedEvent();
                                    event.setNews(null);
                                    BusProvider.getInstance().post(event);
                                }
                            } catch (IOException e) {
                                Log.e(Constants.TAG, "NewsController.onSuccess: ", e);
                            }
                        } else {
                            Log.e(Constants.TAG, "NewsController.ErrorCode " + response.code());
                            json[0] = null;
                        }
                    } else {
                        Log.e(Constants.TAG, "NewsController.isNOTSuccessful");
                        json[0] = null;
                    }
                }
            });
            Log.i(Constants.TAG, "NewsController.getNewsList: AFTER");
//                OkHttpClient client = new OkHttpClient();
//                Request request = new Request.Builder()
//                        .url("https://" + EndpointWrapper.getBaseNewsURL() +
//                                params[0])
//                        .addHeader(Constants.API_HTTP_HEADER_VERSION,
//                                Utils.getHttpHeaderVersion(mContext))
//                        .addHeader(Constants.API_HTTP_HEADER_CONTENTTYPE,
//                                Utils.getHttpHeaderContentType())
//                        .addHeader(Constants.API_HTTP_HEADER_AUTHORIZATION,
//                                Utils.getHttpHeaderAuth(mContext))
//                        .build();

//                response = client.newCall(request).execute();
//                if (response.isSuccessful()) {
//                    if (response.code()<500) {
//                        Log.i(Constants.TAG, "NewsAsyncTask.isSuccessful");
//                        json = response.body().string();
//                    } else {
//                        Log.e(Constants.TAG, "NewsAsyncTask.ErrorCode " + response.code());
//                        json = null;
//                    }
//                } else {
//                    Log.e(Constants.TAG, "NewsAsyncTask.isNOTSuccessful");
//                    json = null;
//                }

        } catch (Exception e) {
            Log.e(Constants.TAG, "NewsAsyncTask.doInBackground: ",e);
        }
    }

    private ArrayList<News> loadNews(JSONObject jsonObject) {

        RealmNewsTransactions realmNewsTransactions = null;
        try {
            Log.i(Constants.TAG, "NewsController.loadNews: ");
            JSONArray jsonArray = jsonObject.getJSONArray(Constants.NEWS_DATA);
            News news;

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                news = mapNews(jsonObject);
                newsList.add(news);
            }
            realmNewsTransactions = new RealmNewsTransactions();
            realmNewsTransactions.insertNewsList(newsList);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "NewsController.showNews: " + e.toString());
            return null;
        }
        finally {
            if(null!=realmNewsTransactions) realmNewsTransactions.closeRealm();
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

    public void newsListCallback(String json) {
        Log.i(Constants.TAG, "NewsController.newsListCallback");
        final JSONObject[] jsonResponse = new JSONObject[1];

        if (json != null && json.trim().length()>0) {
            try {
                jsonResponse[0] = new JSONObject(json);
                newsList = loadNews(jsonResponse[0]);
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

    public void closeRealm()
    {
        realmNewsTransactions.closeRealm();
    }
}
