package com.vodafone.mycomms.realm;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.util.Constants;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import model.News;

public class RealmNewsTransactions
{
    public RealmNewsTransactions() {
    }

    public void insertNewsList (ArrayList<News> newsArrayList, Realm realm){
        int size = newsArrayList.size();

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            mRealm.beginTransaction();
            for (int i = 0; i < size; i++) {
                mRealm.copyToRealmOrUpdate(newsArrayList.get(i));
            }
            mRealm.commitTransaction();
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmNewsTransactions.insertNewsList: ",e);
            Crashlytics.logException(e);
            mRealm.cancelTransaction();
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }

    public ArrayList<News> getAllNews(Realm realm)
    {
        ArrayList<News> newsArrayList = new ArrayList<>();

        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            RealmQuery<News> query = mRealm.where(News.class);
            RealmResults<News> result1 = query.findAll();

            if (result1!=null){
                result1.sort(Constants.NEWS_PUBLISHED_AT, RealmResults.SORT_ORDER_DESCENDING);
                for (News newsListItem : result1) {
                    newsArrayList.add(newsListItem);
                }
            }
            return newsArrayList;
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmNewsTransactions.getAllNews: ", e);
            Crashlytics.logException(e);
            return null;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }

    }

    public News getNewById(String id, Realm realm)
    {
        Realm mRealm;
        if(null != realm)
            mRealm = realm;
        else
            mRealm = Realm.getDefaultInstance();
        try
        {
            RealmQuery<News> query = mRealm.where(News.class);
            query.equalTo(Constants.NEWS_UUID, id);
            News news = query.findFirst();
            return news;
        }
        catch(Exception e)
        {
            Log.e(Constants.TAG, "RealmNewsTransactions.getNewById: ",e);
            Crashlytics.logException(e);
            return null;
        }
        finally
        {
            if(null == realm)
                mRealm.close();
        }
    }
}
