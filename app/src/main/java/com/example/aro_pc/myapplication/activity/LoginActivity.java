package com.example.aro_pc.myapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.aro_pc.myapplication.BaseActivity;
import com.example.aro_pc.myapplication.R;
import com.example.aro_pc.myapplication.custom_view.MyEditText;
import com.example.aro_pc.myapplication.helper.UserHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.aro_pc.myapplication.Consts.DATABASE_NAME;
import static com.example.aro_pc.myapplication.Consts.UID;
import static com.example.aro_pc.myapplication.Consts.USER_MODEL_IMAGEURL;
import static com.example.aro_pc.myapplication.Consts.USER_MODEL_LOCATION;
import static com.example.aro_pc.myapplication.Consts.USER_MODEL_NAME;
import static com.example.aro_pc.myapplication.Consts.USER_MODEL_PASSWORD;
import static com.example.aro_pc.myapplication.Consts.USER_MODEL_USERNAME;
import static com.example.aro_pc.myapplication.Consts.USER_MODEL_VOICE;
import static com.example.aro_pc.myapplication.Consts.USER_MODEL_VOICE_URL;

/**
 * Created by Aro-PC on 7/24/2017.
 */

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private Button mCreateAccount, mSignIn;
    private MyEditText mUserName, mPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_layout);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance();
        mSignIn = (Button) findViewById(R.id.sign_in_button);
        mCreateAccount = (Button) findViewById(R.id.create_account_button);
        mUserName = (MyEditText) findViewById(R.id.login_user_name);
        mPassword = (MyEditText) findViewById(R.id.login_password);

        mCreateAccount.setOnClickListener(this);
        mSignIn.setOnClickListener(this);

    }



    private FirebaseUser user;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                firebaseAuth.signInWithEmailAndPassword(mUserName.getText().toString(), mPassword.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user = firebaseAuth.getCurrentUser();

//                            createUserInDatabase(user.getUid(), mUserName.getText().toString(), mPassword.getText().toString());

                            UserHelper.getInstance().getUserModel().setUid(user.getUid());

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else {

                            Toast.makeText(getApplicationContext(), " try again", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
                break;
            case R.id.create_account_button:
                firebaseAuth.createUserWithEmailAndPassword(mUserName.getText().toString(), mPassword.getText().toString()).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("login", "userName : " + mUserName.getText().toString() + " password : " + mPassword.getText().toString());
                        if (task.isSuccessful()) {

                            user = firebaseAuth.getCurrentUser();
                            UserHelper.getInstance().getUserModel().setUid(user.getUid());
                            createUserInDatabase(user.getUid(), mUserName.getText().toString(), mPassword.getText().toString());

                        } else {
                            Toast.makeText(getApplicationContext(), " try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
        }

    }

    private void createUserInDatabase(final String uid, final String userName, final String password) {

        database.getReference(DATABASE_NAME).child(uid).setValue(uid);
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_NAME).setValue("user");
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_USERNAME).setValue(userName);
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_PASSWORD).setValue(password);
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_LOCATION).setValue("location");
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_VOICE).setValue("voice");
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_IMAGEURL).setValue("image");
        database.getReference(DATABASE_NAME).child(uid).child(UID).setValue(uid);
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_VOICE_URL).setValue("voiceUrl");

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);

    }


}
