package com.farah.heavyservice;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by Georgi on 8/19/2016.
 */
public class Common {


    public static String getTimestamp() {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String timestamp = format.format(date);
        return timestamp;
    }

    /*public static void appendLog(String message) {
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
    }*/

    public static boolean writeListToFilecpc(HashMap<String, HashMap<String, String>> captures, String fileName, Boolean append) {
        ObjectOutputStream fileOut = null;
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeCPC + "/");
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


    public static boolean writePackageStatusToFile(String fileName, String state, String packageName, String Date, String FirstInstall) {
        ObjectOutputStream fileOut = null;
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypePackage + "/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File myFile = new File(dir, fileName);
        HashMap pst = new HashMap<String, String>();
        pst.put("package", packageName);
        pst.put("state", state);
        pst.put("timestamp", Date);
        pst.put("installed_on", FirstInstall);
        try {
            if (!myFile.exists()) {
                fileOut = new ObjectOutputStream(new FileOutputStream(myFile));
            } else
                fileOut = new AppendableObjectOutputStream(new FileOutputStream(myFile, true));
            fileOut.writeObject(pst);
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

    public static boolean writeScreenStatusToFile(String fileName, String state) {
        ObjectOutputStream fileOut = null;
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeScreen + "/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File myFile = new File(dir, fileName);
        HashMap scst = new HashMap<String, String>();
        scst.put("state", state);
        scst.put("timestamp", System.currentTimeMillis());
        try {
            if (!myFile.exists()) {
                fileOut = new ObjectOutputStream(new FileOutputStream(myFile));
            } else
                fileOut = new AppendableObjectOutputStream(new FileOutputStream(myFile, true));
            fileOut.writeObject(scst);
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

    public static boolean writeListToFilecxn(HashMap<String, HashMap<String, HashMap<String, String>>> captures, String fileName, Boolean append) {
        ObjectOutputStream fileOut = null;
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeCx + "/");
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
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeTf + "/");
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

    //S_Farah
    public static boolean writeListToFileOF(HashMap<String, String> captures, String fileName, Boolean append) {
        ObjectOutputStream fileOut = null;
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeOF + "/");
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

    public static boolean writeListToFileUT(HashMap<String, String> captures, String fileName, Boolean append) {
        ObjectOutputStream fileOut = null;
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeUT + "/");
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

    //E_Farah
    public static List<HashMap<String, String>> readListFromFilepackage(String filename) {

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypePackage + "/");
        File myFile = new File(dir, filename);
        List<HashMap<String, String>> rtrnList = new ArrayList<>();
        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(myFile));
                while (true) {
                    rtrnList.add((HashMap<String, String>) ois.readObject());
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

    public static List<HashMap<String, HashMap<String, Long>>> readListFromFiletf(File file) {
        List<HashMap<String, HashMap<String, Long>>> rtrnList = new ArrayList<>();

        if (file.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(file));
                while (true) {
                    rtrnList.add((HashMap<String, HashMap<String, Long>>) ois.readObject());
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

    public static List<HashMap<String, HashMap<String, Long>>> readListFromFiletf(String filename) {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeTf + "/");
        File myFile = new File(dir, filename);
        List<HashMap<String, HashMap<String, Long>>> rtrnList = new ArrayList<>();
        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(myFile));
                while (true) {
                    rtrnList.add((HashMap<String, HashMap<String, Long>>) ois.readObject());
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

    public static List<HashMap<String, HashMap<String, String>>> readListFromFilecpc(File file) {
        List<HashMap<String, HashMap<String, String>>> rtrnList = new ArrayList<>();
        if (file.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(file));
                while (true) {
                    rtrnList.add((HashMap<String, HashMap<String, String>>) ois.readObject());
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

    public static List<HashMap<String, HashMap<String, String>>> readListFromFilecpc(String filename) {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeCPC + "/");
        File myFile = new File(dir, filename);
        List<HashMap<String, HashMap<String, String>>> rtrnList = new ArrayList<>();
        ;
        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(myFile));
                while (true) {
                    rtrnList.add((HashMap<String, HashMap<String, String>>) ois.readObject());
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

    public static List<HashMap<String, HashMap<String, HashMap<String, String>>>> readListFromFilecxn(File file) {

        List<HashMap<String, HashMap<String, HashMap<String, String>>>> rtrnList = new ArrayList<>();

        if (file.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(file));
                while (true) {
                    rtrnList.add((HashMap<String, HashMap<String, HashMap<String, String>>>) ois.readObject());
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

    public static List<HashMap<String, HashMap<String, HashMap<String, String>>>> readListFromFilecxn(String filename) {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeCx + "/");
        File myFile = new File(dir, filename);
        List<HashMap<String, HashMap<String, HashMap<String, String>>>> rtrnList = new ArrayList<>();
        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(myFile));
                while (true) {
                    rtrnList.add((HashMap<String, HashMap<String, HashMap<String, String>>>) ois.readObject());
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

    //S_Farah
    public static List<HashMap<String, String>> readListFromFileOF(File file) {
        List<HashMap<String, String>> rtrnList = new ArrayList<>();

        if (file.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(file));
                while (true) {
                    rtrnList.add((HashMap<String, String>) ois.readObject());
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

    public static List<HashMap<String, String>> readListFromFileOF(String filename) {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeOF + "/");
        File myFile = new File(dir, filename);
        List<HashMap<String, String>> rtrnList = new ArrayList<>();
        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(myFile));
                while (true) {
                    rtrnList.add((HashMap<String, String>) ois.readObject());
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

    public static List<HashMap<String, String>> readListFromFileUT(File file) {
        List<HashMap<String, String>> rtrnList = new ArrayList<>();

        if (file.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(file));
                while (true) {
                    rtrnList.add((HashMap<String, String>) ois.readObject());
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

    public static List<HashMap<String, String>> readListFromFileUT(String filename) {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeUT + "/");
        File myFile = new File(dir, filename);
        List<HashMap<String, String>> rtrnList = new ArrayList<>();
        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(myFile));
                while (true) {
                    rtrnList.add((HashMap<String, String>) ois.readObject());
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
    //E_Farah

    public static List<HashMap<String, String>> readListFromFileScreen(String filename) {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeScreen + "/");
        File myFile = new File(dir, filename);
        List<HashMap<String, String>> rtrnList = new ArrayList<>();
        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(myFile));
                while (true) {
                    rtrnList.add((HashMap<String, String>) ois.readObject());
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

    public static JSONArray makeJsonArraytf(String filename) {
        List<HashMap<String, HashMap<String, Long>>> tfArray;
        JSONArray jsonArray = null;
        try {
            tfArray = readListFromFiletf(filename);
            if (!tfArray.isEmpty()) {
                jsonArray = new JSONArray(tfArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public static JSONArray makeJsonArraycpc(String filename) {
        List<HashMap<String, HashMap<String, String>>> cpcArray;
        JSONArray jsonArray = null;
        try {
            cpcArray = readListFromFilecpc(filename);
            if (!cpcArray.isEmpty()) {
                jsonArray = new JSONArray(cpcArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public static JSONArray makeJsonArraycxn(String filename) {
        List<HashMap<String, HashMap<String, HashMap<String, String>>>> cpcArray;
        JSONArray jsonArray = null;
        try {
            cpcArray = readListFromFilecxn(filename);
            if (!cpcArray.isEmpty()) {
                jsonArray = new JSONArray(cpcArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    //S_Farah
    public static JSONArray makeJsonArrayOF(String filename) {
        List<HashMap<String, String>> OFArray;
        JSONArray jsonArray = null;
        try {
            OFArray = readListFromFileOF(filename);
            if (!OFArray.isEmpty()) {
                jsonArray = new JSONArray(OFArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public static JSONArray makeJsonArrayUT(String filename) {
        List<HashMap<String, String>> UTArray;
        JSONArray jsonArray = null;
        try {
            UTArray = readListFromFileUT(filename);
            if (!UTArray.isEmpty()) {
                jsonArray = new JSONArray(UTArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray;
    }
    //E_Farah

    public static JSONArray makeJsonArrayScreen(String filename) {
        List<HashMap<String, String>> UTArray;
        JSONArray jsonArray = null;
        try {
            UTArray = readListFromFileScreen(filename);
            if (!UTArray.isEmpty()) {
                jsonArray = new JSONArray(UTArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public static JSONArray makeJsonArrayPackages(String filename) {
        List<HashMap<String, String>> UTArray;
        JSONArray jsonArray = null;
        try {
            UTArray = readListFromFilepackage(filename);
            if (!UTArray.isEmpty()) {
                jsonArray = new JSONArray(UTArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray;
    }


    public static boolean checkFileSize(String type, String filename, int size) {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + type + "/");
        File myFile = new File(dir, filename);

        int file_size = Integer.parseInt(String.valueOf(myFile.length() / 1024));
        if (file_size >= size)
            return true;
        else
            return false;
    }

    public static boolean isConnectedToWifi(Context context) {

        boolean isConnected = false;
        boolean isWiFi = false;
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        }

        return isWiFi;
    }

    public static HttpsURLConnection setUpHttpsConnection(String urlString, Context context, String type) {
        HttpsURLConnection urlConnection = null;
        String userCredentials = CommonVariables.username + ":" + CommonVariables.password;
        String basicAuth = "Basic " + new String(new Base64().encode(userCredentials.getBytes()));
        try {
            //Create certificate from the certificate in Assets
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            InputStream caInput = new BufferedInputStream(context.getAssets().open("apache.crt"));
            Certificate ca;
            try {
                ca = certificateFactory.generateCertificate(caInput);
                Log.i(CommonVariables.TAG, "ca= " + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            //Create a key store to handle the new certificate
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a trust manager the trusts the server CA
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            //Create SSL context to use the trust manager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            //Config the URL connection to sue the SocketFactory
            URL url = new URL(urlString);
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            urlConnection.setHostnameVerifier(new NullHostNameVerifier());
            urlConnection.setRequestProperty("Authorization", basicAuth);
            // Log.d("Authentication","we used Credentials:"+userCredentials+" as "+basicAuth);
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setRequestMethod(type);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Host", CommonVariables.UploadHost);

        } catch (java.net.SocketTimeoutException e) {
            // the server is not responding
            FirebaseCrash.report(new Exception(e.getMessage()));
            e.printStackTrace();
        } catch (IOException e) {
            FirebaseCrash.report(new Exception(e.getMessage()));
            e.printStackTrace();
        } catch (CertificateException e) {
            FirebaseCrash.report(new Exception(e.getMessage()));
            e.printStackTrace();
        } catch (KeyStoreException e) {
            FirebaseCrash.report(new Exception(e.getMessage()));
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            FirebaseCrash.report(new Exception(e.getMessage()));
            e.printStackTrace();
        } catch (KeyManagementException e) {
            FirebaseCrash.report(new Exception(e.getMessage()));
            e.printStackTrace();
        }
        return urlConnection;
    }

    public static HttpURLConnection setUpHttpConnection(String urlString, String type) {

        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setRequestMethod(type);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Host", CommonVariables.UploadHost);

            return urlConnection;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized static void get3rdPartyApps() {
        String pLine = null;
        CommonVariables.installed3rdPartyApps.clear();
        try {
            Process pmProcess = Runtime.getRuntime().exec("pm list packages -3");
            BufferedReader pmBufferedStream = new BufferedReader(new InputStreamReader(pmProcess.getInputStream()));
            while ((pLine = pmBufferedStream.readLine()) != null) {
                String packageLine[] = pLine.split(":");
                String packageName = packageLine[packageLine.length - 1];
                CommonVariables.installed3rdPartyApps.add(packageName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void getInstalledPackages(Context context) {
        final PackageManager PM = context.getPackageManager();
        CommonVariables.installedPackages = PM.getInstalledApplications(0);
    }

    public static ApplicationInfo getAppInfo(Context context, String Name) {
        final PackageManager PM = context.getPackageManager();
        try {
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(Name, 0);
            return app;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPackageInstallTime(Context context, String Package) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(Package, 0);
            String appFile = appInfo.sourceDir;
            long installed = new File(appFile).lastModified(); //Epoch Time
            return String.valueOf(installed);
        } catch (PackageManager.NameNotFoundException e) {
            // e.printStackTrace();
            Log.d(CommonVariables.TAG, "Package install date not found");
            return "";
        }
    }

    public static void regUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_preference), Context.MODE_PRIVATE);
        final String user = sharedPreferences.getString(context.getString(R.string.user_name), "");

        try {
            // if (user.equals("")) {
            new RegisterUserTask(context).execute(CommonVariables.registrationUrl);
            // }
            CommonVariables.username = sharedPreferences.getString(context.getString(R.string.user_name), "");

            if (!CommonVariables.username.equals("")) {
                CommonVariables.userRegistered = true;
                Log.d(CommonVariables.TAG, "User is registered :" + CommonVariables.username);
            } else {
                CommonVariables.userRegistered = false;
                Log.d(CommonVariables.TAG, "User is not Registered");
            }

        } catch (Exception e) {
            e.printStackTrace();
            CommonVariables.userRegistered = false;
        }
    }

    public static boolean getThresholds(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.trsh_preference), Context.MODE_PRIVATE);
        final String state = sharedPreferences.getString(context.getString(R.string.trsh_preference), "");

        try {
            if (state.equals("")) {
                new DownloadThresholdsTask(context).execute(CommonVariables.DownloadThresholdsURL);
                CommonVariables.startUpdateThresholds = false;
                return true;
            } else {
                CommonVariables.cxAgeThreshold = (sharedPreferences.getString(context.getString(R.string.cxAge), "").equals("")) ? Float.valueOf(0) : Float.valueOf(sharedPreferences.getString(context.getString(R.string.cxAge), ""));
                CommonVariables.prCPUThreshold = (sharedPreferences.getString(context.getString(R.string.prCPU), "").equals("")) ? Float.valueOf(0) : Float.valueOf(sharedPreferences.getString(context.getString(R.string.prCPU), ""));
                CommonVariables.prRSSThreshold = (sharedPreferences.getString(context.getString(R.string.prRSS), "").equals("")) ? Float.valueOf(0) : Float.valueOf(sharedPreferences.getString(context.getString(R.string.prRSS), ""));
                CommonVariables.prVSSThreshold = (sharedPreferences.getString(context.getString(R.string.prVSS), "").equals("")) ? Float.valueOf(0) : Float.valueOf(sharedPreferences.getString(context.getString(R.string.prVSS), ""));
                CommonVariables.txBytesThreshold = (sharedPreferences.getString(context.getString(R.string.txBytes), "").equals("")) ? Float.valueOf(0) : Float.valueOf(sharedPreferences.getString(context.getString(R.string.txBytes), ""));
                CommonVariables.rxBytesThreshold = (sharedPreferences.getString(context.getString(R.string.rxBytes), "").equals("")) ? Float.valueOf(0) : Float.valueOf(sharedPreferences.getString(context.getString(R.string.rxBytes), ""));
                CommonVariables.txPacketsThreshold = (sharedPreferences.getString(context.getString(R.string.txPackets), "").equals("")) ? Float.valueOf(0) : Float.valueOf(sharedPreferences.getString(context.getString(R.string.txPackets), ""));
                CommonVariables.rxPacketsThreshold = (sharedPreferences.getString(context.getString(R.string.rxPackets), "").equals("")) ? Float.valueOf(0) : Float.valueOf(sharedPreferences.getString(context.getString(R.string.rxPackets), ""));
                Log.i(CommonVariables.TAG, " Thresholds are set to local values");
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void  regBroadcastRec(Context context) {
        IntentFilter intentFilter= new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        ScreenReceiver sBroadcast = new ScreenReceiver() ;
        context.registerReceiver(sBroadcast,intentFilter);
        ComponentName eventcomponent = new ComponentName(context, EventReceiver.class);
        ComponentName bootcomponent = new ComponentName(context, BootReceiver.class);
        ComponentName restartcomponent = new ComponentName(context, RestartServiceReceiver.class);
        context.getPackageManager().setComponentEnabledSetting(bootcomponent, PackageManager. COMPONENT_ENABLED_STATE_ENABLED , PackageManager.DONT_KILL_APP);
        context.getPackageManager().setComponentEnabledSetting(eventcomponent, PackageManager. COMPONENT_ENABLED_STATE_ENABLED , PackageManager.DONT_KILL_APP);
        context.getPackageManager().setComponentEnabledSetting(restartcomponent, PackageManager. COMPONENT_ENABLED_STATE_ENABLED , PackageManager.DONT_KILL_APP);
    }

    public static boolean deleteAppDirectory() {

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/");
        if (dir.exists()) {
            dir.delete();
            Log.d(CommonVariables.TAG, "deleteAppDirectory: CrowdApp");
        }
        return true;
    }

    public static boolean checkFirstRun(Context context, SharedPreferences sharedPreferences) {
        final String state = sharedPreferences.getString(context.getString(R.string.firstrun), "");
        try {
            if (state.equals("")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void SafeFirstRun(Context context) {
        SharedPreferences editor = context.getSharedPreferences(context.getString(R.string.interval_preference), Context.MODE_PRIVATE);
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        String version = "";
        try {
            info = manager.getPackageInfo(
                    context.getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // e.printStackTrace();
        }
        boolean firstrun = checkFirstRun(context, editor);
        if (!firstrun) {
            String state = editor.getString(context.getString(R.string.firstrun), "");
            if (!state.equals(version)) {
                deleteAppDirectory();
                editor.edit().putString(context.getString(R.string.firstrun), version).apply();
            }
            }else {
            deleteAppDirectory();
            editor.edit().putString(context.getString(R.string.firstrun), version).apply();
        }
    }
    public static boolean getIntervals(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.interval_preference), Context.MODE_PRIVATE);
        final String state = sharedPreferences.getString(context.getString(R.string.interval_preference), "");

        try {
            if (state.equals("")) {
                new DownloadIntervalsTask(context).execute(CommonVariables.DownloadIntervalsURL);
                CommonVariables.startUpdateIntervals = false;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void showNotificationRunning(Context context) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setSmallIcon(R.drawable.mushroom);
        builder.setContentTitle("CrowdApp Monitor is Running");
        //  Uri alarmtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(71422673, builder.build());
    }

}
