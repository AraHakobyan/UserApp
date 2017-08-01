package com.example.aro_pc.myapplication.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.aro_pc.myapplication.R;
import com.example.aro_pc.myapplication.adapter.ChatAdapter;
import com.example.aro_pc.myapplication.model.ChatMessageModel;
import com.example.aro_pc.myapplication.model.UserModel;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.example.aro_pc.myapplication.Consts.CHAT_MODEL_NAME;
import static com.example.aro_pc.myapplication.Consts.DATABASE_NAME;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements View.OnClickListener {

    private Button mSendButton;
    private Context context;
    private EditText chatText;
    private UserModel user2;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference user2Ref;
    private DatabaseReference user1Ref;
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private ArrayList<ChatMessageModel> messageModelArrayList;
    private Gson gson;
    private JSONObject jsonObject;


    public void setUser1(UserModel user1) {
        this.user1 = user1;
    }

    private UserModel user1;


    public void setUser2(UserModel user2) {
        this.user2 = user2;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.chat_recycler_view);
        firebaseDatabase = FirebaseDatabase.getInstance();
        user2Ref = firebaseDatabase.getReference(DATABASE_NAME).child(user2.getUid()).child(CHAT_MODEL_NAME).child(user1.getUid());
        user1Ref = firebaseDatabase.getReference(DATABASE_NAME).child(user1.getUid()).child(CHAT_MODEL_NAME).child(user2.getUid());
        prepareRecyclerView();
        addListener(user1Ref);
        //addListener(user2Ref);

        mSendButton = (Button) view.findViewById(R.id.send_message);
        mSendButton.setOnClickListener(this);
        chatText = (EditText) view.findViewById(R.id.chat_edit_text);
        return view;
    }

    private void addListener(DatabaseReference userRef) {
        userRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String jsonMessage = dataSnapshot.getValue(String.class);
                ChatMessageModel messageModel = new ChatMessageModel();

                try {
                    gson = new Gson();
                    jsonObject = new JSONObject(jsonMessage);
                    messageModel = gson.fromJson(jsonMessage, ChatMessageModel.class);
                    messageModelArrayList.add(messageModel);
                    adapter.notifyItemInserted(messageModelArrayList.size());
                    recyclerView.smoothScrollToPosition(messageModelArrayList.size());
                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void prepareRecyclerView() {
        messageModelArrayList = new ArrayList<>();
        adapter = new ChatAdapter(messageModelArrayList, getActivity(),user2,user1);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);




    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.send_message:
                String messageJson = "";
                JSONObject jsonObject = new JSONObject();
                ChatMessageModel messageModel = new ChatMessageModel();
                messageModel.setmText(chatText.getText().toString());
                messageModel.setmUid1(user1.getUid());
                messageModel.setmUid2(user2.getUid());
                messageModel.setTime(getTime());
                Gson gson = new Gson();
                messageJson = gson.toJson(messageModel);
                user2Ref.push().setValue(messageJson);
                user1Ref.push().setValue(messageJson);
                break;
        }
    }

    public static String getTime(){
        Date presentTime_Date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(presentTime_Date);
    }
}
