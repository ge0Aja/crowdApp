package com.farah.heavyservice;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Georgi on 9/3/2016.
 *
 * this class extends the firebase messaging service to handle the received notifications
 */
public class FcmMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            //gives a unique id for the received notification
            int iUniqueId = (int) (System.currentTimeMillis() & 0xfffffff);
            //reads the information embeded in the notification body and title which is sent from the server
            String type = remoteMessage.getData().get("type");
            String Qid = remoteMessage.getData().get("Qid");
            String question = remoteMessage.getData().get("question");
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("body");

            //creates an intent to be used in starting the Alertactivity and show the notification
            // message to the user as a notification which is sticky and doesn't disappear unless the user click on it
            //once the user clicks on the notification the AlertActivity will start
            Intent intent = new Intent(this,TransferActivity.class);
            intent.addFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP );
            intent.putExtra("question",question);
            intent.putExtra("message",message);
            intent.putExtra("type",type);
            intent.putExtra("Qid",Qid);
            intent.putExtra("notid", String.valueOf(iUniqueId));
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(AlertActivity.class);
            stackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(iUniqueId, PendingIntent.FLAG_ONE_SHOT); //PendingIntent.FLAG_ONE_SHOT
         // PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
            notificationBuilder.setContentTitle(title);
            notificationBuilder.setContentText(message);
            notificationBuilder.setSmallIcon(R.drawable.crowdapp);
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
