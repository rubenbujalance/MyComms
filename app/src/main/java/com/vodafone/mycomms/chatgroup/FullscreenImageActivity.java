package com.vodafone.mycomms.chatgroup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.main.connection.MainAppCompatActivity;

import java.io.File;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FullscreenImageActivity extends MainAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        SubsamplingScaleImageView ivImage = (SubsamplingScaleImageView)findViewById(R.id.image);

        Intent in = getIntent();
        String filePath = in.getStringExtra("imageFilePath");

        File file = new File(filePath);
        ivImage.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
        ivImage.setImage(ImageSource.uri(filePath));

        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
