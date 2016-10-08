package com.farah.heavyservice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.Manifest;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Georgi on 8/23/2016.
 */
public class CommonVariables {

    public static String TAG = "HeavyService";

    public static boolean startService =false;

    public static final int PERMISSION_ALL =1;
    public static String[] Permissions = {Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECEIVE_BOOT_COMPLETED, Manifest.permission.BATTERY_STATS,Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE};

    public static Context mContext;
    public static final String filetypeTf = "TF";
    public static final String filetypeCx = "CX";
    public static final String filetypeCPC = "CP";
    //S_Farah
    public static final String filetypeOF = "OF";
    public static final String filetypeUT = "UT";
    //E_Farah
    public static final String filetypeScreen = "Screen";
    public static final String filetypePackage = "Packages";
    public static final String filetypeAll = "All";

    public static String username;
    public static String password = "P@ssw0rd!";

    //stats collection interval
    public static int collectInterval = 10000;

    // start_upload interval for Dir after connecting to wifi
    public static int uploadIntervalNormal = 120000;

    // repeat_upload interval for Dir after connecting to wifi
    public static int uploadIntervalRetry = 7200000;

    //collect interval fast
    public static int collectIntervalFast;

    //collect interval miid
    public static int collectIntervalMid;

    //collect interval slow
    public static int collectIntervalSlow;

    //collect interval

    //set max file size to upload
    public static int maxFileSize = 1024;

    //set max file size for screen log
    public static int maxFileSizeScreen = 256;

    //set max file size for OF UT log
    public static int maxFileSizeOFUT = 256;

    public static float txPacketsThreshold;
    public static float rxPacketsThreshold;
    public static float txBytesThreshold;
    public static float rxBytesThreshold;
    public static float cxAgeThreshold;
    public static float prCPUThreshold;
    public static float prVSSThreshold;
    public static float prRSSThreshold;

    public static boolean PermissionsGranted = false;
    public static boolean screenOn = false;
    public static boolean isWiFi = false;
    public static boolean startUpload = false;
    public static boolean startUploadDir = false;
    public static boolean userRegistered = false;
    public static boolean startUpdateIntervals = false;
    public static boolean startUpdateThresholds = false;

    public static String TAG_U = "UploadService";
    public static String fileToUpload;
    public static String fileUploadType;

    public static String TFBkup;
    public static String CPCBkup;
    public static String CxBkup;
    public static String ScreenBkup;
    public static String PackagesBkup;
    //S_Farah
    public static String OFBkup;
    public static String UTBkup;
    //E_Farah

    public static String TFUploadURL = "https://72.14.183.152:4433/CrowdApp/InsertTF.php";
    public static String CPCUploadURL = "https://72.14.183.152:4433/CrowdApp/InsertCPC.php";
    public static String CxUploadURL = "https://72.14.183.152:4433/CrowdApp/InsertCx.php";
    public static String registrationUrl = "https://72.14.183.152:4433/CrowdApp/fcm_insert.php";
    public static String DownloadThresholdsURL = "https://72.14.183.152:4433/CrowdApp/getThresholds.php";
    public static String DownloadIntervalsURL = "https://72.14.183.152:4433/CrowdApp/getIntervals.php";
    public static String SubmitAnswerURL = "https://72.14.183.152:4433/CrowdApp/submitAnswer.php";
    public static String SubmitIntervalUpdate = "https://72.14.183.152:4433/CrowdApp/userIntervalUpdate.php";
    //S_Farah
    public static String OFUploadURL = "https://72.14.183.152:4433/CrowdApp/InsertOF.php";
    public static String UTUploadURL = "https://72.14.183.152:4433/CrowdApp/InsertUT.php";
    //E_Farah
    public static String ScreenUploadURL = "https://72.14.183.152:4433/CrowdApp/InsertScreen.php";
    public static String PackagesUploadURL = "https://72.14.183.152:4433/CrowdApp/InsertPackages.php";
    public static String UploadHost = "72.14.183.152";


