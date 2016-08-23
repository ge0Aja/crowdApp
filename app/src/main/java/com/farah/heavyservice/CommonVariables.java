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
    public static boolean uploadFile = false;
    public static boolean uploadType = false;


    private static String fileToUpload = "";
    private static String fileUploadType = "";
    public static String TFBkup = "TrafficStatsBkup";
    public static String CPCBkup = "CPUMEMStatsBkup";
    public static String CxBkup = "CxStatsBkup";
}
