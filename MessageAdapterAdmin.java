package com.example.caipsgcms;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;




        import android.content.Context;
        import android.graphics.Color;
        import android.graphics.PorterDuff;
        import android.graphics.drawable.Drawable;
        import android.os.Build;
        import android.text.TextUtils;
        import android.view.Gravity;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.ProgressBar;
        import android.widget.TextView;
        import android.widget.Toast;

        import androidx.annotation.NonNull;
        import androidx.annotation.Nullable;
        import androidx.annotation.RequiresApi;
        import androidx.recyclerview.widget.RecyclerView;

        import com.bumptech.glide.Glide;
        import com.bumptech.glide.load.DataSource;
        import com.bumptech.glide.load.engine.GlideException;
        import com.bumptech.glide.request.RequestListener;
        import com.bumptech.glide.request.target.Target;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.ValueEventListener;

        import java.util.List;

public class MessageAdapterAdmin extends RecyclerView.Adapter<MessageAdapterAdmin.ViewHolder> {
    private Context mContext;
    private List<Message> mMessageList;

    public MessageAdapterAdmin(Context mContext,List<Message> mMessageList){
        this.mContext = mContext;
        this.mMessageList = mMessageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.message_layout,parent,false);
        return new MessageAdapterAdmin.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Message message = mMessageList.get(position);


        if(message.getImageurl()==null){
            holder.msgImage.setVisibility(View.GONE);
        }else
        {
            holder.progressBar.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(message.getImageurl()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    holder.progressBar.setVisibility(View.GONE);
                    Toast.makeText(mContext, "load failed", Toast.LENGTH_SHORT).show();
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    holder.progressBar.setVisibility(View.GONE);
                    Toast.makeText(mContext, "load done", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }).into(holder.msgImage);
        }
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.message.getLayoutParams();
        LinearLayout.LayoutParams layoutParams1 = (LinearLayout.LayoutParams) holder.message.getLayoutParams();
        if(TextUtils.equals(message.getPublisher(),"admin")){
            holder.message.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            holder.message.setBackgroundColor(Color.RED);
            layoutParams.rightMargin = 60;
            layoutParams1.gravity = Gravity.START;

        }
        else {
            holder.message.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            holder.message.setBackgroundColor(Color.DKGRAY);
            layoutParams.leftMargin = 60;
            layoutParams1.gravity = Gravity.END;

        }
        holder.message.setText(message.getMessage());
        holder.time.setText(message.getTime());

        getUserInformation(holder.publisher,message.getPublisher());
    }



    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView publisher,message,time;
        public ImageView msgImage;
        ProgressBar progressBar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.msg);
            time = itemView.findViewById(R.id.msgTime);
            msgImage = itemView.findViewById(R.id.msg_img);
            progressBar = itemView.findViewById(R.id.loadimg_msg);
        }
    }
    private void getUserInformation(TextView publisher, String publisherid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(publisherid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
