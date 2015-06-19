package com.vodafone.mycomms.main.connection;

import com.vodafone.mycomms.connection.IConnectionCallback;

import java.util.ArrayList;

import model.News;

public interface INewsConnectionCallback extends IConnectionCallback {
    void onNewsResponse(ArrayList<News> newsList, boolean morePages, int offsetPaging);
}