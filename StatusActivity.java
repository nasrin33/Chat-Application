package com.example.user.nasrinchatapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Button changeStatus;
    private TextView statusInput;
    private DatabaseReference statusReference;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        toolbar=(Toolbar)findViewById(R.id.status_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Status Setting");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        changeStatus=(Button)findViewById(R.id.Save_status);
        statusInput=(TextView)findViewById(R.id.status_update);

        String status_value=getIntent().getStringExtra("status_value");
        statusInput.setText(status_value);

        FirebaseUser currentUser= FirebaseAuth.getInstance().getCurrentUser();
        String uid=currentUser.getUid();
        statusReference= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        changeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog=new ProgressDialog(StatusActivity.this);
                progressDialog.setTitle("Saving Changes");
                progressDialog.setMessage("Please wait while we are updating your status");
                progressDialog.show();

                String status=statusInput.getText().toString();


                statusReference.child("Status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            progressDialog.dismiss();

                        }
                        else{
                            Toast.makeText(getApplicationContext(), "There was some error in saving changes", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }
}
