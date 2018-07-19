package com.example.user.nasrinchatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    Button regButton;
    private Button logButton;
    private Toolbar mtoolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        regButton=(Button)findViewById(R.id.start_reg_button);
        mtoolbar=(Toolbar)findViewById(R.id.start_toolbar);
        logButton=(Button)findViewById(R.id.have_account);
        //setSupportActionBar(mtoolbar);

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg_intent=new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(reg_intent);
                finish();
            }
        });

        logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent log_intent=new Intent(StartActivity.this,LoginActivity.class);
                startActivity(log_intent);
                finish();
            }
        });
    }
}
