package com.farah.heavyservice;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Georgi on 8/23/2016.
 *
 * the common variables class include static variables that are used on a global scale in the App
 *
 * thresholds and intervals have a preset value at the start of the application in case of the App
 * didn't find a way to connect to the server
 * then , the thresholds and invervals will get updated once the app is connected
 */
public class CommonVariables {

    public static final int PERMISSION_ALL =1;

    public static final String filetypeTf = "TF";
    public static final String filetypeCx = "CX";
    public static final String filetypeCPC = "CP";
    //S_Farah
    public static final String filetypeOF = "OF";
    public static final String filetypeUT = "UT";
    //E_Farah
    public static final String filetypeCxCount = "CxCount";

    public static final String filetypeScreen = "Screen";
    public static final String filetypePackage = "Package";
    public static final String filetypeAnswers = "Answers";
    public static final String filetypeAll = "All";
    public static final String th_cxAge = "cxAge";
    public static final String th_cxCount = "cxCount";
    public static final String th_prCPU = "prCPU";
    public static final String th_prRSS = "prRSS";
    public static final String th_prVSS = "prVSS";
    public static final String th_prOF = "prOF";
    public static final String th_prUT = "prUT";
    public static final String th_txBytes = "txBytes";
    public static final String th_rxBytes = "rxBytes";

    // check threshold interval
    public static final String th_txPackets = "txPackets";
    public static final String th_rxPackets = "rxPackets";
    public static String TAG = "HeavyService";
    public static boolean startService = false;
    public static boolean startApp = false;
    public static String[] Permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static Context mContext;
    public static String username;
    public static String password = "v3Ry$t0ngP@$$w0rd!";
    //stats collection interval
    public static int collectInterval = 10000;
    // start_upload interval for Dir after connecting to wifi
    public static int uploadIntervalNormal = 120000;


    // repeat_upload interval for Dir after connecting to wifi
    public static int uploadIntervalRetry = 7200000;

    // checking thresholds intervals
    public static int checkCPCThresholdInterval = 120 * 1000;
    public static int checkCxnThresholdInterval = 120 * 1000;
    public static int checkTfThresholdInterval = 120 * 1000;
    public static boolean checkCPCT = false;
    public static boolean checkTfT = false;
    public static boolean checkCxT = false;
    public static boolean RequestedThresholds = false;
    //set max file size to upload
    public static int maxFileSize = 25;
    //set max file size for screen log
    public static int maxFileSizeScreen = 5;
    //set max file size for OF UT log
    public static int maxFileSizeOFUT = 5;
    public static int checkEvents = 0;
    public static boolean screenOn = false;
    public static boolean isWiFi = false;
    public static boolean isServerReachable = false;
    public static boolean startUpload = false;
    public static boolean startUploadDir = false;
    public static boolean thresholdsAvailable = false;
    public static boolean userRegistered = false;
    //E_Farah
    public static boolean startUpdateIntervals = false;
    public static boolean startUpdateThresholds = false;
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
    public static String CxCountBkup;
    public static String AnswersBkup = "Answers";
    public static HashMap<String, HashMap<String, HashMap<String, Float>>> thresholdsMap;
    public static HashMap<String, Float> ratingsMap;
    public static String thresholdsFile = "Thresholds";
    public static String ratingsFile = "Ratings";

    public static boolean checkChange = false ;

//    // URLS from the server used in the process
//    public static String TFUploadURL = "https://192.168.137.43/CrowdApp/InsertTF.php";
//    public static String CPCUploadURL = "https://192.168.137.43/CrowdApp/InsertCPC.php";
//    public static String CxUploadURL = "https://192.168.137.43/CrowdApp/InsertCx.php";
//
//    // public static String CxUploadURL = "https://192.168.137.79/CrowdApp/InsertCxSpecial.php";
//    public static String registrationUrl = "https://192.168.137.43/CrowdApp/fcm_insert.php";
//    public static String DownloadThresholdsURL = "https://192.168.137.43/CrowdApp/getThresholds.php";
//    public static String DownloadIntervalsURL = "https://192.168.137.43/CrowdApp/getIntervals.php";
//    public static String DownloadRatingsURL = "https://192.168.137.43/CrowdApp/getAppRatings.php";
//    public static String SubmitAnswerURL = "https://192.168.137.43/CrowdApp/submitAnswer.php";
//    public static String SubmitIntervalUpdate = "https://192.168.137.43/CrowdApp/userIntervalUpdate.php";
//    public static String SubmitAlarm = "https://192.168.137.43/CrowdApp/submitAlarm.php";
//    public static String SubmitMultiAnswer = "https://192.168.137.43/CrowdApp/submitAnswerMulti.php";
//    //S_Farah
//    public static String OFUploadURL = "https://192.168.137.43/CrowdApp/InsertOF.php";
//    public static String UTUploadURL = "https://192.168.137.43/CrowdApp/InsertUT.php";
//    //E_Farah
//    public static String CxCountUploadURL = "https://192.168.137.43/CrowdApp/InsertCxCount.php";
//    public static String ScreenUploadURL = "https://192.168.137.43/CrowdApp/InsertScreen.php";
//    public static String PackagesUploadURL = "https://192.168.137.43/CrowdApp/InsertPackages.php";
//    public static String SubmitThresholdUpdate = "https://192.168.137.43/CrowdApp/userThresholdUpdate.php";
//    public static String UploadHost = "192.168.137.43";

