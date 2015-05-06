package com.vodafone.mycomms.mediagallery;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.ToolbarActivity;

public class MediaGalleryMainActivity extends ToolbarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_activity);
        activateToolbar();
        setToolbarTitle("Upload Media");
        activateFooter();

        setFooterListeners(this);
        setContactsListeners(this);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(MediaGalleryMainActivity.this, "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });

    }
}
