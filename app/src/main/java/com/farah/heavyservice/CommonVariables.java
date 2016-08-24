package com.farah.heavyservice;

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
    public static boolean startUpload=false;

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
