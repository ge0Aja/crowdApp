package com.farah.heavyservice;

import android.app.ActivityManager;
import android.app.NotificationManager;
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
import org.json.JSONObject;

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
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by Georgi on 8/19/2016.
 * the common class contains static mehtods that are used on a global scale
 *
 * read and write hashmaps and lists methods all use an encryption key to encrypt the saved stats and
 * lists with AES algorithm
 */
public class Common {

    /*the ciphered key is used to encrypt the binary objects which we are storing in the binary files*/
    private static byte[] key = {6, 8, 5, 3, 1, 0, 4, 4, 9, 9, 0, 7, 7, 3, 0, 9};
    private static SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

    /*
    public static String getTimestamp() {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String timestamp = format.format(date);
        return timestamp;
    }
*/
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

    // write the hashmaps of CPU and Memory stats to a binary backup file
    public synchronized static boolean writeListToFilecpc(HashMap<String, HashMap<String, String>> captures, String fileName, Boolean append) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        ObjectOutputStream fileOut = null;
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeCPC + "/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File myFile = new File(dir, fileName); //"CPUMEMStats"
        try {
            FileOutputStream os;
            //  CipherOutputStream cos;
            if (!myFile.exists() || !append) {
                os = new FileOutputStream(myFile);
                // cos = new CipherOutputStream(os,cipher);
                fileOut = new ObjectOutputStream(os);
            } else {
                os = new FileOutputStream(myFile, append);
                //cos = new CipherOutputStream(os,cipher);
                fileOut = new AppendableObjectOutputStream(os);
            }
            SealedObject sealedObject = new SealedObject(captures, cipher);
            fileOut.writeObject(sealedObject);
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


    // write the package information of installation and removal when the event is caught by the Boot receiver broadcast receiver  to a binary files
    public synchronized static boolean writePackageStatusToFile(String fileName, String state, String packageName, String Date, String FirstInstall, Context context) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        ObjectOutputStream fileOut = null;
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypePackage + "/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File myFile = new File(dir, fileName);
        HashMap pst = new HashMap<String, String>();
        pst.put("package", packageName);
        pst.put("dispname", getAppName(packageName, context));
        pst.put("state", state);
        pst.put("timestamp", Date);
        pst.put("installed_on", FirstInstall);
        try {
            FileOutputStream os;
            // CipherOutputStream cos;
            if (!myFile.exists()) {
                os = new FileOutputStream(myFile);
                // cos = new CipherOutputStream(os,cipher);
                fileOut = new ObjectOutputStream(os);
            } else {
                os = new FileOutputStream(myFile, true);
                //cos = new CipherOutputStream(os, cipher);
                fileOut = new AppendableObjectOutputStream(os);
            }
            SealedObject sealedObject = new SealedObject(pst, cipher);
            fileOut.writeObject(sealedObject);
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

    // write the screen status logs which are caught by the Screen Receiver Broadcast receiver to a binary file
    public synchronized static boolean writeScreenStatusToFile(String fileName, String state) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        ObjectOutputStream fileOut = null;
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeScreen + "/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File myFile = new File(dir, fileName);
        HashMap scst = new HashMap<String, String>();
        scst.put("state", state);
        scst.put("timestamp", System.currentTimeMillis());
        try {
            FileOutputStream os;
            // CipherOutputStream cos;
            if (!myFile.exists()) {
                os = new FileOutputStream(myFile);
                //  cos = new CipherOutputStream(os,cipher);
                fileOut = new ObjectOutputStream(os);
            } else {
                os = new FileOutputStream(myFile, true);
                // cos = new CipherOutputStream(os, cipher);
                fileOut = new AppendableObjectOutputStream(os);
            }
            SealedObject sealedObject = new SealedObject(scst, cipher);
            fileOut.writeObject(sealedObject);
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

    //write the hashmaps of ConnectionStats to a binary backup file
    public synchronized static boolean writeListToFilecxn(HashMap<String, HashMap<String, HashMap<String, String>>> captures, String fileName, Boolean append) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        ObjectOutputStream fileOut = null;
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        HashMap<String, HashMap<String, HashMap<String, String>>> existing_captures = new HashMap<>();

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeCx + "/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File myFile = new File(dir, fileName); //"CPUMEMStats"
        try {
            FileOutputStream os;
            // CipherOutputStream cos;

            if (myFile.exists()) {
                existing_captures = readListFromFilecxn(myFile);
                Iterator iterator_app = captures.entrySet().iterator();
                while (iterator_app.hasNext()) {
                    Map.Entry app_cxn = (Map.Entry) iterator_app.next();
                    HashMap<String, HashMap<String, String>> new_cxns = (HashMap<String, HashMap<String, String>>) app_cxn.getValue();
                    Iterator iterator_cxn = new_cxns.entrySet().iterator();
                    while (iterator_cxn.hasNext()) {
                        Map.Entry cxn_cxn = (Map.Entry) iterator_cxn.next();
                        if (existing_captures.get(app_cxn.getKey()) != null) {
                            if (existing_captures.get(app_cxn.getKey()).get(cxn_cxn.getKey()) != null) {
                                existing_captures.get(app_cxn.getKey()).get(cxn_cxn.getKey()).put("Age", captures.get(app_cxn.getKey()).get(cxn_cxn.getKey()).get("Age"));
                            } else {
                                existing_captures.get(app_cxn.getKey()).put((String) cxn_cxn.getKey(), (HashMap<String, String>) cxn_cxn.getValue());
                            }
                        } else {
                            existing_captures.put((String) app_cxn.getKey(), (HashMap<String, HashMap<String, String>>) app_cxn.getValue());
                        }
                    }
                }
                os = new FileOutputStream(myFile);
                // cos = new CipherOutputStream(os,cipher);
                fileOut = new ObjectOutputStream(os);
                SealedObject sealedObject = new SealedObject(existing_captures, cipher);
                fileOut.writeObject(sealedObject);
            } else {
                os = new FileOutputStream(myFile);
                // cos = new CipherOutputStream(os,cipher);
                fileOut = new ObjectOutputStream(os);
                SealedObject sealedObject = new SealedObject(captures, cipher);
                fileOut.writeObject(sealedObject);
            }
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


    // the method reads the notification answers from the backup file which are stored when there wasn't a connection and the
    // user answered the notifiation question
    public static JSONArray readAnswersFromFile(String filename) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        SealedObject sealedObject;

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeAnswers + "/");
        File myFile = new File(dir, filename);
        JSONArray jsonArray = new JSONArray();
        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                FileInputStream is = new FileInputStream(myFile);
                // CipherInputStream cipherInputStream = new CipherInputStream(is, cipher);
                ois = new ObjectInputStream(is);
                while (true) {
                    sealedObject = (SealedObject) ois.readObject();
                    jsonArray.put(sealedObject.getObject(cipher));
                }
            } catch (EOFException e) {
                return jsonArray;
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
        return jsonArray;


    }

    // write the hashmaps of the connectionCount stats to a binary backup file
    public synchronized static boolean writeCxCountToFile(HashMap<String, Integer> CxCounts, String filename, Boolean append) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        ObjectOutputStream fileOut = null;
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeCxCount + "/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File myFile = new File(dir, filename); //  "CxCounts"
        try {
            FileOutputStream os;
            //  CipherOutputStream cos;
            if (!myFile.exists() || !append) {
                os = new FileOutputStream(myFile);
                //   cos = new CipherOutputStream(os,cipher);
                fileOut = new ObjectOutputStream(os);
            } else {
                os = new FileOutputStream(myFile, append);
                // cos = new CipherOutputStream(os,cipher);
                fileOut = new AppendableObjectOutputStream(os);
            }
            SealedObject sealedObject = new SealedObject(CxCounts, cipher);
            fileOut.writeObject(sealedObject);
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

    // write the user answer for a specific notification to a backup file in case there wasn't a conneciton
    public synchronized static boolean writeAnswertoFile(JSONObject jsonObject, String filename, Boolean append) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        ObjectOutputStream fileOut = null;
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeAnswers + "/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File myFile = new File(dir, filename); //  "Answers"
        try {
            FileOutputStream os;
            // CipherOutputStream cos;
            if (!myFile.exists() || !append) {
                os = new FileOutputStream(myFile);
                // cos = new CipherOutputStream(os,cipher);
                fileOut = new ObjectOutputStream(os);
            } else {
                os = new FileOutputStream(myFile, append);
                // cos = new CipherOutputStream(os,cipher);
                fileOut = new AppendableObjectOutputStream(os);
            }
            SealedObject sealedObject = new SealedObject((Serializable) jsonObject, cipher);
            fileOut.writeObject(sealedObject);
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

    // write hashmaps of traffic stats to binary backup file
    public synchronized static boolean writeListToFile(HashMap<String, HashMap<String, Long>> captures, String fileName, Boolean append) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        //  AppendToFileNoHeader fileOut = null;
        ObjectOutputStream fileOut = null;
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeTf + "/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File myFile = new File(dir, fileName); //  "TrafficStats"

        try {
            FileOutputStream os;
            //  CipherOutputStream cos;

            if (!myFile.exists() || !append) {
                os = new FileOutputStream(myFile);
                // cos = new CipherOutputStream(os,cipher);
                fileOut = new ObjectOutputStream(os);
            } else {
                os = new FileOutputStream(myFile, append);
                // cos = new CipherOutputStream(os,cipher);
                fileOut = new AppendableObjectOutputStream(os);
            }
            SealedObject sealedObject = new SealedObject(captures, cipher);
            fileOut.writeObject(sealedObject);
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

    // read the saved hashmaps of Connections stats from the binary file
    // this method is called when the upload service call to constrcut a json array with the collected stats
    // this method is called by another satic method (create JSONCx)
    public synchronized static List<HashMap<String, Integer>> readCxCountListFromFile(String filename) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeCxCount + "/");
        File myFile = new File(dir, filename);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        SealedObject sealedObject;

        List<HashMap<String, Integer>> rtrnList = new ArrayList<>();
        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                FileInputStream is = new FileInputStream(myFile);
                // CipherInputStream cipherInputStream = new CipherInputStream(is, cipher);
                ois = new ObjectInputStream(is);
                while (true) {
                    sealedObject = (SealedObject) ois.readObject();
                    rtrnList.add((HashMap<String, Integer>) sealedObject.getObject(cipher));
                }
            } catch (EOFException e) {
                //e.printStackTrace();
                return rtrnList;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
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


   /* public static String convertHexToString(String hexValue) {
        String IP = "";
        String hex = hexValue.substring(hexValue.length() - 8);
        IP = String.valueOf(Integer.parseInt(hex.substring(6, 8), 16)) + "." + String.valueOf(Integer.parseInt(hex.substring(4, 6), 16)) + "." + String.valueOf(Integer.parseInt(hex.substring(2, 4), 16)) + "." + String.valueOf(Integer.parseInt(hex.substring(0, 2), 16));
        return IP;
    }*/

    // read the thresholds list which are downloaded from the server and saved in a binary file
    public synchronized static HashMap<String, HashMap<String, HashMap<String, Float>>> readThreshListFromFile(String filename) {

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/");
        File myFile = new File(dir, filename);
        HashMap<String, HashMap<String, HashMap<String, Float>>> rtrnList = new HashMap<>();
        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(myFile));
                while (true) {
                    rtrnList = ((HashMap<String, HashMap<String, HashMap<String, Float>>>) ois.readObject());
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

    // write the downloaded thresholds from the server to a binary file
    public synchronized static boolean writeThreshListToFile(HashMap<String, HashMap<String, HashMap<String, Float>>> thresholds, String fileName, Boolean append) {
        //  AppendToFileNoHeader fileOut = null;
        ObjectOutputStream fileOut = null;
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/");
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
            fileOut.writeObject(thresholds);
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
    //write the hashmaps of Open frequency stats to a binary file
    public synchronized static boolean writeListToFileOF(HashMap<String, String> captures, String fileName, Boolean append) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        ObjectOutputStream fileOut = null;
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeOF + "/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File myFile = new File(dir, fileName); //"CPUMEMStats"
        try {
            FileOutputStream os;
            // CipherOutputStream cos;

            if (!myFile.exists() || !append) {
                os = new FileOutputStream(myFile);
                //  cos = new CipherOutputStream(os,cipher);
                fileOut = new ObjectOutputStream(os);
            } else {
                os = new FileOutputStream(myFile, append);
                // cos = new CipherOutputStream(os, cipher);
                fileOut = new AppendableObjectOutputStream(os);
            }
            SealedObject sealedObject = new SealedObject(captures, cipher);
            fileOut.writeObject(sealedObject);
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

    //write the hashmap of usagetime stats to a binary file
    public synchronized static boolean writeListToFileUT(HashMap<String, String> captures, String fileName, Boolean append) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        ObjectOutputStream fileOut = null;
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeUT + "/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File myFile = new File(dir, fileName); //"CPUMEMStats"
        try {
            FileOutputStream os;
            // CipherOutputStream cos;
            if (!myFile.exists() || !append) {
                os = new FileOutputStream(myFile);
                //  cos = new CipherOutputStream(os,cipher);
                fileOut = new ObjectOutputStream(os);
            } else {
                os = new FileOutputStream(myFile, append);
                //  cos = new CipherOutputStream(os, cipher);
                fileOut = new AppendableObjectOutputStream(os);
            }
            SealedObject sealedObject = new SealedObject(captures, cipher);
            fileOut.writeObject(sealedObject);
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
    // read package changes from a binary file (installation and removal)
    public synchronized static List<HashMap<String, String>> readListFromFilepackage(String filename) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        SealedObject sealedObject;
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypePackage + "/");
        File myFile = new File(dir, filename);
        List<HashMap<String, String>> rtrnList = new ArrayList<>();
        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                FileInputStream is = new FileInputStream(myFile);
                // CipherInputStream cipherInputStream = new CipherInputStream(is, cipher);
                ois = new ObjectInputStream(is);
                while (true) {
                    sealedObject = (SealedObject) ois.readObject();
                    rtrnList.add((HashMap<String, String>) sealedObject.getObject(cipher));
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

    //read the list of traffic stats hashmaps from a binary file (this method takes a file as an input)
    public synchronized static List<HashMap<String, HashMap<String, Long>>> readListFromFiletf(File file) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        List<HashMap<String, HashMap<String, Long>>> rtrnList = new ArrayList<>();
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        SealedObject sealedObject;
        if (file.exists()) {
            ObjectInputStream ois = null;
            try {
                FileInputStream is = new FileInputStream(file);
                //  CipherInputStream cipherInputStream = new CipherInputStream(is, cipher);
                ois = new ObjectInputStream(is);
                while (true) {
                    sealedObject = (SealedObject) ois.readObject();
                    rtrnList.add((HashMap<String, HashMap<String, Long>>) sealedObject.getObject(cipher));
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

    //read the list of traffic stats hashmaps from a binary file (this method takes a file name string as an input)
    public synchronized static List<HashMap<String, HashMap<String, Long>>> readListFromFiletf(String filename) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeTf + "/");
        File myFile = new File(dir, filename);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        SealedObject sealedObject;
        List<HashMap<String, HashMap<String, Long>>> rtrnList = new ArrayList<>();
        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                FileInputStream is = new FileInputStream(myFile);
                // CipherInputStream cipherInputStream = new CipherInputStream(is, cipher);
                ois = new ObjectInputStream(is);
                while (true) {
                    sealedObject = (SealedObject) ois.readObject();
                    rtrnList.add((HashMap<String, HashMap<String, Long>>) sealedObject.getObject(cipher));
                }
            } catch (EOFException e) {
                // e.printStackTrace();
                return rtrnList;
            } catch (Exception e) {
                e.printStackTrace();
                myFile.delete();
                Log.d(CommonVariables.TAG, "file " + myFile.getName() + " is deleted due to erros");
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

    //read the list of CPU and MEm stats hashmaps from a binary file (this method takes a file as an input)
    public synchronized static List<HashMap<String, HashMap<String, String>>> readListFromFilecpc(File file) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        SealedObject sealedObject;
        List<HashMap<String, HashMap<String, String>>> rtrnList = new ArrayList<>();
        if (file.exists()) {
            ObjectInputStream ois = null;
            try {
                FileInputStream is = new FileInputStream(file);
                //  CipherInputStream cipherInputStream = new CipherInputStream(is, cipher);
                ois = new ObjectInputStream(is);
                while (true) {
                    sealedObject = (SealedObject) ois.readObject();
                    rtrnList.add((HashMap<String, HashMap<String, String>>) sealedObject.getObject(cipher));
                }
            } catch (EOFException e) {
                //e.printStackTrace();
                return rtrnList;
            } catch (Exception e) {
                e.printStackTrace();
                file.delete();
                Log.d(CommonVariables.TAG, "file " + file.getName() + " is deleted due to erros");
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

    //read the list of CPU and MEm stats hashmaps from a binary file (this method takes a file name string as an input)
    public synchronized static List<HashMap<String, HashMap<String, String>>> readListFromFilecpc(String filename) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        SealedObject sealedObject;

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeCPC + "/");
        File myFile = new File(dir, filename);
        List<HashMap<String, HashMap<String, String>>> rtrnList = new ArrayList<>();
        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                FileInputStream is = new FileInputStream(myFile);
                //  CipherInputStream cipherInputStream = new CipherInputStream(is, cipher);
                ois = new ObjectInputStream(is);
                while (true) {
                    sealedObject = (SealedObject) ois.readObject();
                    rtrnList.add((HashMap<String, HashMap<String, String>>) sealedObject.getObject(cipher));
                }
            } catch (EOFException e) {
                //e.printStackTrace();
                return rtrnList;
            } catch (Exception e) {
                e.printStackTrace();
                myFile.delete();
                Log.d(CommonVariables.TAG, "file " + myFile.getName() + " is deleted due to erros");
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

    //read the list of Connection stats hashmaps from a binary file (this method takes a file as an input)
    public synchronized static HashMap<String, HashMap<String, HashMap<String, String>>> readListFromFilecxn(File file) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        HashMap<String, HashMap<String, HashMap<String, String>>> rtrnList = new HashMap<>();
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        SealedObject sealedObject;

        if (file.exists()) {
            ObjectInputStream ois = null;
            try {
                FileInputStream is = new FileInputStream(file);
                //   CipherInputStream cipherInputStream = new CipherInputStream(is, cipher);
                ois = new ObjectInputStream(is);
                while (true) {
                    sealedObject = (SealedObject) ois.readObject();
                    rtrnList = ((HashMap<String, HashMap<String, HashMap<String, String>>>) sealedObject.getObject(cipher));
                }
            } catch (EOFException e) {
                //e.printStackTrace();
                return rtrnList;
            } catch (Exception e) {
                e.printStackTrace();
                file.delete();
                Log.d(CommonVariables.TAG, "file " + file.getName() + " is deleted due to erros");
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

    //read the list of Connection stats hashmaps from a binary file (this method takes a file name string as an input)
    public synchronized static HashMap<String, HashMap<String, HashMap<String, String>>> readListFromFilecxn(String filename) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeCx + "/");
        File myFile = new File(dir, filename);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        SealedObject sealedObject;
        HashMap<String, HashMap<String, HashMap<String, String>>> rtrnList = new HashMap<>();
        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                FileInputStream is = new FileInputStream(myFile);
                //   CipherInputStream cipherInputStream = new CipherInputStream(is, cipher);
                ois = new ObjectInputStream(is);
                while (true) {
                    sealedObject = (SealedObject) ois.readObject();
                    rtrnList = ((HashMap<String, HashMap<String, HashMap<String, String>>>) sealedObject.getObject(cipher));
                }
            } catch (EOFException e) {
                //e.printStackTrace();
                return rtrnList;
            } catch (Exception e) {
                e.printStackTrace();
                myFile.delete();
                Log.d(CommonVariables.TAG, "file " + myFile.getName() + " is deleted due to erros");
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
    //read the list of Open Frequency stats hashmaps from a binary file (this method takes a file as an input)
    public synchronized static List<HashMap<String, String>> readListFromFileOF(File file) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        List<HashMap<String, String>> rtrnList = new ArrayList<>();
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        SealedObject sealedObject;
        if (file.exists()) {
            ObjectInputStream ois = null;
            try {
                FileInputStream is = new FileInputStream(file);
                //  CipherInputStream cipherInputStream = new CipherInputStream(is, cipher);
                ois = new ObjectInputStream(is);
                while (true) {
                    sealedObject = (SealedObject) ois.readObject();
                    rtrnList.add((HashMap<String, String>) sealedObject.getObject(cipher));
                }
            } catch (EOFException e) {
                // e.printStackTrace();
                return rtrnList;
            } catch (Exception e) {
                e.printStackTrace();
                file.delete();
                Log.d(CommonVariables.TAG, "file " + file.getName() + " is deleted due to erros");
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

    //read the list of Open Frequency stats hashmaps from a binary file (this method takes a file name string as an input)
    public synchronized static List<HashMap<String, String>> readListFromFileOF(String filename) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeOF + "/");
        File myFile = new File(dir, filename);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        SealedObject sealedObject;
        List<HashMap<String, String>> rtrnList = new ArrayList<>();
        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                FileInputStream is = new FileInputStream(myFile);
                // CipherInputStream cipherInputStream = new CipherInputStream(is, cipher);
                ois = new ObjectInputStream(is);
                while (true) {
                    sealedObject = (SealedObject) ois.readObject();
                    rtrnList.add((HashMap<String, String>) sealedObject.getObject(cipher));
                }
            } catch (EOFException e) {
                // e.printStackTrace();
                return rtrnList;
            } catch (Exception e) {
                e.printStackTrace();
                myFile.delete();
                Log.d(CommonVariables.TAG, "file " + myFile.getName() + " is deleted due to erros");
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

    //read the list of Usage time stats hashmaps from a binary file (this method takes a file as an input)
    public synchronized static List<HashMap<String, String>> readListFromFileUT(File file) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        List<HashMap<String, String>> rtrnList = new ArrayList<>();
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        SealedObject sealedObject;

        if (file.exists()) {
            ObjectInputStream ois = null;
            try {
                FileInputStream is = new FileInputStream(file);
                //   CipherInputStream cipherInputStream = new CipherInputStream(is, cipher);
                ois = new ObjectInputStream(is);
                while (true) {
                    sealedObject = (SealedObject) ois.readObject();
                    rtrnList.add((HashMap<String, String>) sealedObject.getObject(cipher));
                }
            } catch (EOFException e) {
                // e.printStackTrace();
                return rtrnList;
            } catch (Exception e) {
                e.printStackTrace();
                file.delete();
                Log.d(CommonVariables.TAG, "file " + file.getName() + " is deleted due to erros");
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

    //read the list of Usage time stats hashmaps from a binary file (this method takes a file name string as an input)
    public synchronized static List<HashMap<String, String>> readListFromFileUT(String filename) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeUT + "/");
        File myFile = new File(dir, filename);
        List<HashMap<String, String>> rtrnList = new ArrayList<>();
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        SealedObject sealedObject;

        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                FileInputStream is = new FileInputStream(myFile);
                // CipherInputStream cipherInputStream = new CipherInputStream(is, cipher);
                ois = new ObjectInputStream(is);
                while (true) {
                    sealedObject = (SealedObject) ois.readObject();
                    rtrnList.add((HashMap<String, String>) sealedObject.getObject(cipher));
                }
            } catch (EOFException e) {
                // e.printStackTrace();
                return rtrnList;
            } catch (Exception e) {
                e.printStackTrace();
                myFile.delete();
                Log.d(CommonVariables.TAG, "file " + myFile.getName() + " is deleted due to erros");
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

    // read the log of screen activity which is appended to a file when received in the screen broadcast receiver
    public synchronized static List<HashMap<String, String>> readListFromFileScreen(String filename) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + CommonVariables.filetypeScreen + "/");
        File myFile = new File(dir, filename);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        SealedObject sealedObject;

        List<HashMap<String, String>> rtrnList = new ArrayList<>();
        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                FileInputStream is = new FileInputStream(myFile);
                // CipherInputStream cipherInputStream = new CipherInputStream(is, cipher);
                ois = new ObjectInputStream(is);
                while (true) {
                    sealedObject = (SealedObject) ois.readObject();
                    rtrnList.add((HashMap<String, String>) sealedObject.getObject(cipher));
                }
            } catch (EOFException e) {
                // e.printStackTrace();
                return rtrnList;
            } catch (Exception e) {
                e.printStackTrace();
                myFile.delete();
                Log.d(CommonVariables.TAG, "file " + myFile.getName() + " is deleted due to erros");
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

    //create a json array of traffic stats called when uploading the stats which are logged in the binary files
    // the method calls readListFromFiletf method inside
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

    //create a json array of Connections Count stats called when uploading the stats which are logged in the binary files
    // the method calls readCxCountListFromFile method inside
    public static JSONArray makeJsonArrayCxCount(String filename) {
        List<HashMap<String, Integer>> CxCountArray;
        JSONArray jsonArray = null;

        try {
            CxCountArray = readCxCountListFromFile(filename);
            if (!CxCountArray.isEmpty()) {
                jsonArray = new JSONArray(CxCountArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    //create a json array of CPU and Mem stats called when uploading the stats which are logged in the binary files
    // the method calls readListFromFilecpc method inside
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

    //create a json array of Connections stats called when uploading the stats which are logged in the binary files
    // the method calls readListFromFilecxn method inside
    public static JSONObject makeJsonArraycxn(String filename) {
        HashMap<String, HashMap<String, HashMap<String, String>>> cpcArray;
        JSONObject jsonObject = null;
        try {
            //
            //
            //
            cpcArray = readListFromFilecxn(filename);
            //   JSONObject jsonObject = new JSONObject(cpcArray);
            if (!cpcArray.isEmpty()) {
                //   jsonArray = new JSONArray(jsonObject.toString());

                jsonObject = new JSONObject(cpcArray);
                //
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    //S_Farah
    //create a json array of Open Frequency called when uploading the stats which are logged in the binary files
    // the method calls readListFromFileOF method inside
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

    //create a json array of Usage Time called when uploading the stats which are logged in the binary files
    // the method calls readListFromFileUT method inside
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

    //create a json array of Screen activity logs when uploading the stats which are logged in the binary files
    // the method calls readListFromFileScreen method inside
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

    //craete a json array of Packet information (removal and installation) which is saved in binary files
    // when received in the BootReceiver broadcast receiver
    // this method calls readListFromFilepackage inside
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


    // the method is used to check the file size against a specific threshold
    // downloaded from the server
    public static boolean checkFileSize(String type, String filename, int size) {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + type + "/");
        File myFile = new File(dir, filename);

        int file_size = Integer.parseInt(String.valueOf(myFile.length() / 1024));
        return file_size >= size;
    }

    // starts an Async task to check the connection state (wifi + certain IP is reachable)
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

    // create an Https connection using a specific URL provided when the upload service is starting the upload process
    // the method uses a certificate generated and signed by the App server
    // and set the content that should be sent in as json
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
                //  Log.i(CommonVariables.TAG, "ca= " + ((X509Certificate) ca).getSubjectDN());
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

    // create an Http connection using a specific URl provided when the upload service is starting the upload process
    // this method is not used because of the security concerns
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

    // get the installed thrid part Apps
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

    // get all installed packages
    public synchronized static void getInstalledPackages(Context context) {
        final PackageManager PM = context.getPackageManager();
        CommonVariables.installedPackages = new CopyOnWriteArrayList<ApplicationInfo>(PM.getInstalledApplications(0));
    }

    // get app info by app name
    public static ApplicationInfo getAppInfo(Context context, String Name) {
        // final PackageManager PM = context.getPackageManager();
        try {
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(Name, 0);
            return app;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    //get the package install time, this method is called when deleting a package which triggers alisterner
    // or when reporting the installed packages to the server at user registeration
    public static String getPackageInstallTime(Context context, String Package) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(Package, 0);
            String appFile = appInfo.sourceDir;
            long installed = new File(appFile).lastModified(); //Epoch Time
            return String.valueOf(installed / 1000);
        } catch (PackageManager.NameNotFoundException e) {
            // e.printStackTrace();
            Log.d(CommonVariables.TAG, "Package install date not found");
            return "";
        }
    }

    // start the register user task at first run of the service or after service restart to make sure
    // that a user is registered
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

    //check connection checks that the phone is conneced to WIFI and a certain IP is reachable
    public static void checkConnection(Context context) {
        new checkConnectivity(context).execute();
    }

    // triggers the getThresholds task to update the threshold values from the server
    public static boolean getThresholds(Context context) {
        CommonVariables.RequestedThresholds = true;
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.trsh_preference), Context.MODE_PRIVATE);
        final String state = sharedPreferences.getString(context.getString(R.string.trsh_preference), "");

        try {
            if (state.equals("") || !state.equals(String.valueOf(System.currentTimeMillis()))) {
                new DownloadThresholdsTask(context).execute(CommonVariables.DownloadThresholdsURL);
                CommonVariables.startUpdateThresholds = false;
                return true;
            } else {
                Log.i(CommonVariables.TAG, " Thresholds are set to local values");
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }

    // write the received ratings from the server to a binary file as hashmap <string,float>
    public synchronized static boolean writeAppRatingsToFile(HashMap<String, Float> ratings, String filename, Boolean append) {
        ObjectOutputStream fileOut = null;
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File myFile = new File(dir, filename);

        try {
            if (!myFile.exists() || !append) {
                fileOut = new ObjectOutputStream(new FileOutputStream(myFile));
            } else {
                fileOut = new AppendableObjectOutputStream(new FileOutputStream(myFile, append));
            }
            fileOut.writeObject(ratings);
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


    public synchronized static HashMap<String, Float> readAppRatingsListFromFile(String filename) {

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/");
        File myFile = new File(dir, filename);
        HashMap<String, Float> rtrnList = new HashMap<>();
        if (myFile.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(myFile));
                while (true) {
                    rtrnList = ((HashMap<String, Float>) ois.readObject());
                }
            } catch (EOFException e) {
                //e.printStackTrace();
                //return rtrnList;
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

    // register the broadcast receiver at application start
    public static void regBroadcastRec(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        ScreenReceiver sBroadcast = new ScreenReceiver();
        context.registerReceiver(sBroadcast, intentFilter);
        ComponentName eventcomponent = new ComponentName(context, EventReceiver.class);
        ComponentName bootcomponent = new ComponentName(context, BootReceiver.class);
        ComponentName restartcomponent = new ComponentName(context, RestartServiceReceiver.class);
        context.getPackageManager().setComponentEnabledSetting(bootcomponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        context.getPackageManager().setComponentEnabledSetting(eventcomponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        context.getPackageManager().setComponentEnabledSetting(restartcomponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    // delete the binary files in a directory after upload has sucessfully finished
    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    // delete the application directory if a new version of the App is installed
    // the method is called to prevent App files compatibilty issues
    public static boolean deleteAppDirectory() {

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/");
        if (dir.exists()) {
            deleteRecursive(dir);
            Log.d(CommonVariables.TAG, "deleteAppDirectory: CrowdApp");
        }
        return true;
    }

    //report if this is the first run of the service
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

    //checks if it is the first run of the service and if yes it deletes the old files from the old
    //service version
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
        } else {
            deleteAppDirectory();
            editor.edit().putString(context.getString(R.string.firstrun), version).apply();
        }
    }

    //calls an asyn task to download the intervals from the server and update the current invetvals values
    public static boolean getIntervals(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.interval_preference), Context.MODE_PRIVATE);
        final String state = sharedPreferences.getString(context.getString(R.string.interval_preference), "");

        try {
            if (state.equals("") || !state.equals(String.valueOf(System.currentTimeMillis()))) {
                new DownloadIntervalsTask(context).execute(CommonVariables.DownloadIntervalsURL);
                CommonVariables.startUpdateIntervals = false;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // check if the App has been granted the required permissions in case of Android OS 6.0
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

    // delete a specific file in a specific directory called when the single file upload has finished
    // successfully
    public static void deleteFilesFromDirectory(String type, String currentfile) {
        String Dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + type + "/";
        File f = new File(Dir);
        try {
            if (f != null) {
                File files[] = f.listFiles();
                if (files != null) {
                    if (files.length != 0) {
                        for (File fi : files
                                ) {

                            if (fi != null) {
                                if (!currentfile.equals("") && !fi.getName().equals(currentfile) && !fi.getName().equals("CumulativeTrafficStatsBkup") && !fi.getName().equals("CumulativeCxStatsBkup")) {
                                    if (fi.delete())
                                        Log.d(CommonVariables.TAG, "file " + fi.getName() + " was deleted successfully");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // check if the service running when the event broadcast receiver receives that the phone
    // has completed the boot operation
    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    // shows a sticky notification that the service is running
    public static void showNotificationRunning(Context context) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setSmallIcon(R.drawable.mushroom); // pp
        builder.setContentTitle("CrowdApp Monitor is Running!!");
        //  Uri alarmtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(71422673, builder.build());
    }


}
