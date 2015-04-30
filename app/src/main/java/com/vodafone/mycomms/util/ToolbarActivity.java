package com.vodafone.mycomms.util;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.vodafone.mycomms.R;

public class ToolbarActivity extends ActionBarActivity {

    private Toolbar mToolbar;

    protected Toolbar activateToolbar() {
        if(mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.app_bar);
            if(mToolbar != null) {
                setSupportActionBar(mToolbar);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }
        return mToolbar;
    }

    protected Toolbar activateToolbarWithHomeEnabled() {
        activateToolbar();
        if(mToolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            changeArrowColor(R.color.white);
        }
        return mToolbar;
    }

    protected void changeArrowColor(int color){
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        if (upArrow != null) {
            upArrow.setColorFilter(getResources().getColor(color), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }
    }

    protected Toolbar setToolbarBackground(int image) {
        if(mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.app_bar);
        }
        if(mToolbar != null) {
            mToolbar.setBackground(getResources().getDrawable(image));
        }
        return mToolbar;
    }
}
