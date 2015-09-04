package com.vodafone.mycomms.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;

public class NewsDetailActivity  extends ToolbarActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler
                (
                        new UncaughtExceptionHandlerController(this, SplashScreenActivity.class)
                );
        setContentView(R.layout.layout_news_detail);

        Log.d(Constants.TAG, "NewsDetailActivity.onCreate: ");

        Intent intent = getIntent();

        ImageView picture = (ImageView) findViewById(R.id.picture);
        Picasso.with(this)
                .load("https://"+ EndpointWrapper.getBaseNewsURL()+intent.getExtras().getString(Constants.NEWS_IMAGE))
                .into(picture);

        TextView title = (TextView) findViewById(R.id.title);
        title.setText(intent.getExtras().getString(Constants.NEWS_TITLE));

        ImageView avatar = (ImageView) findViewById(R.id.avatar);

        if(intent.getExtras().getString(Constants.NEWS_AUTHOR_AVATAR) == null){
            Picasso.with(this)
                    .load(R.mipmap.icon_account_white)
                    .into(avatar);
        } else {
            Picasso.with(this)
                    .load("https://" + EndpointWrapper.getBaseURL() + intent.getExtras().getString(Constants.NEWS_AUTHOR_AVATAR))
                    .into(avatar);
        }

        TextView author = (TextView) findViewById(R.id.author);
        author.setText(intent.getExtras().getString(Constants.NEWS_AUTHOR_NAME));

        TextView published = (TextView) findViewById(R.id.published);
        published.setText(intent.getExtras().getString(Constants.NEWS_PUBLISHED_AT));

        WebView html = (WebView) findViewById(R.id.newstext);
//        html.loadData(intent.getExtras().getString(Constants.NEWS_HTML), "text/html; charset=utf-8", "UTF-8");
//        html.loadUrl("file:///android_asset/demo.html");
        String pish = "<html><head><style type=\"text/css\">@font-face {font-family: SourceSansPro-Regular;src: url(\"file:///android_asset/fonts/SourceSansPro-Regular.ttf\")}body {font-family: SourceSansPro-Regular;font-size: medium;text-align: justify;}</style></head><body>";
        String pas = "</body></html>";
        String myHtmlString = pish + intent.getExtras().getString(Constants.NEWS_HTML) + pas;
        html.loadDataWithBaseURL(null, myHtmlString, "text/html", "UTF-8", null);

        ImageView ivBtBack = (ImageView)findViewById(R.id.btn_back);
        ivBtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start Contacts activity
                backAction();
            }
        });
    }

    @Override
    public void onBackPressed() {
        backAction();
    }

    private void backAction() {
//        Intent in = new Intent(NewsDetailActivity.this, DashBoardActivity.class);
//        in.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//        startActivity(in);
        finish();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        MycommsApp.activityStarted();
    }

    @Override
    public void onStop()
    {
        MycommsApp.activityStopped();
        super.onStop();
    }
}
