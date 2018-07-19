package com.example.user.nasrinchatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import static com.example.user.nasrinchatapp.R.menu.main_menu;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar mtoolbar;

    private DatabaseReference mUserRef;
    private ViewPager viewpager;
    private SectionPagerAdapter sectionPagerAdapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        //toolbar
        mtoolbar=(Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Nasrin Chat App");

        if (mAuth.getCurrentUser()!=null) {

            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }

        viewpager=(ViewPager)findViewById(R.id.main_viewpager);
        sectionPagerAdapter=new SectionPagerAdapter(getSupportFragmentManager());

        viewpager.setAdapter(sectionPagerAdapter);

        tabLayout=(TabLayout)findViewById(R.id.main_tab_layout);
        tabLayout.setupWithViewPager(viewpager);
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser==null){
           sendTostart();
        }

        else
        {
            mUserRef.child("online").setValue("true");
           // mUserRef.child( "online" ).onDisconnect().setValue( ServerValue.TIMESTAMP );
        }


    }



    private void sendTostart() {
        Intent startIntent=new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
        if (item.getItemId()==R.id.log_out){
            FirebaseAuth.getInstance().signOut();
            sendTostart();
            mUserRef.child( "online" ).setValue( ServerValue.TIMESTAMP );
        }

        if (item.getItemId()==R.id.account_settings){
            Intent settings_Intent=new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(settings_Intent);
            mUserRef.child( "online" ).setValue( "true");

        }

        if (item.getItemId()==R.id.all_users){
            Intent users_intent=new Intent(MainActivity.this,UsersActivity.class);
            startActivity(users_intent);
            mUserRef.child( "online" ).setValue( "true");
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser !=null ) {
            //mUserRef.child( "online" ).onDisconnect().setValue( ServerValue.TIMESTAMP );


            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
            //mUserRef.child("lastSeen").setValue(ServerValue.TIMESTAMP);
        }

    }
}
