package com.vodafone.mycomms.events;

import java.util.ArrayList;

import model.News;

/**
 * Created by str_rbm on 04/06/2015.
 */
public class NewsReceivedEvent {
    private ArrayList<News> newslist;

    public ArrayList<News> getNews() {
        return newslist;
    }

    public void setNews(ArrayList<News> newslist) {
        this.newslist = newslist;
    }
}

