package com.farah.heavyservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Georgi on 10/7/2016.
 *
 * this broadcast receiver is reponsible to make sure that the service is running at all times
 */
public class RestartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(RestartServiceReceiver.class.getSimpleName(),"Service Stopped and will be restarted");
        context.startService(new Intent(context,MyService.class));
    }
}