    // URLS from the server used in the process
    public static final String TFUploadURL = "https://crowdappaub.com/CrowdApp/InsertTF.php";
    public static final String CPCUploadURL = "https://crowdappaub.com/CrowdApp/InsertCPC.php";
    public static final String CxUploadURL = "https://crowdappaub.com/CrowdApp/InsertCx.php";

    // public static String CxUploadURL = "https://192.168.137.79/CrowdApp/InsertCxSpecial.php";
    public static final String registrationUrl = "https://crowdappaub.com/CrowdApp/fcm_insert.php";
    public static final String DownloadThresholdsURL = "https://crowdappaub.com/CrowdApp/getThresholds.php";
    public static final String DownloadIntervalsURL = "https://crowdappaub.com/CrowdApp/getIntervals.php";
    public static final String DownloadRatingsURL = "https://crowdappaub.com/CrowdApp/getAppRatings.php";
    public static final String SubmitAnswerURL = "https://crowdappaub.com/CrowdApp/submitAnswer.php";
    public static final String SubmitIntervalUpdate = "https://crowdappaub.com/CrowdApp/userIntervalUpdate.php";
    public static final String SubmitAlarm = "https://crowdappaub.com/CrowdApp/submitAlarm.php";
    public static final String SubmitMultiAnswer = "https://crowdappaub.com/CrowdApp/submitAnswerMulti.php";
    //S_Farah
    public static final String OFUploadURL = "https://crowdappaub.com/CrowdApp/InsertOF.php";
    public static final String UTUploadURL = "https://crowdappaub.com/CrowdApp/InsertUT.php";
    //E_Farah
    public static final String CxCountUploadURL = "https://crowdappaub.com/CrowdApp/InsertCxCount.php";
    public static final String ScreenUploadURL = "https://crowdappaub.com/CrowdApp/InsertScreen.php";
    public static final String PackagesUploadURL = "https://crowdappaub.com/CrowdApp/InsertPackages.php";
    public static final String SubmitThresholdUpdate = "https://crowdappaub.com/CrowdApp/userThresholdUpdate.php";
    public static final String UploadHost = "crowdappaub.com";


    public static final String UploadTypeFile = "File";
    public static final String UploadTypeDir = "Dir";

    public static CopyOnWriteArrayList<String> installed3rdPartyApps = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<ApplicationInfo> installedPackages;

    public static Calendar cal = Calendar.getInstance();
    public static AlarmManager alarm;
    public static PendingIntent pintent;

    // this reuslt receiver is used with multiple files upload services
    // the service will broadcast finish information and it will be received in this receiver
    // the upload service will send the resultCode and and uploaded files type
    // if the files are uploaded the receiver will delete the files
    // incase of an error it wil be reported using Firebase
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
                    String currentFile = "";
                    String error = "";
                    switch (status) {
                        case ClientServerService.STATUS_FINISHED_SUCCESS:
                            type = resultData.getString("type");
                            Log.d(ClientServerService.TAG, "Files from " + type + " directory are uploaded !");
                            currentFile = resultData.getString("currentfile");
                            Log.d(TAG, "attempt to delete the file " + currentFile + " of type " + type);
                            Common.deleteFilesFromDirectory(type,currentFile);

                            if (type.equals(filetypeScreen)) {
                                if(CommonVariables.startUpdateThresholds){
                                    Common.getThresholds(mContext);
                                }
                                if(CommonVariables.startUpdateIntervals){
                                    Common.getIntervals(mContext);
                                }
                            }
                            break;
                        case ClientServerService.STATUS_FINISHED_NOFILES:
                            type = resultData.getString("type");
                            Log.d(ClientServerService.TAG, "No Files found in " + type + " directory !");
                            break;
                        case ClientServerService.STATUS_FINISHED_NOWIFI:
                            Log.d(ClientServerService.TAG, "No Wifi Schedule upload for later");
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
                        case ClientServerService.STATUS_FINISHED_NO_RESPONSE_FROM_SERVER:
                            Log.d(ClientServerService.TAG, " DirUpload No response from server !");
                            //file not confirmed
                            break;
                    }
                    startUploadDir = false;
                    break;
                case ClientServerService.STATUS_ERROR:
                    error = resultData.getString("result");
                    Log.d("FileUploadDir", "Boom");
                    Log.d("FileUploadDir", error);
                    FirebaseCrash.report(new Exception(error));
                    startUploadDir = false;
                    break;
            }
            alarm.cancel(pintent);
        }
    };

    // a public method to set the wifi property
    public static void setWiFi(boolean WiFi) {
        isWiFi = WiFi;
    }

    public static void setServerReachable(boolean Reachable){
        isServerReachable = Reachable;
    }

    // set the upload setting when a file reached the maximum size
    public static void setUploadSettings(String fileName, Boolean upload, String filetypetoupload) {
        fileToUpload = fileName;
        startUpload = upload;
        fileUploadType = filetypetoupload;
    }

    /* change the backup files names when the upload service is started for a specific type*/
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

    public static void changeScreenBkupName(String Name){
        ScreenBkup = Name;
    }

    public static void changePackageBkupName(String Name) {
        PackagesBkup = Name;
    }

    public static void changeCxCountBkupName(String Name) {
        CxCountBkup = Name;
    }

    public static void changeCheckFlag(boolean b){
        checkChange = b;
    }


}
