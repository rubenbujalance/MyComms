package com.vodafone.mycomms.chatgroup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.vodafone.mycomms.R;

import java.io.File;

public class FullscreenImageActivity extends ActionBarActivity {

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

}
