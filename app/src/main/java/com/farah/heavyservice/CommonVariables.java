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
    public static int waitInterval = 0;
    public static int retryIntervalLog = 0;
    public static int retryIntervalEvent = 0;
    public static int collectInterval = 0;
    public static int uploadIntervalNormal = 0;
    public static int uploadIntervalRetry = 0;
    public static int thresholdInterval = 0;
    public static int[] thresholds = new int[10];


    public static boolean screenOn = false;
    public static boolean isWiFi = false;
    public static boolean startUpload = false;

    public static boolean startUploadDir = false;

    public static String TAG = "Collect Service";
    public static String TAG_U = "Upload Service";
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
                    startUploadDir = true;
                    break;
                case ClientServerService.STATUS_FINISHED:
                    //TODO get the results and see what is happening
                    int status = resultData.getInt("status");
                    String type = "";
                    switch (status) {
                        case ClientServerService.STATUS_FINISHED_SUCCESS:
                            type = resultData.getString("type");
                            Log.i(TAG_U, "Files from " + type + " directory is uploaded !");
                            alarm.cancel(pintent);
                            break;
                        case ClientServerService.STATUS_FINISHED_NOFILES:
                            type = resultData.getString("type");
                            Log.i(TAG_U, "No Files found in " + type + " directory !");
                            alarm.cancel(pintent);
                            break;
                        case ClientServerService.STATUS_FINISHED_NOWIFI:
                            Log.i(TAG_U, "No Wifi Schedule upload for later");
                            //TODO reschedule inerval
                            alarm.setRepeating(AlarmManager.RTC_WAKEUP, CommonVariables.cal.getTimeInMillis(), 30 * 1000, CommonVariables.pintent);
                            break;
                        case ClientServerService.STATUS_FINISHED_ERROR:
                            //TODO Errors
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
