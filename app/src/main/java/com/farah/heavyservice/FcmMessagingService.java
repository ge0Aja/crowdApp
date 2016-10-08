package com.farah.heavyservice;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by Georgi on 9/3/2016.
 */
public class FcmMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            int iUniqueId = (int) (System.currentTimeMillis() & 0xfffffff);
            String type = remoteMessage.getData().get("type");
            String Qid = remoteMessage.getData().get("Qid");
            String question = remoteMessage.getData().get("question");
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("body");
          //  Log.d("GGGGGGGG","the message is received in background and the information "+message+"  "+type+"  "+Qid+" "+question);
            Intent intent = new Intent(this,TransferActivity.class);
            intent.addFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP );
            intent.putExtra("question",question);
            intent.putExtra("message",message);
            intent.putExtra("type",type);
            intent.putExtra("Qid",Qid);
            intent.putExtra("notid", String.valueOf(iUniqueId));

          //  Log.d("GGGG","Notification id"+ String.valueOf(iUniqueId));
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(AlertActivity.class);
            stackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(iUniqueId, PendingIntent.FLAG_ONE_SHOT); //PendingIntent.FLAG_ONE_SHOT
         // PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
            notificationBuilder.setContentTitle(title);
            notificationBuilder.setContentText(message);
            notificationBuilder.setSmallIcon(R.drawable.mushroom);
            Uri alarmtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            notificationBuilder.setSound(alarmtone);
         //   notificationBuilder.setAutoCancel(false);
            notificationBuilder.setAutoCancel(true);
            notificationBuilder.setOngoing(true);
            notificationBuilder.setContentIntent(pendingIntent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(iUniqueId,notificationBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
           // FirebaseCrash.report(new Exception(e.getMessage()));
        }
    }
}
