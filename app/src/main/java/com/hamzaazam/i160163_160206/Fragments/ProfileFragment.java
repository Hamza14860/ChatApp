package com.hamzaazam.i160163_160206.Fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.hamzaazam.i160163_160206.R;
import com.hamzaazam.i160163_160206.User;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    CircleImageView profileImage;
    TextView profileUsername;

    FirebaseUser fuser;
    DatabaseReference reference;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST=1;
    private Uri imageUri;
    private StorageTask uploadTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage=view.findViewById(R.id.profileImage);
        profileUsername=view.findViewById(R.id.tvUsername);

        storageReference= FirebaseStorage.getInstance().getReference("uploads");

        fuser= FirebaseAuth.getInstance().getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user=dataSnapshot.getValue(User.class);
                profileUsername.setText(user.getUsername());

                if(user.getImageURL().equals("default")){
                    profileImage.setImageResource(R.drawable.ic_action_profile_photo);
                }
                else{
                    Glide.with(getContext()).load(user.getImageURL()).into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
            }
        });

        return view;
    }

    private void openImage(){
        Intent imageIntent=new Intent();
        imageIntent.setType("image/*");
        imageIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(imageIntent,IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver=getContext().getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd=new ProgressDialog(getContext());
        pd.setMessage("Uploading");
        pd.show();

        if(imageUri!=null){
            final StorageReference fileReference=storageReference.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));
            uploadTask=fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot,Task<Uri>>() {
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

                        reference=FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

                        HashMap <String ,Object>map =new HashMap<>();
                        map.put("imageURL",mUri);
                        reference.updateChildren(map);

                        pd.dismiss();
                    }
                    else {
                        Toast.makeText(getContext(),"Failed..",Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    pd.dismiss();

                }
            });
        }else{
            Toast.makeText(getContext(),"No Image Selected..",Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imageUri=data.getData();

            if(uploadTask!=null && uploadTask.isInProgress()){
                Toast.makeText(getContext(),"Uploading..",Toast.LENGTH_SHORT).show();
            }
            else{
                uploadImage();
            }

        }
    }
}