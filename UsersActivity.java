package com.example.user.nasrinchatapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

//    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);


        mToolbar=(Toolbar)findViewById(R.id.users_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mRecyclerView=(RecyclerView)findViewById(R.id.users_recycler);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

       // mAuth= FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");

        myRef.keepSynced(true);


    }


    protected void onStart() {
        super.onStart();


        FirebaseRecyclerAdapter<Users, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, BlogViewHolder>(
                Users.class,
                R.layout.user_single_layout,
                BlogViewHolder.class,
                myRef
        ) {
            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, Users model, int position) {

                viewHolder.setTitle(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setImage(getApplicationContext(),model.getImage());

                final String user_id=getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileIntent=new Intent(UsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);


                    }
                });

            }
        };

        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }


    public static  class BlogViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public BlogViewHolder(View itemView) {

            super(itemView);
            mView = itemView;

            /*
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://androidsquad.space/"));
                    Intent browserChooserIntent = Intent.createChooser(browserIntent, "Choose browser of ypir Choice");
                    v.getContext().startActivity(browserChooserIntent);
                }
            });
            */
        }

        public void setTitle(String name){

            TextView post_title=(TextView)mView.findViewById(R.id.titleText);
            post_title.setText(name);
        }


        public void setStatus(String status) {

            TextView post_title1=(TextView)mView.findViewById(R.id.titleText2);
            post_title1.setText(status);
        }

        public void setImage(final Context applicationContext, final String image) {
            final ImageView post_image=(ImageView)mView.findViewById(R.id.user_image);
            Picasso.with(applicationContext).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(post_image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {

                    Picasso.with(applicationContext).load(image).placeholder(R.drawable.avatar)
                            .into(post_image);
                }
            });
        }
    }
}
