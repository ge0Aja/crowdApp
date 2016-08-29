package com.farah.heavyservice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Georgi on 8/23/2016.
 */
public class CommonVariables {

    public static final String filetypeTf = "TF";
    public static final String filetypeCx = "CX";
    public static final String filetypeCPC = "CP";
    public static final String filetypeAll = "All";

    public static int waitInterval = 0;
    public static int retryIntervalLog = 0;
    public static int retryIntervalEvent = 0;
    public static int collectInterval = 0;
    public static int uploadIntervalNormal = 10 * 1000;
    public static int uploadIntervalRetry = 7200 * 1000;
    public static int thresholdInterval = 0;
    public static int[] thresholds = new int[10];
    public static int maxFileSize = 1024;

    public static boolean screenOn = false;
    public static boolean isWiFi = false;
    public static boolean startUpload = false;

    public static boolean startUploadDir = false;

    public static String TAG = "CollectService";
    public static String TAG_U = "UploadService";
    public static String fileToUpload = "";
    public static String fileUploadType = "";

    public static String TFBkup = "TrafficStatsBkup";
    public static String CPCBkup = "CPUMEMStatsBkup";
    public static String CxBkup = "CxStatsBkup";

    public static String TFUploadURL = "url";
    public static String CPCUploadURL = "url";
    public static String CxUploadURL = "url";
    public static String UploadHost = "IP";

    public static String UploadTypeFile = "File";
    public static String UploadTypeDir = "Dir";


    public static Calendar cal = Calendar.getInstance();
    public static AlarmManager alarm;
    public static PendingIntent pintent;

    public static ResultsReceiver uploadResultDir = new ResultsReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case ClientServerService.STATUS_RUNNING:
                    // TODO use this flag somewhere
                    // startUploadDir = true;
                    break;
                case ClientServerService.STATUS_FINISHED:
                    //TODO get the results and see what is happening
                    int status = resultData.getInt("Status");
                    //  startUpload = false;
                    Log.i(ClientServerService.TAG, "Status is " + String.valueOf(status));
                    String type = "";
                    switch (status) {
                        case ClientServerService.STATUS_FINISHED_SUCCESS:
                            type = resultData.getString("type");
                            Log.i(ClientServerService.TAG, "Files from " + type + " directory are uploaded !");
                            // alarm.cancel(pintent);
                            startUploadDir = false;
                            break;
                        case ClientServerService.STATUS_FINISHED_NOFILES:
                            type = resultData.getString("type");
                            Log.i(ClientServerService.TAG, "No Files found in " + type + " directory !");
                            alarm.cancel(pintent);
                            startUploadDir = false;
                            break;
                        case ClientServerService.STATUS_FINISHED_NOWIFI:
                            Log.i(ClientServerService.TAG, "No Wifi Schedule upload for later");
                            //TODO I think better not to reschedule just leave it for the regular upload service
                            // alarm.setRepeating(AlarmManager.RTC_WAKEUP, CommonVariables.cal.getTimeInMillis(), 30 * 1000, CommonVariables.pintent);
                            // alarm.cancel(pintent);
                            startUploadDir = false;
                            break;
                        case ClientServerService.STATUS_FINISHED_ERROR:
                            Log.i(ClientServerService.TAG, "FINISHED WITH ERRORS" + String.valueOf(status));
                            // TODO send not to server
                            break;
                        case ClientServerService.STATUS_FINISHED_SERVER_UNAVAILABLE:
                            // TODO the server is not responding right now cancel for the rest
                            Log.i(ClientServerService.TAG, " The response was found to be server not found the alaram should turn off");
                            // alarm.cancel(pintent);
                            startUploadDir = false;
                            break;
                        case ClientServerService.STATUS_FINISHED_FORBIDDEN:
                            // TODO send not to server
                            startUploadDir = false;
                            break;
                        case ClientServerService.STATUS_FINISHED_MALFORMED_HTTP:
                            // TODO send not to server
                            startUploadDir = false;
                            break;
                    }
                    //  alarm.cancel(pintent);
                    break;
                case ClientServerService.STATUS_ERROR:
                    //TODO something
                    alarm.cancel(pintent);
                    break;
            }
        }
    };

    public static void setWiFi(boolean WiFi) {
        isWiFi = WiFi;
    }

    public static void setUploadSettings(String fileName, Boolean upload, String filetypetoupload) {
        fileToUpload = fileName;
        startUpload = upload;
        fileUploadType = filetypetoupload;
    }

    public static void changeCxBkupName(String Name) {
        CxBkup = Name;
    }

    public static void changeCPCBkupName(String Name) {
        CPCBkup = Name;
    }

    public static void changeTFBkupName(String Name) {
        TFBkup = Name;
    }
}
