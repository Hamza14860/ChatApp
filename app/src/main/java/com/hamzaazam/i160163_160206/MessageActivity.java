package com.hamzaazam.i160163_160206;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hamzaazam.i160163_160206.Adapter.MessageAdapter;
import com.hamzaazam.i160163_160206.Fragments.APIService;
import com.hamzaazam.i160163_160206.Notifications.Client;
import com.hamzaazam.i160163_160206.Notifications.Data;
import com.hamzaazam.i160163_160206.Notifications.MyResponse;
import com.hamzaazam.i160163_160206.Notifications.Sender;
import com.hamzaazam.i160163_160206.Notifications.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profileImage;
    TextView userName;

    FirebaseUser fUser;
    DatabaseReference reference;

    Intent intent;


    ImageButton sendBtn;
    EditText messageSend;

    /////////
    MessageAdapter messageAdapter;
    List<Chat> mChat;
    RecyclerView recyclerViewMessage;
    //////////

    /////seen message
    ValueEventListener seenListener;
    //////
    String userid_;


    /////for notifications
    APIService apiService;

    boolean notify=false;
    /////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //finish();
                startActivity(new Intent(MessageActivity.this,Main2Activity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });


        ////////
        apiService= Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        ///////

        recyclerViewMessage=findViewById(R.id.recyclerViewMessages);
        recyclerViewMessage.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewMessage.setLayoutManager(linearLayoutManager);

        profileImage=findViewById(R.id.profileImage);
        userName=findViewById(R.id.userName);

        sendBtn=findViewById(R.id.btnSend);
        messageSend=findViewById(R.id.txtSend);

        intent=getIntent();
        /////////
        final String userID=intent.getStringExtra("userid");
        userid_=userID;
        //////////

        fUser= FirebaseAuth.getInstance().getCurrentUser();


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /////send notification
                notify=true;
                ////
                String messageT=messageSend.getText().toString();
                if(!messageT.equals("")){
                    sendMessage(fUser.getUid(), userID,messageT);
                }
                else {
                    Toast.makeText(MessageActivity.this,"Cant Send Empty Message",Toast.LENGTH_SHORT).show();
                }
                messageSend.setText("");
            }
        });

        reference= FirebaseDatabase.getInstance().getReference("Users").child(userID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user=dataSnapshot.getValue(User.class);
                userName.setText(user.getUsername());
                if (user.getImageURL().equals("default")){
                    profileImage.setImageResource(R.mipmap.ic_launcher);
                }
                else{
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profileImage);
                }
                receiveMessages(fUser.getUid(), userID, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(userID);

    }

    ///seen message
    private void seenMessage(final String userid){
        reference=FirebaseDatabase.getInstance().getReference("Chats");

        seenListener=reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Chat chat=snapshot.getValue(Chat.class);

                    if(chat.getReceiver().equals(fUser.getUid()) && chat.getSender().equals(userid)){
                        HashMap <String, Object> hashMap=new HashMap<>();
                        hashMap.put("isseen",true);

                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, final String receiver, String message){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("isseen",false);


        reference.child("Chats").push().setValue(hashMap);

        //Add user to chat fragment
        final DatabaseReference chatref=FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fUser.getUid())
                .child(userid_);

        chatref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatref.child("id").setValue(userid_);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //////send notifications
        final String msg= message;

        reference=FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user=dataSnapshot.getValue(User.class);
                if(notify) {
                    sendNotification(receiver, user.getUsername(), msg);
                }
                notify=false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        /////
    }



    private void sendNotification(String receiver, final String username, final String message){
        DatabaseReference tokens=FirebaseDatabase.getInstance().getReference("Tokens");
        Query query=tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Token token=snapshot.getValue(Token.class);
                    Data data=new Data(fUser.getUid(),R.mipmap.ic_launcher, username+": "+message, "New Message",userid_);

                    Sender sender=new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                        .enqueue(new Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                if(response.code()== 200){
                                    if(response.body().success !=1){
                                        Toast.makeText(MessageActivity.this, "Failed!",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {

                            }
                        });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void receiveMessages(final String myid, final String userid, final String imageurl){
        mChat=new ArrayList<>();

        reference=FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Chat chat=snapshot.getValue(Chat.class);

                    if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid)
                            || chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mChat.add(chat);

                    }

                    messageAdapter=new MessageAdapter(MessageActivity.this, mChat, imageurl);
                    recyclerViewMessage.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void status(String status){
        reference=FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());
        HashMap<String ,Object>hashMap=new HashMap<>();
        hashMap.put("status",status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();

        ///
        reference.removeEventListener(seenListener);
        ///
        status("offline");
    }
}
