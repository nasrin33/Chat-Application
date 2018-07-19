package com.example.user.nasrinchatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private EditText Email;
    private EditText Password;
    private Button Login;
    private Toolbar mtoolbar;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference loginDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mProgressDialog=new ProgressDialog(this);
        mAuth=FirebaseAuth.getInstance();

        Email=(EditText)findViewById(R.id.email);
        Password=(EditText)findViewById(R.id.password);
        Login=(Button)findViewById(R.id.login_btn);

        mtoolbar=(Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Login");

        loginDatabase= FirebaseDatabase.getInstance().getReference().child("Users");

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email=Email.getText().toString();
                String password=Password.getText().toString();
                if(!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password)){
                    mProgressDialog.setTitle("Logging in");
                    mProgressDialog.setMessage("Please wait while we are checking your information!");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();
                    loginUser(email,password);
                }

            }
        });

    }

    private void loginUser(String email, String password) {
          mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
                 if (task.isSuccessful()){

                     mProgressDialog.dismiss();
                     String current_user_id=mAuth.getCurrentUser().getUid();

                     String deviceToken= FirebaseInstanceId.getInstance().getToken();

                     loginDatabase.child(current_user_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                         @Override
                         public void onSuccess(Void aVoid) {

                             Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
                             mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                             startActivity(mainIntent);
                             finish();

                         }
                     });



                 }
                 else
                 {
                     mProgressDialog.hide();
                     Toast.makeText(LoginActivity.this, "Cannot sign in.Please try again",
                             Toast.LENGTH_SHORT).show();

                 }
              }
          });
    }
}
