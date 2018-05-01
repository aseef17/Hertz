package com.boilerplate.music.musicrecognition;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPass;
    private Button mLoginBtn;
    private Button mRegBtn;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser current_user = mAuth.getCurrentUser();
        if (current_user != null) {

            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        mEmail = findViewById(R.id.login_email);
        mPass = findViewById(R.id.login_pas);
        mLoginBtn = findViewById(R.id.login_btn);
        mRegBtn = findViewById(R.id.login_register_btn);
        mProgressBar = findViewById(R.id.login_progress);

        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent regIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(regIntent);
                finish();

            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgressBar.setVisibility(View.VISIBLE);

                final String email = mEmail.getText().toString();
                final String pass = mPass.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)) {

                    mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {

                                final String token_id = FirebaseInstanceId.getInstance().getToken();
                                final String current_id = mAuth.getCurrentUser().getUid();


                                firebaseFirestore.collection("Users").document(mAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                                        String token_in_firebase = documentSnapshot.getString("token_id");

                                        if(token_in_firebase.equals("")) {

                                            Map<String, Object> tokenMap = new HashMap<>();
                                            tokenMap.put("token_id", token_id);

                                            firebaseFirestore.collection("Users").document(current_id).update(tokenMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    Toasty.success(LoginActivity.this, "Successfully Logged In", Toast.LENGTH_SHORT, true).show();

                                                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                                    startActivity(mainIntent);
                                                    finish();

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    mProgressBar.setVisibility(View.INVISIBLE);

                                                    Toasty.error(LoginActivity.this, "ERROR :" + e.getMessage(), Toast.LENGTH_LONG, true).show();

                                                }
                                            });

                                        } else {

                                            mProgressBar.setVisibility(View.INVISIBLE);

                                            mAuth.signOut();

                                            Toasty.error(LoginActivity.this, "Account is already logged in a different device , please log out from the other device", Toast.LENGTH_LONG, true).show();


                                        }


                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        mProgressBar.setVisibility(View.INVISIBLE);

                                        Toasty.error(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG, true).show();


                                    }
                                });



                            } else {

                                mProgressBar.setVisibility(View.INVISIBLE);

                                Toasty.error(LoginActivity.this, "ERROR :" + task.getException().getMessage(), Toast.LENGTH_LONG, true).show();

                            }

                            mProgressBar.setVisibility(View.INVISIBLE);

                        }

                    });

                } else {

                    mProgressBar.setVisibility(View.INVISIBLE);

                    Toasty.warning(LoginActivity.this, "Please Enter Your Email & Password", Toast.LENGTH_LONG, true).show();

                }

            }
        });

    }

}
