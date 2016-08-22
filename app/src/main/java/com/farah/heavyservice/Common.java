package com.farah.heavyservice;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Georgi on 8/19/2016.
 */
public class Common {
    public static String TAG = "Heavy Service";

    public static String convertHexToString(String hexValue) {
        String IP = "";
        String hex = hexValue.substring(hexValue.length() - 8);
        IP = String.valueOf(Integer.parseInt(hex.substring(6, 8), 16)) + "." + String.valueOf(Integer.parseInt(hex.substring(4, 6), 16)) + "." + String.valueOf(Integer.parseInt(hex.substring(2, 4), 16)) + "." + String.valueOf(Integer.parseInt(hex.substring(0, 2), 16));
        return IP;
    }

    public static String getTimestamp() {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String timestamp = format.format(date);
        return timestamp;
    }

    public static void appendLog(String message) {
        // File dir = getDir(Environment.DIRECTORY_DOCUMENTS,MODE_PRIVATE);
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File logFile = new File(dir, "Log.txt");
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


    public static boolean writeListToFilecpc(HashMap<String, HashMap<String, String>> captures, String fileName, Boolean append) {
        ObjectOutputStream fileOut = null;
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File myFile = new File(dir, fileName); //"CPUMEMStats"
        /*if (!myFile.exists()) {
            try {
                myFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        /*
        try {
            //// TODO: 8/17/2016 we have to replace the hardcoded file name
            fileOut = new FileOutputStream(myFile,true);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(captures);
            out.close();
            fileOut.close();
            Log.d(TAG, "The CPU outerhash is appended to a file");
            return true;
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File Not found exception while saving list to file");
            return false;
        } catch (IOException e) {
            Log.d(TAG, "Input output exception while saving list to file");
            return false;
        }*/
        try {
            if (!myFile.exists() || !append) {
                //Log.i("ReadList", "The file " + fileName + " Doesn't exist and should be created");
                fileOut = new ObjectOutputStream(new FileOutputStream(myFile));
            } else
                fileOut = new AppendableObjectOutputStream(new FileOutputStream(myFile, append));
            fileOut.writeObject(captures);
            fileOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (fileOut != null) fileOut.close();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }


    public static boolean writeListToFilecxn(HashMap<String, HashMap<String,HashMap<String, String>>> captures, String fileName, Boolean append) {
        ObjectOutputStream fileOut = null;
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File myFile = new File(dir, fileName); //"CPUMEMStats"

        try {
            if (!myFile.exists() || !append) {
                //Log.i("ReadList", "The file " + fileName + " Doesn't exist and should be created");
                fileOut = new ObjectOutputStream(new FileOutputStream(myFile));
            } else
                fileOut = new AppendableObjectOutputStream(new FileOutputStream(myFile, append));
            fileOut.writeObject(captures);
            fileOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (fileOut != null) fileOut.close();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static boolean writeListToFile(HashMap<String, HashMap<String, Long>> captures, String fileName, Boolean append) {
        //  AppendToFileNoHeader fileOut = null;
        ObjectOutputStream fileOut = null;
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File myFile = new File(dir, fileName); //  "TrafficStats"

        try {
            if (!myFile.exists() || !append) {
              //  Log.i("ReadList", "The file " + fileName + " Doesn't exist and should be created");
                fileOut = new ObjectOutputStream(new FileOutputStream(myFile));
            } else {
                fileOut = new AppendableObjectOutputStream(new FileOutputStream(myFile, append));
            }
            fileOut.writeObject(captures);
            fileOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (fileOut != null) fileOut.close();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static List<HashMap<String,HashMap<String,Long>>> readListFromFiletf(String filename) {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp");
        File myFile = new File(dir, filename);
        List<HashMap<String,HashMap<String,Long>>> rtrnList = new ArrayList<>();
        ;
        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(myFile));
                while (true) {
                    rtrnList.add((HashMap<String,HashMap<String,Long>>)ois.readObject());
                }
            } catch (EOFException e) {
              // e.printStackTrace();
                return rtrnList;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (ois != null) ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return rtrnList;
    }

    public static List<HashMap<String,HashMap<String,String>>> readListFromFilecpc(String filename) {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp");
        File myFile = new File(dir, filename);
        List<HashMap<String,HashMap<String,String>>> rtrnList = new ArrayList<>();
        ;
        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(myFile));
                while (true) {
                    rtrnList.add((HashMap<String,HashMap<String,String>>)ois.readObject());
                }
            } catch (EOFException e) {
                //e.printStackTrace();
                return rtrnList;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (ois != null) ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return rtrnList;
    }



    public static List<HashMap<String, HashMap<String,HashMap<String, String>>>> readListFromFilecxn(String filename) {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp");
        File myFile = new File(dir, filename);
        List<HashMap<String, HashMap<String,HashMap<String, String>>>> rtrnList = new ArrayList<>();
        ;
        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(myFile));
                while (true) {
                    rtrnList.add((HashMap<String, HashMap<String,HashMap<String, String>>>)ois.readObject());
                }
            } catch (EOFException e) {
                //e.printStackTrace();
                return rtrnList;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (ois != null) ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return rtrnList;
    }

    // This function is called every time the app name is required given the package name
    public static String getAppName(String packageName, Context context) {
        final PackageManager PM = context.getPackageManager();
        ApplicationInfo AI;
        try {
            AI = PM.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            AI = null;
        }
        final String appName = (String) (AI != null ? PM.getApplicationLabel(AI) : "(unknown)");
        return appName;
    }


    public static JSONArray makeJsonArraytf(String filename){
        List<HashMap<String,HashMap<String,Long>>> tfArray;
        JSONArray jsonArray = null;
        try {
            tfArray = readListFromFiletf(filename);
            if(!tfArray.isEmpty()) {
                 jsonArray = new JSONArray(tfArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  jsonArray;
    }


    public static JSONArray makeJsonArraycpc(String filename){
        List<HashMap<String,HashMap<String,String>>> cpcArray;
        JSONArray jsonArray = null;
        try {
            cpcArray = readListFromFilecpc(filename);
            if(!cpcArray.isEmpty()) {
                jsonArray = new JSONArray(cpcArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  jsonArray;
    }

    public static JSONArray makeJsonArraycxn(String filename){
        List<HashMap<String, HashMap<String,HashMap<String, String>>>> cpcArray;
        JSONArray jsonArray = null;
        try {
            cpcArray = readListFromFilecxn(filename);
            if(!cpcArray.isEmpty()) {
                jsonArray = new JSONArray(cpcArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  jsonArray;
    }

    public static boolean isConnectedToWifi(Context context){

        boolean isConnected =false;
        boolean isWiFi =false;
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if(isConnected){
           isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        }

        return  isWiFi;
    }

}
