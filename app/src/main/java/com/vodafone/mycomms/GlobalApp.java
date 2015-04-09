package com.vodafone.mycomms;

import android.app.Application;

/**
 * Created by str_rbm on 02/04/2015.
 *
 * Main application singleton class
 * It handles global data and backend services
 */

public class GlobalApp extends Application {

    //Global data
    private UserProfile userProfile;

    //Methods
    public UserProfile getUserProfile() {
        if(userProfile == null) {
            userProfile = UserProfile.readUserProfile(this);

            if(userProfile == null)
                userProfile = new UserProfile();
        }

        return userProfile;
    }
}
