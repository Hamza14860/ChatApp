package com.hamzaazam.i160163_160206.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hamzaazam.i160163_160206.Chat;
import com.hamzaazam.i160163_160206.MessageActivity;
import com.hamzaazam.i160163_160206.R;
import com.hamzaazam.i160163_160206.User;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    public static final int MSG_TYPE_LEFT=0;
    public static final int MSG_TYPE_RIGHT=1;

    private Context mContext;
    private List<Chat> mChat;
    private String imageurl;

    FirebaseUser fuser;

    /////upload image url
    private String msgImgURL;
    ////

    public MessageAdapter(Context context, List<Chat> mChat, String imageurl, String msgImgURL){
        this.mContext=context;
        this.mChat=mChat;
        this.imageurl=imageurl;
        this.msgImgURL=msgImgURL;
    }

    @NonNull
    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.MessageViewHolder(view);
        }
        else{
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MessageViewHolder holder, int position) {
        Chat chat=mChat.get(position);

        holder.showMessage.setText(chat.getMessage());

        if(imageurl.equals("default")){
            holder.profileImage.setImageResource(R.mipmap.contact_photo_def);
        }
        else{
            Glide.with(mContext).load(imageurl).into(holder.profileImage);
        }

        ////
        Log.e("MSG IMG",chat.getMsgImageURL()+" . ");
        if(chat.getMsgImageURL()==null || chat.getMsgImageURL().equals("noImage")){
            holder.msgImg.setVisibility(View.GONE);
        }
        else{
            holder.msgImg.setVisibility(View.VISIBLE);
            //Toast.makeText(mContext,"image sent or received",Toast.LENGTH_SHORT).show();//
            Glide.with(mContext).load(chat.getMsgImageURL()).into(holder.msgImg);
        }
        ////

        //message seen conditions
        if(position==mChat.size()-1){
            if(chat.isIsseen()){
                holder.txtSeen.setText("Seen");
            }
            else {
                holder.txtSeen.setText("Delivered");
            }
        }else{
            holder.txtSeen.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public ImageView profileImage;
        public TextView showMessage;
        public TextView txtSeen;

        ///
        public ImageView msgImg;
        ///

        public MessageViewHolder( View itemView) {
            super(itemView);

            showMessage=itemView.findViewById(R.id.showMessage);
            profileImage=itemView.findViewById(R.id.profileImage);

            txtSeen=itemView.findViewById(R.id.txtSeen);


            msgImg=itemView.findViewById(R.id.msgImg);


        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser= FirebaseAuth.getInstance().getCurrentUser();
        if(mChat.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }
}
