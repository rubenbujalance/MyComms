package com.vodafone.mycomms.main;

/**
 * Created by str_oan on 16/09/2015.
 */


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.BuildConfig;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.test.util.Util;
import com.vodafone.mycomms.util.Constants;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import io.realm.Realm;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by str_evc on 18/05/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.vodafone.mycomms", sdk = 21,
        manifest = "./src/main/AndroidManifest.xml")
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "javax.net.ssl.*", "org.json.*", "com.crashlytics.*"})
@PrepareForTest({Realm.class, Crashlytics.class})
public class NewsDetailActivityTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();
    public NewsDetailActivity mActivity;
    public Context mContext;

    @Before
    public void setUp()
    {
        mockStatic(Realm.class);
        when(Realm.getDefaultInstance()).thenReturn(null);
        mockStatic(Crashlytics.class);
        MockRepository.addAfterMethodRunner(new Util.MockitoStateCleaner());
        mContext = RuntimeEnvironment.application.getApplicationContext();
        mockParams();
    }

    @After
    public void tearDown() throws Exception
    {
        //Try to shutdown server if it was started
        try {
            Robolectric.reset();
        } catch (Exception e) {}

        mActivity = null;
        mContext = null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testActivityLifeCycle() throws Exception
    {
        Intent intent = new Intent();
        intent.putExtra(Constants.NEWS_IMAGE, "mockImageURL");
        intent.putExtra(Constants.NEWS_TITLE, "mockTitle");
        intent.putExtra(Constants.NEWS_AUTHOR_AVATAR, "mockAvatar");
        intent.putExtra(Constants.NEWS_AUTHOR_NAME, "mockAuthorName");

        intent.putExtra(Constants.NEWS_PUBLISHED_AT, "mockPublishedAt");
        intent.putExtra(Constants.NEWS_HTML, "mockHTML");
        mActivity = Robolectric.buildActivity(NewsDetailActivity.class).withIntent(intent)
                .create()
                .start()
                .resume()
                .pause()
                .stop()
                .destroy()
                .get();
        Assert.assertTrue(mActivity.isDestroyed());
    }

    @Test
    public void testWithoutAvatarURL() throws Exception
    {
        Intent intent = new Intent();
        intent.putExtra(Constants.NEWS_IMAGE, "mockImageURL");
        intent.putExtra(Constants.NEWS_TITLE, "mockTitle");
        intent.putExtra(Constants.NEWS_AUTHOR_NAME, "mockAuthorName");
        intent.putExtra(Constants.NEWS_AUTHOR_AVATAR, "mockAvatar");
        intent.putExtra(Constants.NEWS_PUBLISHED_AT, "mockPublishedAt");
        intent.putExtra(Constants.NEWS_HTML, "mockHTML");
        mActivity = Robolectric.buildActivity(NewsDetailActivity.class).withIntent(intent)
                .create()
                .start()
                .resume()
                .get();

        Thread.sleep(3000);
        Robolectric.flushForegroundThreadScheduler();

        TextView title = (TextView) mActivity.findViewById(R.id.title);
        ImageView avatar = (ImageView) mActivity.findViewById(R.id.avatar);
        TextView author = (TextView) mActivity.findViewById(R.id.author);
        TextView published = (TextView) mActivity.findViewById(R.id.published);
        WebView html = (WebView) mActivity.findViewById(R.id.newstext);

        Assert.assertTrue(title.getText().toString().equals("mockTitle"));
        Assert.assertNotNull(avatar);
        Assert.assertTrue(author.getText().toString().equals("mockAuthorName"));
        Assert.assertTrue(published.getText().toString().equals("mockPublishedAt"));
        Assert.assertNull(html.getUrl());
    }

    @Test
    public void testWithAvatarURL() throws Exception
    {
        Intent intent = new Intent();
        intent.putExtra(Constants.NEWS_IMAGE, "mockImageURL");
        intent.putExtra(Constants.NEWS_TITLE, "mockTitle");
        intent.putExtra(Constants.NEWS_AUTHOR_NAME, "mockAuthorName");

        intent.putExtra(Constants.NEWS_PUBLISHED_AT, "mockPublishedAt");
        intent.putExtra(Constants.NEWS_HTML, "mockHTML");
        mActivity = Robolectric.buildActivity(NewsDetailActivity.class).withIntent(intent)
                .create()
                .start()
                .resume()
                .get();

        Thread.sleep(3000);
        Robolectric.flushForegroundThreadScheduler();

        TextView title = (TextView) mActivity.findViewById(R.id.title);
        ImageView avatar = (ImageView) mActivity.findViewById(R.id.avatar);
        TextView author = (TextView) mActivity.findViewById(R.id.author);
        TextView published = (TextView) mActivity.findViewById(R.id.published);
        WebView html = (WebView) mActivity.findViewById(R.id.newstext);

        Assert.assertTrue(title.getText().toString().equals("mockTitle"));
        Assert.assertNotNull(avatar);
        Assert.assertTrue(author.getText().toString().equals("mockAuthorName"));
        Assert.assertTrue(published.getText().toString().equals("mockPublishedAt"));
        Assert.assertNull(html.getUrl());
    }

    @Test
    public void testClickOnBack() throws Exception
    {
        Intent intent = new Intent();
        intent.putExtra(Constants.NEWS_IMAGE, "mockImageURL");
        intent.putExtra(Constants.NEWS_TITLE, "mockTitle");
        intent.putExtra(Constants.NEWS_AUTHOR_NAME, "mockAuthorName");

        intent.putExtra(Constants.NEWS_PUBLISHED_AT, "mockPublishedAt");
        intent.putExtra(Constants.NEWS_HTML, "mockHTML");
        mActivity = Robolectric.buildActivity(NewsDetailActivity.class).withIntent(intent)
                .create()
                .start()
                .resume()
                .get();

        Thread.sleep(3000);
        ImageView ivBtBack = (ImageView)mActivity.findViewById(R.id.btn_back);
        ivBtBack.performClick();

        Assert.assertTrue(mActivity.isFinishing());
    }

    @Test
    public void testClickOnBackAction() throws Exception
    {
        Intent intent = new Intent();
        intent.putExtra(Constants.NEWS_IMAGE, "mockImageURL");
        intent.putExtra(Constants.NEWS_TITLE, "mockTitle");
        intent.putExtra(Constants.NEWS_AUTHOR_NAME, "mockAuthorName");

        intent.putExtra(Constants.NEWS_PUBLISHED_AT, "mockPublishedAt");
        intent.putExtra(Constants.NEWS_HTML, "mockHTML");
        mActivity = Robolectric.buildActivity(NewsDetailActivity.class).withIntent(intent)
                .create()
                .start()
                .resume()
                .get();

        Thread.sleep(3000);
        mActivity.onBackPressed();
        Assert.assertTrue(mActivity.isFinishing());
    }

    private void mockParams()
    {
        Downloader downloader = new OkHttpDownloader(mContext, Long.MAX_VALUE);
        Picasso.Builder builder = new Picasso.Builder(mContext);
        builder.downloader(downloader);
        MycommsApp.picasso = builder.build();
    }
}
