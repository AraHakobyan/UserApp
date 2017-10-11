package com.example.aropc.myapplication.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.aro_pc.myapplication.R;
import com.example.aropc.myapplication.BaseActivity;
import com.example.aropc.myapplication.Consts;
import com.example.aropc.myapplication.UserApp;
import com.example.aropc.myapplication.custom_view.MyEditText;
import com.example.aropc.myapplication.helper.SharedPreferanceHelper;
import com.example.aropc.myapplication.helper.UserHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.aropc.myapplication.Consts.DATABASE_NAME;
import static com.example.aropc.myapplication.Consts.UID;
import static com.example.aropc.myapplication.Consts.USER_MODEL_IMAGEURL;
import static com.example.aropc.myapplication.Consts.USER_MODEL_LOCATION;
import static com.example.aropc.myapplication.Consts.USER_MODEL_NAME;
import static com.example.aropc.myapplication.Consts.USER_MODEL_PASSWORD;
import static com.example.aropc.myapplication.Consts.USER_MODEL_USERNAME;
import static com.example.aropc.myapplication.Consts.USER_MODEL_VOICE;
import static com.example.aropc.myapplication.Consts.USER_MODEL_VOICE_URL;

/**
 * Created by Aro-PC on 7/24/2017.
 */

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private FrameLayout mCreateAccount, mSignIn;
    private MyEditText mUserName, mPassword;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private LinearLayout layoutCreateAccount, layoutlogIn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_layout);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseApp.initializeApp(UserApp.getInstance());
        database = FirebaseDatabase.getInstance();
        mSignIn = (FrameLayout) findViewById(R.id.sign_in_button);
        mCreateAccount = (FrameLayout) findViewById(R.id.create_account_button);
        mUserName = (MyEditText) findViewById(R.id.login_user_name);
        mPassword = (MyEditText) findViewById(R.id.login_password);
        layoutCreateAccount = (LinearLayout) findViewById(R.id.login_new_account);
        layoutlogIn = (LinearLayout) findViewById(R.id.login_first_page);

        mCreateAccount.setOnClickListener(this);
        mSignIn.setOnClickListener(this);


        isLogedIn();
    }

    private void isLogedIn() {
        sharedPreferences = getApplicationContext().getSharedPreferences(Consts.SHARED_PREFERANCES_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        SharedPreferanceHelper.getInstance().setSharedPreferences(sharedPreferences);
        SharedPreferanceHelper.getInstance().setEditor(editor);

        if (UserApp.readFromFile(getApplicationContext(),Consts.FILE_NAME_USER_LOGIN).equals(true)){

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
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
//                            SharedPreferanceHelper.getInstance().saveBoolean(SHARED_PREFERANCES_LOGEDIN,true);
//                            SharedPreferanceHelper.getInstance().saveString(SHARED_PREFERANCES_UID,user.getUid());



                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else {

                            Toast.makeText(getApplicationContext(), " try again", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
                break;
            case R.id.create_account_button:

//                layoutlogIn.setVisibility(View.GONE);
//                layoutCreateAccount.setVisibility(View.VISIBLE);

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
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_NAME).setValue(userName);
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_USERNAME).setValue(userName);
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_PASSWORD).setValue(password);
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_LOCATION).setValue("location");
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_VOICE).setValue("voice");
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_IMAGEURL).setValue(Consts.USER_MODEL_IMAGEURL_VALUE);
        database.getReference(DATABASE_NAME).child(uid).child(UID).setValue(uid);
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_VOICE_URL).setValue(USER_MODEL_VOICE_URL);

//        SharedPreferanceHelper.getInstance().saveBoolean(SHARED_PREFERANCES_LOGEDIN,true);
//        SharedPreferanceHelper.getInstance().saveString(SHARED_PREFERANCES_UID,user.getUid());

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);

    }


}
