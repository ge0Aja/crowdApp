package com.farah.heavyservice;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.HashMap;

/**
 * Created by Georgi on 8/13/2016.
 * <p/>
 * this service is used when the main service initiate a file or a directory upload
 */
public class ClientServerService extends IntentService {

    // reported status of the upload service
    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;
    public static final int STATUS_FINISHED_SUCCESS = 1;
    public static final int STATUS_FINISHED_ERROR = 2;
    public static final int STATUS_FINISHED_NOFILES = 3;
    public static final int STATUS_FINISHED_FORBIDDEN = 8;
    public static final int STATUS_FINISHED_SERVER_ERROR = 9;
    public static final int STATUS_FINISHED_NOWIFI = 4;
    public static final int STATUS_FINISHED_NO_RESPONSE_FROM_SERVER = 7;

    public static final String TAG = "HeavyService";

    public ClientServerService() {
        super("ClientServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // after receiving the intent to start the upload sleep for a specific interval
        // this was added to decrease the load on the server
        try {
            Thread.sleep(10000);
         //   Log.d(TAG, "onHandleIntent: "+ intent.toString());
            Log.d(TAG, "Upload Service Started!");
            // check which receiver should receive the status of the service
            // and get the upload type file or directory
            if(intent.getParcelableExtra("receiver") == null || intent.getStringExtra("uploadtype") == null){
                Log.d(TAG, "Post Service Stopping! Because false intent");
                this.stopSelf();
            }else {
                final ResultReceiver receiver = intent.getParcelableExtra("receiver");
                String uploadType = intent.getStringExtra("uploadtype");
                Log.d(TAG, "Upload type is " + uploadType);
                // create a bundle to append the result data to it
                Bundle bundle = new Bundle();
                // if the upload type is file and we have connection to the server and no other upload service is running start
                if (uploadType.equals(CommonVariables.UploadTypeFile) && CommonVariables.isWiFi && CommonVariables.startUpload && CommonVariables.userRegistered) {
                    Log.d(TAG, "The upload type is file and should start uploading");
                    // get the url from the intent
                    String url = intent.getStringExtra("url");
                    // get the file name that has exceeded the size
                    String filename = intent.getStringExtra("filename");
                    Log.d(TAG, "file to upload is " + filename);
                    // get the type of the upload file (CPU , MEM , Traffic , Conn)
                    String type = intent.getStringExtra("type");
                    // if the url is not empty
                    if (!TextUtils.isEmpty(url)) {
                        bundle.putString("type", type);
                        receiver.send(STATUS_RUNNING, bundle);
                        try {
                            //call the upload method which uses a secure connection HTTPS
                            String[] results = uploadDataSecure(url, type, filename);
                            Log.d(TAG, String.valueOf(results));
                /* Sending result back to Service
                * put the file name and the status of the file along with its type
                * */
                            if (results[0] != null && results.length != 0) {
                                if (!results[0].equals("Unconfirmed")) {
                                    bundle.putString("filename", filename);
                                    bundle.putString("type", type);
                                    if (results[0].equals("success")
                                            ) {
                                        bundle.putInt("Status", STATUS_FINISHED_SUCCESS);
                                        // bundle.putStringArray("result", results);
                                    } else {
                                        bundle.putInt("Status", STATUS_FINISHED_ERROR);
                                        bundle.putStringArray("result", results);
                                    }
                                    receiver.send(STATUS_FINISHED, bundle);
                                } else { // Handled
                                    bundle.putInt("Status", STATUS_FINISHED_NO_RESPONSE_FROM_SERVER);
                                    receiver.send(STATUS_FINISHED, bundle);
                                }
                                Log.d(TAG, "Upload Service executed successfully!");
                            }
                            this.stopSelf();
                            //    }
                        } catch (Exception e) {
                /* Sending error message back to activity */
                            e.printStackTrace();
                            bundle.putString("result", e.toString());
                            receiver.send(STATUS_ERROR, bundle);
                            Log.d(TAG, "Upload Service executed with errors!");
                            this.stopSelf();
                        }
                    }
                }
                // if the upload type is directory and the app is connected to wifi and there is no other upload running
                else if (uploadType.equals(CommonVariables.UploadTypeDir) && CommonVariables.isWiFi && CommonVariables.startUploadDir && CommonVariables.userRegistered) {
                    // check the directory type that we want to upload the files from
            /*
            the type of the directories are (All, CPC, TF, CONN, OF, UT)
            * */
                    String type = intent.getStringExtra("type");
                    if (!type.equals(CommonVariables.filetypeAll)) {
                        // the directory upload is not multiple
                        Log.d(TAG, "The upload type is one Dir and should start uploading");
                        try {
                            // call the directory upload method which uses a secure connection
                            HashMap<String, String> results = uploadDataDirSecure(type);
                            bundle.putString("type", type);
                    /*return the results of the upload to the receiver if the common variables class*/
                            if (results.get("Unauthorized") != null) {
                                bundle.putInt("Status", STATUS_FINISHED_FORBIDDEN);
                                receiver.send(STATUS_FINISHED, bundle);
                            } else if (results.get("finished") != null) {
                                bundle.putString("finished_message", results.get("finished"));
                                bundle.putInt("Status", STATUS_FINISHED_SERVER_ERROR);
                                receiver.send(STATUS_FINISHED, bundle);
                            } else if (results.get("Error") != null) {
                                bundle.putString("Error_message", results.get("Error"));
                                bundle.putInt("Status", STATUS_FINISHED_ERROR);
                                receiver.send(STATUS_FINISHED, bundle);
                            } else if (results.get("Unconfirmed") != null) {
                                bundle.putInt("Status", STATUS_FINISHED_NO_RESPONSE_FROM_SERVER);
                                receiver.send(STATUS_FINISHED, bundle);
                            } else {
                                bundle.putInt("Status", STATUS_FINISHED_SUCCESS);
                                bundle.putString("currentfile", results.get("currentfile"));
                                receiver.send(STATUS_FINISHED, bundle);
                            }
                            Log.d(TAG, "Upload Service executed successfully!");
                            this.stopSelf();
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                            bundle.putString("result", e.toString());
                            receiver.send(STATUS_ERROR, bundle);
                            Log.d(TAG, "Upload Service executed with errors!");
                            this.stopSelf();
                        }
                    } else {
                /*the type of the upload is multi directory and the upload will repeat for each one*/
                        Log.d(TAG, "The upload type is multiple Dir and should start uploading");
                        String[] fileTypes = {CommonVariables.filetypeAnswers, CommonVariables.filetypeCPC, CommonVariables.filetypeCx, CommonVariables.filetypeTf, CommonVariables.filetypeOF, CommonVariables.filetypeUT, CommonVariables.filetypeScreen};
                        for (String t : fileTypes
                                ) {
                            try {
                                receiver.send(STATUS_RUNNING, bundle);
                                HashMap<String, String> results = uploadDataDirSecure(String.valueOf(t));
                                bundle.putString("type", String.valueOf(t));
                        /*return the results of the upload to the receiver if the common variables class*/
                                if (results.get("Unauthorized") != null) {
                                    bundle.putInt("Status", STATUS_FINISHED_FORBIDDEN);
                                    receiver.send(STATUS_FINISHED, bundle);
                                } else if (results.get("finished") != null) {
                                    bundle.putString("finished_message", results.get("finished"));
                                    bundle.putInt("Status", STATUS_FINISHED_SERVER_ERROR);
                                    receiver.send(STATUS_FINISHED, bundle);
                                } else if (results.get("Error") != null) {
                                    bundle.putString("Error_message", results.get("Error"));
                                    bundle.putInt("Status", STATUS_FINISHED_ERROR);
                                    receiver.send(STATUS_FINISHED, bundle);
                                } else if (results.get("Unconfirmed") != null) {
                                    bundle.putInt("Status", STATUS_FINISHED_NO_RESPONSE_FROM_SERVER);
                                    receiver.send(STATUS_FINISHED, bundle);
                                } else {
                                    bundle.putInt("Status", STATUS_FINISHED_SUCCESS);
                                    bundle.putString("currentfile", results.get("currentfile"));
                                    receiver.send(STATUS_FINISHED, bundle);
                                }
                                Log.d(TAG, "Upload Service executed successfully for type" + String.valueOf(t));
                                this.stopSelf();
                            } catch (java.io.IOException e) {
                                e.printStackTrace();
                                bundle.putString("result", e.toString());
                                receiver.send(STATUS_ERROR, bundle);
                                Log.d(TAG, "Upload Service executed with errors for type: " + String.valueOf(t));
                                this.stopSelf();
                            }
                        }
                    }
                } else if (!CommonVariables.isWiFi) {
                    bundle.putInt("Status", STATUS_FINISHED_NOWIFI);
                    receiver.send(STATUS_FINISHED, bundle);
                }
                Log.d(TAG, "Post Service Stopping!");
                this.stopSelf();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //secured upload for a directory of files
    private HashMap<String, String> uploadDataDirSecure(String type) throws IOException {
        /*added the delay to protect the server from being overloaded */
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String tempOutput = "";
        String currentFileName = "";
        StringBuilder sb = new StringBuilder();
        HttpURLConnection postUrlConnection = null;
        HashMap<String, String> output = new HashMap<>();
        JSONArray newJsonArray = null;
        JSONObject newJsonObject = null;
        /*get the type of the directory and start listing the files */
        String Dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + type + "/";
        File f = new File(Dir);
        if (f != null) {
            File files[] = f.listFiles();
            if (files != null) {
                if (files.length != 0) {
                    try {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // according to the file type set the https connection variables (URL)
                        if (type.equals(CommonVariables.filetypeCPC)) {
                            postUrlConnection = Common.setUpHttpsConnection(CommonVariables.CPCUploadURL, this.getApplicationContext(), "POST");
                            currentFileName = CommonVariables.CPCBkup;
                        } else if (type.equals(CommonVariables.filetypeCx)) {
                            postUrlConnection = Common.setUpHttpsConnection(CommonVariables.CxUploadURL, this.getApplicationContext(), "POST");
                            currentFileName = CommonVariables.CxBkup;
                        } else if (type.equals(CommonVariables.filetypeCxCount)) {
                            postUrlConnection = Common.setUpHttpsConnection(CommonVariables.CxCountUploadURL, this.getApplicationContext(), "POST");
                            currentFileName = CommonVariables.CxCountBkup;
                        } else if (type.equals(CommonVariables.filetypeTf)) {
                            postUrlConnection = Common.setUpHttpsConnection(CommonVariables.TFUploadURL, this.getApplicationContext(), "POST");
                            currentFileName = CommonVariables.TFBkup;
                        } else if (type.equals(CommonVariables.filetypeUT)) {
                            postUrlConnection = Common.setUpHttpsConnection(CommonVariables.UTUploadURL, this.getApplicationContext(), "POST");
                            currentFileName = CommonVariables.UTBkup;
                        } else if (type.equals(CommonVariables.filetypeOF)) {
                            postUrlConnection = Common.setUpHttpsConnection(CommonVariables.OFUploadURL, this.getApplicationContext(), "POST");
                            currentFileName = CommonVariables.OFBkup;
                        } else if (type.equals(CommonVariables.filetypeScreen)) {
                            postUrlConnection = Common.setUpHttpsConnection(CommonVariables.ScreenUploadURL, this.getApplicationContext(), "POST");
                            currentFileName = CommonVariables.ScreenBkup;
                        } else if (type.equals(CommonVariables.filetypeAnswers)) {
                            postUrlConnection = Common.setUpHttpsConnection(CommonVariables.SubmitMultiAnswer, this.getApplicationContext(), "POST");
                            currentFileName = "Not";
                        }
                        if (postUrlConnection != null) {
                            // start creating the upload JSON array which has the logged stats
                            // when iterating over the files if we find any file named delete we have to delete it
                            // this was added to delete the files that were uploaded or caused problems
                            // iterate over all the files and add each created JSon array to a bigger JSON array structure
                            // which will be uploaded
                            JSONArray AllFileJsonArray = new JSONArray();
                            for (File fi : files
                                    ) {
                                if (fi.getName().startsWith("delete")) {
                                    if (fi.delete())
                                        output.put(fi.getName(), "delete");
                                    else {
                                        output.put(fi.getName(), "deleteFailed");
                                    }
                                } else if (!currentFileName.equals("") && !fi.getName().equals(currentFileName) && !fi.getName().equals("CumulativeTrafficStatsBkup") && !fi.getName().equals("CumulativeCxStatsBkup")) {
                                    tempOutput = "";
                                    sb.setLength(0);
                                    if (type.equals(CommonVariables.filetypeCPC)) {
                                        newJsonArray = Common.makeJsonArraycpc(fi.getName());
                                    } else if (type.equals(CommonVariables.filetypeTf)) {
                                        newJsonArray = Common.makeJsonArraytf(fi.getName());
                                    } else if (type.equals(CommonVariables.filetypeCx)) {
                                        newJsonObject = Common.makeJsonArraycxn(fi.getName());
                                    } else if (type.equals(CommonVariables.filetypeCxCount)) {
                                        newJsonArray = Common.makeJsonArrayCxCount(fi.getName());
                                    } else if (type.equals(CommonVariables.filetypeOF)) {
                                        newJsonArray = Common.makeJsonArrayOF(fi.getName());
                                    } else if (type.equals(CommonVariables.filetypeUT)) {
                                        newJsonArray = Common.makeJsonArrayUT(fi.getName());
                                    } else if (type.equals(CommonVariables.filetypeScreen)) {
                                        newJsonArray = Common.makeJsonArrayScreen(fi.getName());
                                    } else if (type.equals(CommonVariables.filetypeAnswers)) {
                                        newJsonArray = Common.readAnswersFromFile(fi.getName());
                                    }

                                    // for (int i = 0; i < newJsonArray.length(); i++) {
                                    if (type.equals(CommonVariables.filetypeCx))
                                        AllFileJsonArray.put(newJsonObject);
                                    else
                                        AllFileJsonArray.put(newJsonArray);
                                    // }
                                } else {
                                    output.put(fi.getName(), "CurrentFile");
                                }
                            }
                            // after creating the json array try to connect to the specified URL and start
                            // the upload process
                            postUrlConnection.connect();
                            if (AllFileJsonArray != null && AllFileJsonArray.length() != 0) {
                                OutputStream os = null;
                                os = postUrlConnection.getOutputStream();
                                OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                                if (!type.equals(CommonVariables.filetypeCx)) {
                                    osw.write(newJsonArray.toString());
                                } else {
                                    osw.write(newJsonObject.toString());
                                }
                                osw.flush();
                                osw.close();
                                // buffer the response that you get from the server
                                // according to the response of the server we report the status
                                // of the upload service
                                BufferedReader br = new BufferedReader(new InputStreamReader(
                                        (postUrlConnection.getInputStream())));
                                while ((tempOutput = br.readLine()) != null) {
                                    sb.append(tempOutput);
                                }
                                if (sb.length() != 0) {
                                    if (sb.toString().equals("")) {
                                        output.put(type, "Unconfirmed");
                                        //fi.renameTo(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + type + "/unconfirmed" + System.currentTimeMillis()));
                                    } else {
                                        JSONObject json = new JSONObject(sb.toString());
                                        if (json.getString("status").equals("success")
                                                ) {
                                            output.put(type, "success");
                                            output.put("currentfile", currentFileName);
                                       /* boolean delcpc = fi.delete();
                                        if (!delcpc) {
                                            File delFR = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + type + "/delete" + System.currentTimeMillis());
                                            fi.renameTo(delFR);
                                        }*/
                                            /*if we get an update interval or update threshold flag in the reponse of the server
                                            * the service will trigger an async task to start updating the intervals / thresholds
                                            * */
                                            if (json.getString("update_interval").equals("1")) {
                                                CommonVariables.startUpdateIntervals = true;
                                            }
                                            if (json.getString("update_threshold").equals("1")) {
                                                CommonVariables.startUpdateThresholds = true;
                                            }
                                        } else if (json.getString("status").equals("Unauthorized")) {
                                            //  res[0] = "Unauthorized";
                                            output.put("Unauthorized", "Unauthorized");
                                            //  break;
                                        } else if (json.getString("status").equals("finished")) {
                                            output.put("finished", json.getString("error_message"));
                                            // break;
                                        } else if (json.getString("status").equals("fail")) {
                                            output.put("Error", json.getString("error"));
                                            //  break;
                                        }
                                    }

                                } else {
                                    output.put(type, "Unconfirmed");
                                }
                            } else {
                                output.put(type, "NoRecords");
                            }
                        }
                    } catch (java.net.SocketTimeoutException e) {
                        output.put("Error", e.getMessage());
                        return output;
                    } catch (MalformedURLException e) {
                        output.put("Error", e.getMessage());
                    } catch (JSONException e) {
                        output.put("Error", e.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (postUrlConnection != null)
                            try {
                                postUrlConnection.disconnect();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                } else {
                    output.put("Error", "NoFiles");
                }
            }
        } else {
            output.put("Error", "NoDirectory");
        }
        return output;
    }

    // secure upload file method used to upload a single file
    private String[] uploadDataSecure(String url, String type, String filename) throws IOException {
        /*added the delay to protect the server from being overloaded*/
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String output = "";
        StringBuilder sb = new StringBuilder();
        HttpURLConnection postUrlConnection = null;
        String[] res = new String[3];
        JSONArray newJsonArray = null;
        JSONObject newJsonObject = null;
        /*according to the type of file to upload create a json array which contains the logged stats*/
        if (type.equals(CommonVariables.filetypeCPC)) {
            newJsonArray = Common.makeJsonArraycpc(filename);
            //  Log.d("CPC uppload",newJsonArray.toString());
        } else if (type.equals(CommonVariables.filetypeTf)) {
            newJsonArray = Common.makeJsonArraytf(filename);
        } else if (type.equals(CommonVariables.filetypeCx)) {
            newJsonObject = Common.makeJsonArraycxn(filename);
        } else if (type.equals(CommonVariables.filetypeCxCount)) {
            newJsonArray = Common.makeJsonArrayCxCount(filename);
        } else if (type.equals(CommonVariables.filetypeOF)) {
            newJsonArray = Common.makeJsonArrayOF(filename);
        } else if (type.equals(CommonVariables.filetypeUT)) {
            newJsonArray = Common.makeJsonArrayUT(filename);
        } else if (type.equals(CommonVariables.filetypeScreen)) {
            newJsonArray = Common.makeJsonArrayScreen(filename);
        } else if (type.equals(CommonVariables.filetypePackage)) {
            newJsonArray = Common.makeJsonArrayPackages(filename);
        }
        try {
            /*set the variables of the connection (URL)*/
            postUrlConnection = Common.setUpHttpsConnection(url, this.getApplicationContext(), "POST");
            postUrlConnection.connect();
            // after connection start the upload process and buffer the reponse from the server
            if ((newJsonArray != null && newJsonArray.length() != 0) || (newJsonObject != null && newJsonObject.length() != 0)) {
                OutputStream os = postUrlConnection.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                // Log.d("GGGG",newJsonArray.toString());
                if (!type.equals(CommonVariables.filetypeCx)) {
                    osw.write(newJsonArray.toString());

                } else {
                    osw.write(newJsonObject.toString());
                }
                osw.flush();
                osw.close();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (postUrlConnection.getInputStream())));
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }
                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + type + "/");
                File myFile = new File(dir, filename);
                //  Log.d(TAG,"Server Response is "+sb.toString());
                //report the buffered reponse from the server to be included in the service reponse to the receiver
                if (sb.length() != 0) {
                    if (sb.toString().equals("")) {
                        myFile.renameTo(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + type + "/unconfirmed" + System.currentTimeMillis()));
                        res[0] = "Unconfirmed";
                    } else {

                        JSONObject json = new JSONObject(sb.toString());
                        if (json.getString("status").equals("success")
                                ) {
                            boolean del = myFile.delete();
                            if (!del) {
                                File delFR = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + type + "/delete" + System.currentTimeMillis());
                                myFile.renameTo(delFR);
                            }
                            res[0] = "success";
                            /*if we get an update interval or update threshold flag in the reponse of the server
                            * the service will trigger an async task to start updating the intervals / thresholds
                            * */
                            if (json.getString("update_interval").equals("1")) {
                                CommonVariables.startUpdateIntervals = true;
                            }
                            if (json.getString("update_threshold").equals("1")) {
                                CommonVariables.startUpdateThresholds = true;
                            }

                        } else if (json.getString("status").equals("finished")) {
                            //    return "finished";
                            res[0] = "finished";
                            res[1] = json.getString("errors");
                            res[2] = json.getString("error_message");

                        } else if (json.getString("status").equals("Unauthorized")) {
                            res[0] = "Unauthorized";
                            // the server is down
                        } else if (json.getString("status").equals("fail")) {
                            res[0] = "fail";
                            res[1] = json.getString("error");
                        }
                    }
                } else {
                    res[0] = "Unconfirmed";
                }

            }
        } catch (MalformedURLException e) {
            FirebaseCrash.report(new Exception(e.getMessage()));
        } catch (java.net.SocketTimeoutException e) {
            // FirebaseCrash.report(new Exception(e.getMessage()));
        } catch (JSONException e) {
            e.printStackTrace();
            // FirebaseCrash.report(new Exception(e.getMessage()));
        } finally {
            if (postUrlConnection != null)
                try {
                    postUrlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return res;
    }


}
