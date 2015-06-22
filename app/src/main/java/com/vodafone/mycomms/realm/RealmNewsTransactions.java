package com.vodafone.mycomms.realm;

import android.util.Log;

import com.vodafone.mycomms.util.Constants;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import model.News;

public class RealmNewsTransactions {
    private Realm mRealm;

    public RealmNewsTransactions(Realm realm) {
        mRealm = realm;
    }

    public void insertNewsList (ArrayList<News> newsArrayList){
        int size = newsArrayList.size();
        try {
            mRealm.beginTransaction();
            for (int i = 0; i < size; i++) {
                mRealm.copyToRealmOrUpdate(newsArrayList.get(i));
            }
            mRealm.commitTransaction();
        } catch (IllegalArgumentException e){
            Log.e(Constants.TAG, "RealmNewsTransactions.insertNewsList: " + e);
        }
    }

    public ArrayList<News> getAllNews(){
        ArrayList<News> newsArrayList = new ArrayList<>();
        RealmQuery<News> query = mRealm.where(News.class);
        RealmResults<News> result1 = query.findAll();

        if (result1!=null){
            result1.sort(Constants.NEWS_PUBLISHED_AT, RealmResults.SORT_ORDER_DESCENDING);
            for (News newsListItem : result1) {
                if (newsArrayList.size()<2)
                newsArrayList.add(newsListItem);
            }
        }
        return newsArrayList;
    }
}
