package com.vodafone.mycomms.login;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.R;

public class LoginActivity extends ActionBarActivity {

    Button btLoginSalesforce;
    Button btLogin;
    TextView tvForgotPass;
    EditText etEmail;
    EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btLogin = (Button)findViewById(R.id.btLogin);
        btLoginSalesforce = (Button)findViewById(R.id.btLoginSalesforce);
        tvForgotPass = (TextView)findViewById(R.id.tvForgotPass);
        etEmail = (EditText)findViewById(R.id.etEmail);
        etPassword = (EditText)findViewById(R.id.etPassword);

        //Button back
        ImageView ivBack = (ImageView)findViewById(R.id.ivBtBack);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
