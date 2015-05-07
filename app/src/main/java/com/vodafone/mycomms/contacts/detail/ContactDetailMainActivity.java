package com.vodafone.mycomms.contacts.detail;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.ToolbarActivity;

public class ContactDetailMainActivity extends ToolbarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_detail);
        activateFooter();
        setFooterListeners(this);

        ImageView ivBtBack = (ImageView)findViewById(R.id.ivBtBack);
        ivBtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
