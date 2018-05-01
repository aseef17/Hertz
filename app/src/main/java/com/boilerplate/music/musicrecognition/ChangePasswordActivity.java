package com.boilerplate.music.musicrecognition;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;




import es.dmoral.toasty.Toasty;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText current_password;
    private EditText new_password;
    private EditText new_password_confirm;
    private Button change_paassword_button;
    private ProgressBar change_password_progressBar;

    private Toolbar change_password_toolbar;

    private FirebaseAuth firebaseAuth;

    private String user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        firebaseAuth = FirebaseAuth.getInstance();

        change_password_toolbar = findViewById(R.id.change_pass_toolbar);
        setSupportActionBar(change_password_toolbar);
        getSupportActionBar().setTitle("Change Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String user_mail = firebaseAuth.getCurrentUser().getEmail();

        current_password = findViewById(R.id.change_pass_current);
        new_password = findViewById(R.id.change_pass_new);//
        new_password_confirm = findViewById(R.id.change_pass_new_confirm);
        change_paassword_button = findViewById(R.id.change_pass_btn);
        change_password_progressBar = findViewById(R.id.change_pass_progress);

        change_password_progressBar.setVisibility(View.INVISIBLE);

        change_paassword_button.setEnabled(true);

        change_paassword_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String current_pass = current_password.getText().toString();
                String new_pass = new_password.getText().toString();
                final String confrim_new_pass = new_password_confirm.getText().toString();

                if(!TextUtils.isEmpty(current_pass) && !TextUtils.isEmpty(new_pass) && !TextUtils.isEmpty(confrim_new_pass) ) {

                    if(confrim_new_pass.equals(new_pass)) {

                        if (!new_pass.equals(current_pass)) {

                            change_password_progressBar.setVisibility(View.VISIBLE);

                            change_paassword_button.setEnabled(false);

                            AuthCredential credential = EmailAuthProvider
                                    .getCredential(user_mail, current_pass);


                            firebaseAuth.getCurrentUser().reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                firebaseAuth.getCurrentUser().updatePassword(confrim_new_pass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {

                                                        /*Toast.makeText(ChangePasswordActivity.this, "Password changed Successfully", Toast.LENGTH_LONG).show();*/

                                                            Toasty.success(ChangePasswordActivity.this, "Password changed Successfully!", Toast.LENGTH_LONG, true).show();

                                                            finish();

                                                        } else {

                                                            change_paassword_button.setEnabled(true);

                                                        /*Toast.makeText(ChangePasswordActivity.this, "ERROR : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();*/
                                                            Toasty.error(ChangePasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG, true).show();

                                                        }
                                                    }
                                                });
                                            } else {

                                                change_paassword_button.setEnabled(true);

                                            /*Toast.makeText(ChangePasswordActivity.this, "ERROR : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();*/
                                                Toasty.error(ChangePasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG, true).show();

                                            }

                                            change_password_progressBar.setVisibility(View.INVISIBLE);

                                        }
                                    });

                        } else {

                            change_paassword_button.setEnabled(true);

                            change_password_progressBar.setVisibility(View.INVISIBLE);

                        /*Toast.makeText(ChangePasswordActivity.this, "Current & New Password cannot be the same", Toast.LENGTH_LONG).show();*/

                            Toasty.warning(ChangePasswordActivity.this, "Current & New Password cannot be the same", Toast.LENGTH_LONG, true).show();
                        }

                    } else {

                        change_paassword_button.setEnabled(true);

                        change_password_progressBar.setVisibility(View.INVISIBLE);

                        Toasty.warning(ChangePasswordActivity.this, "Passwords don't match", Toast.LENGTH_LONG, true).show();


                    }

                } else {

                    change_paassword_button.setEnabled(true);

                    change_password_progressBar.setVisibility(View.INVISIBLE);

                    /*Toast.makeText(ChangePasswordActivity.this, "Passwords don't match", Toast.LENGTH_LONG).show();*/

                    Toasty.warning(ChangePasswordActivity.this, "All Fields are Mandatory", Toast.LENGTH_LONG, true).show();

                }

            }
        });

    }
}





