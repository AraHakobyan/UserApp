package com.example.aro_pc.myapplication.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.aro_pc.myapplication.Consts;
import com.example.aro_pc.myapplication.R;
import com.example.aro_pc.myapplication.adapter.AllUsersAdapter;
import com.example.aro_pc.myapplication.helper.SharedPreferanceHelper;
import com.example.aro_pc.myapplication.model.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChooseUserFragment extends Fragment {

    private RecyclerView recyclerView;
    private AllUsersAdapter adapter;
    private ArrayList<UserModel> allUsers;
    private FirebaseDatabase database;

    private Context context;
    private UserModel mInfo;

    public void setContext(Context context) {
        this.context = context;
    }

    public ChooseUserFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        UserModel userModel = new UserModel();
//        userModel.setName("xoren");
//        UserHelper.getInstance().setmUser(userModel);


    }

    private void getAllUsers() {
        final String mUid = SharedPreferanceHelper.getInstance().getString(Consts.SHARED_PREFERANCES_UID,"");
        database.getReference(Consts.DATABASE_NAME).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allUsers.clear();
                allUsers = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    UserModel post = postSnapshot.getValue(UserModel.class);
                    if(mUid.equals(post.getUid())){
                        mInfo = post;
                    } else {
                        allUsers.add(post);
//                        recyclerView.getAdapter().notifyDataSetChanged();

                    }
                }
                adapter = new AllUsersAdapter(getActivity(), allUsers, getFragmentManager(), mInfo);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new GridLayoutManager(context,2));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        allUsers = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        View view = inflater.inflate(R.layout.fragment_choose_user, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.all_users_recycler_view);
        getAllUsers();




        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }
}
