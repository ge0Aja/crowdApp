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
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Georgi on 9/11/2016.
 */
public class DownloadThresholdsTask extends AsyncTask<String, Void, Void> {
    private Context mContext;
    private boolean b = false;
    private SharedPreferences editor;

    public DownloadThresholdsTask(Context context) {
        mContext = context;
        editor = context.getSharedPreferences(context.getString(R.string.trsh_preference), Context.MODE_PRIVATE);
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            if (CommonVariables.isWiFi)
                b = DownloadThresholds(params[0], mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (b) {
            editor.edit().putString(mContext.getString(R.string.trsh_preference), String.valueOf(System.currentTimeMillis())).apply();
            CommonVariables.thresholdsMap = Common.readThreshListFromFile(CommonVariables.thresholdsFile);
            CommonVariables.thresholdsAvailable = true;
            Log.d(CommonVariables.TAG, "Thresholds are updated");
        } else {
            CommonVariables.thresholdsAvailable = false;
            Log.d(CommonVariables.TAG, "Thresholds are Not Downloaded");
        }
        CommonVariables.RequestedThresholds = false;
    }

    private boolean DownloadThresholds(String urlString, Context context) {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        String tempOutput = "";
        // SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.trsh_preference), 0).edit();
        HttpsURLConnection thre_con = Common.setUpHttpsConnection(urlString, context, "GET");

        if (thre_con != null) {
            try {
                thre_con.connect();
                BufferedReader br = new BufferedReader(new InputStreamReader(thre_con.getInputStream()));
                while ((tempOutput = br.readLine()) != null) {
                    sb.append(tempOutput);
                }
                br.close();
                // Log.d("Thresholds","Server Response is "+sb.toString());
                HashMap<String, HashMap<String, HashMap<String, Float>>> app_thresholds = new HashMap<>();
                if (sb.length() != 0) {
                    JSONObject json = new JSONObject(sb.toString());
                    if (json.getString("status").equals("success")) {
                        JSONObject th = (JSONObject) json.get("thresholds");
                        Iterator<?> keys = th.keys();
                        while (keys.hasNext()) {
                            String key = (String) keys.next();
                            if (th.get(key) instanceof JSONObject) {
                                HashMap<String, HashMap<String, Float>> ind_thresholds = new HashMap<>();
                                Iterator<?> inner_keys = ((JSONObject) th.get(key)).keys();
                                while (inner_keys.hasNext()) {
                                    String inner_key = (String) inner_keys.next();
                                    JSONObject th2 = (JSONObject) ((JSONObject) th.get(key)).get(inner_key);
                                    HashMap<String, Float> ind_thresholdsinner = new HashMap<>();
                                    ind_thresholdsinner.put("mean", Float.valueOf((String) th2.get("mean")));
                                    ind_thresholdsinner.put("std", Float.valueOf((String) th2.get("std")));
                                    ind_thresholds.put(inner_key, ind_thresholdsinner);
                                }
                                app_thresholds.put(key, ind_thresholds);
                            }
                        }
                        Common.writeThreshListToFile(app_thresholds, CommonVariables.thresholdsFile, false);
                        return true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (thre_con != null) {
                    thre_con.disconnect();
                }
            }
        }
        return false;
    }
}

