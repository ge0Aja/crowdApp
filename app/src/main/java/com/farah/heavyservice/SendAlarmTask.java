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
 */
public class SendAlarmTask extends AsyncTask<String, Void, Void> {

    private Context mContext;
    private boolean b = false;
    private String Appname = "";
    private String thresh_type = "";

    public SendAlarmTask(Context context) {

        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        Appname = params[0];
        thresh_type = params[1];

        b = sendAlaram(params[0], params[1], mContext);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (b) {
            Log.d(CommonVariables.TAG, "An Alarm for " + Appname + " for Feature " + thresh_type + " was submitted to the server");
        } else {
            Log.d(CommonVariables.TAG, "An Alarm for " + Appname + " for Feature " + thresh_type + " was NOT submitted to the server");
        }
    }

    private boolean sendAlaram(String App, String thresh_type, Context context) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        String tempOutput = "";

        HttpsURLConnection sendalarm_con = Common.setUpHttpsConnection(CommonVariables.SubmitAlarm, context, "POST");

        if (sendalarm_con != null) {
            try {

                OutputStream os = sendalarm_con.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("app", App);
                jsonObject.put("thresh", thresh_type);
                jsonObject.put("timestamp", System.currentTimeMillis());

                sendalarm_con.connect();

                osw.write(jsonObject.toString());
                osw.flush();
                osw.close();

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
