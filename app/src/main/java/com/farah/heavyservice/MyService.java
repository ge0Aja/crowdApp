package com.farah.heavyservice;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.DatabaseErrorHandler;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;


public class MyService extends Service {
    public final String TAG = "Heavy Service";
    String messageLogged = "";
    private static boolean isWiFi = false;
    private static boolean startUpload = false;
    private static String fileToUpload = "";
    private static String fileUploadType = "";

    public static String filetypeCPC = "CPC";
    public static String filetypeCx = "Cx";
    public static String filetypeTf = "TF";


    public static String TFBkup = "TrafficStatsBkup";
    public static String CPCBkup = "CPUMEMStatsBkup";
    public static String CxBkup = "CxStatsBkup";

    //TODO read from files if they exist if not create new outerHashses

    HashMap<String, HashMap<String, HashMap<String, String>>> catOuterHash = new HashMap<>();


    HashMap<String, HashMap<String, Long>> outerHash = new HashMap<String, HashMap<String, Long>>();
    HashMap<String, HashMap<String, String>> outerHashCPUMEM = new HashMap<String, HashMap<String, String>>();


    //TODO read from files if they exist if not create new Cumulative outerHashses
    HashMap<String, HashMap<String, Long>> cumulativeOuterHash;//= new HashMap<String, HashMap<String, Long>>();
    HashMap<String, HashMap<String, HashMap<String, String>>> cumulativeOuterHashCx;//= new HashMap<String, HashMap<String, String>>();


    public static void setUploadSettings(String fileName, Boolean upload, String filetypetoupload){

        fileToUpload = fileName;
        startUpload = upload;
        fileUploadType = filetypetoupload;
    }

    public static void setWiFi(boolean WiFi) {
        isWiFi = WiFi;
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

    private void getOuterHashes() {
        try {
            //Log.i("ListCumTraffic",Common.readListFromFiletf("CumulativeTrafficStatsBkup").get(Common.readListFromFilecpc("CumulativeTrafficStatsBkup").size() - 1).toString());
            cumulativeOuterHash = Common.readListFromFiletf("CumulativeTrafficStatsBkup").get(Common.readListFromFilecpc("CumulativeTrafficStatsBkup").size() - 1);
        } catch (Exception e) {
            cumulativeOuterHash = new HashMap<String, HashMap<String, Long>>();
        }
        try {
            //  Log.i("ListCumCx",Common.readListFromFilecpc("CumulativeCxStatsBkup").get(Common.readListFromFilecpc("CumulativeCxStatsBkup").size() - 1).toString());
            cumulativeOuterHashCx = Common.readListFromFilecxn("CumulativeCxStatsBkup").get(Common.readListFromFilecpc("CumulativeCxStatsBkup").size() - 1);
        } catch (Exception e) {
            cumulativeOuterHashCx = new HashMap<String, HashMap<String, HashMap<String, String>>>();
        }

    }


    // The broadcast receiver is used to detect changes in screen status, new installations, and packages removed from device
    public boolean screenON = false;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String packageName;
            String action = intent.getAction();
            switch (action) {
                case "Intent.ACTION_SCREEN_OFF":
                    messageLogged = "Screen switched off ";
                    Common.appendLog(messageLogged);
                    Log.i("Reduced", messageLogged);
                    screenON = false;
                    break;
                case "Intent.ACTION_SCREEN_ON":
                    messageLogged = "Screen switched on ";
                    Common.appendLog(messageLogged);
                    Log.i("Reduced", messageLogged);
                    screenON = true;
                    break;
                case "Intent.ACTION_PACKAGE_ADDED":
                    packageName = intent.getData().getEncodedSchemeSpecificPart();
                    messageLogged = Common.getAppName(packageName, getApplicationContext()) + " installed";
                    Common.appendLog(messageLogged);
                    Log.i("Reduced", messageLogged);
                    break;
                case "Intent.ACTION_PACKAGE_REMOVED":
                    Uri uri = intent.getData();
                    packageName = uri != null ? uri.getSchemeSpecificPart() : null;
                    try {
                        Date dateAdded = new Date(context.getPackageManager().getPackageInfo(packageName, 0).firstInstallTime);
                        messageLogged = Common.getAppName(packageName, getApplicationContext()) + " uninstalled (installed on " + dateAdded + ")";
                        Common.appendLog(messageLogged);
                        Log.i("Reduced", messageLogged);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    //Results receiver for the upload service
    ResultsReceiver uploadResult = new ResultsReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case ClientServerService.STATUS_RUNNING:
                    String filenametype = (String) resultData.get("type");
                    switch (filenametype) {
                        case "CPC":
                            changeCPCBkupName(CPCBkup + System.currentTimeMillis());
                            break;
                        case "Cx":
                            changeCxBkupName(CxBkup + System.currentTimeMillis());
                            break;
                        case "TF":
                            changeTFBkupName(TFBkup + System.currentTimeMillis());
                            break;
                    }
                    Log.i("FileName", "Backup File Name are changed");
                    break;
                case ClientServerService.STATUS_FINISHED:
                    //delete the file from filename
                    Log.i("FileUpload",resultData.get("result").toString());
                    Log.i("FileUpload", "Files are Uploaded Successfully");
                    startUpload =false;
                    break;
                case ClientServerService.STATUS_ERROR:
                    startUpload =false;
                    String error = resultData.getString("result");
                    Log.i("FileUpload", "Boom");
                    Log.i("FileUpload", error);
                    startUpload =false;
                    stopSelf();
                    //keep the file and reschedule an upload for that file
                    break;
            }
        }
    };

