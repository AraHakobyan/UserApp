package com.example.aro_pc.myapplication.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.aro_pc.myapplication.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserInfoFragment extends Fragment implements View.OnClickListener {

    private Context context;
    private Button chatButton;

    public void setContext(Context context) {
        this.context = context;
    }

    public UserInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_info, container, false);
        chatButton = (Button) view.findViewById(R.id.chat_to_user_button);
        chatButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.chat_to_user_button:

                break;
        }
    }
}
