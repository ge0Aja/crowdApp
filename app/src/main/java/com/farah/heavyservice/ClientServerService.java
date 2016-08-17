package com.farah.heavyservice;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

    private static final String TAG = "UploadService";

    public ClientServerService(){
        super("ClientServerService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "Service Started!");

        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        String url = intent.getStringExtra("url");
        HashMap<String,String> params = (HashMap<String,String>) intent.getSerializableExtra("map");
        Bundle bundle = new Bundle();

        if (!TextUtils.isEmpty(url)) {
            /* Update UI: Download Service is Running */
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
            try {
                String results = uploadData(url,params);
                /* Sending result back to activity */
                if (null != results) {
                    bundle.putString("result", results);
                    receiver.send(STATUS_FINISHED, bundle);
                }
            } catch (Exception e) {
                /* Sending error message back to activity */
                bundle.putString(Intent.EXTRA_TEXT, e.toString());
                receiver.send(STATUS_ERROR, bundle);
            }
        }
        Log.d(TAG, "Service Stopping!");
        this.stopSelf();

    }


    private String uploadData(String url, HashMap<String,String> params) throws IOException {
        String output = "";
        // DataOutputStream out =null;
        StringBuilder sb = new StringBuilder();
        HttpURLConnection postUrlConnection = null;
        try {
            URL useURL = new URL(url);
            //  if (method == "POST"){
            postUrlConnection = (HttpURLConnection) useURL.openConnection();
            postUrlConnection.setReadTimeout(1000);
            postUrlConnection.setConnectTimeout(1000);
            postUrlConnection.setRequestMethod("POST");
            postUrlConnection.setDoInput(true);
            postUrlConnection.setDoOutput(true);
            postUrlConnection.setUseCaches(false);
            postUrlConnection.setRequestProperty("Content-Type", "application/json");
            postUrlConnection.setRequestProperty("Host", "192.168.0.1");
            postUrlConnection.connect();

            // create JSON OBJECT
            JSONObject newJsonObj = makeJsonObject(params);
            Log.i(TAG, newJsonObj.toString());
            OutputStream os = postUrlConnection.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os,"UTF-8");
            osw.write(newJsonObj.toString());
            osw.flush();
            osw.close();
            // Log.i("Response from server", String.valueOf(postUrlConnection.getResponseCode()));

            if (postUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + postUrlConnection.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (postUrlConnection.getInputStream())));
            //String output;
            sb.append("Output from Server .... \n");
            sb.append(postUrlConnection.getResponseCode()+"\n");
            while ((output = br.readLine()) != null) {
                sb.append(output);
                //System.out.println(output);
            }
            //  Log.i("Return Result",sb.toString());
            // postUrlConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }  catch (java.net.SocketTimeoutException e) {
            return "Server Time Out!";
        }catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if(postUrlConnection != null)
                postUrlConnection.disconnect();
        }
        return sb.toString();
    }

    private JSONObject makeJsonObject(HashMap<String,String> params) throws JSONException {
        JSONObject json = new JSONObject();
        for (Map.Entry<String,String> param: params.entrySet()
                ) {
            json.put(param.getKey(),param.getValue());
        }
        return json;
    }

}
