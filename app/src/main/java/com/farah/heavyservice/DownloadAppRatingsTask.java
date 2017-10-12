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
import java.util.LinkedHashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Georgi on 3/21/2017.
 */
public class DownloadAppRatingsTask extends AsyncTask<String, Void, Void> {

    private Context mContext;
    private boolean b = false;
    private SharedPreferences editor;
    private String error_message = "";
    private OnAppRatingsDownloadCompleted listener;


    public DownloadAppRatingsTask(Context context,OnAppRatingsDownloadCompleted listener){
        mContext = context;
        this.listener =listener;
        editor =context.getSharedPreferences(context.getString(R.string.ratings_preference), Context.MODE_PRIVATE);
    }
    @Override
    protected Void doInBackground(String... params) {
        try {
            b = DownloadRatings(params[0], mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if(b){
            editor.edit().putString(mContext.getString(R.string.ratings_last_updated), String.valueOf(System.currentTimeMillis())).apply();
           // CommonVariables.ratingsAvailable = true;
        }else{
          //  CommonVariables.ratingsAvailable = false;
            editor.edit().putString(mContext.getString(R.string.ratings_update_message), error_message).apply();
        }
        try {
            listener.OnTaskCompleted(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPostExecute(aVoid);
    }

    private boolean DownloadRatings(String urlString, Context context){
        StringBuilder sb = new StringBuilder();
        String tempOutput = "";
        HttpsURLConnection rating_con = Common.setUpHttpsConnection(urlString, context, "GET");

        if(rating_con != null){
            try {
                rating_con.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(rating_con.getInputStream()));
                while ((tempOutput = br.readLine()) != null) {
                    sb.append(tempOutput);
                }
                br.close();
                LinkedHashMap<String,Float> app_ratings = new LinkedHashMap<>();

                if(sb.length() != 0){
                    JSONObject json = new JSONObject(sb.toString());
                    if (json.getString("status").equals("success")) {
                        JSONObject th = (JSONObject) json.get("ratings");
                        Iterator<?> keys = th.keys();
                        while (keys.hasNext()) {
                            String key = (String) keys.next();
                          //  if (th.get(key) instanceof JSONObject) {
                                app_ratings.put(key, Float.valueOf((String)th.get(key)));
                          ///  }
                        }
                        Common.writeAppRatingsToFile(app_ratings,CommonVariables.ratingsFile,false);
                        return true;
                    }else if (json.getString("status").equals("finished")){
                        error_message = json.getString("error_message");
                        Log.d(CommonVariables.TAG,"App ratings download finished with message "+error_message);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }finally {
                if(rating_con != null)
                    rating_con.disconnect();
            }
        }
        return false;
    }
}