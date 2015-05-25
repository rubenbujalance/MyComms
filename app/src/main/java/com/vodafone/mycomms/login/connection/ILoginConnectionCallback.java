package com.vodafone.mycomms.login.connection;

import com.vodafone.mycomms.connection.IConnectionCallback;

/**
 * Created by str_vig on 14/05/2015.
 */
public interface ILoginConnectionCallback extends IConnectionCallback {
    void onLoginSuccess();
    void onLoginError();
}