   /* public static String TFUploadURL = "https://192.168.137.79/CrowdApp/InsertTF.php";
    public static String CPCUploadURL = "https://192.168.137.79/CrowdApp/InsertCPC.php";
    public static String CxUploadURL = "https://192.168.137.79/CrowdApp/InsertCx.php";
    public static String registrationUrl = "https://192.168.137.79/CrowdApp/fcm_insert.php";
    public static String DownloadThresholdsURL = "https://192.168.137.79/CrowdApp/getThresholds.php";
    public static String DownloadIntervalsURL = "https://192.168.137.79/CrowdApp/getIntervals.php";
    public static String SubmitAnswerURL = "https://192.168.137.79/submitAnswer.php";
    public static String SubmitIntervalUpdate = "https://192.168.137.79/CrowdApp/userIntervalUpdate.php";
    //S_Farah
    public static String OFUploadURL = "https://192.168.137.79/CrowdApp/InsertOF.php";
    public static String UTUploadURL = "https://192.168.137.79/CrowdApp/InsertUT.php";
    //E_Farah
    public static String ScreenUploadURL = "https://192.168.137.79/CrowdApp/InsertScreen.php";
    public static String PackagesUploadURL = "https://192.168.137.79/CrowdApp/InsertPackages.php";
    public static String UploadHost = "192.168.137.79";*/


    public static String UploadTypeFile = "File";
    public static String UploadTypeDir = "Dir";

    public static List<String> installed3rdPartyApps = new ArrayList<>();
    public static  List<ApplicationInfo> installedPackages;

    public static Calendar cal = Calendar.getInstance();
    public static AlarmManager alarm;
    public static PendingIntent pintent;

    public static ResultsReceiver uploadResultDir = new ResultsReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case ClientServerService.STATUS_RUNNING:
                    // use this flag somewhere
                    break;
                case ClientServerService.STATUS_FINISHED:
                    int status = resultData.getInt("Status");
                    String type = "";
                    String error = "";
                    switch (status) {
                        case ClientServerService.STATUS_FINISHED_SUCCESS:
                            type = resultData.getString("type");
                            Log.i(ClientServerService.TAG, "Files from " + type + " directory are uploaded !");

                            //TODO update the interval or Threshold According to the response
                            if(CommonVariables.startUpdateThresholds){
                                Common.getThresholds(mContext);
                            }
                            if(CommonVariables.startUpdateIntervals){
                                Common.getIntervals(mContext);
                            }
                            break;
                        case ClientServerService.STATUS_FINISHED_NOFILES:
                            type = resultData.getString("type");
                            Log.i(ClientServerService.TAG, "No Files found in " + type + " directory !");
                            break;
                        case ClientServerService.STATUS_FINISHED_NOWIFI:
                            Log.i(ClientServerService.TAG, "No Wifi Schedule upload for later");
                            break;
                        case ClientServerService.STATUS_FINISHED_SERVER_ERROR:
                            error = resultData.getString("finished_message");
                            FirebaseCrash.report(new Exception("Server Error: " + error));
                            break;
                        case ClientServerService.STATUS_FINISHED_ERROR:
                            error = resultData.getString("Error_message");
                            FirebaseCrash.report(new Exception("Server Failure: " + error));
                            break;
                        case ClientServerService.STATUS_FINISHED_FORBIDDEN:
                            FirebaseCrash.report(new Exception("Unauthorized Access"));
                            break;
                    }
                    startUploadDir = false;
                    break;
                case ClientServerService.STATUS_ERROR:
                    error = resultData.getString("result");
                    Log.i("FileUploadDir", "Boom");
                    Log.i("FileUploadDir", error);
                    FirebaseCrash.report(new Exception(error));
                    startUploadDir = false;
                    break;
            }
            alarm.cancel(pintent);
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

    //S_Farah
    public static void changeOFBkupName(String Name) {
        OFBkup = Name;
    }

    public static void changeUTBkupName(String Name) {
        UTBkup = Name;
    }
    //E_Farah
}
