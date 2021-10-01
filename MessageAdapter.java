package com.example.caipsgcms;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.viewHolder> {
    public Context mContext;
    public List<Message> mMessageList;

    public MessageAdapter(Context mContext,List<Message> mMessageList){
        this.mContext = mContext;
        this.mMessageList = mMessageList;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.message_layout,parent,false);

        return new MessageAdapter.viewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull  viewHolder holder,  int position) {
        //holder.setIsRecyclable(false);
        final Message messageObj = mMessageList.get(position);
        Log.d("imgurl",messageObj.getImageurl());
        if(TextUtils.equals(messageObj.getImageurl(),"")){
            holder.msgImage.setVisibility(View.GONE);
        }else
        {
            holder.progressBar.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(messageObj.getImageurl()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    holder.progressBar.setVisibility(View.GONE);
                    holder.msgImage.setBackgroundResource(R.drawable.pdf);
                    //Toast.makeText(mContext, "load failed", Toast.LENGTH_SHORT).show();
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    holder.progressBar.setVisibility(View.GONE);
                    //Toast.makeText(mContext, "load done", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }).into(holder.msgImage);
        }
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.msgBackLayout.getLayoutParams();
        LinearLayout.LayoutParams layoutParams1 = (LinearLayout.LayoutParams) holder.msgBackLayout.getLayoutParams();
        LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) holder.time.getLayoutParams();

        Log.d("publisher",messageObj.getPublisher());
        String publisher = messageObj.getPublisher();
        try {
            publisher = AESCrypt.decrypt("asdfgh",publisher);

        }catch (GeneralSecurityException e)
        {
            Log.d("exception",e.getMessage());
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        Log.d("encryptId",publisher);
        if(TextUtils.equals(publisher,"admin")){
            holder.message.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            //holder.message.setBackgroundColor(Color.RED);
            holder.msgBackLayout.setBackgroundResource(R.drawable.chat_back_admin);
            //holder.msgBackLayout.setBackgroundColor(Color.RED);
            layoutParams.rightMargin = 60;
            layoutParams1.gravity = Gravity.START;
            layoutParams2.gravity = Gravity.START;

        }
        else {
            holder.message.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            //holder.message.setBackgroundColor(Color.DKGRAY);
            holder.msgBackLayout.setBackgroundResource(R.drawable.chat_back);
            //holder.msgBackLayout.setBackgroundColor(Color.DKGRAY);
            layoutParams.leftMargin = 60;
            layoutParams1.gravity = Gravity.END;
            layoutParams2.gravity = Gravity.END;


        }
        String msgOrg = messageObj.getMessage();
        try {
                msgOrg = AESCrypt.decrypt("asdfgh",msgOrg);
        }catch (GeneralSecurityException e)
        {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        holder.message.setText(msgOrg);
        holder.time.setText(messageObj.getTime());
        holder.msgImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.equals(messageObj.getType(),"image")){

                    Intent intent = new Intent(mContext,ShowImage.class);
                intent.putExtra("id",messageObj.getPublisher());
                intent.putExtra("imageUrl",messageObj.getImageurl());
                //intent.putExtra("id",messageObj.get)
                //intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);

                //((Activity)mContext).finish();
                }else
                {
                    Toast.makeText(mContext,"pdf open",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri =  Uri.parse( messageObj.getImageurl() );
                    intent.setDataAndType(uri, "application/pdf");
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mContext.startActivity(intent);
                }
            }
        });

        getUserInformation(holder.publisher,messageObj.getPublisher());
    }



    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        public TextView publisher,message,time;
        public ImageView msgImage;
        ProgressBar progressBar;
        LinearLayout msgBackLayout;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.msg);
            time = itemView.findViewById(R.id.msgTime);
            msgImage = itemView.findViewById(R.id.msg_img);
            progressBar = itemView.findViewById(R.id.loadimg_msg);
            msgBackLayout = itemView.findViewById(R.id.msgBackLayout);
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
