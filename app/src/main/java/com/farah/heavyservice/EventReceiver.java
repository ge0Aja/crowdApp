package com.farah.heavyservice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Environment;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import java.io.File;

/**
 * Created by Georgi on 10/7/2016.
 *
 * the Event receiver catch the boot and connectivity change events plus battery level warnings
 *
 * on the boot complete we are starting the service and asking for permissions in the case of OS 6.0
 *
 * in the case of connectivity change I'm starting the upload process for the All directories
 *
 * in case of a battery level warning we decrease the collect interval to save power
 */
public class EventReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        switch (action) {
            case "android.intent.action.BOOT_COMPLETED":
                File dirtf = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeTf + "/");
                File myFiletf = new File(dirtf, "CumulativeTrafficStatsBkup");
                if (myFiletf.exists())
                    myFiletf.delete();

                File dircx = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeCx + "/");
                File myFilecx = new File(dircx, "CumulativeCxStatsBkup");
                if (myFilecx.exists())
                    myFilecx.delete();
                if (Common.hasPermissions(context, CommonVariables.Permissions)) {
                    CommonVariables.startService = true;
                }
                if (CommonVariables.startService) {
                    if (!Common.isMyServiceRunning(MyService.class, context)) {
                        Intent startServiceIntent = new Intent(context, MyService.class);
                        context.startService(startServiceIntent);
                    }
                } else {
                    Log.d(CommonVariables.TAG, "The Service Cannot Start Due to Missing Permissions");
                    FirebaseCrash.report(new Exception("The Service Cannot Start Due to Missing Permissions"));
                }
                break;
            case "android.net.conn.CONNECTIVITY_CHANGE":
                Common.checkConnection(context);
                if (Common.isConnectedToWifi(context)) {
                    // CommonVariables.setWiFi(true);
                    if (!CommonVariables.userRegistered && CommonVariables.isWiFi) {
                        Common.regUser(context);
                    }
                    Log.i(CommonVariables.TAG, "ConnectivityChange: The WIFI status should change");
                    Intent intentStartUpload = new Intent(context, ClientServerService.class);
                    intentStartUpload.putExtra("uploadtype", CommonVariables.UploadTypeDir);
                    intentStartUpload.putExtra("receiver", CommonVariables.uploadResultDir);
                    intentStartUpload.putExtra("type", CommonVariables.filetypeAll);
                    CommonVariables.pintent = PendingIntent.getService(context, 0, intentStartUpload, PendingIntent.FLAG_ONE_SHOT);
                    CommonVariables.alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    CommonVariables.startUploadDir = true;
                    CommonVariables.alarm.setRepeating(AlarmManager.RTC_WAKEUP, CommonVariables.cal.getTimeInMillis() + CommonVariables.uploadIntervalNormal, CommonVariables.uploadIntervalRetry, CommonVariables.pintent);
                    Log.i(CommonVariables.TAG, "Upload Scheduled after" + CommonVariables.uploadIntervalNormal / 1000 + " Seconds");
                } else {
                    CommonVariables.setWiFi(false);
                    CommonVariables.startUploadDir = false;
                    CommonVariables.startUpload = false;
                    if (CommonVariables.alarm != null && CommonVariables.pintent != null) {
                        CommonVariables.alarm.cancel(CommonVariables.pintent);
                    }
                    Log.i(CommonVariables.TAG, "Connectivity Change The WIFI status should change");
                }
                break;
            case "android.intent.action.ACTION_BATTERY_LOW":
                CommonVariables.collectInterval *= 2;
                break;
            case "android.intent.action.ACTION_BATTERY_OKAY":
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;

                if (isCharging) {
                    CommonVariables.collectInterval = 10000;
                }
                break;
        }
    }
}
