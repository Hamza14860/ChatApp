package com.hamzaazam.i160163_160206;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
//import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    MaterialEditText userName;
    MaterialEditText email;
    MaterialEditText pass;
    Button btnReister;

    FirebaseAuth auth;
    DatabaseReference ref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" Register ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        userName=findViewById(R.id.userName);
        email=findViewById(R.id.email);
        pass=findViewById(R.id.pass);
        btnReister=findViewById(R.id.btnRegister);


        auth=FirebaseAuth.getInstance();


        btnReister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txtUserName=userName.getText().toString();
                String txtPass=pass.getText().toString();
                String txtEmail=email.getText().toString();

                if(TextUtils.isEmpty(txtUserName) || TextUtils.isEmpty(txtPass) || TextUtils.isEmpty(txtEmail)){
                    Toast.makeText(RegisterActivity.this,"No Empty Fields Allowed ",Toast.LENGTH_SHORT).show();

                }
                else if (txtPass.length()<5){
                    Toast.makeText(RegisterActivity.this," Minimum Password Length is 5 Characters ",Toast.LENGTH_SHORT).show();
                }
                else{
                    registerAccount(txtUserName,txtEmail,txtPass);
                }

            }
        });
    }

    private void registerAccount(final String uName, String uEmail, String uPass){
        auth.createUserWithEmailAndPassword(uEmail,uPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser firebaseUser=auth.getCurrentUser();
                    assert firebaseUser !=null;
                    String userId=firebaseUser.getUid();

                    ref=FirebaseDatabase.getInstance().getReference("Users").child(userId);

                    HashMap<String,String> hashMap=new HashMap<>();
                    //NOTE: YOUR USER MODEL SHOULD HAVE SAME NAMES AS HERE IN HASHMAP
                    hashMap.put("id",userId);
                    hashMap.put("username",uName);
                    hashMap.put("imageURL", "default");
                    hashMap.put("status","offline");
                    hashMap.put("search",uName.toLowerCase());


                    ref.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                                Intent intent=new Intent(RegisterActivity.this, Main2Activity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                        }
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, "Cant Register",Toast.LENGTH_LONG).show();
                    Log.e("ERROR REGISTER", "createUserWithEmail:failure", task.getException());
                }
            }
        });

    }
}
