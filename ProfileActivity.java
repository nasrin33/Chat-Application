package com.example.user.nasrinchatapp;

import android.app.ProgressDialog;
import android.icu.text.DateFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView profileName, profileStatus, totalFriend;
    private Button friendRequestbtn,declineRequestbtn;

    private DatabaseReference profiledatabase,friendRequestDatabase,friendDatabase,notificationDatabase;
    private DatabaseReference mRootRef;

    private ProgressDialog profileProgress;
    private FirebaseUser mCurrent_user;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

       // if(getIntent().getExtras().get("visit_user_id")!=null)

        profileImageView=(ImageView)findViewById(R.id.profile_image);
        profileName=(TextView)findViewById(R.id.profile_display_name);
        profileStatus=(TextView)findViewById(R.id.profile_user_status);
        totalFriend=(TextView)findViewById(R.id.profile_total_friends);
        friendRequestbtn=(Button)findViewById(R.id.profile_send_request);
        declineRequestbtn=(Button)findViewById(R.id.profile_decline_request);

        mCurrent_state="not_friends";

        profileProgress=new ProgressDialog(this);
        profileProgress.setTitle("Loading User Data");
        profileProgress.setMessage("Please wait while we load the user data");
        profileProgress.setCanceledOnTouchOutside(false);
        profileProgress.show();

        mRootRef=FirebaseDatabase.getInstance().getReference();
        final String user_id= getIntent().getStringExtra("user_id");
        profiledatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        friendRequestDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_request");
        notificationDatabase=FirebaseDatabase.getInstance().getReference().child("notifications");
        friendDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mCurrent_user= FirebaseAuth.getInstance().getCurrentUser();
        profiledatabase.keepSynced(true);
        friendRequestDatabase.keepSynced(true);
        friendDatabase.keepSynced(true);

        declineRequestbtn.setVisibility(View.INVISIBLE);
        declineRequestbtn.setEnabled(false);

        profiledatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String profile_name=dataSnapshot.child("name").getValue().toString();
                String profile_status=dataSnapshot.child("Status").getValue().toString();
                String profile_image=dataSnapshot.child("image").getValue().toString();

                profileName.setText(profile_name);
                profileStatus.setText(profile_status);
                Picasso.with(ProfileActivity.this).load(profile_image).placeholder(R.drawable.avatar3).into(profileImageView);
                declineRequestbtn.setVisibility(View.INVISIBLE);
                declineRequestbtn.setEnabled(false);



                //------------------Friends List/Request feature----------

                friendRequestDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)){

                            //-----------if not friend----------------

                            String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (req_type.equals("received")){
                                mCurrent_state="req_received";
                                friendRequestbtn.setText("Accept Friend Request");

                                //--------------decline button will show-------------

                                declineRequestbtn.setVisibility(View.VISIBLE);
                                declineRequestbtn.setEnabled(true);
                            }

                            else if(req_type.equals("sent")){

                                mCurrent_state="req_sent";
                                friendRequestbtn.setText("Cancel Friend Request");

                                //----------decline button wont show------------------

                                declineRequestbtn.setVisibility(View.INVISIBLE);
                                declineRequestbtn.setEnabled(false);
                            }
                            profileProgress.dismiss();
                        }

                        else{  //------------if already friend---------

                            friendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(user_id)){

                                        mCurrent_state = "friends";
                                        friendRequestbtn.setText("Unfriend this Person");

                                        declineRequestbtn.setVisibility(View.INVISIBLE);
                                        declineRequestbtn.setEnabled(false);
                                    }

                                    profileProgress.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    profileProgress.dismiss();

                                }
                            });
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



  friendRequestbtn.setOnClickListener(new View.OnClickListener() {
      @RequiresApi(api = Build.VERSION_CODES.N)
      @Override
      public void onClick(View v) {

          friendRequestbtn.setEnabled(false);

          // ---------------------------not_friends State----------------------

          if (mCurrent_state.equals("not_friends")){

              DatabaseReference newNotificationRef=mRootRef.child("notifications").child(user_id).push();
              String newNotificationId=newNotificationRef.getKey();

              HashMap<String,String> notificationData=new HashMap<>();
              notificationData.put("from",mCurrent_user.getUid());
              notificationData.put("type","request");

              Map requestMap=new HashMap();
              requestMap.put("Friend_request/" + mCurrent_user.getUid()+"/" +user_id+ "/request_type","sent");
              requestMap.put( "Friend_request/" +user_id+ "/" + mCurrent_user.getUid()+ "/request_type", "received");
              requestMap.put("notifications/" +user_id+ "/" +newNotificationId, notificationData);

              mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                  @Override
                  public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                      if (databaseError != null){
                          Toast.makeText(ProfileActivity.this, "There was some error in sending rrequest", Toast.LENGTH_SHORT).show();
                      }

                      friendRequestbtn.setEnabled(true);
                      mCurrent_state="req_sent";
                      friendRequestbtn.setText("Cancel Friend Request");

                  }
              });

          }



          //--------------------------cancel Friend Request--------------------------------------

          if (mCurrent_state.equals("req_sent")){

              friendRequestDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                  @Override
                  public void onSuccess(Void aVoid) {

                      friendRequestDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                          @Override
                          public void onSuccess(Void aVoid) {

                              friendRequestbtn.setEnabled(true);
                              mCurrent_state = "not_friends";
                              friendRequestbtn.setText("Send Friend Request");

                              declineRequestbtn.setVisibility(View.INVISIBLE);
                              declineRequestbtn.setEnabled(false);

                          }
                      });
                  }
              });
          }


          //-----------------------------req received state----------------------------------------------

          if (mCurrent_state.equals("req_received")){

              final String currentDate=DateFormat.getDateTimeInstance().format(new Date());

              Map friendMap=new HashMap();
              friendMap.put("Friends/" +mCurrent_user.getUid()+ "/" +user_id+ "/date", currentDate);
              friendMap.put("Friends/" +user_id+ "/" +mCurrent_user.getUid()+ "/date",currentDate);

              friendMap.put("Friend_request/" +mCurrent_user.getUid()+ "/" +user_id, null);
              friendMap.put("Friend_request/" +user_id+ "/" +mCurrent_user.getUid() ,null);

              mRootRef.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                  @Override
                  public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                      if (databaseError==null){

                          friendRequestbtn.setEnabled(true);

                          mCurrent_state="friends";
                          friendRequestbtn.setText("Unfriend this person");

                          declineRequestbtn.setVisibility(View.INVISIBLE);
                          declineRequestbtn.setEnabled(false);
                      }

                      else{

                          String error=databaseError.getMessage();
                          Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                      }
                  }
              });
          }
    //---------------------------------------Unfriends----------------------------------------
          if (mCurrent_state.equals("friends")){

              Map unfriendMap=new HashMap();

              unfriendMap.put("Friends/" +mCurrent_user.getUid()+ "/" +user_id, null);
              unfriendMap.put("Friends/" +user_id+ "/" +mCurrent_user.getUid() ,null);

              mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                  @Override
                  public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                      if (databaseError==null){

                          mCurrent_state="not_friends";
                          friendRequestbtn.setText("Send Friend Request");

                          declineRequestbtn.setVisibility(View.INVISIBLE);
                          declineRequestbtn.setEnabled(false);
                      }

                      else{

                          String error=databaseError.getMessage();
                          Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                      }

                      friendRequestbtn.setEnabled(true);
                  }
              });
          }


      }
  });

        declineRequestbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCurrent_state.equals("req_received")){

                    Map declineRequest=new HashMap();

                    declineRequest.put("Friend_request/" +mCurrent_user.getUid()+ "/" +user_id, null);
                    declineRequest.put("Friend_request/" +user_id+ "/" +mCurrent_user.getUid() ,null);

                    mRootRef.updateChildren(declineRequest, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError==null){

                                mCurrent_state="not_friends";
                                friendRequestbtn.setText("Send Friend Request");

                                declineRequestbtn.setVisibility(View.INVISIBLE);
                                declineRequestbtn.setEnabled(false);
                            }

                            else{

                                String error=databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                            }

                            declineRequestbtn.setEnabled(true);
                        }
                    });
                }


            }
        });


    }
}
