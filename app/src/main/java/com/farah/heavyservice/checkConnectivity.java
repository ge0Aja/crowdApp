package com.farah.heavyservice;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Georgi on 10/20/2016.
 */
public class checkConnectivity extends AsyncTask<Void, Void, Void> {
    Context mContext;
    private boolean isConnected;

    public checkConnectivity(Context context) {
        mContext = context;
    }

    private static boolean hasConenction(boolean isWifi) {
        if (isWifi) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(10000);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                Log.e(CommonVariables.TAG, "Error checking internet connection", e);
            }
        } else {
            Log.d(CommonVariables.TAG, "No network available!");
        }
        return false;
    }

    private static boolean isConnectedToWifi(Context context) {
        boolean isConnected = false;
        boolean isWiFi = false;
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        }
        return hasConenction(isWiFi);
    }

    @Override
    protected Void doInBackground(Void... params) {
        isConnected = isConnectedToWifi(mContext);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (isConnected)
            CommonVariables.setWiFi(true);
        else
            CommonVariables.setWiFi(false);
    }

}
