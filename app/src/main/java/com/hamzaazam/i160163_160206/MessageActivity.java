package com.hamzaazam.i160163_160206;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
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

    /////////////sharing image
    ImageView msgImg;
    Button btnUploadImg;
    StorageReference storageReference;
    private static final int IMAGE_REQUEST=1;
    private Uri imageUri;
    private StorageTask uploadTask;
    String mUriTBU;
    ///////////////////////

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
                    sendMessage(fUser.getUid(), userID, messageT, mUriTBU);

                }
                else {
                    Toast.makeText(MessageActivity.this,"Cant Send Empty Message",Toast.LENGTH_SHORT).show();
                }
                messageSend.setText("");
            }
        });



        ///////upload image
        btnUploadImg=findViewById(R.id.uploadImgg);
        msgImg=findViewById(R.id.msgImg);
        storageReference= FirebaseStorage.getInstance().getReference("uploads");

        btnUploadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadSelectImage();
            }
        });
        /////////

        reference= FirebaseDatabase.getInstance().getReference("Users").child(userID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user=dataSnapshot.getValue(User.class);
                userName.setText(user.getUsername());
                if (user.getImageURL().equals("default")){
                    profileImage.setImageResource(R.mipmap.contact_photo_def);
                }
                else{
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profileImage);
                }


                receiveMessages(fUser.getUid(), userID, user.getImageURL(),mUriTBU);
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

    private void sendMessage(String sender, final String receiver, String message, String msgImgURL){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("isseen",false);
        if(msgImgURL==null || msgImgURL.equals("")){
            hashMap.put("msgImageURL","noImage");
        }
        else{
            //Toast.makeText(MessageActivity.this," Msg Send with Image",Toast.LENGTH_LONG).show();///
            hashMap.put("msgImageURL",msgImgURL);
        }


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

    private void receiveMessages(final String myid, final String userid, final String imageurl, final String msgImgURL){
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
                    messageAdapter=new MessageAdapter(MessageActivity.this, mChat, imageurl,msgImgURL);
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



    /////////////upload image

    private void uploadSelectImage(){

        Intent imageIntent=new Intent();
        imageIntent.setType("image/*");
        imageIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(imageIntent,IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver=MessageActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd=new ProgressDialog(MessageActivity.this);
        pd.setMessage("Uploading");
        pd.show();

        if(imageUri!=null){
            final StorageReference fileReference=storageReference.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));
            uploadTask=fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri=task.getResult();
                        String mUri=downloadUri.toString();

                        mUriTBU=mUri;

                        //reference=FirebaseDatabase.getInstance().getReference("Chats").child(fuser.getUid());

                        //HashMap <String ,Object>map =new HashMap<>();
                        //map.put("imageURL",mUri);
                        //reference.updateChildren(map);
                        Toast.makeText(MessageActivity.this," Image Selected.. Write Text and Send",Toast.LENGTH_LONG).show();///

                        pd.dismiss();
                    }
                    else {
                        Toast.makeText(MessageActivity.this,"Failed..",Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MessageActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    pd.dismiss();

                }
            });
        }else{
            Toast.makeText(MessageActivity.this,"No Image Selected..",Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imageUri=data.getData();

            if(uploadTask!=null && uploadTask.isInProgress()){
                Toast.makeText(MessageActivity.this,"Uploading..",Toast.LENGTH_SHORT).show();
            }
            else{
                uploadImage();
            }

        }
    }

    //////////////////////
}
