package com.farah.heavyservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by Georgi on 8/21/2016.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent startServiceIntent = new Intent(context, MyService.class);
            context.startService(startServiceIntent);
        }

        if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            if(Common.isConnectedToWifi(context)){
                // Start uploading remaining files
                MyService.setWiFi(true);
                Log.i("ConnectivityChange","The WIFI status should change");
            }
            else{
                MyService.setWiFi(false);
                Log.i("ConnectivityChange","The WIFI status should change");
            }

        }
    }
}
