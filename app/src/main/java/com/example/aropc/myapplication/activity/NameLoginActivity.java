package com.example.aropc.myapplication.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;

import com.example.aro_pc.myapplication.R;
import com.example.aropc.myapplication.BaseActivity;
import com.example.aropc.myapplication.Consts;
import com.example.aropc.myapplication.UserApp;
import com.example.aropc.myapplication.custom_view.MyEditText;
import com.example.aropc.myapplication.helper.SharedPreferanceHelper;
import com.example.aropc.myapplication.services.BackgroundService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

import static com.example.aropc.myapplication.Consts.DATABASE_NAME;
import static com.example.aropc.myapplication.Consts.UID;
import static com.example.aropc.myapplication.Consts.USER_MODEL_IMAGEURL;
import static com.example.aropc.myapplication.Consts.USER_MODEL_LOCATION;
import static com.example.aropc.myapplication.Consts.USER_MODEL_NAME;
import static com.example.aropc.myapplication.Consts.USER_MODEL_PASSWORD;
import static com.example.aropc.myapplication.Consts.USER_MODEL_USERNAME;
import static com.example.aropc.myapplication.Consts.USER_MODEL_VOICE;
import static com.example.aropc.myapplication.Consts.USER_MODEL_VOICE_URL;
import static com.example.aropc.myapplication.Consts.UUIDS;

/**
 * Created by Aro-PC on 10/11/2017.
 */

public class NameLoginActivity extends BaseActivity implements View.OnClickListener {
    private MyEditText nameEditText;
    private FrameLayout button;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private String userName;
    private String uid;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.name_layout_activity);
        nameEditText = (MyEditText) findViewById(R.id.user_name_edit_text_id);
        button = (FrameLayout) findViewById(R.id.name_login_next_button);

        button.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        sharedPreferences = getApplicationContext().getSharedPreferences(Consts.SHARED_PREFERANCES_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        SharedPreferanceHelper.getInstance().setSharedPreferences(sharedPreferences);
        SharedPreferanceHelper.getInstance().setEditor(editor);

        if (UserApp.readFromFile(getApplicationContext(),Consts.FILE_NAME_USER_LOGIN).equals(true)){
            uid = UserApp.readFromFile(getApplicationContext(),Consts.FILE_NAME_UID);

            Intent intent = new Intent(NameLoginActivity.this, MainActivity.class);
            startActivity(intent);
        } else {
            createUser();
            Intent intent = new Intent(getApplicationContext(), BackgroundService.class);
            startService(intent);
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.name_login_next_button:
//                createUser();
//                createUser();
                changeName();
                break;
        }
    }

    private void changeName() {
        userName = nameEditText.getText().toString();
        if (!userName.equals("")){
            database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_USERNAME).setValue(userName);
        }
    }

    private void hasAlreadySignIn() {

    }

    private void createUser() {
        Random random = new Random();

        uid = String.valueOf(random.nextInt(1382458769));
        database.getReference(DATABASE_NAME).child(UUIDS).child(uid).setValue(uid);


        database.getReference(DATABASE_NAME).child(uid).setValue(uid);
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_NAME).setValue(userName);
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_USERNAME).setValue(userName);
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_PASSWORD).setValue("password");
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_LOCATION).setValue("location");
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_VOICE).setValue("voice");
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_IMAGEURL).setValue(Consts.USER_MODEL_IMAGEURL_VALUE);
        database.getReference(DATABASE_NAME).child(uid).child(UID).setValue(uid);
        database.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_VOICE_URL).setValue(USER_MODEL_VOICE_URL);

        UserApp.writeToFile(Consts.FILE_NAME_UID,uid,getApplicationContext());
        UserApp.writeToFile(Consts.FILE_NAME_USER_LOGIN,"true",getApplicationContext());



        Intent intent = new Intent(NameLoginActivity.this, MainActivity.class);
        startActivity(intent);
    }


}
