package com.example.user.nasrinchatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText displayName;
    private EditText emailId;
    private EditText passWord;
    private Button createAccount;
    private FirebaseAuth mAuth;
    private Toolbar mtoolbar;
    private ProgressDialog mProgressDialog;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mProgressDialog=new ProgressDialog(this);

        displayName=(EditText) findViewById(R.id.display_name);
        emailId=(EditText) findViewById(R.id.email);
        passWord=(EditText) findViewById(R.id.password);
        createAccount=(Button)findViewById(R.id.create_account);

        mtoolbar=(Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String display_name=displayName.getText().toString();
                String email=emailId.getText().toString();
                String password=passWord.getText().toString();

                if(!TextUtils.isEmpty(display_name) ||!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password)){
                    mProgressDialog.setTitle("Registering User");
                    mProgressDialog.setMessage("Please wait while we create your account");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();

                    register_user(display_name,email,password);

                }
            }
        });


    }

    private void register_user(final String display_name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser currentUser=FirebaseAuth.getInstance().getCurrentUser();
                            String uid=currentUser.getUid();

                            String deviceToken= FirebaseInstanceId.getInstance().getToken();
                            databaseReference=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);


                            HashMap<String,String> userMap=new HashMap<>();

                            userMap.put("name",display_name);
                            userMap.put("Status","Hi there");
                            userMap.put("image","default");
                            userMap.put("thumb_image","default");
                            userMap.put("device_token",deviceToken);

                            databaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        mProgressDialog.dismiss();
                                        Intent mainIntent=new Intent(RegisterActivity.this, MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }
                            });





                        } else {
                            // If sign in fails, display a message to the user.
                            mProgressDialog.hide();
                            Toast.makeText(RegisterActivity.this, "Cannot sign in.Please try again",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }
}


