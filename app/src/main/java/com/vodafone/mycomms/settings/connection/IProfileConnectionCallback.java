package com.vodafone.mycomms.settings.connection;

import com.vodafone.mycomms.connection.IConnectionCallback;

import model.UserProfile;

/**
 * Created by str_vig on 26/05/2015.
 */
public interface IProfileConnectionCallback extends IConnectionCallback {
    void onProfileReceived(UserProfile userProfile);

    void onProfileConnectionError();

    void onUpdateProfileConnectionError();

    void onUpdateProfileConnectionCompleted();

    void  onPasswordChangeError(String error);

    void onPasswordChangeCompleted();

}
