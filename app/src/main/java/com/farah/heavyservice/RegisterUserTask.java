package com.farah.heavyservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.Certificate;

/**
 * Created by Georgi on 9/10/2016.
 */
public class RegisterUserTask extends AsyncTask<String, Void, Void> {
    private Context mContext;
    private boolean b = false;
    private SharedPreferences sharedPreferencesApp;
    private SharedPreferences sharedPreferencesFCM;

    public RegisterUserTask(Context context) {
        mContext = context;
        sharedPreferencesApp = mContext.getSharedPreferences(mContext.getString(R.string.app_preference), Context.MODE_PRIVATE);
        sharedPreferencesFCM = mContext.getSharedPreferences(context.getString(R.string.fcm_preference), Context.MODE_PRIVATE);
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            b = registerUser(params[0], mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (b) {
         //   sharedPreferencesApp = mContext.getSharedPreferences(mContext.getString(R.string.app_preference), Context.MODE_PRIVATE);
            CommonVariables.username = sharedPreferencesApp.getString(mContext.getString(R.string.user_name), "");
            CommonVariables.userRegistered = true;
            Log.d("RegisterUser", "User Registered, the new username is :" + CommonVariables.username);
        }
    }

    private boolean registerUser(String urlString, Context context) {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        String tempOutput = "";
        HttpsURLConnection reg_con = Common.setUpHttpsConnection(urlString, context, "POST");
      //  sharedPreferences = context.getSharedPreferences(context.getString(R.string.fcm_preference), Context.MODE_PRIVATE);
        final String token = sharedPreferencesFCM.getString(context.getString(R.string.fcm_token), "");
        final String os_version = String.valueOf(Build.VERSION.SDK_INT);

        JSONArray user_apps = new JSONArray();

        for (String appname: CommonVariables.installed3rdPartyApps
             ) {
            try {
                JSONObject app = new JSONObject();
                app.put("name",appname);
                app.put("timestamp",Common.getPackageInstallTime(mContext,appname));
                user_apps.put(app);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONObject jsonObject = new JSONObject();
        try {
            if (reg_con != null) {
                reg_con.connect();
                jsonObject.put("fcm_token", token);
                jsonObject.put("os_version", os_version);
                jsonObject.put("timestamp",(System.currentTimeMillis()/1000));
                jsonObject.put("apps",user_apps);
                OutputStream os = reg_con.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                osw.write(jsonObject.toString());
                osw.flush();
                osw.close();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (reg_con.getInputStream())));
                while ((tempOutput = br.readLine()) != null) {
                    sb.append(tempOutput);
                }

           //     Log.d("User","Server Response is "+sb.toString());
                if (!sb.toString().equals("")) {
                       //Log.d("RegisterUser","Server Response is "+sb.toString());
                    JSONObject json = new JSONObject(sb.toString());
                    if (json.getString("status").equals("success") || json.getString("status").equals("finished")) {
                      //  sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_preference), Context.MODE_PRIVATE);
                        sharedPreferencesApp.edit().putString(context.getString(R.string.user_name), json.getString("username")).apply();
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } catch (MalformedURLException e) {
            // output.put("Error", String.valueOf(HttpURLConnection.HTTP_NOT_FOUND));
            e.printStackTrace();
        } catch (java.net.SocketTimeoutException e) {
            Log.d(CommonVariables.TAG, "User Register TimeOut");
            FirebaseCrash.report(new Exception("Register User"+e.getMessage()));
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reg_con != null) {
                reg_con.disconnect();
            }
        }

        return false;
    }
}
