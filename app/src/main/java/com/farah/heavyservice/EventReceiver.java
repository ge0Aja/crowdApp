package com.farah.heavyservice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;

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
                if (Common.hasPermissions(context, CommonVariables.Permissions) && android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    CommonVariables.startService = true;
                }
                if (CommonVariables.startService) {
                    if (!Common.isMyServiceRunning(MyService.class, context)) {
                        Intent startServiceIntent = new Intent(context, MyService.class);
                        context.startService(startServiceIntent);
                    }
                } else {
                    Log.d(CommonVariables.TAG, "The Service Cannot Start Due to Missing Permissions or incompatible API");
                   // FirebaseCrash.report(new Exception("The Service Cannot Start Due to Missing Permissions"));
                }
                break;
            case "android.net.conn.CONNECTIVITY_CHANGE":
                if (Common.isConnectedToWifi(context)) {
                    final PendingResult pendingResult = goAsync();
                    AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            if(hasConenction()){
                                CommonVariables.setWiFi(true);
                                CommonVariables.setServerReachable(true);
                                Log.d(CommonVariables.TAG, "Connectivity The Phone is Connected to internet");
                            }else{
                                CommonVariables.setWiFi(false);
                                CommonVariables.setServerReachable(false);
                                Log.d(CommonVariables.TAG, "Connectivity The Phone is NOT Connected to internet");
                            }
                            pendingResult.finish();
                            return null;
                        }
                    };
                    asyncTask.execute();

                    Intent intentStartUpload = new Intent(context, ClientServerService.class);
                    intentStartUpload.putExtra("uploadtype", CommonVariables.UploadTypeDir);
                    intentStartUpload.putExtra("receiver", CommonVariables.uploadResultDir);
                    intentStartUpload.putExtra("type", CommonVariables.filetypeAll);
                    CommonVariables.pintent = PendingIntent.getService(context, 0, intentStartUpload, PendingIntent.FLAG_ONE_SHOT);

                    CommonVariables.alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    CommonVariables.startUploadDir = true;
                    CommonVariables.changeCheckFlag(true);
                    CommonVariables.alarm.setRepeating(AlarmManager.RTC_WAKEUP, CommonVariables.cal.getTimeInMillis() + CommonVariables.uploadIntervalNormal, CommonVariables.uploadIntervalRetry, CommonVariables.pintent);
                    Log.d(CommonVariables.TAG, "Upload Scheduled after " + CommonVariables.uploadIntervalNormal / 1000 + " Seconds");

                } else {
                    if(Common.isConnectedToDataPlan(context)){
                        final PendingResult pendingResult = goAsync();
                        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                if(hasConenction()){
                                    CommonVariables.setServerReachable(true);
                                    Log.d(CommonVariables.TAG, "Connectivity The Site is reachable through data plan");
                                }else{
                                    CommonVariables.setServerReachable(false);
                                    Log.d(CommonVariables.TAG, "Connectivity The Site is NOT reachable");
                                }
                                pendingResult.finish();
                                return null;
                            }
                        };
                        asyncTask.execute();
                    }

                    CommonVariables.setWiFi(false);
                    CommonVariables.startUploadDir = false;
                    CommonVariables.startUpload = false;
                    if (CommonVariables.alarm != null && CommonVariables.pintent != null) {
                        CommonVariables.alarm.cancel(CommonVariables.pintent);
                    }
                }
                Log.d(CommonVariables.TAG, "Connectivity Change The WIFI and Internet status should change");
                break;
        }
    }

    private static boolean hasConenction() {

        try {
            HttpURLConnection urlc = (HttpURLConnection) (new URL("http://" + CommonVariables.UploadHost).openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(10000);
            urlc.connect();
            return (urlc.getResponseCode() == 200);
        } catch (IOException e) {
            Log.d(CommonVariables.TAG, "Error checking internet connection", e);
        }
        return false;
    }
}
