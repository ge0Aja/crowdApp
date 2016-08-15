package com.farah.heavyservice;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import android.os.ResultReceiver;

public class MyService extends Service
{
    String messageLogged = "";
    HashMap<String, HashMap<String, Long>> outerHash = new HashMap<String, HashMap<String, Long>>();
    List<HashMap<String,HashMap<String,Long>>> trafficStats = new ArrayList<HashMap<String, HashMap<String, Long>>>();
    HashMap<String, HashMap<String, Long>> cumulativeOuterHash = new HashMap<String, HashMap<String, Long>>();
    List<HashMap<String,HashMap<String,Long>>> cumulativeTrafficStats = new ArrayList<HashMap<String, HashMap<String, Long>>>();

    // This function is called every time an entry needs to be logged with a Timestamp
    public String getTimestamp() {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String timestamp = format.format(date);
        return timestamp;
    }

    // This function is called every time a new log is to be added to the Log.txt file which is stored in Internal Memory
    public void appendLog(String message) {
        File logFile = new File("/storage/emulated/0/CrowdApp", "Log.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(getTimestamp() + " " + message);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // This function is called every time the app name is required given the package name
    public String getAppName(String packageName) {
        final PackageManager PM = getApplicationContext().getPackageManager();
        ApplicationInfo AI;
        try {
            AI = PM.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            AI = null;
        }
        final String appName = (String) (AI != null ? PM.getApplicationLabel(AI) : "(unknown)");
        return appName;
    }

    // The broadcast receiver is used to detect changes in screen status, new installations, and packages removed from device
    public boolean screenON = false;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String packageName;
            String action = intent.getAction();
            switch (action) {
                case "Intent.ACTION_SCREEN_OFF": messageLogged = "Screen switched off ";
                    appendLog(messageLogged); Log.i("Reduced", messageLogged);
                    screenON = false;
                    break;
                case "Intent.ACTION_SCREEN_ON": messageLogged = "Screen switched on ";
                    appendLog(messageLogged); Log.i("Reduced", messageLogged);
                    screenON = true;
                    break;
                case "Intent.ACTION_PACKAGE_ADDED": packageName = intent.getData().getEncodedSchemeSpecificPart();
                    messageLogged = getAppName(packageName) + " installed";
                    appendLog(messageLogged); Log.i("Reduced", messageLogged);
                    break;
                case "Intent.ACTION_PACKAGE_REMOVED": Uri uri = intent.getData();
                    packageName = uri != null ? uri.getSchemeSpecificPart() : null;
                    try {
                        Date dateAdded = new Date(context.getPackageManager().getPackageInfo(packageName, 0).firstInstallTime);
                        messageLogged = getAppName(packageName) + " uninstalled (installed on " + dateAdded + ")";
                        appendLog(messageLogged); Log.i("Reduced", messageLogged);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    // This class contains the parsed elements of the executed Linux top command
    public class topClass {
        public String PID;
        public String UID;
        public String VSS;
        public String RSS;
        public String PCY;
        public String CPU;
        public String pName;
        public String aName;
    }

    // These define the collection frequency (collect every 10 seconds)
    Integer interval = 10000;
    Timer timer = new Timer();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(getApplicationContext(), "Service Started..", Toast.LENGTH_LONG).show();

        // This snippet runs the Linux top command every 'interval' amount of milliseconds
        final PackageManager PM = getApplicationContext().getPackageManager();
        final List<String> installedPackagesRunning = new ArrayList<>();
        final List<String> installed3rdPartyApps = new ArrayList<>();
        final String previousOnTop = "";

        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    HashMap<String, Long> cumulativeInnerHash = new HashMap<String, Long>();

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
                    while ((tLine = topBufferedStream.readLine()) != null) {
                        if (!tLine.contains("u0_")) {
                            continue;
                        }
                        String[] tokens = tLine.split("\\s+");
                        topClass top = new topClass();
                        if (tokens.length == 10) {
                            top.PID = tokens[0];
                            top.CPU = tokens[2];
                            top.VSS = tokens[5];
                            top.RSS = tokens[6];
                            top.PCY = tokens[7];
                            top.UID = tokens[8];
                            top.pName = tokens[9];
                        } else if (tokens.length == 11) {
                            top.PID = tokens[1];
                            top.CPU = tokens[3];
                            top.VSS = tokens[6];
                            top.RSS = tokens[7];
                            top.PCY = tokens[8];
                            top.UID = tokens[9];
                            top.pName = tokens[10];
                        }

                        installedPackagesRunning.add(top.pName);

                        top.aName = getAppName(top.pName);
                        if (top.aName.equals("(unknown)")) {
                            top.aName = top.pName;
                        }
                        if (installed3rdPartyApps.contains(top.pName)) {
                            messageLogged = top.aName + " CPU " + top.CPU + " VSS " + top.VSS + " RSS " + top.RSS;
                            appendLog(messageLogged);
                            Log.i("Reduced", messageLogged);
                            if (top.PCY.equals("fg") && !top.aName.equals("top")) {
                                messageLogged = top.aName + " on top";
                                appendLog(messageLogged);
                                Log.i("Reduced", messageLogged);
                                if (!top.aName.equals(previousOnTop)) {
                                    messageLogged = top.aName + " opened";
                                    appendLog(messageLogged);
                                    Log.i("Reduced", messageLogged);
                                    previousOnTop.equals(top.aName);
                                }
                            }
                        }
                    }

                    // This for loop removes installed apps which are not running (installedPackages - !installedPackagesRunning)
                    for (Iterator<ApplicationInfo> iterator = installedPackages.iterator(); iterator.hasNext(); ) {
                        if (!installedPackagesRunning.contains(iterator.next().packageName)) {
                            iterator.remove();
                        }
                    }

                    // This for loop computes the 4 traffic stats for each running app (collected value - previous value)
                    HashMap<String, Long> innerHash = new HashMap<String, Long>();
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
                        } else {
                            innerHash.put("txBytes", txBytes - cumulativeOuterHash.get(appName).get("txBytes"));
                            innerHash.put("rxBytes", rxBytes - cumulativeOuterHash.get(appName).get("rxBytes"));
                            innerHash.put("txPackets", txPackets - cumulativeOuterHash.get(appName).get("txPackets"));
                            innerHash.put("rxPackets", rxPackets - cumulativeOuterHash.get(appName).get("rxPackets"));
                        }
                        outerHash.put(appName, innerHash);
                        cumulativeInnerHash.put("txBytes", txBytes);
                        cumulativeInnerHash.put("rxBytes", rxBytes);
                        cumulativeInnerHash.put("txPackets", txPackets);
                        cumulativeInnerHash.put("rxPackets", rxPackets);
                        cumulativeOuterHash.put(appName, cumulativeInnerHash);
                        messageLogged = appName + " TxBytes " + outerHash.get(appName).get("txBytes") + " RxBytes " + outerHash.get(appName).get("rxBytes") + " TxPackets " + outerHash.get(appName).get("txPackets") + " RxPackets " + outerHash.get(appName).get("rxPackets");
                        appendLog(messageLogged); Log.i("Traffic", messageLogged);
                    }

                    trafficStats.add(outerHash);
                    cumulativeTrafficStats.add(cumulativeOuterHash);
                    /* Here We Should be Adding the Reulst Recevier and Starting the Serivce
                    Start You can comment this section out and keep working fine
                    HttpResultsReceiver mRec = new HttpResultsReceiver(new android.os.Handler());
                    Intent intent = new Intent(Intent.ACTION_SYNC,null,getApplicationContext(),ClientServerService.class);
                    intent.putExtra("url","The URL on the Server");
                    intent.putExtra("receiver",mRec);
                    intent.putExtra("Method","POST");
                    intent.putExtra("HashMap",outerHash); // I'm not sure wetehr to send the List or the Outer Hash every 10 Secs
                    startService(intent);*/

                    ///// End You can comment this section out and keep working fine
                } catch (IOException e) {
                    e.printStackTrace();
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