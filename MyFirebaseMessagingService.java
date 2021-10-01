package com.example.caipsgcms;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        //Toast.makeText(this,"msgh",Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "FCM";
            String description = "Notification for new messages";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("FCM_CHANNEL_ID", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        Intent notificationIntent;
        if(remoteMessage.getData().size()>0){
            String id = remoteMessage.getData().get("id");
            String username = remoteMessage.getData().get("username");
            Log.d("datafromfcm",id);
            Log.d("datafromfcm",username);

            notificationIntent = new Intent(this.getApplicationContext(), AdminChat.class);
            notificationIntent.putExtra("id",id);
            notificationIntent.putExtra("username",username);

        }else {
            notificationIntent = new Intent(this.getApplicationContext(), HomeActivity.class);
        }

        notificationIntent.setFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notification =  new NotificationCompat.Builder(this,"FCM_CHANNEL_ID")
                .setSmallIcon(R.mipmap.ic_launcher_round);
        if (remoteMessage.getNotification().getImageUrl() != null) {
            Bitmap bitmap = getBitmapfromUrl(remoteMessage.getNotification().getImageUrl().toString());
            notification.setStyle(
                    new NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .bigLargeIcon(null)
            )
                    .setLargeIcon(bitmap);

        }
        notification.setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setAutoCancel(true);

        notification.setContentIntent(contentIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //notificationManager.notify(0, notification.build());


        notificationManager.notify(1, notification.build());

    }




    public Bitmap getBitmapfromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);

        } catch (Exception e) {
            Log.e("awesome", "Error in getting notification image: " + e.getLocalizedMessage());
            return null;
        }
    }
}
