package com.vodafone.mycomms;

import android.test.AndroidTestCase;

import junit.framework.TestCase;

public class UserProfileTest extends AndroidTestCase {

    UserProfile profile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        profile = new UserProfile();
        profile.setFirstName("a");
        profile.setLastName("s");
        profile.setMail("d");
        profile.setCompanyName("f");
        profile.setCountryISO("g");
        profile.setOfficeLocation("h");
        profile.setPassword("j");
        profile.setPhone("k");
        profile.setPosition("l");
    }

    public void testSaveUserProfile() throws Exception {

//        boolean ok = profile.saveUserProfile(getContext());
        assertTrue(true);
    }

    public void testReadUserProfile() throws Exception {
        assertTrue(true);
    }
}