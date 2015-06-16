package com.vodafone.mycomms.main.connection;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.framework.library.model.ConnectionResponse;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import model.News;

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
                    morePages = true;
                    offsetPaging = offsetPaging + 1;
                } else {
                    offsetPaging = 0;
                }

                ArrayList<News> newsList = new ArrayList<>();
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
        ArrayList<News> newsList = new ArrayList<>();

        try {
            Log.i(Constants.TAG, "NewsController.loadNews: ");
            JSONArray jsonArray = jsonObject.getJSONArray(Constants.NEWS_DATA);
            News news;

            getActivity().setContentView(R.layout.layout_dashboard);

            FrameLayout contenedor = (FrameLayout) getActivity().findViewById(R.id.list_news);
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                news = mapNews(jsonObject);
                newsList.add(news);

                View child = inflater.inflate(R.layout.layout_news_dashboard, contenedor, false);

                contenedor.addView(child);
               //Log.e(Constants.TAG, "Title: " + news.getTitle() + " Image: " + news.getImage() + " Date: " + news.getPublished_at());

            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "ContactController.insertContactListInRealm: " + e.toString());
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
