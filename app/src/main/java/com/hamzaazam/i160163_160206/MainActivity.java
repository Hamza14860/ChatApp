package com.hamzaazam.i160163_160206;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    //startActivity


    Button loginButton;
    Button registerButton;

    FirebaseUser firebaseUser;

    ImageButton tvAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerButton=findViewById(R.id.registerM);
        loginButton=findViewById(R.id.loginM);
        tvAbout=findViewById(R.id.btnAbout);

        tvAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this," Devs: Hamza, Hammad, Huzaifa, Aleem",Toast.LENGTH_SHORT).show();
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,RegisterActivity.class));

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){//checking if user is null
            Intent changeIntent= new Intent(MainActivity.this,Main2Activity.class);
            startActivity(changeIntent);
            finish();
        }
    }
}