    // These define the collection frequency (collect every 10 seconds)
    //TODO we have to replace the hard coded intervals
    Integer interval = 10000;
    int uploadInterval = 3;
    Timer timer = new Timer();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(), "Service Started..", Toast.LENGTH_LONG).show();
        // This snippet runs the Linux top command every 'interval' amount of milliseconds
        isWiFi = Common.isConnectedToWifi(getApplicationContext());
        final PackageManager PM = getApplicationContext().getPackageManager();
        final List<String> installedPackagesRunning = new ArrayList<>();
        final List<String> installed3rdPartyApps = new ArrayList<>();
        final String protocol = "";
        final String previousOnTop = "";
        getOuterHashes();
        timer.schedule(new TimerTask() {
            public void run() {
                try {

                    // for conenctions info
                    String nLine = null;
                    HashSet<String> usedProtocols = new HashSet<String>();
                    Process nProcess = Runtime.getRuntime().exec("netstat -a");
                    BufferedReader nBufferedStream = new BufferedReader(new InputStreamReader(nProcess.getInputStream()));
                    nBufferedStream.readLine();
                    while ((nLine = nBufferedStream.readLine()) != null) {
                        String delimiter[] = nLine.split("  ");
                        String temp = delimiter[0].trim();
                        if (!temp.equals(protocol)) {
                            usedProtocols.add(temp);
                            protocol.equals(temp);
                        }
                    }

                    String catLine = null;
                    Iterator Iterator = usedProtocols.iterator();
                    catOuterHash.clear();
                    while (Iterator.hasNext()) {
                        String P = Iterator.next().toString();
                        String command = "cat /proc/net/" + P;
                        Process catProcess = Runtime.getRuntime().exec(command);
                        BufferedReader catBufferedStream = new BufferedReader(new InputStreamReader(catProcess.getInputStream()));
                        catBufferedStream.readLine();

                        while ((catLine = catBufferedStream.readLine()) != null) {
                            String[] tokens = catLine.split("\\s+");
                            String sourceIP = tokens[2].split(":")[0];
                            String sourcePort = tokens[2].split(":")[1];
                            String destinationIP = tokens[3].split(":")[0];
                            String destinationPort = tokens[3].split(":")[1];

                            String callingApp = getApplicationContext().getPackageManager().getNameForUid(Integer.valueOf(tokens[8]));

                            HashMap<String, String> catInnerHash = new HashMap<>();
                            //  catInnerHash.put("Protocol", P);
                            catInnerHash.put("Timestamp", String.valueOf(System.currentTimeMillis()));
                            // catInnerHash.put("SourceIP", Common.convertHexToString(sourceIP));
                            // catInnerHash.put("SourcePort", String.valueOf(Integer.parseInt(sourcePort, 16)));
                            // catInnerHash.put("DestinationIP", Common.convertHexToString(destinationIP));
                            // catInnerHash.put("DestinationPort", String.valueOf(Integer.parseInt(destinationPort, 16)));
                            if (cumulativeOuterHashCx.get(callingApp) == null) {
                                catInnerHash.put("Age", "0");
                                //listIncatOuterHash.add(catInnerHash);
                                HashMap<String, HashMap<String, String>> midHash = new HashMap<String, HashMap<String, String>>();
                                midHash.put(P + sourceIP + sourcePort + destinationIP + destinationPort, catInnerHash);
                                cumulativeOuterHashCx.put(callingApp, midHash);
                                catOuterHash.put(callingApp, midHash);
                                // listIncumulativeOuterHashCx.add(catInnerHash);
                            } else {
                                if (cumulativeOuterHashCx.get(callingApp).get(P + sourceIP + sourcePort + destinationIP + destinationPort) == null) {
                                    catInnerHash.put("Age", "0");
                                    cumulativeOuterHashCx.get(callingApp).put(P + sourceIP + sourcePort + destinationIP + destinationPort, catInnerHash);
                                    if (catOuterHash.get(callingApp) != null) {
                                        catOuterHash.get(callingApp).put(P + sourceIP + sourcePort + destinationIP + destinationPort, catInnerHash);
                                    } else {
                                        HashMap<String, HashMap<String, String>> midHash = new HashMap<String, HashMap<String, String>>();
                                        midHash.put(P + sourceIP + sourcePort + destinationIP + destinationPort, catInnerHash);
                                        catOuterHash.put(callingApp, midHash);
                                    }
                                } else {
                                    if (Long.valueOf(catInnerHash.get("Timestamp")) - Long.valueOf(cumulativeOuterHashCx.get(callingApp).get(P + sourceIP + sourcePort + destinationIP + destinationPort).get("Timestamp"))
                                            < 3600000) {
                                        cumulativeOuterHashCx.get(callingApp).get(P + sourceIP + sourcePort + destinationIP + destinationPort).put("Age",
                                                String.valueOf(Integer.valueOf(cumulativeOuterHashCx.get(callingApp).get(P + sourceIP + sourcePort + destinationIP + destinationPort).get("Age")) + interval));
                                        catInnerHash.put("Age", String.valueOf(Integer.valueOf(cumulativeOuterHashCx.get(callingApp).get(P + sourceIP + sourcePort + destinationIP + destinationPort).get("Age")) + interval));

                                        if (catOuterHash.get(callingApp) == null) {
                                            HashMap<String, HashMap<String, String>> midHash = new HashMap<String, HashMap<String, String>>();
                                            midHash.put(P + sourceIP + sourcePort + destinationIP + destinationPort, catInnerHash);
                                            catOuterHash.put(callingApp, midHash);//;.put(P + sourceIP + sourcePort + destinationIP + destinationPort, catInnerHash);//.put("Age", String.valueOf(Integer.valueOf(cumulativeOuterHashCx.get(callingApp).get(P + sourceIP + sourcePort + destinationIP + destinationPort).get("Age"))));
                                        } else {
                                            if (catOuterHash.get(callingApp).get(P + sourceIP + sourcePort + destinationIP + destinationPort) == null) {
                                                catOuterHash.get(callingApp).put(P + sourceIP + sourcePort + destinationIP + destinationPort, catInnerHash);
                                            }
                                        }
                                    } else {
                                        catInnerHash.put("Age", "0");
                                        cumulativeOuterHashCx.get(callingApp).put(P + sourceIP + sourcePort + destinationIP + destinationPort, catInnerHash);
                                        catOuterHash.get(callingApp).put(P + sourceIP + sourcePort + destinationIP + destinationPort, catInnerHash);
                                    }
                                }

                            }

                        }
                    }
                    Log.i("CxN", catOuterHash.toString());
                    Log.i("CxNCumulative", cumulativeOuterHashCx.toString());
                    //end for connections info

                    // Start CPU and MEM Stats
                    // HashMap<String, Long> cumulativeInnerHash = new HashMap<String, Long>();
                    String pLine = null;
                    Process pmProcess = Runtime.getRuntime().exec("pm list packages -3");
                    BufferedReader pmBufferedStream = new BufferedReader(new InputStreamReader(pmProcess.getInputStream()));
                    while ((pLine = pmBufferedStream.readLine()) != null) {
                        String packageLine[] = pLine.split(":");
                        String packageName = packageLine[packageLine.length - 1];
                        installed3rdPartyApps.add(packageName);
                    }
                    installedPackagesRunning.clear();
                    List<ApplicationInfo> installedPackages = PM.getInstalledApplications(0);

                    String tLine = null;
                    Process topProcess = Runtime.getRuntime().exec("top -n 1 -d 0");
                    BufferedReader topBufferedStream = new BufferedReader(new InputStreamReader(topProcess.getInputStream()));
                    outerHashCPUMEM.clear();
                    while ((tLine = topBufferedStream.readLine()) != null) {
                        String aName = "";
                        if (!tLine.contains("u0_")) {
                            continue;
                        }
                        String[] tokens = tLine.split("\\s+");
                        //  topClass top = new topClass();
                        HashMap<String, String> innerHashCPUMEM = new HashMap<String, String>();
                        if (tokens.length == 10) {
                            innerHashCPUMEM.put("PID", tokens[0]);
                            innerHashCPUMEM.put("CPU", tokens[2]);
                            innerHashCPUMEM.put("VSS", tokens[5]);
                            innerHashCPUMEM.put("RSS", tokens[6]);
                            innerHashCPUMEM.put("PCY", tokens[7]);
                            innerHashCPUMEM.put("UID", tokens[8]);
                            innerHashCPUMEM.put("pName", tokens[9]);
                        } else if (tokens.length == 11) {
                            innerHashCPUMEM.put("PID", tokens[1]);
                            innerHashCPUMEM.put("CPU", tokens[3]);
                            innerHashCPUMEM.put("VSS", tokens[6]);
                            innerHashCPUMEM.put("RSS", tokens[7]);
                            innerHashCPUMEM.put("PCY", tokens[8]);
                            innerHashCPUMEM.put("UID", tokens[9]);
                            innerHashCPUMEM.put("pName", tokens[10]);
                        }
                        innerHashCPUMEM.put("Timestamp", String.valueOf(System.currentTimeMillis()));
                        installedPackagesRunning.add(innerHashCPUMEM.get("pName"));
                        aName = Common.getAppName(innerHashCPUMEM.get("pName"), getApplicationContext());
                        if (aName.equals("(unknown)")) {
                            aName = innerHashCPUMEM.get("pName");
                        }
                        outerHashCPUMEM.put(aName, innerHashCPUMEM);
                        if (installed3rdPartyApps.contains(innerHashCPUMEM.get("pName"))) {
                            messageLogged = aName + " CPU " + innerHashCPUMEM.get("CPU") + " VSS " + innerHashCPUMEM.get("VSS") + " RSS " + innerHashCPUMEM.get("RSS");
                            Common.appendLog(messageLogged);
                            Log.i("Reduced", messageLogged);
                            if (innerHashCPUMEM.get("PCY").equals("fg") && !aName.equals("top")) {
                                messageLogged = aName + " on top";
                                Common.appendLog(messageLogged);
                                Log.i("Reduced", messageLogged);
                                if (!aName.equals(previousOnTop)) {
                                    messageLogged = aName + " opened";
                                    Common.appendLog(messageLogged);
                                    Log.i("Reduced", messageLogged);
                                    previousOnTop.equals(aName);
                                }
                            }
                        }
                    }
                    // add the results of the while to the cumulative list
                    //CPUMEMStats.add(outerHashCPUMEM);
                    Log.i("CPUMEM", outerHashCPUMEM.toString());
                    //End CPU and MEM Stats
                    //Start Traffic Stats
                    // This for loop removes installed apps which are not running (installedPackages - !installedPackagesRunning)
                    for (Iterator<ApplicationInfo> iterator = installedPackages.iterator(); iterator.hasNext(); ) {
                        if (!installedPackagesRunning.contains(iterator.next().packageName)) {
                            iterator.remove();
                        }
                    }

                    // This for loop computes the 4 traffic stats for each running app (collected value - previous value)
                    HashMap<String, Long> innerHash = new HashMap<String, Long>();

                    outerHash.clear();
                    for (ApplicationInfo app : installedPackages) {
                        String appName = app.loadLabel(PM).toString();
                        int uid = app.uid;
                        long txBytes = TrafficStats.getUidTxBytes(uid);
                        long rxBytes = TrafficStats.getUidRxBytes(uid);
                        long txPackets = TrafficStats.getUidTxPackets(uid);
                        long rxPackets = TrafficStats.getUidRxPackets(uid);

                        if (!cumulativeOuterHash.containsKey(appName)) {
                            innerHash.put("txBytes", Long.valueOf(0));
                            innerHash.put("rxBytes", Long.valueOf(0));
                            innerHash.put("txPackets", Long.valueOf(0));
                            innerHash.put("rxPackets", Long.valueOf(0));
                            innerHash.put("Timestamp", System.currentTimeMillis());
                            outerHash.put(appName, innerHash);

                            HashMap<String, Long> cumulativeInnerHash = new HashMap<String, Long>();
                            cumulativeInnerHash.put("txBytes", txBytes);
                            cumulativeInnerHash.put("rxBytes", rxBytes);
                            cumulativeInnerHash.put("txPackets", txPackets);
                            cumulativeInnerHash.put("rxPackets", rxPackets);
                            cumulativeOuterHash.put(appName, cumulativeInnerHash);
                        } else {
                            innerHash.put("txBytes", txBytes - cumulativeOuterHash.get(appName).get("txBytes"));
                            innerHash.put("rxBytes", rxBytes - cumulativeOuterHash.get(appName).get("rxBytes"));
                            innerHash.put("txPackets", txPackets - cumulativeOuterHash.get(appName).get("txPackets"));
                            innerHash.put("rxPackets", rxPackets - cumulativeOuterHash.get(appName).get("rxPackets"));
                            innerHash.put("Timestamp", System.currentTimeMillis());
                            outerHash.put(appName, innerHash);
                            cumulativeOuterHash.get(appName).put("txBytes", txBytes);
                            cumulativeOuterHash.get(appName).put("rxBytes", rxBytes);
                            cumulativeOuterHash.get(appName).put("txPackets", txPackets);
                            cumulativeOuterHash.get(appName).put("rxPackets", rxPackets);
                            cumulativeOuterHash.get(appName).put("Timestamp", System.currentTimeMillis());
                        }
                        messageLogged = appName + " TxBytes " + outerHash.get(appName).get("txBytes") + " RxBytes " + outerHash.get(appName).get("rxBytes") + " TxPackets " + outerHash.get(appName).get("txPackets") + " RxPackets " + outerHash.get(appName).get("rxPackets");
                        Common.appendLog(messageLogged);
                        Log.i("Traffic", messageLogged);

                    }
                    Log.i("TrafficHash", cumulativeOuterHash.toString());
                    //  trafficStats.add(cumulativeOuterHash);
                    // cumulativeTrafficStats.add(cumulativeOuterHash);

                    // End Traffic Stats

                    // Start Write Lists to Storage
                    Common.writeListToFile(outerHash, TFBkup, true);
                    Common.writeListToFilecpc(outerHashCPUMEM, CPCBkup, true);
                    Common.writeListToFilecxn(catOuterHash, CxBkup, true);
                    Common.writeListToFile(cumulativeOuterHash, "CumulativeTrafficStatsBkup", false);
                    Common.writeListToFilecxn(cumulativeOuterHashCx, "CumulativeCxStatsBkup", false);

                    //End Write Lists to storage

                } catch (IOException e) {
                    e.printStackTrace();
                }
// this schedules the upload service to run at user click

                if (isWiFi) {
                    Log.i("WiFi", "The phone is connected to wifi");
                    if (startUpload) {
                        Intent intent = new Intent(Intent.ACTION_SYNC, null,getApplicationContext(), ClientServerService.class);
                        intent.putExtra("url","http://192.168.137.234/CrowdApp/InsertUser.php");
                        intent.putExtra("filename",fileToUpload);
                        intent.putExtra("type",fileUploadType);
                        intent.putExtra("receiver",uploadResult);
                        startService(intent);
                    }
                    // check upload service interval
                    // call upload service
                } else {
                    Log.i("WiFi", "Phone is not connected wait");
                    //wait for the broadcast recevier
                }
            }
        }, 0, interval);


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), "Service Stopped..", Toast.LENGTH_LONG).show();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        timer.cancel();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}