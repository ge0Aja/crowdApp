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
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Georgi on 9/11/2016.
 */
public class DownloadThresholdsTask extends AsyncTask<String, Void, Void> {
    private Context mContext;
    private boolean b = false;
    private SharedPreferences editor;
    public DownloadThresholdsTask(Context context)
    {
        mContext = context;
        editor = context.getSharedPreferences(context.getString(R.string.trsh_preference),  Context.MODE_PRIVATE);
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            b = DownloadThresholds(params[0], mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if(b){
            CommonVariables.cxAgeThreshold = (editor.getString(mContext.getString(R.string.cxAge),"").equals(""))? Float.valueOf(0):Float.valueOf(editor.getString(mContext.getString(R.string.cxAge),""));
            CommonVariables.prCPUThreshold = (editor.getString(mContext.getString(R.string.prCPU),"").equals(""))? Float.valueOf(0):Float.valueOf(editor.getString(mContext.getString(R.string.prCPU),""));
            CommonVariables.prRSSThreshold = (editor.getString(mContext.getString(R.string.prRSS),"").equals(""))? Float.valueOf(0):Float.valueOf(editor.getString(mContext.getString(R.string.prRSS),""));
            CommonVariables.prVSSThreshold = (editor.getString(mContext.getString(R.string.prVSS),"").equals(""))? Float.valueOf(0):Float.valueOf(editor.getString(mContext.getString(R.string.prVSS),""));
            CommonVariables.txBytesThreshold = (editor.getString(mContext.getString(R.string.txBytes),"").equals(""))? Float.valueOf(0):Float.valueOf(editor.getString(mContext.getString(R.string.txBytes),""));
            CommonVariables.rxBytesThreshold = (editor.getString(mContext.getString(R.string.rxBytes),"").equals(""))? Float.valueOf(0):Float.valueOf(editor.getString(mContext.getString(R.string.rxBytes),""));
            CommonVariables.txPacketsThreshold = (editor.getString(mContext.getString(R.string.txPackets),"").equals(""))? Float.valueOf(0):Float.valueOf(editor.getString(mContext.getString(R.string.txPackets),""));
            CommonVariables.rxPacketsThreshold = (editor.getString(mContext.getString(R.string.rxPackets),"").equals(""))? Float.valueOf(0):Float.valueOf(editor.getString(mContext.getString(R.string.rxPackets),""));
            editor.edit().putString(mContext.getString(R.string.trsh_preference),"updated"+System.currentTimeMillis()).apply();
            Log.d(CommonVariables.TAG,"Thresholds are updated");
        }else{
            Log.d(CommonVariables.TAG,"Thresholds are Not Downloaded");
        }
    }

    private boolean DownloadThresholds(String urlString, Context context) {
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
                JSONObject json = new JSONObject(sb.toString());
                if (json.getString("status").equals("success")) {
                    JSONObject th = (JSONObject) json.get("thresholds");
                    Iterator<?> keys = th.keys();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        if (th.get(key) instanceof JSONObject) {
                            editor.edit().putString(key, (String) th.get(key)).apply();
                        }
                    }

                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }finally {
                if(thre_con !=null){
                    thre_con.disconnect();
                }
            }
        }
        return false;
    }
}

