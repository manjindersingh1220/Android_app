package com.example.caipsgcms;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context mContext;
    private List<User> mUserList;

    public UserAdapter() {
    }

    public UserAdapter(Context mContext, List<User> mUserList) {
        this.mContext = mContext;
        this.mUserList = mUserList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_show,parent,false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = mUserList.get(position);
            String user1 = user.getName();

            try {
                user1 = AESCrypt.decrypt("asdfgh",user1);
            }catch (GeneralSecurityException e){
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            holder.userName.setText(user1);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userName = user.getName();

                    try {
                        userName = AESCrypt.decrypt("asdfgh",userName);
                    }catch (GeneralSecurityException e){
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    Intent intent = new Intent(mContext,AdminChat.class);
                    intent.putExtra("id", user.getId());
                    intent.putExtra("username",userName);
                    mContext.startActivity(intent);
                    Toast.makeText(mContext,user.getName(),Toast.LENGTH_SHORT).show();
                }
            });

    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView userName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.display_user);
        }
    }
}
