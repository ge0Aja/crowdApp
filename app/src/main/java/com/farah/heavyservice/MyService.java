package com.farah.heavyservice;

import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.TrafficStats;
import android.os.Build;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.NoSuchPaddingException;


public class MyService extends Service {

    //A Result receiver for the single file upload service, the upload service returns the result code
    //of the upload operation and a bundle that has the type of the uploaded file to be used when changing
    // the storage file name
    //when the results are received the receiver deletes the uploaded file and checks if an update interval
    // or update threshold flag is turned on by the server in the response
    // if yes the result receiver triggers the update interval/threshold task
    // in case of an error the result receiver reports the error using Firebase
    public static ResultsReceiver uploadResult = new ResultsReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.d(ClientServerService.TAG, "Results are received in the MyServiceReceiver");
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
                        case "CxCount":
                            CommonVariables.changeCxCountBkupName("CxCount" + System.currentTimeMillis());
                            break;
                    }
                    Log.d("FileName", "Backup File Name are changed");
                    break;
                case ClientServerService.STATUS_FINISHED_NOWIFI:
                    Log.d(ClientServerService.TAG, "Wifi Schedule upload for later");
                    break;
                case ClientServerService.STATUS_FINISHED:
                    //delete the file from filename
                    int status = resultData.getInt("Status");
                    String filePlanned = resultData.getString("filename");
                    String filePlannedType = resultData.getString("type");
                    switch (status) {
                        case ClientServerService.STATUS_FINISHED_SUCCESS:
                            Log.d(ClientServerService.TAG, " FileUpload Files are Uploaded Successfully");
                            File delF = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + filePlannedType + "/" + filePlanned);
                            boolean del = delF.delete();
                            if (del)
                                Log.d("FileUpload", "Files are Deleted Successfully");
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
                            Log.d(ClientServerService.TAG, " FileUpload No response from server !");
                            // FirebaseCrash.report(new Exception("Server Response is Blank"));
                            break;
                        case ClientServerService.STATUS_FINISHED_ERROR:
                            String[] error = resultData.getStringArray("result");
                            // FirebaseCrash.report(new Exception(error[0]+"Server Response is Blank"));
                            switch (error[0]) {
                                case "finished":
                                    FirebaseCrash.report(new Exception("Server Error: " + error[2]));
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
                    Log.d(ClientServerService.TAG, "FileUpload Boom");
                    Log.d(ClientServerService.TAG, "FileUpload" + error);
                    FirebaseCrash.report(new Exception(error));
                    CommonVariables.startUpload = false;
                    break;
            }
        }
    };
    // intializing variables to be used in the service
    String messageLogged = "";
    String regex = "[^\\d]";
    String protocol = "";
    String previousOnTop = "";
    //timestamps to comapre against the check thresholds interval
    long stamp, startStamp, ThreshStampCPC, ThreshStampCxn, ThreshStampTF;
    //Hashmaps to store the extracted data
    //catOuterHash for connections information stats
    //ConnectionsCount for number of connections stats
    //outerHash for traffic stats
    //outerHashCPUMEM for CPU and memory stats
    // outerHashOF for open frequency stats
    // usedProtocols is used when extracting connections stats
    // cumulativeOuterHash and CumulativeOuterHashCx are lists used in storing cumulative information about the
    //traffic and connections stats to update the collected stats at each iteration for the service
    // interval is the collectInterval which is set in the database and updated at first run of the service
    //which is the interval of the service timer
    HashMap<String, HashMap<String, HashMap<String, String>>> catOuterHash = new HashMap<>();
    HashMap<String, Integer> ConnectionsCount = new HashMap<>();
    HashMap<String, HashMap<String, Long>> outerHash = new HashMap<String, HashMap<String, Long>>();
    HashMap<String, HashMap<String, String>> outerHashCPUMEM = new HashMap<String, HashMap<String, String>>();
    //S_Farah
    HashMap<String, String> outerHashOF = new HashMap<String, String>();
    HashMap<String, String> outerHashUT = new HashMap<String, String>();
    HashSet<String> usedProtocols = new HashSet<String>();
    //E_Farah
    HashMap<String, HashMap<String, Long>> cumulativeOuterHash;//= new HashMap<String, HashMap<String, Long>>();
    HashMap<String, HashMap<String, HashMap<String, String>>> cumulativeOuterHashCx;//= new HashMap<String, HashMap<String, String>>();
    Integer interval = CommonVariables.collectInterval;
    // These define the collection frequency (collect every 10 seconds)
    Timer timer = new Timer();
    private NetworkStatsManager networkStatsManager;

    //the method initializes the backup files names at first run or after service restart
    private void setBkupFiles() {
        CommonVariables.CxBkup = "CxStatsBkup" + String.valueOf(System.currentTimeMillis());
        CommonVariables.CPCBkup = "CPUMEMStatsBkup" + String.valueOf(System.currentTimeMillis());
        CommonVariables.TFBkup = "TrafficStatsBkup" + String.valueOf(System.currentTimeMillis());
        CommonVariables.ScreenBkup = "ScreenStatsBkup" + String.valueOf(System.currentTimeMillis());
        CommonVariables.UTBkup = "UsageTimeBkup" + String.valueOf(System.currentTimeMillis());
        CommonVariables.OFBkup = "OpeningFrequencyBkup" + String.valueOf(System.currentTimeMillis());
        CommonVariables.PackagesBkup = "PackageBkup" + String.valueOf(System.currentTimeMillis());
        CommonVariables.CxCountBkup = "CxCountBkup" + String.valueOf(System.currentTimeMillis());
    }

    // this method extracts the cumulative information which is backedup if files in case of service crash
    // the extracted information updates the values stored in the cumulative lists to be used in updating the
    // values in catOuterHash and outerHash Hashmaps
    private void getOuterHashes() {
        try {
            //Log.d("ListCumTraffic",Common.readListFromFiletf("CumulativeTrafficStatsBkup").get(Common.readListFromFilecpc("CumulativeTrafficStatsBkup").size() - 1).toString());
            cumulativeOuterHash = Common.readListFromFiletf("CumulativeTrafficStatsBkup").get(Common.readListFromFiletf("CumulativeTrafficStatsBkup").size() - 1);
        } catch (Exception e) {
            cumulativeOuterHash = new HashMap<String, HashMap<String, Long>>();
        }
        try {
            //  Log.d("ListCumCx",Common.readListFromFilecpc("CumulativeCxStatsBkup").get(Common.readListFromFilecpc("CumulativeCxStatsBkup").size() - 1).toString());
            ///// commented for test the add
            cumulativeOuterHashCx = Common.readListFromFilecxn("CumulativeCxStatsBkup"); //.get(Common.readListFromFilecpc("CumulativeCxStatsBkup").size() - 1)
        } catch (Exception e) {
            cumulativeOuterHashCx = new HashMap<String, HashMap<String, HashMap<String, String>>>();
        }

    }

    //what happens when the service start
    // get global application context
    // check if connected to wifi and ip is reachable
    // check if the service is running for the first time
    //init backup files
    // init outHash lists
    //get the installed third party apps
    // get all installed packages
    // if the user is not registered then start registration task
    // then start downloading latest thresholds and intervals from the server
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        CommonVariables.mContext = getApplicationContext();
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            Common.checkConnection(CommonVariables.mContext);
            Common.regUser(CommonVariables.mContext);
        }

        Common.SafeFirstRun(CommonVariables.mContext);
        setBkupFiles();
        Toast.makeText(CommonVariables.mContext, "Service Started..", Toast.LENGTH_LONG).show();
        Common.showNotificationRunning(CommonVariables.mContext);
        getOuterHashes();
        Common.get3rdPartyApps();
        Common.getInstalledPackages(getApplicationContext());
        Common.regBroadcastRec(CommonVariables.mContext);


        // CommonVariables.mContext.registerReceiver(EventReceiver);

        if (CommonVariables.isWiFi && CommonVariables.userRegistered) {
            Common.getThresholds(CommonVariables.mContext);
            Common.getIntervals(CommonVariables.mContext);
        }

        // at start set the thresholds timestamps to check against
        stamp = startStamp = System.currentTimeMillis();
        ThreshStampCPC = stamp + CommonVariables.checkCPCThresholdInterval;
        ThreshStampCxn = ThreshStampCPC + CommonVariables.checkCxnThresholdInterval;
        ThreshStampTF = ThreshStampCxn + CommonVariables.checkTfThresholdInterval;

        Log.d(CommonVariables.TAG, " Time starting stamp is " + stamp);
        final List<String> installedPackagesRunning = new ArrayList<>();

        // repeat this section on background at each interval
        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    //check the conn change flag and start tasks accordingly
                    if (CommonVariables.checkChange) {
                        Common.regUser(CommonVariables.mContext);

                        if (CommonVariables.isWiFi && CommonVariables.userRegistered) {
                            Common.getThresholds(CommonVariables.mContext);
                            Common.getIntervals(CommonVariables.mContext);
                        }
                        CommonVariables.changeCheckFlag(false);
                    }

                    // first task is to collect CPU and memory stats for all running Apps
                    // clear associated lists
                    //S_Farah
                    outerHashOF.clear();
                    outerHashUT.clear();
                    //E_Farah

                    String tLine = "";
                    // run the top command at every interval then store the outputs in a buffered reader
                    // check against the threshold check timestamp and if met set the check threshold flag to true for CPU and MEM
                    Process topProcess = Runtime.getRuntime().exec("top -n 1 -d 0");
                    BufferedReader topBufferedStream = new BufferedReader(new InputStreamReader(topProcess.getInputStream()));
                    outerHashCPUMEM.clear();
                    if (System.currentTimeMillis() - ThreshStampCPC > CommonVariables.checkCPCThresholdInterval) {
                        ThreshStampCPC = System.currentTimeMillis();
                        CommonVariables.checkCPCT = true;
                    }
                    // while reading the results from the buffered reader store the information in the corresponding hashmap
                    while ((tLine = topBufferedStream.readLine()) != null) {
                        String aName = "";
                        if (!tLine.contains("u0_")) {
                            continue;
                        }
                        String[] tokens = tLine.split("\\s+");
                        //  topClass top = new topClass();
                        HashMap<String, String> innerHashCPUMEM = new HashMap<String, String>();

                        //some linux kernels return different results when executing the top command
                        if (tokens.length == 10) { //&& CommonVariables.installed3rdPartyApps.contains(tokens[9])
                            // innerHashCPUMEM.put("PID", tokens[0]);
                            innerHashCPUMEM.put("CPU", tokens[2].replaceAll(regex, ""));
                            innerHashCPUMEM.put("VSS", tokens[5].replaceAll(regex, ""));
                            innerHashCPUMEM.put("RSS", tokens[6].replaceAll(regex, ""));
                            innerHashCPUMEM.put("PCY", tokens[7]);
                            // innerHashCPUMEM.put("UID", tokens[8]);
                            innerHashCPUMEM.put("pName", tokens[9]);
                        } else if (tokens.length == 11) { //&& CommonVariables.installed3rdPartyApps.contains(tokens[10])
                            // innerHashCPUMEM.put("PID", tokens[1]);
                            innerHashCPUMEM.put("CPU", tokens[3].replaceAll(regex, ""));
                            innerHashCPUMEM.put("VSS", tokens[6].replaceAll(regex, ""));
                            innerHashCPUMEM.put("RSS", tokens[7].replaceAll(regex, ""));
                            innerHashCPUMEM.put("PCY", tokens[8]);
                            // innerHashCPUMEM.put("UID", tokens[9]);
                            innerHashCPUMEM.put("pName", tokens[10]);
                        }
                        innerHashCPUMEM.put("Timestamp", String.valueOf(System.currentTimeMillis()));
                        installedPackagesRunning.add(innerHashCPUMEM.get("pName"));
                        aName = innerHashCPUMEM.get("pName");
                        // if the check thresholds flag is set to true, after saving the stats for each app check its thresholds
                        // by triggering separate AsyncTasks
                        if (CommonVariables.installed3rdPartyApps.contains(aName) && !aName.equals("com.farah.heavyservice")) {
                            outerHashCPUMEM.put(aName, innerHashCPUMEM);
                            if (CommonVariables.checkCPCT && CommonVariables.isServerReachable && CommonVariables.checkEvents != 0) { //Sep 2 changed to isServerReachable
                                new CompareThresholdsTask(CommonVariables.mContext, Float.valueOf(innerHashCPUMEM.get("CPU")), aName, CommonVariables.th_prCPU).execute();
                                new CompareThresholdsTask(CommonVariables.mContext, Float.valueOf(innerHashCPUMEM.get("RSS")), aName, CommonVariables.th_prRSS).execute();
                                //new CompareThresholdsTask(CommonVariables.mContext, Float.valueOf(innerHashCPUMEM.get("VSS")), aName, CommonVariables.th_prVSS).execute();
                                // Common.compareThreshold(Float.valueOf(innerHashCPUMEM.get("CPU")), CommonVariables.th_prCPU, aName, CommonVariables.mContext);
                                // Common.compareThreshold(Float.valueOf(innerHashCPUMEM.get("RSS")), CommonVariables.th_prRSS, aName, CommonVariables.mContext);
                                // Common.compareThreshold(Float.valueOf(innerHashCPUMEM.get("VSS")), CommonVariables.th_prVSS, aName, CommonVariables.mContext);
                            }
                        }
                        // if the app is running in foreground update the usage time stats and if wasn't previously on foreground
                        // update the open frequency stats
                        if (CommonVariables.installed3rdPartyApps.contains(innerHashCPUMEM.get("pName")) && !innerHashCPUMEM.get("pName").toString().equals("com.farah.heavyservice")) {
                            if (innerHashCPUMEM.get("PCY").equals("fg") && !aName.equals("top")) {
                                messageLogged = aName + " on top";
                                outerHashUT.put("appName", aName);
                                outerHashUT.put("type", "UT");
                                outerHashUT.put("interval", String.valueOf(interval));
                                outerHashUT.put("timestamp", String.valueOf(System.currentTimeMillis()));
                                //E_Farah
                                if (!aName.equals(previousOnTop)) {
                                    messageLogged = aName + " opened";
                                    // Common.appendLog(messageLogged);
                                    Log.d(CommonVariables.TAG, "Reduced " + messageLogged);
                                    previousOnTop = aName;
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
                    // after finishing set the check threshold for CPU and Mem to false
                    CommonVariables.checkCPCT = false;
                    Log.d(CommonVariables.TAG, "CPUMEM " + outerHashCPUMEM.toString());
                    //End CPU and MEM Stats

                    // second start collecting connections stats
                    String nLine = null;
                    //  HashSet<String> usedProtocols = new HashSet<String>();
                    // at each interval execute the netstat command and extract the available connections
                    // save the infomation to a buffered reader
                    Process nProcess = Runtime.getRuntime().exec("netstat -a");
                    BufferedReader nBufferedStream = new BufferedReader(new InputStreamReader(nProcess.getInputStream()));
                    nBufferedStream.readLine();
                    // run the top command at every interval then store the outputs in a buffered reader
                    // check against the threshold check timestamp and if met set the check threshold flag to true for Connections stats
                    if (System.currentTimeMillis() - ThreshStampCxn > CommonVariables.checkCxnThresholdInterval) {
                        ThreshStampCxn = System.currentTimeMillis();
                        CommonVariables.checkCxT = true;
                    }
                    // while reading from the buffered reader parse the information to save each available protocol
                    while ((nLine = nBufferedStream.readLine()) != null) {
                        String delimiter[] = nLine.split("  ");
                        String temp = delimiter[0].trim();
                        if (!temp.equals(protocol)) {
                            usedProtocols.add(temp);
                            protocol.equals(temp);
                        }
                    }
                    // some versions of linux kernels do not include tcp6 in the netstat command
                    if (!usedProtocols.contains("tcp6"))
                        usedProtocols.add("tcp6");

                    String catLine = null;
                    Iterator Iterator = usedProtocols.iterator();
                    catOuterHash.clear();
                    ConnectionsCount.clear();
                    // for each available protocol in the netstat command
                    //read the corresponding file at /proc/net and extract connections information
                    // save the extracted information in another buffered reader
                    while (Iterator.hasNext()) {
                        String P = Iterator.next().toString();
                        if (P.equals("tcp") || P.equals("udp") || P.equals("tcp6")) {
                            String command = "cat /proc/net/" + P;
                            Process catProcess = Runtime.getRuntime().exec(command);
                            BufferedReader catBufferedStream = new BufferedReader(new InputStreamReader(catProcess.getInputStream()));
                            catBufferedStream.readLine();
                            // for each extracted line in the second buffer reader parse connections information
                            while ((catLine = catBufferedStream.readLine()) != null) {
                                String[] tokens = catLine.split("\\s+");
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
                                // get the corresponding app from the Uid extracted from the parsed line
                                String callingApp = CommonVariables.mContext.getPackageManager().getNameForUid(Integer.valueOf(tokens[8]));

                                // if there is a corresponding app and it is not our service save its information to the corresponding hashmap
                                // multiple steps are done inside this section of code to save the extracted information cumulatively for each connection
                                // and to insure we don't add new lines for existing connections
                                if (callingApp != null) {
                                    callingApp = callingApp.split(":")[0];
                                    if (CommonVariables.installed3rdPartyApps.contains(callingApp) && !callingApp.equals("com.farah.heavyservice")) {
                                        HashMap<String, String> catInnerHash = new HashMap<>();
                                        //  catInnerHash.put("Protocol", P);
                                        catInnerHash.put("Timestamp", String.valueOf(System.currentTimeMillis()));
                                        /*catInnerHash.put("SourceIP", Common.convertHexToString(sourceIP));
                                        catInnerHash.put("SourcePort", String.valueOf(Integer.parseInt(sourcePort, 16)));
                                        catInnerHash.put("DestinationIP", Common.convertHexToString(destinationIP));
                                        catInnerHash.put("DestinationPort", String.valueOf(Integer.parseInt(destinationPort, 16)));
                                       */
                                        if (cumulativeOuterHashCx.get(callingApp) == null) {
                                            catInnerHash.put("Age", "0");

                                            HashMap<String, HashMap<String, String>> midHash = new HashMap<String, HashMap<String, String>>();
                                            midHash.put(P + sourceIP + sourcePort + destinationIP + destinationPort, catInnerHash);
                                            cumulativeOuterHashCx.put(callingApp, midHash);
                                            catOuterHash.put(callingApp, midHash);
                                            ConnectionsCount.put(callingApp, 1);

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

                                                if (ConnectionsCount.get(callingApp) != null) {
                                                    ConnectionsCount.put(callingApp, ConnectionsCount.get(callingApp) + 1);
                                                } else {
                                                    ConnectionsCount.put(callingApp, 1);
                                                }

                                            } else {
                                                cumulativeOuterHashCx.get(callingApp).get(P + sourceIP + sourcePort + destinationIP + destinationPort).put("Age",
                                                        String.valueOf((Integer.valueOf(cumulativeOuterHashCx.get(callingApp).get(P + sourceIP + sourcePort + destinationIP + destinationPort).get("Age")) + (interval / 1000))));
                                                catInnerHash.put("Age", String.valueOf((Integer.valueOf(cumulativeOuterHashCx.get(callingApp).get(P + sourceIP + sourcePort + destinationIP + destinationPort).get("Age")) + (interval / 1000))));

                                                if (catOuterHash.get(callingApp) == null) {
                                                    HashMap<String, HashMap<String, String>> midHash = new HashMap<String, HashMap<String, String>>();
                                                    midHash.put(P + sourceIP + sourcePort + destinationIP + destinationPort, catInnerHash);
                                                    catOuterHash.put(callingApp, midHash);
                                                } else {
                                                    if (catOuterHash.get(callingApp).get(P + sourceIP + sourcePort + destinationIP + destinationPort) == null) {
                                                        catOuterHash.get(callingApp).put(P + sourceIP + sourcePort + destinationIP + destinationPort, catInnerHash);
                                                    }
                                                }
                                                if (CommonVariables.checkCxT && CommonVariables.isServerReachable && CommonVariables.checkEvents != 0) {
                                                    new CompareThresholdsTask(CommonVariables.mContext, Float.valueOf(catInnerHash.get("Age")), callingApp, CommonVariables.th_cxAge).execute();
                                                    //Common.compareThreshold(Float.valueOf(catInnerHash.get("Age")), CommonVariables.th_cxAge, callingApp, CommonVariables.mContext);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            // if the check flag for connections is set to true and we are connected to wifi start
                            // comparing the threshold for each connection on a separate asyn task
                            if (CommonVariables.checkCxT && CommonVariables.isServerReachable && CommonVariables.checkEvents != 0) {
                                Iterator callingAppIter = ConnectionsCount.entrySet().iterator();
                                while (callingAppIter.hasNext()) {
                                    Map.Entry callinAppPair = (Map.Entry) callingAppIter.next();
                                    new CompareThresholdsTask(CommonVariables.mContext, Float.valueOf(ConnectionsCount.get(callinAppPair.getKey())), (String) callinAppPair.getKey(), CommonVariables.th_cxCount).execute();
                                    //Common.compareThreshold(Float.valueOf(catOuterHash.get(callinAppPair.getKey()).size()), CommonVariables.th_cxCount, (String) callinAppPair.getKey(), CommonVariables.mContext);
                                }
                            }

                        }
                    }
                    Log.d(CommonVariables.TAG, "CxN " + catOuterHash.toString());
                    Log.d(CommonVariables.TAG, "CxN Count " + ConnectionsCount.toString());
                    CommonVariables.checkCxT = false;
                    //end for connections info


                    //Start Traffic Stats
                    // This for loop removes installed apps which are not running (installedPackages - !installedPackagesRunning)
                   /* for (Iterator<ApplicationInfo> iterator = CommonVariables.installedPackages.iterator(); iterator.hasNext(); ) {
                        if (!installedPackagesRunning.contains(iterator.next().packageName)) {
                            iterator.remove();
                        }
                    }*/

                    // This for loop computes the 4 traffic stats for each running app (collected value - previous value)

                    // clear the temp hashmap
                    outerHash.clear();
                    String appName = "";
                    // get an iterator over the installed third party Apps
                    Iterator<String> iterator3rdParty = CommonVariables.installed3rdPartyApps.iterator();
                    // for (String appName : CommonVariables.installed3rdPartyApps) {

                    // check against the threshold check timestamp and if met set the check threshold flag to true for Traffic stats
                    if (System.currentTimeMillis() - ThreshStampTF > CommonVariables.checkTfThresholdInterval) {
                        ThreshStampTF = System.currentTimeMillis();
                        CommonVariables.checkTfT = true;
                    }
                    // for each third party app get the uid of that app and check the traffic counters if changed
                    // if there's a change in the traffic for that app log the change in the traffic hashmap
                    // the code was edited to make sure that we only log the changes to prevent redundancy
                    while (iterator3rdParty.hasNext()) {
                        appName = iterator3rdParty.next().toString();
                        if (!appName.equals("com.farah.heavyservice")) { //Common.getAppInfo(CommonVariables.mContext, appName) != null
                            ApplicationInfo app = Common.getAppInfo(CommonVariables.mContext, appName);
                            if (app != null) {
                                int uid = app.uid;
                                long txBytes = TrafficStats.getUidTxBytes(uid);
                                long rxBytes = TrafficStats.getUidRxBytes(uid);
                                long txPackets = TrafficStats.getUidTxPackets(uid);
                                long rxPackets = TrafficStats.getUidRxPackets(uid);

                                if (!cumulativeOuterHash.containsKey(appName)) {
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
                                        HashMap<String, Long> innerHash = new HashMap<String, Long>();
                                        innerHash.put("txBytes", txBytes - cumulativeOuterHash.get(appName).get("txBytes"));
                                        innerHash.put("rxBytes", rxBytes - cumulativeOuterHash.get(appName).get("rxBytes"));
                                        innerHash.put("txPackets", txPackets - cumulativeOuterHash.get(appName).get("txPackets"));
                                        innerHash.put("rxPackets", rxPackets - cumulativeOuterHash.get(appName).get("rxPackets"));
                                        innerHash.put("Timestamp", System.currentTimeMillis());
                                        outerHash.put(appName, innerHash);

                                        HashMap<String, Long> cumulativeInnerHash = new HashMap<String, Long>();
                                        cumulativeInnerHash.put("txBytes", txBytes);
                                        cumulativeInnerHash.put("rxBytes", rxBytes);
                                        cumulativeInnerHash.put("txPackets", txPackets);
                                        cumulativeInnerHash.put("rxPackets", rxPackets);
                                        cumulativeOuterHash.put(appName, cumulativeInnerHash);
                                        // messageLogged = appName + " TxBytes " + outerHash.get(appName).get("txBytes") + " RxBytes " + outerHash.get(appName).get("rxBytes") + " TxPackets " + outerHash.get(appName).get("txPackets") + " RxPackets " + outerHash.get(appName).get("rxPackets");
                                        // Common.appendLog(messageLogged);
                                        //  Log.d(CommonVariables.TAG, "Traffic " + messageLogged);

                                        // if the check threshold flag was set to true for traffic stats start an Async task
                                        // to check the traffic thresholds for each App
                                        if (CommonVariables.checkTfT && CommonVariables.isServerReachable && CommonVariables.checkEvents != 0) {
                                            new CompareThresholdsTask(CommonVariables.mContext, Float.valueOf(innerHash.get("txBytes")), appName, CommonVariables.th_txBytes).execute();
                                            // Common.compareThreshold(Float.valueOf(innerHash.get("txBytes")), CommonVariables.th_txBytes, appName, CommonVariables.mContext);
                                            new CompareThresholdsTask(CommonVariables.mContext, Float.valueOf(innerHash.get("rxBytes")), appName, CommonVariables.th_rxBytes).execute();
                                            //Common.compareThreshold(Float.valueOf(innerHash.get("rxBytes")), CommonVariables.th_rxBytes, appName, CommonVariables.mContext);
                                            new CompareThresholdsTask(CommonVariables.mContext, Float.valueOf(innerHash.get("txPackets")), appName, CommonVariables.th_txPackets).execute();
                                            // Common.compareThreshold(Float.valueOf(innerHash.get("txPackets")), CommonVariables.th_txPackets, appName, CommonVariables.mContext);
                                            new CompareThresholdsTask(CommonVariables.mContext, Float.valueOf(innerHash.get("rxPackets")), appName, CommonVariables.th_rxPackets).execute();
                                            // Common.compareThreshold(Float.valueOf(innerHash.get("rxPackets")), CommonVariables.th_rxPackets, appName, CommonVariables.mContext);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Log.d(CommonVariables.TAG, "Traffic " + outerHash.toString());
                    //after finishing the check set the check threshold app for the traffic to false
                    CommonVariables.checkTfT = false;
                    //  Log.d(CommonVariables.TAG, "TrafficHash " + cumulativeOuterHash.toString());
                    //  trafficStats.add(cumulativeOuterHash);
                    // cumulativeTrafficStats.add(cumulativeOuterHash);

                    // End Traffic Stats


                    // after finishing from stats collection for CPU, MEM, Conn, Traffic
                    // append the collected stats in hashmaps to the initialized binary files
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
                    //E_Farah4
                    if (ConnectionsCount.size() != 0)
                        Common.writeCxCountToFile(ConnectionsCount, CommonVariables.CxCountBkup, true);
                    //End Write Lists to storage

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }
                //check storage size
                // check the size of the saved files and if any file exceeds a specific size start the upload service
                // for that file
                // checking the file size is set at a 2 min interval

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
                            CommonVariables.setUploadSettings(CommonVariables.UTBkup, true, CommonVariables.filetypeUT);
                        }
                        //E_Farah
                        if (Common.checkFileSize(CommonVariables.filetypeScreen, CommonVariables.ScreenBkup, CommonVariables.maxFileSizeScreen) && (!CommonVariables.startUpload)) {
                            CommonVariables.setUploadSettings(CommonVariables.ScreenBkup, true, CommonVariables.filetypeScreen);
                        }
                        if (Common.checkFileSize(CommonVariables.filetypeCxCount, CommonVariables.CxCountBkup, CommonVariables.maxFileSizeOFUT) && (!CommonVariables.startUpload)) {
                            CommonVariables.setUploadSettings(CommonVariables.CxCountBkup, true, CommonVariables.filetypeCxCount);
                        }

                    }
                    // one a day check if there are any new installed packages and send any changes to
                    // the server
                    if ((System.currentTimeMillis() - startStamp) >= 86400 * 1000) {
                        startStamp = System.currentTimeMillis();
                        if (!CommonVariables.startUpload) {
                            CommonVariables.setUploadSettings(CommonVariables.PackagesBkup, true, CommonVariables.filetypePackage);
                        }
                    }
                    Log.d(CommonVariables.TAG, " WIFI The phone is connected to wifi");

                    // if any of the files exceeded the size and there is not other uploading service running
                    // set the upload variables to meet that file which are
                    // upload type : file , upload file type : CPU, MEM, Traffic ... , and the upload URL
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
                            case CommonVariables.filetypeCxCount:
                                intent.putExtra("url", CommonVariables.CxCountUploadURL);
                        }
                        intent.putExtra("receiver", uploadResult);
                        startService(intent);
                    }

                } else {
                    Log.d(CommonVariables.TAG, "WIFI Phone is not connected wait");
                    //wait for the broadcast recevier
                }
            }
        }, 0, interval);
        // keep the service running in background as long as possible
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // if the service stops notify and remove the sticky notification on top then broadcast the service stopped
        // to restart it again
        Toast.makeText(CommonVariables.mContext, "Service Stopped..", Toast.LENGTH_LONG).show();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(96369636);
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