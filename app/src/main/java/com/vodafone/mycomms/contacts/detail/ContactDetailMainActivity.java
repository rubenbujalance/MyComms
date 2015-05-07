package com.vodafone.mycomms.contacts.detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.view.WindowManager;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chat.ChatMainActivity;
import com.vodafone.mycomms.util.ToolbarActivity;

public class ContactDetailMainActivity extends ToolbarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.contact_detail);

        ImageView ivBtBack = (ImageView)findViewById(R.id.ivBtBack);
        ivBtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageView btnChat = (ImageView)findViewById(R.id.btchat);

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ContactDetailMainActivity.this, ChatMainActivity.class));
            }
        });
    }
}
