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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service
{
    // This set stores the list of 3rd party packages installed on the device (Local to the user)
    public List<String> packages = new ArrayList<>();
    public HashSet installedApps = new HashSet();
    public HashMap <String, String> Map = new HashMap <>();

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
                case "Intent.ACTION_SCREEN_OFF": appendLog("Screen switched off ");
                    screenON = false;
                    break;
                case "Intent.ACTION_SCREEN_ON": appendLog("Screen switched on ");
                    screenON = true;
                    break;
                case "Intent.ACTION_PACKAGE_ADDED": packageName = intent.getData().getEncodedSchemeSpecificPart().toString();
                    appendLog(getAppName(packageName) + " installed ");
                    installedApps.add(packageName);
                    break;
                case "Intent.ACTION_PACKAGE_REMOVED": Uri uri = intent.getData();
                    packageName = uri != null ? uri.getSchemeSpecificPart() : null;
                    try {
                        Date dateAdded = new Date(context.getPackageManager().getPackageInfo(packageName, 0).firstInstallTime);
                        Date dateRemoved = new Date();
                        long daysInstalled = (dateRemoved.getTime() - dateAdded.getTime()/(1000 * 60 * 60 *24));
                        // This works with UTC dates. The difference may be a day off if you look at local dates.
                        // Getting it to work correctly with local dates requires a different approach due to daylight savings.
                        appendLog(getAppName(packageName) + " uninstalled after " + daysInstalled + " days");
                        installedApps.remove(packageName);
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

    Integer interval = 10000;
    Timer timer = new Timer();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(), "Service Started..", Toast.LENGTH_LONG).show();

        // This snippet runs the Linux pm command to get a list of 3rd party packages installed on the device and it stores them in the created HashSet
        String pLine = null;
        String tLine = null;
        try {
            Process pmProcess = Runtime.getRuntime().exec("pm list packages -3");
            BufferedReader bufferedStream = new BufferedReader(new InputStreamReader(pmProcess.getInputStream()));
            while ((pLine = bufferedStream.readLine()) != null) {
                String packageLine[] = pLine.split(":");
                String packageName = packageLine[packageLine.length - 1];
                installedApps.add(packageName);
            }
            Process topProcess = Runtime.getRuntime().exec("top -n 1 -d 0");
            bufferedStream = new BufferedReader(new InputStreamReader(topProcess.getInputStream()));
            while ((tLine = bufferedStream.readLine()) != null) {
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
                }
                else if (tokens.length == 11) {
                    top.PID = tokens[1];
                    top.CPU = tokens[3];
                    top.VSS = tokens[6];
                    top.RSS = tokens[7];
                    top.PCY = tokens[8];
                    top.UID = tokens[9];
                    top.pName = tokens[10];
                }
                if (installedApps.contains(top.pName)) {
                    Map.put(top.PID, top.pName);
                }
            }
            Log.i("Packages"," Packages from map " + Map.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This snippet runs the Linux top command every 'interval' amount of milliseconds and it stores the parsed results in the Log.txt file
        final String previousOnTop = "";
        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    String tLine = null;
                    Process topProcess = Runtime.getRuntime().exec("top -n 1 -d 0");
                    BufferedReader bufferedStream = new BufferedReader(new InputStreamReader(topProcess.getInputStream()));
                    packages.clear();
                    while ((tLine = bufferedStream.readLine()) != null) {
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
                        }
                        else if (tokens.length == 11) {
                            top.PID = tokens[1];
                            top.CPU = tokens[3];
                            top.VSS = tokens[6];
                            top.RSS = tokens[7];
                            top.PCY = tokens[8];
                            top.UID = tokens[9];
                            top.pName = tokens[10];
                        }

                        // Map is ready
                        packages.add(top.pName);

                        top.aName = getAppName(top.pName);
                        if (top.aName.equals("(unknown)")) {
                            top.aName = top.pName;
                        }
                        if (installedApps.contains(top.pName)) {
                            appendLog(top.aName + " CPU " + top.CPU + " VSS " + top.VSS + " RSS " + top.RSS);
                            if (top.PCY.equals("fg") && !top.aName.equals("top")) {
                                appendLog(top.aName + " on top");
                                if (!top.aName.equals(previousOnTop)) {
                                    appendLog(top.aName + " opened");
                                    previousOnTop.equals(top.aName);
                                }
                            }
                        }
                    }
                    Log.i("Traffic", "Running 3rd party processes names: " + packages.toString());
                    PackageManager pm = getApplicationContext().getPackageManager();
                    List<ApplicationInfo> apps = pm.getInstalledApplications(0);
                    // Remove apps which are not running.
                    for (Iterator<ApplicationInfo> it = apps.iterator(); it.hasNext(); ) {
                        if (!packages.contains(it.next().packageName)) {
                            it.remove();
                        }
                    }
                    for (ApplicationInfo app : apps) {
                        String appName = app.loadLabel(pm).toString();
                        int uid = app.uid;
                        long txBytes = TrafficStats.getUidTxBytes(uid);
                        long rxBytes = TrafficStats.getUidRxBytes(uid);
                        Log.i("Traffic", appName + " TxBytes " + txBytes + " RxBytes " + rxBytes);
                    }
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
        unregisterReceiver(receiver);
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