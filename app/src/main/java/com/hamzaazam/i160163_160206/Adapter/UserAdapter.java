package com.hamzaazam.i160163_160206.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hamzaazam.i160163_160206.Chat;
import com.hamzaazam.i160163_160206.MessageActivity;
import com.hamzaazam.i160163_160206.R;
import com.hamzaazam.i160163_160206.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context mContext;
    private List<User> mUsers;

    private boolean ischat;

    String theLastMessage;

    public UserAdapter(Context context, List<User> usersList, boolean ischat){
        this.mContext=context;
        this.mUsers=usersList;
        this.ischat=ischat;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.user_item,parent, false);
        return new UserAdapter.UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        final User user=mUsers.get(position);
        holder.userName.setText(user.getUsername());
        if(user.getImageURL().equals("default")){
            holder.profileImage.setImageResource(R.mipmap.contact_photo_def);
        }
        else{
            Glide.with(mContext).load(user.getImageURL()).into(holder.profileImage);
            //Picasso.get().load(user.getImageURL()).fit().centerCrop().into(holder.profileImage);
        }

        if(ischat){
            lastMessage(user.getId(), holder.lastMsg);
        }
        else {
            holder.lastMsg.setVisibility(View.GONE);
        }


        ////status updating
        if (ischat){
            if(user.getStatus().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            }
            else{
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        }
        else{
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid",user.getId());
                mContext.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder{
        public ImageView profileImage;
        public TextView userName;
        private ImageView img_on;
        private ImageView img_off;
        private TextView lastMsg;


        public UserViewHolder( View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.usernameItem);
            profileImage=itemView.findViewById(R.id.profileImageItem);
            img_on=itemView.findViewById(R.id.img_on);
            img_off=itemView.findViewById(R.id.img_off);
            lastMsg=itemView.findViewById(R.id.lastMsg);


        }
    }

    //checking for last message
    private void lastMessage(final String userid, final TextView last_msg){
        theLastMessage="default";
        final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Chats");

        if (firebaseUser!=null) {
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Chat chat = snapshot.getValue(Chat.class);
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)
                                || chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                            theLastMessage = chat.getMessage();
                        }
                    }

                    switch (theLastMessage) {
                        case "default":
                            last_msg.setText("No Message");
                            break;

                        default:
                            last_msg.setText(theLastMessage);
                            break;

                    }
                    theLastMessage = "default";
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }
}
