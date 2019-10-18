package com.hamzaazam.i160163_160206;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

public class LoginActivity extends AppCompatActivity {

    MaterialEditText email;
    MaterialEditText pass;
    Button btnLogin;

    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" Login ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        email=findViewById(R.id.email);
        pass=findViewById(R.id.pass);
        btnLogin=findViewById(R.id.btnLogin);

        auth= FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txtEmail=email.getText().toString();
                String txtPass=pass.getText().toString();

                if( TextUtils.isEmpty(txtPass) || TextUtils.isEmpty(txtEmail)){
                    Toast.makeText(LoginActivity.this,"No Empty Fields Allowed ",Toast.LENGTH_SHORT).show();

                }
                else{
                    auth.signInWithEmailAndPassword(txtEmail,txtPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Intent loginIntent=new Intent(LoginActivity.this,Main2Activity.class);
                                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(loginIntent);
                                finish();
                            }else{
                                Toast.makeText(LoginActivity.this,"Login Failed ",Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }


            }
        });



    }
}
