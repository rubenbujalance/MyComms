package com.vodafone.mycomms.main.connection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.framework.library.model.ConnectionResponse;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.connection.BaseController;
import com.vodafone.mycomms.realm.RealmNewsTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;
import model.News;

public class NewsController extends BaseController {
    private Context mContext;
    private NewsConnection newsConnection;
    private ArrayList<News> newsList;

    public NewsController(Context context) {
        super(context);
        this.mContext = getContext();
        newsList = new ArrayList<>();
    }

    public void getNewsList(String api) {
        Log.i(Constants.TAG, "NewsController.getNewsList: ");
//        if (newsConnection != null) {
//            newsConnection.cancel();
//        }
//        newsConnection = new NewsConnection(getContext(), this, api);
//        newsConnection.request();
        new NewsListAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                (String)Constants.NEWS_API_GET);
    }

    @Override
    public void onConnectionComplete(ConnectionResponse response) {
        super.onConnectionComplete(response);
        String result = response.getData().toString();

        Log.i(Constants.TAG, "NewsController.onConnectionComplete" + result);
        JSONObject jsonResponse;

        if (result != null && result.trim().length()>0) {
            try {
                jsonResponse = new JSONObject(result);
                newsList = loadNews(jsonResponse);
                if (this.getConnectionCallback() != null && this.getConnectionCallback() instanceof INewsConnectionCallback) {
                    ((INewsConnectionCallback) this.getConnectionCallback()).onNewsResponse(newsList);
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

    public void newsListCallback(String json) {
        Log.i(Constants.TAG, "NewsController.newsListCallback" + json);
        JSONObject jsonResponse;

        if (json != null && json.trim().length()>0) {
            try {
                jsonResponse = new JSONObject(json);
                newsList = loadNews(jsonResponse);
                if (this.getConnectionCallback() != null && this.getConnectionCallback() instanceof INewsConnectionCallback) {
                    ((INewsConnectionCallback) this.getConnectionCallback()).onNewsResponse(newsList);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class NewsListAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Log.e(Constants.TAG, "NewsAsyncTask.doInBackground: START");

            Response response = null;
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
                json = response.body().string();

            } catch (Exception e) {
                Log.e(Constants.TAG, "NewsAsyncTask.doInBackground: ",e);
            }

            Log.e(Constants.TAG, "NewsAsyncTask.doInBackground: END");

            return json;
        }

        @Override
        protected void onPostExecute(String json) {
            newsListCallback(json);
        }
    }
}
