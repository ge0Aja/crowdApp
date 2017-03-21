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
 *
 * we migrated to check the connection status through an Async task because it involves network
 * operations
 *
 * this task tries to connect to a specific IP witha specified timeout interval
 * and if the device is connected through wifi and can reach the IP then the App has a connection
 * and can communicate with the server
 * we added this part after mark requested that the app should operate within the company only
 *
 * the app will not log any events and will not receive any notifications if it is not connected
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
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://192.168.137.79").openConnection());
                //HttpURLConnection urlc = (HttpURLConnection) (new URL("http://72.14.183.152").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(10000);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                Log.d(CommonVariables.TAG, "Error checking internet connection", e);
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
