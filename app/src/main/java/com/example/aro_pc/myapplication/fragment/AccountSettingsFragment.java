package com.example.aro_pc.myapplication.fragment;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.aro_pc.myapplication.Consts;
import com.example.aro_pc.myapplication.R;
import com.example.aro_pc.myapplication.helper.SharedPreferanceHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.example.aro_pc.myapplication.Consts.ACCOUNT_IMAGE_STORAGE;
import static com.example.aro_pc.myapplication.Consts.USERS_STORAGE_NAME;
import static com.example.aro_pc.myapplication.Consts.USER_MODEL_IMAGEURL;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountSettingsFragment extends Fragment implements View.OnClickListener {

    private ImageView accountImageView;
    private static final int PICK_PHOTO_FOR_AVATAR = 0;
    private StorageReference mStorageRef;
    private FirebaseUser user;
    private StorageReference storageReference;
    private DatabaseReference reference;
    private FirebaseDatabase database;
    private String mFileName;
    private TextView profileUserName, profileUserEmail,profileUserPassword;


    public AccountSettingsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account_settings, container, false);
        accountImageView =  view.findViewById(R.id.account_settings_image_view);
        profileUserName = (TextView) view.findViewById(R.id.user_info_name);
        profileUserEmail = (TextView) view.findViewById(R.id.user_info_mail);
        profileUserPassword = (TextView) view.findViewById(R.id.user_info_password);
        accountImageView.setOnClickListener(this);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference(Consts.DATABASE_NAME).child(user.getUid()).child(Consts.USER_MODEL_VOICE);
        reference.getParent().child(Consts.USER_MODEL_IMAGEURL).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = (String)dataSnapshot.getValue();
                switch (data){
                    case Consts.USER_MODEL_IMAGEURL:
                        accountImageView.setImageResource(R.drawable.ic_account_box_black_24dp);
                        break;
                    default:
                        if (SharedPreferanceHelper.getInstance().getBoolean(Consts.SHARED_PREFERANCES_PROFILE_PICTURE,false)){
                            String absolutePath = SharedPreferanceHelper.getInstance().getString(Consts.SHARED_PREFERANCES_PROFILE_PICTURE_FILE_DIR,"");
                            Bitmap myBitmap = BitmapFactory.decodeFile(absolutePath);
                            accountImageView.setImageBitmap(myBitmap);

                        } else {
                            StorageReference storageReference1 = storageReference.child(USERS_STORAGE_NAME).child(user.getUid()).child(Consts.ACCOUNT_IMAGE_STORAGE);
                            try {
                                downloadFromDatabase(data,storageReference1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        break;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        return view;
    }

    private File imageFile;
    private void downloadFromDatabase(String data, StorageReference storageReference1) throws IOException {
        mFileName = getActivity().getCacheDir().getAbsolutePath();
        mFileName +=  "/" +user.getUid() + "image.jpg";
        imageFile = new File(mFileName);
        imageFile.createNewFile();
        storageReference1.getFile(imageFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Successfully downloaded data to local file
                        // ...
                        if(imageFile.exists()){

                            SharedPreferanceHelper.getInstance().saveBoolean(Consts.SHARED_PREFERANCES_PROFILE_PICTURE,true);
                            SharedPreferanceHelper.getInstance().saveString(Consts.SHARED_PREFERANCES_PROFILE_PICTURE_FILE_DIR,imageFile.getAbsolutePath());
                            Bitmap myBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

                            accountImageView.setImageBitmap(myBitmap);

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle failed download
                // ...
                Log.d("sd","sdsdsd");
            }
        });
    }


    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.account_settings_image_view:
                pickImage();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            try {
                InputStream inputStream = getActivity().getContentResolver().openInputStream(data.getData());
                File file = new File(getActivity().getCacheDir(), "accountImage.jpg");
                copyInputStreamToFile(inputStream, file);
                sendImageToServer(file);
//                accountImageView.setImageBitmap(BitmapFactory.decodeStream(inputStream));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }
    }
    private void sendImageToServer(File fileImage) {
        Uri file = Uri.fromFile(fileImage);
        StorageReference storageReference1 = storageReference.child(USERS_STORAGE_NAME).child(user.getUid()).child(ACCOUNT_IMAGE_STORAGE);
        storageReference1.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                reference.getParent().child(USER_MODEL_IMAGEURL).setValue(String.valueOf(downloadUrl));
            }
        });
    }
    private void copyInputStreamToFile(InputStream in, File file) {
        OutputStream out = null;

        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            // Ensure that the InputStreams are closed even if there's an exception.
            try {
                if ( out != null ) {
                    out.close();
                }

                // If you want to close the "in" InputStream yourself then remove this
                // from here but ensure that you close it yourself eventually.
                in.close();
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }
}
