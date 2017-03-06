package com.farah.heavyservice;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Georgi on 9/17/2016.
 *
 * this async task is called after the user submits an answer for a received notification
 */
public class SubmitAnswerTask extends AsyncTask<String, Void, Void> {

    private Context mContext;
    private boolean b = false;
    private Activity activity;
    private JSONObject jsonObject = new JSONObject();
    private int notid;
    private int More;

    public SubmitAnswerTask(Activity calling, Context context,String Notid) {
        mContext = context;
        activity = calling;
        notid = Integer.valueOf(Notid);
    }

    @Override
    protected Void doInBackground(String... params) {
        // we've set the thread priority to highest to make sure that the response is submitted directly
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_MORE_FAVORABLE);// setThreadPriority (THREAD_PRIORITY_BACKGROUND + THREAD_PRIORITY_MORE_FAVORABLE);
        jsonObject = new JSONObject();
        try {
            // the response include a flag which indicates if the user clickd the more button
            // the answer
            // the question id which is received with the notification
            // a timestamp
            jsonObject.put("More", params[3]);
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
        // if the reponse is submitted with no problems OK
        // else save the response to a backup file to be uploaded as soon the connection is back
        if (b) {
            Toast.makeText(mContext, "Your Answer is Submitted", Toast.LENGTH_SHORT);
        } else {
            try {
                Common.writeAnswertoFile(jsonObject, CommonVariables.AnswersBkup, true);
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
            Toast.makeText(mContext, "Your Answer is Saved", Toast.LENGTH_SHORT);
        }
        // activity.finish();
    }

    // this method submits the answer of th user to a spicified notification
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
