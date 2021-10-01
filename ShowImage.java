package com.example.caipsgcms;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShowImage extends AppCompatActivity {
    PhotoView photoView;
    FirebaseAuth mAuth;
    String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        photoView = findViewById(R.id.photoView);
        mAuth = FirebaseAuth.getInstance();
        Toast.makeText(this, "Long press to download image", Toast.LENGTH_LONG).show();
        if (getIntent().getExtras() != null) {
            id = getIntent().getStringExtra("id");
            String imageUrl = getIntent().getStringExtra("imageUrl");
            Glide.with(ShowImage.this).load(imageUrl).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    Toast.makeText(ShowImage.this, "Error downloading image", Toast.LENGTH_SHORT).show();
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {


                    return false;
                }
            }).into(photoView);

        } else {
            photoView.setImageResource(R.drawable.logo);
        }
        photoView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ShowImage.this);
                alertDialogBuilder.setMessage("Do you want to download this image?");
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        download();
                    }
                });
                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return false;
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
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(String.valueOf(path)), "image/*");
        PendingIntent contentIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, intent, 0);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "DOWNLOAD")
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

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification.build());

    }



    private void download() {
        ActivityCompat.requestPermissions(ShowImage.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //Log.v(TAG,"Permission is granted");
                //File write logic here
                BitmapDrawable draw = (BitmapDrawable) photoView.getDrawable();
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
                Toast.makeText(ShowImage.this, "Downloaded", Toast.LENGTH_SHORT).show();

                createnotification(path);
            } else {
                ActivityCompat.requestPermissions(ShowImage.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

    }

    @Override
    public void onBackPressed() {
        if(TextUtils.equals(mAuth.getCurrentUser().getUid(),"2Wc3jYgqdBTjIC1JDwUH9NMesfx2")){
            Intent intent = new Intent(this,AdminChat.class);
            Log.d("idinimg",id);
            //intent.putExtra("id",id);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(this,HomeActivity.class);
            startActivity(intent);
        }

    }
}
