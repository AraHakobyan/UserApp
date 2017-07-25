package com.example.aro_pc.myapplication.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Aro-PC on 7/24/2017.
 */

public class AllUsersAdapter extends RecyclerView.Adapter<AllUsersAdapter.ViewHolder> {



    public AllUsersAdapter() {
    }

    @Override
    public AllUsersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(AllUsersAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
