package com.farah.heavyservice;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Georgi on 10/12/2016.
 *
 * this async task is called when the compare threshold task find a significant probability value for the reported feature value
 * when compared against the downloaded thresholds
 */
public class SendAlarmTask extends AsyncTask<String, Void, Void> {

    private Context mContext;
    private boolean b = false;
    private String Appname = "";
    private String thresh_type = "";
    private double prt;
    private double val;

    public SendAlarmTask(Context context) {

        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        // from the input take the appname and threshold type plus the reported value and
        //ratio  then call a method to submit the event (alarm)
        Appname = params[0];
        thresh_type = params[1];
        prt = Double.valueOf(params[2]);
        val = Double.valueOf(params[3]);

        b = sendAlaram(params[0], params[1], mContext);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        // log what happened after the method finish the execute
        if (b) {
            Log.d(CommonVariables.TAG, "An Alarm for " + Appname + " for Feature " + thresh_type + " was submitted to the server");
        } else {
            Log.d(CommonVariables.TAG, "An Alarm for " + Appname + " for Feature " + thresh_type + " was NOT submitted to the server");
        }
    }

    private boolean sendAlaram(String App, String thresh_type, Context context) {
        //added a delay to prevent server carsh
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        String tempOutput = "";

        //create a secure connection using the specified URL
        HttpsURLConnection sendalarm_con = Common.setUpHttpsConnection(CommonVariables.SubmitAlarm, context, "POST");

        if (sendalarm_con != null) {
            try {

                OutputStream os = sendalarm_con.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");

                // include the app name and threshold type plus value and ratio and a timestamp
                // when submitting the event
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("app", App);
                jsonObject.put("thresh", thresh_type);
                jsonObject.put("value", val);
                jsonObject.put("percentage", prt);
                jsonObject.put("timestamp", System.currentTimeMillis());
                sendalarm_con.connect();

                osw.write(jsonObject.toString());
                osw.flush();
                osw.close();


                //get the response from the server and log the reason of error
                BufferedReader br = new BufferedReader(new InputStreamReader(sendalarm_con.getInputStream()));
                while ((tempOutput = br.readLine()) != null) {
                    sb.append(tempOutput);
                }
                if (sb.length() != 0 && !sb.toString().equals("")) {
                    Log.d(CommonVariables.TAG, "Server Response is " + sb.toString());
                    JSONObject jsonObject1 = new JSONObject(sb.toString());
                    if (jsonObject1.getString("status").equals("success")) {
                        return true;
                    /*Log.d(CommonVariables.TAG, "Server Response is " + sb.toString());*/
                    } else if (jsonObject1.getString("status").equals("error")) {
                        String error = jsonObject1.getString("error");
                        Log.d(CommonVariables.TAG, "Server Responded with error" + error);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (sendalarm_con != null) {
                    sendalarm_con.disconnect();
                }
            }
        }
        return false;
    }
}
