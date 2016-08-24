package com.farah.heavyservice;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

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
    public static final int IOException = -15;

    private static final String TAG = "UploadService";

    public ClientServerService() {
        super("ClientServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Post Service Started!");
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        String uploadType = intent.getStringExtra("uploadtype");
        if (uploadType.equals(CommonVariables.UploadTypeFile)) {
            String url = intent.getStringExtra("url");
            String filename = intent.getStringExtra("filename");
            String type = intent.getStringExtra("type");
            Bundle bundle = new Bundle();
            if (!TextUtils.isEmpty(url)) {
            /* Update UI: Upload Service is Running */
                bundle.putString("type", type);
                receiver.send(STATUS_RUNNING, bundle);
                try {
                    String results = uploadData(url, type, filename);
                    Log.i(TAG, String.valueOf(results));
                /* Sending result back to Service */
                    if (results != null) {
                   /* if (results == HttpURLConnection.HTTP_OK || results == HttpURLConnection.HTTP_CREATED
                            || results == HttpURLConnection.HTTP_ACCEPTED) {*/
                        bundle.putString("result", results);
                        bundle.putString("filename", filename);
                        bundle.putString("type", type);
                        receiver.send(STATUS_FINISHED, bundle);
                    } else {
                        bundle.putInt("result", Integer.valueOf(results));
                        receiver.send(STATUS_ERROR, bundle);
                    }
                    Log.d(TAG, "Upload Service executed successfully!");
                    //  }
                } catch (Exception e) {
                /* Sending error message back to activity */
                    e.printStackTrace();
                    bundle.putString("result", e.toString());
                    receiver.send(STATUS_ERROR, bundle);
                    Log.d(TAG, "Upload Service executed with errors!");
                }
            }
        } else {
            String type = intent.getStringExtra("type");
            Bundle bundle = new Bundle();
            try {
                ArrayList<String> results = uploadDataDir(type);
                bundle.putString("type", type);
                receiver.send(STATUS_RUNNING, bundle);
                if (results.contains("Can't delete")) {
                    //TODO there are files that can't be deleted
                }
                else if(results.contains("No Files")){
                    bundle.putInt("Status",STATUS_FINISHED_NOFILES);
                    receiver.send(STATUS_FINISHED,bundle);
                }
                else if (results.contains("Error")) {
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
            } catch (java.io.IOException e) {
                e.printStackTrace();
                bundle.putString("result", e.toString());
                receiver.send(STATUS_ERROR, bundle);
                Log.d(TAG, "Upload Service executed with errors!");
            }
        }
        Log.d(TAG, "Post Service Stopping!");
        this.stopSelf();

    }

    private ArrayList<String> uploadDataDir(String type) throws IOException {
        String tempOutput = "";
        StringBuilder sb = new StringBuilder();
        HttpURLConnection postUrlConnection = null;
        JSONArray newJsonArray = null;
        String Dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrowdApp" + type + "/";
        File f = new File(Dir);
        File files[] = f.listFiles();
        ArrayList<String> output = new ArrayList<>();
        if(files.length != 0) {
            if (type.equals(R.string.filetypeCPC)) {
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
                        } else {
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
                                output.add(sb.toString());
                            } else {
                                output.add("Error");
                            }
                        }
                    }
                } catch (java.net.SocketTimeoutException e) {
                    //TODO the server is not responding
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO files can't be read
                    e.printStackTrace();
                    // return IOException;
                } finally {
                    if (postUrlConnection != null)
                        postUrlConnection.disconnect();
                }

            } else if (type.equals(R.string.filetypeCx)) {

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
                        } else {
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
                                output.add(sb.toString());
                            } else {
                                output.add("Error");
                            }
                        }
                    }
                } catch (java.net.SocketTimeoutException e) {
                    //TODO the server is not responding
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO files can't be read
                    e.printStackTrace();
                    // return IOException;
                } finally {
                    if (postUrlConnection != null)
                        postUrlConnection.disconnect();

                }
            } else {
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
                        } else {
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
                                output.add(sb.toString());
                            } else {
                                output.add("Error");
                            }
                        }
                    }
                } catch (java.net.SocketTimeoutException e) {
                    //TODO the server is not responding
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO files can't be read
                    e.printStackTrace();
                    // return IOException;
                } finally {
                    if (postUrlConnection != null)
                        postUrlConnection.disconnect();

                }
            }
        }
        else{
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
            if (type.equals(R.string.filetypeCPC)) {
                newJsonArray = Common.makeJsonArraycpc(filename);
            } else if (type.equals(R.string.filetypeTf)) {
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
