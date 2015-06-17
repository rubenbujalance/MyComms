package com.vodafone.mycomms.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.vodafone.mycomms.ContactListMainActivity;
import com.vodafone.mycomms.EndpointWrapper;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.ChatsReceivedEvent;
import com.vodafone.mycomms.events.InitNews;
import com.vodafone.mycomms.events.InitProfileAndContacts;
import com.vodafone.mycomms.events.RefreshNewsEvent;
import com.vodafone.mycomms.main.connection.INewsConnectionCallback;
import com.vodafone.mycomms.main.connection.NewsController;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.util.Utils;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import model.ChatMessage;
import model.News;

public class DashBoardActivity extends ToolbarActivity implements INewsConnectionCallback {
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private LinearLayout noConnectionLayout;
    private NewsController mNewsController;
    private String apiCall;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(Constants.TAG, "DashBoardActivity.onCreate: ");
        super.onCreate(savedInstanceState);

        BusProvider.getInstance().post(new InitProfileAndContacts());
        BusProvider.getInstance().post(new InitNews());

        BusProvider.getInstance().register(this);
        setContentView(R.layout.layout_dashboard);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                // Set Time line
                DateFormat tf = new SimpleDateFormat("HH:mm");
                String time = tf.format(Calendar.getInstance().getTime());

                TextView timeText = (TextView) findViewById(R.id.timeDashboard);
                timeText.setText(time);

                // Set Date line
                DateFormat df = new SimpleDateFormat("EEEE, d MMMM");
                String date = df.format(Calendar.getInstance().getTime());

                TextView dateText = (TextView) findViewById(R.id.dateDashboard);
                dateText.setText(date);
            }
        });

        noConnectionLayout = (LinearLayout) findViewById(R.id.no_connection_layout);
        activateFooter();

        setFooterListeners(this);

        activateFooterSelected(Constants.TOOLBAR_DASHBOARD);

        // Event Click listeners
        ImageView btMagnifier = (ImageView) findViewById(R.id.magnifier);
        btMagnifier.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Start Contacts activity
                Intent in = new Intent(DashBoardActivity.this, ContactListMainActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(in);
                finish();
            }
        });

        LinearLayout btFavourite = (LinearLayout) findViewById(R.id.LayoutFavourite);
        btFavourite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Start Favourites activity
                Intent in = new Intent(DashBoardActivity.this, ContactListMainActivity.class);
                in.putExtra(Constants.toolbar, false);
                in.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(in);
                finish();
            }
        });

    }

    //Prevent of going from main screen back to login
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);

        // Disconnect from the XMPP server
        XMPPTransactions.disconnectMsgServerSession();
    }

    @Override
    public void onNewsResponse(ArrayList newsList, boolean morePages, int offsetPaging) {
        Log.i(Constants.TAG, "onNewsResponse: " + apiCall);

        if (morePages){
            mNewsController.getNewsList(apiCall + "&o=" + offsetPaging);
        }
    }

    @Override
    public void onConnectionNotAvailable() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        XMPPTransactions.initializeMsgServerSession(getApplicationContext());
    }

    @Subscribe
    public void onEventNewsReceived(RefreshNewsEvent event){
        ArrayList<News> news = event.getNews();
        if(news != null)
        {
            try {
                setContentView(R.layout.layout_dashboard);

                LinearLayout contenedor = (LinearLayout) findViewById(R.id.list_news);
                LayoutInflater inflater = LayoutInflater.from(this);

                for (int i = 0; i < news.size(); i++) {
                    View child = inflater.inflate(R.layout.layout_news_dashboard, contenedor, false);

                    contenedor.addView(child);
                    child.setPadding(10, 10, 10, 10);

                    /*ImageView newsImage = (ImageView) child.findViewById(R.id.notice_image);
                    Picasso.with(this)
                            .load("https://"+EndpointWrapper.getBaseNewsURL()+news.get(i).getImage())
                            .into(newsImage);*/

                    TextView title = (TextView) child.findViewById(R.id.notice_title);
                    title.setText(news.get(i).getTitle());

                    TextView date = (TextView) child.findViewById(R.id.notice_date);
                    date.setText(Utils.getStringChatTimeDifference(news.get(i).getPublished_at()));

                }
            } catch (Exception e) {
                Log.e(Constants.TAG, "Load news error: " + e);
            }
            Log.e(Constants.TAG, "NEWS SIZE: " + news.size());
        }
    }
}
