package com.example.caipsgcms;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.scottyab.aescrypt.AESCrypt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import static androidx.core.content.ContextCompat.checkSelfPermission;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    public Context mContext;
    public List<Message> mMessageList;

    public CustomAdapter(List<Message> mMessageList,Context context){
        this.mMessageList = mMessageList;
        this.mContext = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = mMessageList.get(position);
        //holder.setIsRecyclable(false);
        final Message messageObj = mMessageList.get(position);
        Log.d("imgurl", messageObj.getImageurl());
        if (TextUtils.equals(messageObj.getImageurl(), "")) {
            holder.msgImage.setVisibility(View.GONE);
        } else {
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
        Log.d("publisher", messageObj.getPublisher());
        String publisher = messageObj.getPublisher();
        try {
            publisher = AESCrypt.decrypt("asdfgh", publisher);

        } catch (GeneralSecurityException e) {
            Log.d("exception", e.getMessage());
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        Log.d("encryptId", publisher);
        if (TextUtils.equals(publisher, "admin")) {
            holder.message.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            //holder.message.setBackgroundColor(Color.RED);
            holder.msgBackLayout.setBackgroundResource(R.drawable.chat_back_admin);
            //holder.msgBackLayout.setBackgroundColor(Color.RED);
            layoutParams.rightMargin = 60;
            layoutParams1.gravity = Gravity.START;
            layoutParams2.gravity = Gravity.START;

        } else {
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
            msgOrg = AESCrypt.decrypt("asdfgh", msgOrg);
        } catch (GeneralSecurityException e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        holder.message.setText(msgOrg);
        holder.time.setText(messageObj.getTime());
        holder.msgImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.equals(messageObj.getType(), "image")) {
                    /*holder.frameLayout.setVisibility(View.VISIBLE);
                    AppCompatActivity activity = (AppCompatActivity) v.getContext();
                    ImageFragment fragment = new ImageFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("imageUrl",messageObj.getImageurl());
                    fragment.setArguments(bundle);
                    activity.getSupportFragmentManager().beginTransaction().add(R.id.containerfrag,fragment).addToBackStack(null).commit();
*/
                   /* final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                    alertDialogBuilder.setMessage("Do you want to download this image?");
                    alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions((Activity)mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (1 == 1) {
                                    //checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                                    //Log.v(TAG,"Permission is granted");
                                    //File write logic here
                                    BitmapDrawable draw = (BitmapDrawable) holder.msgImage.getDrawable();
                                    Bitmap bitmap = draw.getBitmap();

                                    FileOutputStream outStream = null;
                                    File sdCard = Environment.getExternalStorageDirectory();
                                    File dir = new File(sdCard + "/CAIPS");


                                    dir.mkdirs();
                                    String fileName = String.format("%d.jpg", System.currentTimeMillis());
                                    Uri path = Uri.parse(sdCard.getAbsolutePath() + "/CAIPS/" + fileName);
                                    File outFile = new File(dir, fileName);
                                    try {
                                        outStream = new FileOutputStream(outFile);
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                                    try {
                                        outStream.flush();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        outStream.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    Toast.makeText(mContext, "Downloaded", Toast.LENGTH_SHORT).show();

                                    createnotification(path);
                                } else {
                                    ActivityCompat.requestPermissions((Activity)mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                }
                            }

                        }
                    });
                    alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                    */
                    Intent intent = new Intent(mContext,ShowImage.class);
                    intent.putExtra("id",messageObj.getPublisher());
                    intent.putExtra("imageUrl",messageObj.getImageurl());
                    //intent.putExtra("id",messageObj.get)
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mContext.startActivity(intent);

                    //((Activity)mContext).finish();
                } else {
                    Toast.makeText(mContext, "pdf open", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri.parse(messageObj.getImageurl());
                    intent.setDataAndType(uri, "application/pdf");
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mContext.startActivity(intent);
                }
            }
        });
    }



    private void createnotification(Uri path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Download";
            String description = "Download Resources Notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("DOWNLOAD", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            //NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
           // notificationManager.createNotificationChannel(channel);
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(String.valueOf(path)), "image/*");
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext, "DOWNLOAD")
                .setSmallIcon(R.mipmap.ic_launcher_round);
        /*Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            notification.setStyle(
                    new NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .bigLargeIcon(null)
            ).setLargeIcon(bitmap);*/


        notification.setContentTitle("Image Downloaded")
                .setAutoCancel(true);

        notification.setContentIntent(contentIntent);

       // NotificationManager notificationManager =
                //(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
       // notificationManager.notify(1, notification.build());


    }


    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView publisher,message,time;
        public ImageView msgImage;
        ProgressBar progressBar;
        LinearLayout msgBackLayout;
        FrameLayout frameLayout;
        ClipboardManager clipboardManager;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.msg);
            time = itemView.findViewById(R.id.msgTime);
            msgImage = itemView.findViewById(R.id.msg_img);
            progressBar = itemView.findViewById(R.id.loadimg_msg);
            msgBackLayout = itemView.findViewById(R.id.msgBackLayout);


        }
    }
}
