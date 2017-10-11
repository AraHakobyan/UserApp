package com.example.aropc.myapplication.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.aro_pc.myapplication.R;
import com.example.aropc.myapplication.Consts;
import com.example.aropc.myapplication.fragment.ChatFragment;
import com.example.aropc.myapplication.helper.SharedPreferanceHelper;
import com.example.aropc.myapplication.model.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Aro-PC on 7/24/2017.
 */

public class AllUsersAdapter extends RecyclerView.Adapter<AllUsersAdapter.UsersListViewHolder> {

    private StorageReference storageReference;
    private Context context;
    private ArrayList<UserModel> userModels;
    private FragmentManager fragmentManager;
    private UserModel mInfo;

    public AllUsersAdapter(Context context, ArrayList<UserModel> userModels, FragmentManager fragmentManager, UserModel mInfo) {
        this.context = context;
        this.userModels = userModels;
        this.fragmentManager = fragmentManager;
        this.mInfo = mInfo;
        storageReference = FirebaseStorage.getInstance().getReference(Consts.USERS_STORAGE_NAME);
    }

    @Override
    public AllUsersAdapter.UsersListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_model, null);
        AllUsersAdapter.UsersListViewHolder viewHolder = new AllUsersAdapter.UsersListViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(UsersListViewHolder holder, final int position) {
        holder.textView.setText(userModels.get(position).getName());

        if (userModels.get(position).getImageUrl().equals(Consts.USER_MODEL_IMAGEURL_VALUE)) {
            holder.imageView.setBackgroundResource(R.drawable.dicas);

        } else {
            StorageReference storageReference1 = storageReference.child(userModels.get(position).getUid()).child(Consts.ACCOUNT_IMAGE_STORAGE);

            try {
                downloadFromDatabase(storageReference1,holder.imageView,userModels.get(position));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (userModels.get(position).getOnline().equals(Consts.USER_MODEL_STATUS_VALUE_ONLINE)) {
            holder.imageView.setBorderColor(Color.GREEN);
        } else {
            holder.imageView.setBorderColor(Color.GRAY);

        }


        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatFragment chatFragment = new ChatFragment();
                chatFragment.setContext(context);
                chatFragment.setUser2(userModels.get(position));
                chatFragment.setUser1(mInfo);
                fragmentManager.beginTransaction().replace(R.id.container, chatFragment).commit();

            }
        });
    }

    @Override
    public int getItemCount() {
        return userModels.size();
    }

    public class UsersListViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView imageView;
        private CardView cardView;
        private TextView textView;

        public UsersListViewHolder(View itemView) {
            super(itemView);

            imageView = (CircleImageView) itemView.findViewById(R.id.users_model_circular_image);
            cardView = (CardView) itemView.findViewById(R.id.user_list_cardview);
            textView = (TextView) itemView.findViewById(R.id.user_name_in_list_tv);
        }
    }

    private File imageFile;
    private String mFileName;

    private void downloadFromDatabase( StorageReference storageReference1, final CircleImageView imageView, UserModel userModel) throws IOException {
        mFileName = context.getCacheDir().getAbsolutePath();
        mFileName +=  "/" +userModel.getUid() + "image.jpg";
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

                            imageView.setImageBitmap(myBitmap);

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


}
