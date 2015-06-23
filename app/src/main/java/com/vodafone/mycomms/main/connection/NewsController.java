package com.vodafone.mycomms.main.connection;

import android.content.Context;
import android.util.Log;

import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.realm.RealmNewsTransactions;
import com.vodafone.mycomms.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;
import model.News;

public class NewsController extends BaseController {
    private Context mContext;
    private NewsConnection newsConnection;
    private INewsConnectionCallback newsConnectionCallback;
    private ArrayList<News> newsList;

    private String apiCall;

    private int offsetPaging = 0;

    public NewsController(Context context) {
        super(context);
        this.mContext = context;
        newsList = new ArrayList<>();
    }

    public void getNewsList(String api) {
        Log.i(Constants.TAG, "NewsController.getNewsList: ");
        if (newsConnection != null) {
            newsConnection.cancel();
        }
        apiCall = api;
        newsConnection = new NewsConnection(getContext(), this, apiCall);
        newsConnection.request();
    }

    @Override
    public void onConnectionComplete(ConnectionResponse response) {
        super.onConnectionComplete(response);
        boolean morePages = false;
        String result = response.getData().toString();

        Log.i(Constants.TAG, "NewsController.onConnectionComplete" + result);
        JSONObject jsonResponse;

        if (result != null && result.trim().length()>0) {
            try {
                jsonResponse = new JSONObject(result);
                JSONObject jsonPagination = jsonResponse.getJSONObject(Constants.NEWS_PAGINATION);

                if (jsonPagination.getBoolean(Constants.NEWS_PAGINATION_MORE_PAGES)) {
                    int pageSize = jsonPagination.getInt(Constants.NEWS_PAGINATION_PAGESIZE);
                    morePages = true;
                    offsetPaging = offsetPaging + pageSize;
                } else {
                    offsetPaging = 0;
                }

                newsList = loadNews(jsonResponse);
                if (this.getConnectionCallback() != null && this.getConnectionCallback() instanceof INewsConnectionCallback) {
                    ((INewsConnectionCallback) this.getConnectionCallback()).onNewsResponse(newsList, morePages, offsetPaging);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
               //Log.e(Constants.TAG, "Title: " + news.getTitle() + " Image: " + news.getImage() + " Date: " + news.getPublished_at());
            }
            Realm realm = Realm.getInstance(getContext());
            RealmNewsTransactions realmNewsTransactions = new RealmNewsTransactions(realm);
            realmNewsTransactions.insertNewsList(newsList);
            realm.close();

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
}
