package com.boilerplate.music.musicrecognition;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private CircleImageView mProfileImage;
    private EditText mName;
    private EditText mEmail;
    private EditText mPassword;
    private Button mRegister;
    private Button mLogin;
    private ProgressBar mProgressBar;

    private Uri imageUri;

    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private FirebaseFirestore mFireStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        imageUri = null;

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference().child("profile_images");
        mFireStore = FirebaseFirestore.getInstance();

        mProfileImage = findViewById(R.id.register_image_btn);
        mName = findViewById(R.id.reg_name);
        mEmail = findViewById(R.id.reg_email);
        mPassword = findViewById(R.id.reg_pas);
        mRegister = findViewById(R.id.reg_btn);
        mLogin = findViewById(R.id.reg_login_btn);
        mProgressBar = findViewById(R.id.reg_progress_bar);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();

            }
        });

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent image_intent = new Intent();
                image_intent.setType("image/*");
                image_intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(image_intent, "Select Picture"), PICK_IMAGE);

            }
        });

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgressBar.setVisibility(View.VISIBLE);

                final String name = mName.getText().toString();
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

                    if (imageUri != null) {

                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    final String user_id = mAuth.getCurrentUser().getUid();

                                    Toasty.info(RegisterActivity.this, "Uploading User Image", Toast.LENGTH_LONG, true).show();

                                    StorageReference user_profile = mStorageRef.child(user_id + ".jpg");
                                    user_profile.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                            if (task.isSuccessful()) {

                                                final String download_uri = task.getResult().getDownloadUrl().toString();

                                                Map<String, Object> userMap = new HashMap<>();
                                                userMap.put("name", name);
                                                userMap.put("image", download_uri);

                                                mFireStore.collection("Users").document(user_id).set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        Toasty.success(RegisterActivity.this, "User Successfully Created", Toast.LENGTH_LONG, true).show();

                                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                                        startActivity(mainIntent);
                                                        finish();

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                        mProgressBar.setVisibility(View.INVISIBLE);

                                                        Toasty.error(RegisterActivity.this, "User could not be Created", Toast.LENGTH_LONG, true).show();

                                                    }
                                                });

                                                mProgressBar.setVisibility(View.INVISIBLE);

                                                mProgressBar.setVisibility(View.INVISIBLE);

                                            } else {

                                                mProgressBar.setVisibility(View.INVISIBLE);

                                                Toasty.error(RegisterActivity.this, "Failed to Upload Image", Toast.LENGTH_LONG, true).show();


                                            }

                                            mProgressBar.setVisibility(View.INVISIBLE);

                                        }


                                    });

                                } else {

                                    mProgressBar.setVisibility(View.INVISIBLE);

                                    Toasty.error(RegisterActivity.this, "Account Creation Failed", Toast.LENGTH_LONG, true).show();

                                }

                            }
                        });


                    } else {

                        mProgressBar.setVisibility(View.INVISIBLE);

                        Toasty.warning(RegisterActivity.this, "Please Select An Image", Toast.LENGTH_LONG, true).show();

                    }


                } else {

                    mProgressBar.setVisibility(View.INVISIBLE);

                    Toasty.warning(RegisterActivity.this, "All Fields Are Mandatory", Toast.LENGTH_LONG, true).show();
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE) {

            imageUri = data.getData();
            mProfileImage.setImageURI(imageUri);

        }
    }
}
