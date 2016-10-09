package com.farah.heavyservice;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MyService extends Service {
    //Results receiver for the upload service
    public static ResultsReceiver uploadResult = new ResultsReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.i(ClientServerService.TAG, "Results are received in the MyServiceReceiver");
            switch (resultCode) {
                case ClientServerService.STATUS_RUNNING:
                    String filenametype = (String) resultData.get("type");
                    switch (filenametype) {
                        case "CPC":
                            CommonVariables.changeCPCBkupName("CPUMEMStatsBkup" + System.currentTimeMillis());
                            break;
                        case "Cx":
                            CommonVariables.changeCxBkupName("CxStatsBkup" + System.currentTimeMillis());
                            break;
                        case "TF":
                            CommonVariables.changeTFBkupName("TrafficStatsBkup" + System.currentTimeMillis());
                            break;
                        //S_Farah
                        case "OF":
                            CommonVariables.changeOFBkupName("OpeningFrequencyBkup" + System.currentTimeMillis());
                            break;
                        case "UT":
                            CommonVariables.changeUTBkupName("UsageTimeBkup" + System.currentTimeMillis());
                            break;
                        //E_Faarah
                        case "Screen":
                            CommonVariables.changeScreenBkupName("ScreenStatsBkup" + System.currentTimeMillis());
                            break;
                        case "Packages":
                            CommonVariables.changePackageBkupName("PackageBkup" + System.currentTimeMillis());
                            break;
                    }
                    Log.i("FileName", "Backup File Name are changed");
                    break;
                case ClientServerService.STATUS_FINISHED_NOWIFI:
                    Log.i(ClientServerService.TAG, "Wifi Schedule upload for later");
                    break;
                case ClientServerService.STATUS_FINISHED:
                    //delete the file from filename
                    int status = resultData.getInt("Status");
                    String filePlanned = resultData.getString("filename");
                    String filePlannedType = resultData.getString("type");
                    switch (status) {
                        case ClientServerService.STATUS_FINISHED_SUCCESS:
                            Log.i(ClientServerService.TAG, " FileUpload Files are Uploaded Successfully");
                            File delF = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + filePlannedType + "/" + filePlanned);
                            boolean del = delF.delete();
                            if (del)
                                Log.i("FileUpload", "Files are Deleted Successfully");
                            else {
                                File delFR = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + filePlannedType + "/delete" + System.currentTimeMillis());
                                delF.renameTo(delFR);
                                delF.renameTo(delFR);
                            }

                            //TODO update the interval or Threshold According to the response
                            if (CommonVariables.startUpdateThresholds) {
                                Common.getThresholds(CommonVariables.mContext);
                            }
                            if (CommonVariables.startUpdateIntervals) {
                                Common.getIntervals(CommonVariables.mContext);
                            }
                            break;
                        case ClientServerService.STATUS_FINISHED_NO_RESPONSE_FROM_SERVER:
                            Log.i(ClientServerService.TAG, " FileUpload No response from server !");
                            FirebaseCrash.report(new Exception("Server Response is Blank"));
                            break;
                        case ClientServerService.STATUS_FINISHED_ERROR:
                            String[] error = resultData.getStringArray("result");
                            // FirebaseCrash.report(new Exception(error[0]+"Server Response is Blank"));
                            switch (error[0]) {
                                case "finished":
                                    FirebaseCrash.report(new Exception("Server Error: " + error[3]));
                                    break;
                                case "Unauthorized":
                                    FirebaseCrash.report(new Exception("Unauthorized Access"));
                                    break;
                                case "fail":
                                    FirebaseCrash.report(new Exception("Server Failure: " + error[1]));
                                    break;
                            }
                            break;
                    }
                    CommonVariables.startUpload = false;
                    break;
                case ClientServerService.STATUS_ERROR:
                    String error = resultData.getString("result");
                    Log.i(ClientServerService.TAG, "FileUpload Boom");
                    Log.i(ClientServerService.TAG, "FileUpload" + error);
                    FirebaseCrash.report(new Exception(error));
                    CommonVariables.startUpload = false;
                    break;
            }
        }
    };
    String messageLogged = "";

    long stamp;
    long startStamp;
    HashMap<String, HashMap<String, HashMap<String, String>>> catOuterHash = new HashMap<>();
    HashMap<String, HashMap<String, Long>> outerHash = new HashMap<String, HashMap<String, Long>>();
    HashMap<String, HashMap<String, String>> outerHashCPUMEM = new HashMap<String, HashMap<String, String>>();
    //S_Farah
    HashMap<String, String> outerHashOF = new HashMap<String, String>();
    //E_Farah
    HashMap<String, String> outerHashUT = new HashMap<String, String>();
    HashMap<String, HashMap<String, Long>> cumulativeOuterHash;//= new HashMap<String, HashMap<String, Long>>();
    HashMap<String, HashMap<String, HashMap<String, String>>> cumulativeOuterHashCx;//= new HashMap<String, HashMap<String, String>>();
    Integer interval = CommonVariables.collectInterval;

    // These define the collection frequency (collect every 10 seconds)
    Timer timer = new Timer();


    private void setBkupFiles() {
        CommonVariables.CxBkup = "CxStatsBkup" + String.valueOf(System.currentTimeMillis());
        CommonVariables.CPCBkup = "CPUMEMStatsBkup" + String.valueOf(System.currentTimeMillis());
        CommonVariables.TFBkup = "TrafficStatsBkup" + String.valueOf(System.currentTimeMillis());
        CommonVariables.ScreenBkup = "ScreenStatsBkup" + String.valueOf(System.currentTimeMillis());
        CommonVariables.UTBkup = "UsageTimeBkup" + String.valueOf(System.currentTimeMillis());
        CommonVariables.OFBkup = "OpeningFrequencyBkup" + String.valueOf(System.currentTimeMillis());
        CommonVariables.PackagesBkup = "PackageBkup" + String.valueOf(System.currentTimeMillis());
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        CommonVariables.mContext = getApplicationContext();
        CommonVariables.isWiFi = Common.isConnectedToWifi(CommonVariables.mContext);
        Common.SafeFirstRun(CommonVariables.mContext);
        setBkupFiles();
        Toast.makeText(CommonVariables.mContext, "Service Started..", Toast.LENGTH_LONG).show();
        Common.showNotificationRunning(CommonVariables.mContext);
        getOuterHashes();
        Common.get3rdPartyApps();
        Common.getInstalledPackages(getApplicationContext());
        Common.regBroadcastRec(CommonVariables.mContext);
        if (CommonVariables.isWiFi) {
            Common.regUser(CommonVariables.mContext);
        }
        if (CommonVariables.isWiFi && CommonVariables.userRegistered) {
            Common.getThresholds(CommonVariables.mContext);
            Common.getIntervals(CommonVariables.mContext);
        }

        // This snippet runs the Linux top command every 'interval' amount of milliseconds

        stamp = System.currentTimeMillis();
        startStamp = System.currentTimeMillis();
        Log.d(CommonVariables.TAG, " Time starting stamp is " + stamp);
        final List<String> installedPackagesRunning = new ArrayList<>();

        final String protocol = "";
        final String previousOnTop = "";

        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    //S_Farah
                    outerHashOF.clear();
                    outerHashUT.clear();
                    //E_Farah

                    String tLine = "";
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
                        if (tokens.length == 10) { //&& CommonVariables.installed3rdPartyApps.contains(tokens[9])
                            // innerHashCPUMEM.put("PID", tokens[0]);
                            innerHashCPUMEM.put("CPU", tokens[2]);
                            innerHashCPUMEM.put("VSS", tokens[5]);
                            innerHashCPUMEM.put("RSS", tokens[6]);
                            innerHashCPUMEM.put("PCY", tokens[7]);
                            // innerHashCPUMEM.put("UID", tokens[8]);
                            innerHashCPUMEM.put("pName", tokens[9]);
                        } else if (tokens.length == 11) { //&& CommonVariables.installed3rdPartyApps.contains(tokens[10])
                            // innerHashCPUMEM.put("PID", tokens[1]);
                            innerHashCPUMEM.put("CPU", tokens[3]);
                            innerHashCPUMEM.put("VSS", tokens[6]);
                            innerHashCPUMEM.put("RSS", tokens[7]);
                            innerHashCPUMEM.put("PCY", tokens[8]);
                            // innerHashCPUMEM.put("UID", tokens[9]);
                            innerHashCPUMEM.put("pName", tokens[10]);
                        }
                        innerHashCPUMEM.put("Timestamp", String.valueOf(System.currentTimeMillis()));
                        installedPackagesRunning.add(innerHashCPUMEM.get("pName"));
                        aName = innerHashCPUMEM.get("pName");
                        if (CommonVariables.installed3rdPartyApps.contains(aName)) {
                            outerHashCPUMEM.put(aName, innerHashCPUMEM);
                        }
                        if (CommonVariables.installed3rdPartyApps.contains(innerHashCPUMEM.get("pName"))) {
                            // messageLogged = aName + " CPU " + innerHashCPUMEM.get("CPU") + " VSS " + innerHashCPUMEM.get("VSS") + " RSS " + innerHashCPUMEM.get("RSS");
                            // Common.appendLog(messageLogged);
                            // Log.i("Reduced", messageLogged);
                            if (innerHashCPUMEM.get("PCY").equals("fg") && !aName.equals("top")) {
                                messageLogged = aName + " on top";
                                //Common.appendLog(messageLogged);
                                //  Log.i("Reduced", messageLogged);
                                //S_Farah
                                outerHashUT.put("appName", aName);
                                outerHashUT.put("type", "UT");
                                outerHashUT.put("interval", String.valueOf(interval));
                                outerHashUT.put("timestamp", String.valueOf(System.currentTimeMillis()));
                                //E_Farah
                                if (!aName.equals(previousOnTop)) {
                                    messageLogged = aName + " opened";
                                    // Common.appendLog(messageLogged);
                                    Log.i(CommonVariables.TAG, "Reduced " + messageLogged);
                                    previousOnTop.equals(aName);
                                    //S_Farah
                                    outerHashOF.put("appName", aName);
                                    outerHashOF.put("type", "OF");
                                    outerHashOF.put("interval", String.valueOf(interval));
                                    outerHashOF.put("timestamp", String.valueOf(System.currentTimeMillis()));
                                    //E_Farah
                                }
                            }
                        }
                    }
                    // add the results of the while to the cumulative list
                    //CPUMEMStats.add(outerHashCPUMEM);
                    Log.i(CommonVariables.TAG, "CPUMEM " + outerHashCPUMEM.toString());
                    //End CPU and MEM Stats


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
                        if (P.equals("tcp") || P.equals("udp")) {

                            String command = "cat /proc/net/" + P;
                            Process catProcess = Runtime.getRuntime().exec(command);
                            BufferedReader catBufferedStream = new BufferedReader(new InputStreamReader(catProcess.getInputStream()));
                            catBufferedStream.readLine();

                            while ((catLine = catBufferedStream.readLine()) != null) {
                                String[] tokens = catLine.split("\\s+");

                                //Log.d("GGGGGG",tokens.toString());

                                String sourceIP = tokens[2].split(":")[0];

                                String sourcePort = null;
                                try {
                                    sourcePort = tokens[2].split(":")[1];
                                } catch (Exception e) {
                                    sourcePort = "";
                                }
                                String destinationIP = tokens[3].split(":")[0];
                                String destinationPort = null;
                                try {
                                    destinationPort = tokens[3].split(":")[1];
                                } catch (Exception e) {
                                    destinationPort = "";
                                }

                                String callingApp = CommonVariables.mContext.getPackageManager().getNameForUid(Integer.valueOf(tokens[8]));
                                if (callingApp != null) {
                                    // Log.i("Cx", callingApp);
                                    callingApp = callingApp.split(":")[0];
                                    if (CommonVariables.installed3rdPartyApps.contains(callingApp)) {
                                        // Log.i("Cx", callingApp);
                                        //  String callingApp = getApplicationContext().getPackageManager().geinsa  getNameForUid(Integer.valueOf(tokens[8]));
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
                                            //    if ((Long.valueOf(catInnerHash.get("Timestamp")) - Long.valueOf(cumulativeOuterHashCx.get(callingApp).get(P + sourceIP + sourcePort + destinationIP + destinationPort).get("Timestamp"))) / 1000
                                             //           < 3600) {
                                                    cumulativeOuterHashCx.get(callingApp).get(P + sourceIP + sourcePort + destinationIP + destinationPort).put("Age",
                                                            String.valueOf((Integer.valueOf(cumulativeOuterHashCx.get(callingApp).get(P + sourceIP + sourcePort + destinationIP + destinationPort).get("Age")) + (interval / 1000))));
                                                    catInnerHash.put("Age", String.valueOf((Integer.valueOf(cumulativeOuterHashCx.get(callingApp).get(P + sourceIP + sourcePort + destinationIP + destinationPort).get("Age")) + (interval / 1000))));

                                                    if (catOuterHash.get(callingApp) == null) {
                                                        HashMap<String, HashMap<String, String>> midHash = new HashMap<String, HashMap<String, String>>();
                                                        midHash.put(P + sourceIP + sourcePort + destinationIP + destinationPort, catInnerHash);
                                                        catOuterHash.put(callingApp, midHash);//;.put(P + sourceIP + sourcePort + destinationIP + destinationPort, catInnerHash);//.put("Age", String.valueOf(Integer.valueOf(cumulativeOuterHashCx.get(callingApp).get(P + sourceIP + sourcePort + destinationIP + destinationPort).get("Age"))));
                                                    } else {
                                                        if (catOuterHash.get(callingApp).get(P + sourceIP + sourcePort + destinationIP + destinationPort) == null) {
                                                            catOuterHash.get(callingApp).put(P + sourceIP + sourcePort + destinationIP + destinationPort, catInnerHash);
                                                        }
                                                    }
                                              /*  } else {
                                                    catInnerHash.put("Age", "0");
                                                    cumulativeOuterHashCx.get(callingApp).put(P + sourceIP + sourcePort + destinationIP + destinationPort, catInnerHash);
                                                    catOuterHash.get(callingApp).put(P + sourceIP + sourcePort + destinationIP + destinationPort, catInnerHash);
                                                }*/
                                            }

                                        }
                                        Log.i(CommonVariables.TAG, "CxN " + catOuterHash.toString());
                                    }

                                }

                            }
                        }
                    }
                    //
                    //  Log.i(CommonVariables.TAG, "CxNCumulative " + cumulativeOuterHashCx.toString());
                    //end for connections info


                    //Start Traffic Stats
                    // This for loop removes installed apps which are not running (installedPackages - !installedPackagesRunning)
                    for (Iterator<ApplicationInfo> iterator = CommonVariables.installedPackages.iterator(); iterator.hasNext(); ) {
                        if (!installedPackagesRunning.contains(iterator.next().packageName)) {
                            iterator.remove();
                        }
                    }

                    // This for loop computes the 4 traffic stats for each running app (collected value - previous value)
                    HashMap<String, Long> innerHash = new HashMap<String, Long>();

                    outerHash.clear();
                    for (String appName : CommonVariables.installed3rdPartyApps) {
                        if (Common.getAppInfo(CommonVariables.mContext, appName) != null) {
                            ApplicationInfo app = Common.getAppInfo(CommonVariables.mContext, appName);
                            int uid = app.uid;
                            long txBytes = TrafficStats.getUidTxBytes(uid);
                            long rxBytes = TrafficStats.getUidRxBytes(uid);
                            long txPackets = TrafficStats.getUidTxPackets(uid);
                            long rxPackets = TrafficStats.getUidRxPackets(uid);

                            if (!cumulativeOuterHash.containsKey(appName)) {
                               /* innerHash.put("txBytes", Long.valueOf(0));
                                innerHash.put("rxBytes", Long.valueOf(0));
                                innerHash.put("txPackets", Long.valueOf(0));
                                innerHash.put("rxPackets", Long.valueOf(0));
                                innerHash.put("Timestamp", System.currentTimeMillis());
                                outerHash.put(appName, innerHash);*/

                                HashMap<String, Long> cumulativeInnerHash = new HashMap<String, Long>();
                                cumulativeInnerHash.put("txBytes", txBytes);
                                cumulativeInnerHash.put("rxBytes", rxBytes);
                                cumulativeInnerHash.put("txPackets", txPackets);
                                cumulativeInnerHash.put("rxPackets", rxPackets);
                                cumulativeOuterHash.put(appName, cumulativeInnerHash);
                            } else {
                                if ((txBytes - cumulativeOuterHash.get(appName).get("txBytes")) != 0 ||
                                        (rxBytes - cumulativeOuterHash.get(appName).get("rxBytes")) != 0 ||
                                        (txPackets - cumulativeOuterHash.get(appName).get("txPackets") != 0) ||
                                        (rxPackets - cumulativeOuterHash.get(appName).get("rxPackets") != 0)) {
                                    innerHash.put("txBytes", txBytes - cumulativeOuterHash.get(appName).get("txBytes"));
                                    innerHash.put("rxBytes", rxBytes - cumulativeOuterHash.get(appName).get("rxBytes"));
                                    innerHash.put("txPackets", txPackets - cumulativeOuterHash.get(appName).get("txPackets"));
                                    innerHash.put("rxPackets", rxPackets - cumulativeOuterHash.get(appName).get("rxPackets"));
                                    innerHash.put("Timestamp", System.currentTimeMillis());
                                    outerHash.put(appName, innerHash);

                                    messageLogged = appName + " TxBytes " + outerHash.get(appName).get("txBytes") + " RxBytes " + outerHash.get(appName).get("rxBytes") + " TxPackets " + outerHash.get(appName).get("txPackets") + " RxPackets " + outerHash.get(appName).get("rxPackets");
                                    // Common.appendLog(messageLogged);
                                    Log.i(CommonVariables.TAG, "Traffic " + messageLogged);

                                }

                                cumulativeOuterHash.get(appName).put("txBytes", txBytes);
                                cumulativeOuterHash.get(appName).put("rxBytes", rxBytes);
                                cumulativeOuterHash.get(appName).put("txPackets", txPackets);
                                cumulativeOuterHash.get(appName).put("rxPackets", rxPackets);
                                cumulativeOuterHash.get(appName).put("Timestamp", System.currentTimeMillis());
                            }

                        }
                    }
                    //  Log.i(CommonVariables.TAG, "TrafficHash " + cumulativeOuterHash.toString());
                    //  trafficStats.add(cumulativeOuterHash);
                    // cumulativeTrafficStats.add(cumulativeOuterHash);

                    // End Traffic Stats

                    // Start Write Lists to Storage
                    if (outerHash.size() != 0)
                        Common.writeListToFile(outerHash, CommonVariables.TFBkup, true);

                    Common.writeListToFilecpc(outerHashCPUMEM, CommonVariables.CPCBkup, true);

                    if (catOuterHash.size() != 0)
                        Common.writeListToFilecxn(catOuterHash, CommonVariables.CxBkup, true);

                    Common.writeListToFile(cumulativeOuterHash, "CumulativeTrafficStatsBkup", false);
                    Common.writeListToFilecxn(cumulativeOuterHashCx, "CumulativeCxStatsBkup", false);
                    //S_Farah
                    if (outerHashOF.size() != 0)
                        Common.writeListToFileOF(outerHashOF, CommonVariables.OFBkup, true);
                    if (outerHashUT.size() != 0)
                        Common.writeListToFileUT(outerHashUT, CommonVariables.UTBkup, true);
                    //E_Farah
                    //End Write Lists to storage


                    //check storage size

                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (CommonVariables.isWiFi) {
                    if ((System.currentTimeMillis() - stamp) >= 120 * 1000) {
                        stamp = System.currentTimeMillis();
                        if (!CommonVariables.userRegistered) {
                            Common.regUser(CommonVariables.mContext);
                        }
                        if (Common.checkFileSize(CommonVariables.filetypeTf, CommonVariables.TFBkup, CommonVariables.maxFileSize) && (!CommonVariables.startUpload)) {
                            CommonVariables.setUploadSettings(CommonVariables.TFBkup, true, CommonVariables.filetypeTf);
                        }
                        if (Common.checkFileSize(CommonVariables.filetypeCPC, CommonVariables.CPCBkup, CommonVariables.maxFileSize) && (!CommonVariables.startUpload)) {
                            CommonVariables.setUploadSettings(CommonVariables.CPCBkup, true, CommonVariables.filetypeCPC);
                        }
                        if (Common.checkFileSize(CommonVariables.filetypeCx, CommonVariables.CxBkup, CommonVariables.maxFileSize) && (!CommonVariables.startUpload)) {
                            CommonVariables.setUploadSettings(CommonVariables.CxBkup, true, CommonVariables.filetypeCx);
                        }
                        //S_Farah
                        if (Common.checkFileSize(CommonVariables.filetypeOF, CommonVariables.OFBkup, CommonVariables.maxFileSizeOFUT) && (!CommonVariables.startUpload)) {
                            CommonVariables.setUploadSettings(CommonVariables.OFBkup, true, CommonVariables.filetypeOF);
                        }
                        if (Common.checkFileSize(CommonVariables.filetypeUT, CommonVariables.UTBkup, CommonVariables.maxFileSizeOFUT) && (!CommonVariables.startUpload)) {
                            CommonVariables.setUploadSettings(CommonVariables.UTBkup, true, CommonVariables.filetypeOF);
                        }
                        //E_Farah
                        if (Common.checkFileSize(CommonVariables.filetypeScreen, CommonVariables.ScreenBkup, CommonVariables.maxFileSizeScreen) && (!CommonVariables.startUpload)) {
                            CommonVariables.setUploadSettings(CommonVariables.ScreenBkup, true, CommonVariables.filetypeScreen);
                        }

                    }
                    if ((System.currentTimeMillis() - startStamp) >= 86400 * 1000) {
                        startStamp = System.currentTimeMillis();
                        if (!CommonVariables.startUpload) {
                            CommonVariables.setUploadSettings(CommonVariables.PackagesBkup, true, CommonVariables.filetypePackage);
                        }
                    }
                    Log.i(CommonVariables.TAG, " WIFI The phone is connected to wifi");
                    if (CommonVariables.startUpload && !CommonVariables.startUploadDir) {
                        Intent intent = new Intent(Intent.ACTION_SYNC, null, CommonVariables.mContext, ClientServerService.class);
                        intent.putExtra("uploadtype", CommonVariables.UploadTypeFile);
                        intent.putExtra("filename", CommonVariables.fileToUpload);
                        intent.putExtra("type", CommonVariables.fileUploadType);
                        switch (CommonVariables.fileUploadType) {
                            case CommonVariables.filetypeCPC:
                                intent.putExtra("url", CommonVariables.CPCUploadURL);
                                break;
                            case CommonVariables.filetypeCx:
                                intent.putExtra("url", CommonVariables.CxUploadURL);
                                break;
                            case CommonVariables.filetypeTf:
                                intent.putExtra("url", CommonVariables.TFUploadURL);
                                break;
                            //S_Farah
                            case CommonVariables.filetypeOF:
                                intent.putExtra("url", CommonVariables.OFUploadURL);
                                break;
                            case CommonVariables.filetypeUT:
                                intent.putExtra("url", CommonVariables.UTUploadURL);
                                break;
                            //E_Farah
                            case CommonVariables.filetypeScreen:
                                intent.putExtra("url", CommonVariables.ScreenUploadURL);
                                break;
                            case CommonVariables.filetypePackage:
                                intent.putExtra("url", CommonVariables.PackagesUploadURL);
                                break;
                        }
                        intent.putExtra("receiver", uploadResult);
                        startService(intent);
                    }

                } else {
                    Log.i(CommonVariables.TAG, "WIFI Phone is not connected wait");
                    //wait for the broadcast recevier
                }
            }
        }, 0, interval);


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(CommonVariables.mContext, "Service Stopped..", Toast.LENGTH_LONG).show();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(71422673);
        timer.cancel();
        Intent broadcastStop = new Intent("com.farah.heavyservice.RestartSensor");
        sendBroadcast(broadcastStop);
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