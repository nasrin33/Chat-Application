package com.example.user.nasrinchatapp;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.attr.x;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser;

    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    private TextView chatDisplayname;
    private TextView LastSeen;
    private CircleImageView FriendsProfileImage;

    private ImageButton addImage;
    private ImageButton sendBtn;
    private EditText enterMsg;

    private RecyclerView mMessagesList;

    private final List<Messages> messagesList= new ArrayList<>();
    private LinearLayoutManager mLinearLayout;

    private MessageAdapter mAdapter;

   static String  AES= "AES";
    final static String input = "abcd";

    public static String message, message1, decryptedText;
    public static int i,j;
    public  static char p[]={'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
    public static char ch[]={'b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','a'};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //activity_main=(RelativeLayout)findViewById(R.id.activity_main);

        addImage=(ImageButton)findViewById(R.id.add_pic);
        sendBtn=(ImageButton)findViewById(R.id.send_btn);
        enterMsg=(EditText)findViewById(R.id.enter_message);

        mRootRef= FirebaseDatabase.getInstance().getReference();

        mChatUser=getIntent().getStringExtra("user_id");
        String userName=getIntent().getStringExtra("user_name");

        mChatToolbar=(Toolbar)findViewById(R.id.chat_app_bar);

        setSupportActionBar(mChatToolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mAuth=FirebaseAuth.getInstance();
        mCurrentUserId=mAuth.getCurrentUser().getUid();

        mAdapter= new MessageAdapter(messagesList);

        mMessagesList=(RecyclerView)findViewById(R.id.message_list);
        mLinearLayout=new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);

        loadMessages();

        //getSupportActionBar().setTitle(userName);

        LayoutInflater inflater= (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view=inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        //-----------------------Custom Action Bar item-----------------------------------

        chatDisplayname=(TextView)findViewById(R.id.chat_display_name);
        LastSeen=(TextView)findViewById(R.id.chat_last_seen);
        FriendsProfileImage=(CircleImageView)findViewById(R.id.chat_image);

        chatDisplayname.setText(userName);

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online=dataSnapshot.child("online").getValue().toString();
                final String image=dataSnapshot.child("image").getValue().toString();

                if (online.equals("true")){
                    LastSeen.setText("Online");
                }

                else
                {
                    GetTimeAgo getTimeAgo= new GetTimeAgo();

                    long LastTime= Long.parseLong(online);
                    String lastSeenTime= getTimeAgo.getTimeAgo(LastTime, getApplicationContext() );
                    LastSeen.setText(lastSeenTime);
                }

                Picasso.with(ChatActivity.this).load(image).placeholder(R.drawable.avatar)
                        .networkPolicy(NetworkPolicy.OFFLINE).into(FriendsProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                        Picasso.with(ChatActivity.this).load(image).placeholder(R.drawable.avatar).into(FriendsProfileImage);

                    }
                });



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mChatUser)){
                    Map chatAddMap= new HashMap();

                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap= new HashMap();

                    chatUserMap.put("Chat/" +mCurrentUserId+ "/" +mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" +mChatUser+ "/" +mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null){
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }

                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();
            }
        });


    }

    private void loadMessages() {

        mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message= dataSnapshot.getValue(Messages.class);

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void sendMessage() {

        message1= enterMsg.getText().toString();
        try {
            message = encrypt(message1, input);

        } catch (Exception e) {
            e.printStackTrace();
        }


        if (!TextUtils.isEmpty(message)){

            String current_user_ref= "messages/" +mCurrentUserId+ "/" +mChatUser;
            String chat_user_ref= "messages/" +mChatUser+ "/" +mCurrentUserId;

            DatabaseReference user_message_push= mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();
            String push_id= user_message_push.getKey();

            Map messageMap= new HashMap();

            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map messageUserMap= new HashMap();
            messageUserMap.put(current_user_ref +"/"+ push_id, messageMap );
            messageUserMap.put(chat_user_ref +"/"+ push_id, messageMap);

            enterMsg.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if (databaseError != null){
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }
                }
            });
        }
    }

    private String encrypt(String data, String password) throws Exception{
        SecretKeySpec key= generateKey(password);
        Cipher c= Cipher.getInstance(AES);
        c.init(Cipher.ENCRYPT_MODE,key);
        byte[] encVal= c.doFinal(data.getBytes());
        String encrtyptedValue= Base64.encodeToString(encVal,Base64.DEFAULT);
        return encrtyptedValue;

    }

    public static SecretKeySpec generateKey(String password) throws Exception  {

        final MessageDigest digest= MessageDigest.getInstance("SHA-256");
        byte[] bytes= password.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key= digest.digest();
        SecretKeySpec secretKeySpec= new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }
}