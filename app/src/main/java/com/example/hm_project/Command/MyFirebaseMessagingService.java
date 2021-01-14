package com.example.hm_project.Command;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;

import com.example.hm_project.R;
import com.example.hm_project.data.PreferenceManager;
import com.example.hm_project.view.activity.LoginActivity;
import com.example.hm_project.view.activity.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/***
 *  FCM메시지 서비스 ( FCM서버에서 보내는 메시지를 받는다 )
 *  1 - onNewToken ( 어플을 처음 다운받거나 처음 로그인할 때, 디바이스 구분을 위한 토큰을 생성한다 )
 *  2 - onMessageReceived ( FCM서버로부터 메세지를 받는다 )
 *  3 - getCustomDesign ( 사용자에게 보여질 Notification 형식을 정의한다 )
 *  4 - showNotification ( FCM 서버로부터 받은 메세지를 실제로 Notification 형식으로 정의해서 사용자에게 보여준다. )
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    Context mContext;

    // 1 - onNewToken ( 어플을 처음 다운받거나 처음 로그인할 때, 디바이스 구분을 위한 토큰을 생성한다 )
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        mContext = this;
        Log.e("Firebase", "FirebaseInstanceIDService : " + s);

        PreferenceManager.setString(mContext, "token", s);
    }

    // 2 - onMessageReceived ( FCM 서버로부터 메세지를 받는다 )
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            showNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("message"));
        }
        if (remoteMessage.getNotification() != null) {
            showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
    }

    // 3 - getCustomDesign ( 사용자에게 보여질 Notification 형식을 정의한다 )
    private RemoteViews getCustomDesign(String title, String message) {
        RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification);
        remoteViews.setTextViewText(R.id.noti_title, title);
        remoteViews.setTextViewText(R.id.noti_message, message);
        remoteViews.setImageViewResource(R.id.noti_icon, R.drawable.logo_black);
        return remoteViews;
    }

    // 4 - showNotification ( FCM 서버로부터 받은 메세지를 실제로 Notification 형식으로 정의해서 사용자에게 보여준다. )
    public void showNotification(String title, String message) {
        System.out.println("title    " + title + "  " + "message : " + message);
        Intent intent = new Intent(this, MainActivity.class);
        String channel_id = "HairManager";
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.logo_black);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id)
                .setSmallIcon(R.drawable.logo_black)
                .setLargeIcon(icon)
                .setSound(uri)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);

        builder = builder.setContent(getCustomDesign(title, message));

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channel_id, "web_app", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setSound(uri, null);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notificationManager.notify(0, builder.build());
    }
}