package com.vodafone.mycomms.main.connection;

import com.vodafone.mycomms.connection.IConnectionCallback;

import java.util.ArrayList;

import model.Contact;

public interface INewsConnectionCallback extends IConnectionCallback {
    void onNewsResponse(ArrayList newsList, boolean morePages, int offsetPaging);
}