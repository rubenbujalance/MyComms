package com.vodafone.mycomms.main;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.events.BusProvider;
import com.vodafone.mycomms.events.InitProfileAndContacts;
import com.vodafone.mycomms.main.connection.INewsConnectionCallback;
import com.vodafone.mycomms.main.connection.NewsController;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;
import com.vodafone.mycomms.xmpp.XMPPTransactions;

import java.util.ArrayList;

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

        mNewsController = new NewsController(this);
        apiCall = Constants.NEWS_API_GET;
        //mNewsController.getNewsList(apiCall);

        //mNewsController.setConnectionCallback(this);

        Log.d(Constants.TAG, "###news###");

        BusProvider.getInstance().register(this);
        setContentView(R.layout.layout_dashboard);
        noConnectionLayout = (LinearLayout) findViewById(R.id.no_connection_layout);
        activateFooter();

        setFooterListeners(this);

        activateFooterSelected(Constants.TOOLBAR_DASHBOARD);

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
}
