package com.example.aro_pc.myapplication.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.aro_pc.myapplication.R;
import com.example.aro_pc.myapplication.model.ChatMessageModel;
import com.example.aro_pc.myapplication.model.UserModel;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Aro-PC on 7/31/2017.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private ArrayList<ChatMessageModel> messageModelArrayList;
    private Context context;
    private UserModel user2;
    private UserModel user1;

    public ChatAdapter(ArrayList<ChatMessageModel> messageModelArrayList, Context context,UserModel user2, UserModel user1) {
        this.messageModelArrayList = messageModelArrayList;
        this.context = context;
        this.user2 = user2;
        this.user1 = user1;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, null);
        ChatViewHolder viewHolder = new ChatViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {

//        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.itemLayout.getLayoutParams();

        if (messageModelArrayList.get(position).getmUid1().toString().equals(user2.getUid().toString())){
            holder.userName.setText(user2.getUserName());
//            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//            holder.itemLayout.setLayoutParams(layoutParams);
//            holder.itemLayout.requestLayout();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.itemLayout.setBackground(context.getDrawable(R.drawable.rounded_corner_white));
            } else {
                holder.itemLayout.setBackgroundColor(Color.WHITE);

            }
            holder.itemLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.LEFT));
            holder.itemLayout.requestLayout();
            holder.imageView.setImageResource(R.drawable.dicas);
        } else {
            holder.userName.setText(user1.getUserName());
//            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.itemLayout.setBackground(context.getDrawable(R.drawable.rounded_corner_drawable));
            } else {
                holder.itemLayout.setBackgroundColor(Color.parseColor("#009688"));

            }
            holder.itemLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT));
            holder.itemLayout.requestLayout();

            holder.imageView.setImageResource(R.drawable.ic_account_box_black_24dp);
        }
        holder.chatMessage.setText(messageModelArrayList.get(position).getmText());

    }

    @Override
    public int getItemCount() {
        return messageModelArrayList.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {

        private TextView userName;
        private TextView chatMessage;
        private LinearLayout itemLayout;
        private CircleImageView imageView;



        public ChatViewHolder(View itemView) {
            super(itemView);
            itemLayout = (LinearLayout) itemView.findViewById(R.id.item_layout_id);
            userName = (TextView) itemView.findViewById(R.id.user_name_chat);
            chatMessage = (TextView) itemView.findViewById(R.id.chat_id_item_message);
            imageView = (CircleImageView) itemView.findViewById(R.id.cat_item_image);
        }
    }
}
