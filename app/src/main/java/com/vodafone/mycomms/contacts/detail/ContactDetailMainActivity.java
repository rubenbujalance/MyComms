package com.vodafone.mycomms.contacts.detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.chat.ChatMainActivity;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.ToolbarActivity;

import java.util.List;

import io.realm.Realm;
import model.Contact;

public class ContactDetailMainActivity extends ToolbarActivity {
    private Realm realm;
    private Contact contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.contact_detail);
        realm = Realm.getInstance(this);

        Intent intent = getIntent();
        String contactId = intent.getExtras().getString(Constants.CONTACT_ID);
        contact = getContact(contactId);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private Contact getContact(String contactId){
        RealmContactTransactions realmContactTransactions = new RealmContactTransactions(realm);
        List<Contact> contactList = realmContactTransactions.getFilteredContacts(Constants.CONTACT_ID, contactId);

        return contactList.get(0);
    }
}
