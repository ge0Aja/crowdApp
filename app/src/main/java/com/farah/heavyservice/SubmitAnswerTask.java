package com.farah.heavyservice;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Georgi on 9/17/2016.
 */
public class SubmitAnswerTask extends AsyncTask<String, Void, Void> {

    private Context mContext;
    private boolean b = false;
    private Activity activity;
    private int notid;

    public SubmitAnswerTask(Activity calling, Context context,String Notid) {
        mContext = context;
        activity = calling;
        notid = Integer.valueOf(Notid);
    }

    @Override
    protected Void doInBackground(String... params) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Ansid", params[2]);
            jsonObject.put("Qid", params[1]);
            jsonObject.put("Timestamp",String.valueOf(System.currentTimeMillis()));
            b = SubmitAnswer(params[0], jsonObject, mContext);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (b) {
            Toast.makeText(mContext, "Your Answer is Submitted", Toast.LENGTH_SHORT);
            activity.finish();
        } else {
            Toast.makeText(mContext, "Your Answer is not Submitted, are connected ?", Toast.LENGTH_SHORT);
        }
    }

    private boolean SubmitAnswer(String urlString, JSONObject jsonObject, Context context) {

        StringBuilder sb = new StringBuilder();
        String tempOutput = "";

        HttpsURLConnection Ans_con = Common.setUpHttpsConnection(urlString, context, "POST");

        if (Ans_con != null) {
            try {
                Ans_con.connect();
                OutputStream os = Ans_con.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                osw.write(jsonObject.toString());
                osw.flush();
                osw.close();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (Ans_con.getInputStream())));
                while ((tempOutput = br.readLine()) != null) {
                    sb.append(tempOutput);
                }
                if (!sb.toString().equals("")) {
                    Log.d(CommonVariables.TAG,"Submit Answer Server Response is "+sb.toString());
                    JSONObject json = new JSONObject(sb.toString());
                    if (json.getString("status").equals("success")) {
                        return true;
                    } else {
                        Log.d(CommonVariables.TAG,"Submit Answer "+json.getString("error"));
                        return false;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }finally {
                if(Ans_con !=null){
                    Ans_con.disconnect();
                }

            }
        }
        return false;
    }
}
