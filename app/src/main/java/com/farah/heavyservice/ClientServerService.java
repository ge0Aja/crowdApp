package com.farah.heavyservice;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Georgi on 8/13/2016.
 */
public class ClientServerService extends IntentService {
    // public static final int STATUS_RUNNING = 0;
    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;

    public static final int STATUS_FINISHED_SUCCESS = 1;
    public static final int STATUS_FINISHED_ERROR = 2;
    public static final int STATUS_FINISHED_NOFILES = 3;
    public static final int STATUS_FINISHED_NOWIFI = 4;
    public static final int STATUS_FINISHED_SERVER_UNAVAILABLE = 5;
    public static final int STATUS_FINISHED_MALFORMED_HTTP = 6;
    public static final int STATUS_FINISHED_NO_RESPONSE_FROM_SERVER = 7;
    //  public static final int IOException = -15;

    public static final String TAG = "UploadService";

    public ClientServerService() {
        super("ClientServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Upload Service Started!");
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        String uploadType = intent.getStringExtra("uploadtype");
        Bundle bundle = new Bundle();
        if (uploadType.equals(CommonVariables.UploadTypeFile) && CommonVariables.isWiFi && CommonVariables.startUpload) {
            Log.i(TAG,"The upload type is file and should start uploading");
            String url = intent.getStringExtra("url");
            String filename = intent.getStringExtra("filename");
            String type = intent.getStringExtra("type");
            if (!TextUtils.isEmpty(url)) {
                bundle.putString("type", type);
                receiver.send(STATUS_RUNNING, bundle);
                try {
                    String results = uploadData(url, type, filename);
                    Log.i(TAG, String.valueOf(results));
                /* Sending result back to Service */
                    if (!results.equals("")) {
                        bundle.putString("filename", filename);
                        bundle.putString("type", type);
                        if (results.equals(HttpURLConnection.HTTP_ACCEPTED) ||
                                results.equals(HttpURLConnection.HTTP_CREATED) ||
                                results.equals(HttpURLConnection.HTTP_OK)
                                ) {
                            bundle.putInt("Status", STATUS_FINISHED_SUCCESS);
                            bundle.putString("result", results);
                        } else {
                            bundle.putInt("Status", STATUS_FINISHED_ERROR);
                            bundle.putString("result", results);
                        }
                        receiver.send(STATUS_FINISHED, bundle);
                    } else {
                        bundle.putInt("Status", STATUS_FINISHED_NO_RESPONSE_FROM_SERVER);
                        receiver.send(STATUS_FINISHED, bundle);
                    }
                    Log.d(TAG, "Upload Service executed successfully!");
                    this.stopSelf();
                    //  }
                } catch (Exception e) {
                /* Sending error message back to activity */
                    e.printStackTrace();
                    bundle.putString("result", e.toString());
                    receiver.send(STATUS_ERROR, bundle);
                    Log.d(TAG, "Upload Service executed with errors!");
                    this.stopSelf();
                }
            }
        } else if (uploadType.equals(CommonVariables.UploadTypeDir) && CommonVariables.isWiFi && CommonVariables.startUploadDir) {

            String type = intent.getStringExtra("type");
            if (!type.equals(CommonVariables.filetypeAll)) {
                Log.i(TAG,"The upload type is one Dir and should start uploading");
                try {
                    ArrayList<String> results = uploadDataDir(type);
                    bundle.putString("type", type);
                    receiver.send(STATUS_RUNNING, bundle);
                    if (results.size() == 1 && results.contains(String.valueOf(HttpURLConnection.HTTP_UNAVAILABLE))) {
                        bundle.putInt("Status", STATUS_FINISHED_SERVER_UNAVAILABLE);
                        receiver.send(STATUS_FINISHED, bundle);
                    } else if (results.contains("Can't delete")) {
                        //TODO there are files that can't be deleted
                    } else if (results.size() == 1 && results.contains("No Files")) {
                        bundle.putInt("Status", STATUS_FINISHED_NOFILES);
                        receiver.send(STATUS_FINISHED, bundle);
                    } else if (results.contains("Error")) {
                        //TODO there are files aren't uploaded
                        int count = Collections.frequency(results, "Error");
                        bundle.putInt("Status", STATUS_FINISHED_ERROR);
                        bundle.putInt("Error", count);
                        receiver.send(STATUS_FINISHED, bundle);
                    } else {
                        bundle.putInt("Status", STATUS_FINISHED_SUCCESS);
                        receiver.send(STATUS_FINISHED, bundle);
                        //TODO All files were uploaded sucessfully
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
                Log.i(TAG,"The upload type is multiple Dir and should start uploading");
                String[] fileTypes = {CommonVariables.filetypeCPC, CommonVariables.filetypeCx, CommonVariables.filetypeTf};
                for (String t : fileTypes
                        ) {
                    try {
                        ArrayList<String> results = uploadDataDir(String.valueOf(t));
                        bundle.putString("type", String.valueOf(t));
                        receiver.send(STATUS_RUNNING, bundle);
                        if (results.size() == 1 && results.contains(String.valueOf(HttpURLConnection.HTTP_UNAVAILABLE))) {
                            bundle.putInt("Status", STATUS_FINISHED_SERVER_UNAVAILABLE);
                            receiver.send(STATUS_FINISHED, bundle);
                        } else if (results.size() == 1 && results.contains(String.valueOf(HttpURLConnection.HTTP_NOT_FOUND))) {
                            bundle.putInt("Status", STATUS_FINISHED_MALFORMED_HTTP);
                            receiver.send(STATUS_FINISHED, bundle);
                        } else if (results.contains("Can't delete")) {
                            //TODO there are files that can't be deleted
                        } else if (results.size() == 1 && results.contains("No Files")) {
                            bundle.putInt("Status", STATUS_FINISHED_NOFILES);
                            receiver.send(STATUS_FINISHED, bundle);
                        } else if (results.contains("Error")) {
                            //TODO there are files aren't uploaded
                            int count = Collections.frequency(results, "Error");
                            bundle.putInt("Status", STATUS_FINISHED_ERROR);
                            bundle.putInt("Error", count);
                            receiver.send(STATUS_FINISHED, bundle);
                        } else {
                            bundle.putInt("Status", STATUS_FINISHED_SUCCESS);
                            receiver.send(STATUS_FINISHED, bundle);
                            //TODO All files were uploaded sucessfully
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
        } else {
            bundle.putInt("Status", STATUS_FINISHED_NOWIFI);
            receiver.send(STATUS_FINISHED, bundle);
        }
        Log.d(TAG, "Post Service Stopping!");
        this.stopSelf();
    }

    private ArrayList<String> uploadDataDir(String type) throws IOException {
        String tempOutput = "";
        StringBuilder sb = new StringBuilder();
        HttpURLConnection postUrlConnection = null;
        JSONArray newJsonArray = null;
        String Dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp/" + type + "/";
        File f = new File(Dir);
        File files[] = f.listFiles();
        ArrayList<String> output = new ArrayList<>();
        if (files.length != 0) {
            if (type.equals(CommonVariables.filetypeCPC)) {
                try {
                    URL useURL = new URL(CommonVariables.CPCUploadURL);
                    postUrlConnection = (HttpURLConnection) useURL.openConnection();
                    postUrlConnection.setReadTimeout(10000);
                    postUrlConnection.setConnectTimeout(10000);
                    postUrlConnection.setRequestMethod("POST");
                    postUrlConnection.setDoInput(true);
                    postUrlConnection.setDoOutput(true);
                    postUrlConnection.setUseCaches(false);
                    postUrlConnection.setRequestProperty("Content-Type", "application/json");
                    postUrlConnection.setRequestProperty("Host", CommonVariables.UploadHost);
                    postUrlConnection.connect();
                    for (File fi : files
                            ) {
                        if (fi.getName().startsWith("delete")) {
                            if (fi.delete())
                                output.add("delete");
                            else {
                                output.add("Can't delete");
                            }
                        } else if (!fi.getName().equals(CommonVariables.CPCBkup)) {
                            tempOutput = "";
                            sb.setLength(0);
                            newJsonArray = Common.makeJsonArraycpc(fi.getName());
                            OutputStream os = postUrlConnection.getOutputStream();
                            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                            osw.write(newJsonArray.toString());
                            osw.flush();
                            osw.close();
                            BufferedReader br = new BufferedReader(new InputStreamReader(
                                    (postUrlConnection.getInputStream())));
                            while ((tempOutput = br.readLine()) != null) {
                                sb.append(tempOutput);
                            }
                            if (sb.toString().equals(HttpURLConnection.HTTP_ACCEPTED) ||
                                    sb.toString().equals(HttpURLConnection.HTTP_CREATED) ||
                                    sb.toString().equals(HttpURLConnection.HTTP_OK)
                                    ) {
                                if(!sb.toString().equals(""))
                                    output.add(sb.toString());
                                else{
                                    //TODO rename the file to unconfirmed
                                }
                            } else {
                                output.add("Error");
                            }
                        } else {
                            output.add("Current File");
                        }
                    }
                } catch (java.net.SocketTimeoutException e) {
                    //TODO the server is not responding
                    output.add(String.valueOf(HttpURLConnection.HTTP_UNAVAILABLE));
                    return output;
                    // e.printStackTrace();
                } catch (MalformedURLException e) {
                    output.add(String.valueOf(HttpURLConnection.HTTP_NOT_FOUND));
                } catch (IOException e) {
                    // TODO files can't be read
                    e.printStackTrace();
                    // return IOException;
                } finally {
                    if (postUrlConnection != null)
                        postUrlConnection.disconnect();
                }

            } else if (type.equals(CommonVariables.filetypeCx)) {
                try {
                    URL useURL = new URL(CommonVariables.CxUploadURL);
                    postUrlConnection = (HttpURLConnection) useURL.openConnection();
                    postUrlConnection.setReadTimeout(10000);
                    postUrlConnection.setConnectTimeout(10000);
                    postUrlConnection.setRequestMethod("POST");
                    postUrlConnection.setDoInput(true);
                    postUrlConnection.setDoOutput(true);
                    postUrlConnection.setUseCaches(false);
                    postUrlConnection.setRequestProperty("Content-Type", "application/json");
                    postUrlConnection.setRequestProperty("Host", CommonVariables.UploadHost);
                    postUrlConnection.connect();
                    for (File fi : files
                            ) {
                        if (fi.getName().startsWith("delete")) {
                            if (fi.delete())
                                output.add("delete");
                            else {
                                output.add("Can't delete");
                            }
                        } else if (!fi.getName().equals(CommonVariables.CxBkup)) {
                            tempOutput = "";
                            sb.setLength(0);
                            newJsonArray = Common.makeJsonArraycxn(fi.getName());
                            OutputStream os = postUrlConnection.getOutputStream();
                            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                            osw.write(newJsonArray.toString());
                            osw.flush();
                            osw.close();
                            BufferedReader br = new BufferedReader(new InputStreamReader(
                                    (postUrlConnection.getInputStream())));
                            while ((tempOutput = br.readLine()) != null) {
                                sb.append(tempOutput);
                            }
                            if (sb.toString().equals(HttpURLConnection.HTTP_ACCEPTED) ||
                                    sb.toString().equals(HttpURLConnection.HTTP_CREATED) ||
                                    sb.toString().equals(HttpURLConnection.HTTP_OK)
                                    ) {
                                if(!sb.toString().equals(""))
                                    output.add(sb.toString());
                                else{
                                    //TODO there are unconfirmed files
                                }
                            } else {
                                output.add("Error");
                            }
                        } else {
                            output.add("Current File");
                        }
                    }
                } catch (java.net.SocketTimeoutException e) {
                    //TODO the server is not responding
                    output.add(String.valueOf(HttpURLConnection.HTTP_UNAVAILABLE));
                    return output;
                    // e.printStackTrace();
                } catch (MalformedURLException e) {
                    output.add(String.valueOf(HttpURLConnection.HTTP_NOT_FOUND));
                } catch (IOException e) {
                    // TODO files can't be read
                    e.printStackTrace();
                    // return IOException;
                } finally {
                    if (postUrlConnection != null)
                        postUrlConnection.disconnect();

                }
            } else if (type.equals(CommonVariables.filetypeTf)) {
                try {
                    URL useURL = new URL(CommonVariables.TFUploadURL);
                    postUrlConnection = (HttpURLConnection) useURL.openConnection();
                    postUrlConnection.setReadTimeout(10000);
                    postUrlConnection.setConnectTimeout(10000);
                    postUrlConnection.setRequestMethod("POST");
                    postUrlConnection.setDoInput(true);
                    postUrlConnection.setDoOutput(true);
                    postUrlConnection.setUseCaches(false);
                    postUrlConnection.setRequestProperty("Content-Type", "application/json");
                    postUrlConnection.setRequestProperty("Host", CommonVariables.UploadHost);
                    postUrlConnection.connect();
                    for (File fi : files
                            ) {
                        if (fi.getName().startsWith("delete")) {
                            if (fi.delete())
                                output.add("delete");
                            else {
                                output.add("Can't delete");
                            }
                        } else if (!fi.getName().equals(CommonVariables.TFBkup)) {
                            tempOutput = "";
                            sb.setLength(0);
                            newJsonArray = Common.makeJsonArraytf(fi.getName());
                            OutputStream os = postUrlConnection.getOutputStream();
                            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                            osw.write(newJsonArray.toString());
                            osw.flush();
                            osw.close();
                            BufferedReader br = new BufferedReader(new InputStreamReader(
                                    (postUrlConnection.getInputStream())));
                            while ((tempOutput = br.readLine()) != null) {
                                sb.append(tempOutput);
                            }
                            if (sb.toString().equals(HttpURLConnection.HTTP_ACCEPTED) ||
                                    sb.toString().equals(HttpURLConnection.HTTP_CREATED) ||
                                    sb.toString().equals(HttpURLConnection.HTTP_OK)
                                    ) {
                                if(!sb.toString().equals(""))
                                    output.add(sb.toString());
                                else{
                                    //TODO there are unconfirmed files
                                }
                            } else {
                                output.add("Error");
                            }
                        } else {
                            output.add("Current File");
                        }
                    }
                } catch (java.net.SocketTimeoutException e) {
                    //TODO the server is not responding
                    output.add(String.valueOf(HttpURLConnection.HTTP_UNAVAILABLE));
                    return output;
                    //  e.printStackTrace();
                } catch (MalformedURLException e) {
                    output.add(String.valueOf(HttpURLConnection.HTTP_NOT_FOUND));
                } catch (IOException e) {
                    // TODO files can't be read
                    e.printStackTrace();
                    // return IOException;
                } finally {
                    if (postUrlConnection != null)
                        postUrlConnection.disconnect();

                }
            }
        } else {
            output.add("No Files");
        }
        return output;
    }


    private String uploadData(String url, String type, String filename) throws IOException {
        String output = "";
        StringBuilder sb = new StringBuilder();
        HttpURLConnection postUrlConnection = null;
        try {
            URL useURL = new URL(url);
            postUrlConnection = (HttpURLConnection) useURL.openConnection();
//    TODO we have to replace the hard coded intervals and IP
            postUrlConnection.setReadTimeout(10000);
            postUrlConnection.setConnectTimeout(10000);
            postUrlConnection.setRequestMethod("POST");
            postUrlConnection.setDoInput(true);
            postUrlConnection.setDoOutput(true);
            postUrlConnection.setUseCaches(false);
            postUrlConnection.setRequestProperty("Content-Type", "application/json");
            postUrlConnection.setRequestProperty("Host", "192.168.137.234");
            postUrlConnection.connect();

            // create JSON OBJECT
            JSONArray newJsonArray = null;
            if (type.equals(CommonVariables.filetypeCPC)) {
                newJsonArray = Common.makeJsonArraycpc(filename);
            } else if (type.equals(CommonVariables.filetypeTf)) {
                newJsonArray = Common.makeJsonArraytf(filename);
            } else {
                newJsonArray = Common.makeJsonArraycxn(filename);
            }
            //   Log.i(TAG, newJsonArray.toString());
            OutputStream os = postUrlConnection.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(newJsonArray.toString());
            osw.flush();
            osw.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (postUrlConnection.getInputStream())));
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }

        } catch (MalformedURLException e) {
            return String.valueOf(HttpURLConnection.HTTP_NOT_FOUND);
        } catch (java.net.SocketTimeoutException e) {
            return String.valueOf(HttpURLConnection.HTTP_UNAVAILABLE);
        } catch (IOException e) {
            e.printStackTrace();
            // return IOException;
        } finally {
            if (postUrlConnection != null)
                postUrlConnection.disconnect();
        }
        return sb.toString();
    }
}
