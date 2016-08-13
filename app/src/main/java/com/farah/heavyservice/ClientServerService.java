package com.farah.heavyservice;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * Created by Georgi on 8/13/2016.
 */
public class ClientServerService extends IntentService {
   // public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;
    String rtrnValGet = "";
    String rtrnValPost = "";
    Boolean postFinished = false;
    Boolean getFinished = false;
    String url = "";

    public ClientServerService() {
        super("ClientServerService");
    }

    private void HandleAsyncTaskReturnGet(String s){
        rtrnValGet = s;
        getFinished = true;
    }

    private void HandleAsyncTaskReturnPost(String s){
        rtrnValPost = s;
        postFinished = true;
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        /*
        * we haveto put the extras in the intent when calling the method
        * */
        final Bundle bundle = intent.getExtras();
        url = bundle.getString("url");
        final String method = bundle.getString("Method");
        final HashMap<String, HashMap<String, Long>> Hash_map = (HashMap<String, HashMap<String, Long>>) bundle.getSerializable("HashMap");;
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        final Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Bundle toSend = new Bundle();
                if (method == "POST" && Hash_map != null) {
                    try {
                        new CreateHttpPostConn().execute(Hash_map);
                        if(postFinished) {
                            try {
                                toSend.putString("Method", "POST");
                                toSend.putString("POSTResult", rtrnValPost);
                                receiver.send(STATUS_FINISHED, toSend);
                                postFinished =false;
                                stopSelf();
                            } catch (Exception e) {
                               // e.printStackTrace();
                                toSend.putString(Intent.EXTRA_TEXT,e.toString());
                                receiver.send(STATUS_ERROR, toSend);
                            }
                        }
                    } catch (Exception e) {
                        toSend.putString(Intent.EXTRA_TEXT,e.toString());
                        receiver.send(STATUS_ERROR, toSend);
                    }
                } else {
                    try {
                        new CreateHttpGetConn().execute();
                        if(getFinished){
                            try {
                                toSend.putString("Method", "GET");
                                toSend.putString("GETResult",rtrnValGet);
                                receiver.send(STATUS_FINISHED, toSend);
                                getFinished=false;
                                stopSelf();
                            } catch (Exception e) {
                                toSend.putString(Intent.EXTRA_TEXT,e.toString());
                                receiver.send(STATUS_ERROR, toSend);
                            }
                        }
                    } catch (Exception e) {
                       // e.printStackTrace();
                        toSend.putString(Intent.EXTRA_TEXT,e.toString());
                        receiver.send(STATUS_ERROR, toSend);
                    }
                }

                // put a broadcast rtrn value method call here !!
            }
        }, 0, 5000); // The Interval for sending the stats
    }

    class CreateHttpGetConn extends AsyncTask<Void,Integer,String> {
        //private ProgressBar progressBar;
        private JSONParser parser = new JSONParser();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Getting Data from server..", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String rtrnval ="";
            try {
                rtrnval =  parser.makeHTTPGetRequest(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return rtrnval;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
         //   progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
          //  return s;
           // super.onPostExecute(s);
            HandleAsyncTaskReturnGet(s);
        }
    }

    class CreateHttpPostConn extends AsyncTask<HashMap<String,HashMap<String,Long>>,Integer,String> {
       // private ProgressBar progressBar;
        private JSONParser parser = new JSONParser();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Posting Data from server..", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(HashMap<String,HashMap<String,Long>>... params) {
            String rtrnVal ="";
            try {
                rtrnVal =   parser.makeHTTPPostRequest(url,params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return rtrnVal;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
          //  progressBar.setProgress(values[0]);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
           // return s;
            //super.onPostExecute(s);
            HandleAsyncTaskReturnPost(s);
        }
    }

}
