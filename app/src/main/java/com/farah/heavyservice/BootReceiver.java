package com.farah.heavyservice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.util.Log;

import java.util.Date;

/**
 * Created by Georgi on 8/21/2016.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String messageLogged = "";
        String packageName = "";
        String action = intent.getAction();

        switch (action) {
            case "android.intent.action.BOOT_COMPLETED":
                Intent startServiceIntent = new Intent(context, MyService.class);
                context.startService(startServiceIntent);
                break;
            case "android.net.conn.CONNECTIVITY_CHANGE":
                if (Common.isConnectedToWifi(context)) {
                    CommonVariables.setWiFi(true);
                    Log.i("ConnectivityChange", "The WIFI status should change");
                    Intent intentStartUpload = new Intent(context, ClientServerService.class);
                    intentStartUpload.putExtra("uploadtype", CommonVariables.UploadTypeDir);
                    intentStartUpload.putExtra("receiver", CommonVariables.uploadResultDir);
                    intentStartUpload.putExtra("type", R.string.filetypeAll);
                    CommonVariables.pintent = PendingIntent.getService(context, 0, intentStartUpload, 0);
                    CommonVariables.alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    //TODO reschedule inerval
                    CommonVariables.alarm.setRepeating(AlarmManager.RTC_WAKEUP, CommonVariables.cal.getTimeInMillis(), 30 * 1000, CommonVariables.pintent);
                } else {
                    CommonVariables.setWiFi(false);
                    Log.i("ConnectivityChange", "The WIFI status should change");
                }
                break;
            case "Intent.ACTION_SCREEN_OFF":
                messageLogged = "Screen switched off ";
                Common.appendLog(messageLogged);
                Log.i("Reduced", messageLogged);
                CommonVariables.screenOn = false;
                break;
            case "Intent.ACTION_SCREEN_ON":
                messageLogged = "Screen switched on ";
                Common.appendLog(messageLogged);
                Log.i("Reduced", messageLogged);
                CommonVariables.screenOn = true;
                break;
            case "Intent.ACTION_PACKAGE_ADDED":
                packageName = intent.getData().getEncodedSchemeSpecificPart();
                messageLogged = Common.getAppName(packageName, context) + " installed";
                Common.appendLog(messageLogged);
                Log.i("Reduced", messageLogged);
                break;
            case "Intent.ACTION_PACKAGE_REMOVED":
                Uri uri = intent.getData();
                packageName = uri != null ? uri.getSchemeSpecificPart() : null;
                try {
                    Date dateAdded = new Date(context.getPackageManager().getPackageInfo(packageName, 0).firstInstallTime);
                    messageLogged = Common.getAppName(packageName, context) + " uninstalled (installed on " + dateAdded + ")";
                    Common.appendLog(messageLogged);
                    Log.i("Reduced", messageLogged);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case "android.intent.action.ACTION_BATTERY_LOW":
                // TODO edit  intervals
                break;
            case "android.intent.action.ACTION_BATTERY_OKAY":
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;

                if (isCharging) {
                    // TODO edit intervals
                }
                break;
        }
        //TODO add on reboot clear cumulative stats files Cxn Traffic
    }
}
