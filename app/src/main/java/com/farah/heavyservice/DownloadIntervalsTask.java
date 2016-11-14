package com.farah.heavyservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Georgi on 9/27/2016.
 */
public class DownloadIntervalsTask extends AsyncTask<String, Void, Void> {
    private Context mContext;
    private boolean b = false;
    private SharedPreferences editor;

    public DownloadIntervalsTask(Context context) {
        mContext = context;
        editor = context.getSharedPreferences(context.getString(R.string.interval_preference), Context.MODE_PRIVATE);
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            b = DownloadIntervals(params[0], mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (b) {
            Log.d(CommonVariables.TAG, "Intervals are updated");
            editor.edit().putString(mContext.getString(R.string.interval_preference), String.valueOf(System.currentTimeMillis())).apply();
        } else {
            Log.d(CommonVariables.TAG, "Intervals are NOT updated and set to local values");
        }
        CommonVariables.collectInterval = Integer.valueOf((editor.getString(mContext.getString(R.string.collectInterval), "").equals("")) ? Integer.valueOf(10000) : Integer.valueOf(editor.getString(mContext.getString(R.string.collectInterval), "")));
        CommonVariables.uploadIntervalNormal = Integer.valueOf((editor.getString(mContext.getString(R.string.uploadIntervalNormal), "").equals("")) ? Integer.valueOf(120000) : Integer.valueOf(editor.getString(mContext.getString(R.string.uploadIntervalNormal), "")));
        CommonVariables.uploadIntervalRetry = Integer.valueOf((editor.getString(mContext.getString(R.string.uploadIntervalRetry), "").equals("")) ? Integer.valueOf(7200000) : Integer.valueOf(editor.getString(mContext.getString(R.string.uploadIntervalRetry), "")));
        CommonVariables.maxFileSize = Integer.valueOf((editor.getString(mContext.getString(R.string.maxFileSize), "").equals("")) ? Integer.valueOf(1024) : Integer.valueOf(editor.getString(mContext.getString(R.string.maxFileSize), "")));
        CommonVariables.maxFileSizeScreen = Integer.valueOf((editor.getString(mContext.getString(R.string.maxFileSizeScreen), "").equals("")) ? Integer.valueOf(256) : Integer.valueOf(editor.getString(mContext.getString(R.string.maxFileSizeScreen), "")));
        CommonVariables.maxFileSizeOFUT = Integer.valueOf((editor.getString(mContext.getString(R.string.maxFileSizeOFUT), "").equals("")) ? Integer.valueOf(512) : Integer.valueOf(editor.getString(mContext.getString(R.string.maxFileSizeOFUT), "")));
        CommonVariables.checkCPCThresholdInterval = Integer.valueOf((editor.getString(mContext.getString(R.string.checkCPCThresholdInterval), "").equals("")) ? Integer.valueOf(180000) : Integer.valueOf(editor.getString(mContext.getString(R.string.checkCPCThresholdInterval), "")));
        CommonVariables.checkCxnThresholdInterval = Integer.valueOf((editor.getString(mContext.getString(R.string.checkCxnThresholdInterval), "").equals("")) ? Integer.valueOf(240000) : Integer.valueOf(editor.getString(mContext.getString(R.string.checkCxnThresholdInterval), "")));
        CommonVariables.checkTfThresholdInterval = Integer.valueOf((editor.getString(mContext.getString(R.string.checkTfThresholdInterval), "").equals("")) ? Integer.valueOf(90000) : Integer.valueOf(editor.getString(mContext.getString(R.string.checkTfThresholdInterval), "")));
    }

    private boolean DownloadIntervals(String urlString, Context context) {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        String tempOutput = "";
        // SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.trsh_preference), 0).edit();
        HttpsURLConnection interv_con = Common.setUpHttpsConnection(urlString, context, "GET");
        HttpsURLConnection reinterv_con = Common.setUpHttpsConnection(CommonVariables.SubmitIntervalUpdate, context, "POST");
        if (interv_con != null) {
            try {
                interv_con.connect();
                BufferedReader br = new BufferedReader(new InputStreamReader(interv_con.getInputStream()));
                while ((tempOutput = br.readLine()) != null) {
                    sb.append(tempOutput);
                }
                br.close();
                // Log.d("Intervals","Server Response is "+sb.toString());
                if (sb.length() != 0) {
                    JSONObject json = new JSONObject(sb.toString());
                    JSONObject rejson = new JSONObject();

                    if (json.getString("status").equals("success")) {
                        JSONObject th = (JSONObject) json.get("intervals");
                        Iterator<?> keys = th.keys();
                        while (keys.hasNext()) {
                            String key = (String) keys.next();
                            if (th.get(key) instanceof JSONObject) {
                                editor.edit().putString(key, (String) th.get(key)).apply();
                            }
                        }
                        interv_con.disconnect();
                        if (reinterv_con != null) {
                            OutputStream os = reinterv_con.getOutputStream();
                            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                            rejson.put("status", "1");
                            rejson.put("timestamp", System.currentTimeMillis());
                            reinterv_con.connect();
                            osw.write(rejson.toString());
                            osw.flush();
                            osw.close();
                            sb.setLength(0);
                            BufferedReader br2 = new BufferedReader(new InputStreamReader(reinterv_con.getInputStream()));
                            while ((tempOutput = br2.readLine()) != null) {
                                sb.append(tempOutput);
                            }
                            if (sb.length() != 0 && !sb.toString().equals("")) {
                                Log.d(CommonVariables.TAG, "Interval Update Server Response is " + sb.toString());
                            }
                        }
                        return true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (interv_con != null) {
                    interv_con.disconnect();
                }
            }
        }

        return false;
    }
}
