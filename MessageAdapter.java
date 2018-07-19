package com.example.user.nasrinchatapp;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.user.nasrinchatapp.ChatActivity.AES;
import static com.example.user.nasrinchatapp.ChatActivity.ch;
import static com.example.user.nasrinchatapp.ChatActivity.decryptedText;
import static com.example.user.nasrinchatapp.ChatActivity.generateKey;
import static com.example.user.nasrinchatapp.ChatActivity.i;
import static com.example.user.nasrinchatapp.ChatActivity.input;
import static com.example.user.nasrinchatapp.ChatActivity.j;
import static com.example.user.nasrinchatapp.ChatActivity.p;

/**
 * Created by user on 7/5/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    String encryptedText;

    public MessageAdapter(List<Messages> mMessageList){
        this.mMessageList=mMessageList;
    }




    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);

        return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText=(TextView)itemView.findViewById(R.id.message_text);
            profileImage=(CircleImageView)itemView.findViewById(R.id.message_profile);
        }
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {

        mAuth= FirebaseAuth.getInstance();
        String current_user_id= mAuth.getCurrentUser().getUid();

        Messages c= mMessageList.get(position);

        String from_user= c.getFrom();
        if (from_user.equals(current_user_id)){

            holder.messageText.setBackgroundColor(Color.LTGRAY);
            holder.messageText.setTextColor(Color.BLACK);

            holder.profileImage.setVisibility(View.INVISIBLE);

        }else {

            holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.WHITE);

        }

         encryptedText= c.getMessage();
        try {
            decryptedText= decrypt(encryptedText, input);

        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.messageText.setText(decryptedText);

    }



    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    private String decrypt(String encryptString, String password) throws Exception {
        SecretKeySpec key= generateKey(password);
        Cipher c= Cipher.getInstance(AES);
        c.init(Cipher.DECRYPT_MODE,key);
        byte[] decodedVal= Base64.decode(encryptString, Base64.DEFAULT);
        byte[] decValue= c.doFinal(decodedVal);
        String decryptedValue= new String(decValue);
        return decryptedValue;

    }

}
