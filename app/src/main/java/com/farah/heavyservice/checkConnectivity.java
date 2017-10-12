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
 * <p>
 * we migrated to check the connection status through an Async task because it involves network
 * operations
 * <p>
 * this task tries to connect to a specific IP witha specified timeout interval
 * and if the device is connected through wifi and can reach the IP then the App has a connection
 * and can communicate with the server
 * we added this part after mark requested that the app should operate within the company only
 * <p>
 * the app will not log any events and will not receive any notifications if it is not connected
 */
public class checkConnectivity extends AsyncTask<Void, Void, Void> {
    Context mContext;
    private boolean isConnected;
    // private boolean isReachable;

    public checkConnectivity(Context context) {
        mContext = context;
    }

    private static boolean hasConenction(boolean isWifi) {
        boolean tt = false;
        try {
            HttpURLConnection urlc = (HttpURLConnection) (new URL("http://" + CommonVariables.UploadHost).openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(10000);
            urlc.connect();
            tt = (urlc.getResponseCode() == 200);
        } catch (IOException e) {
            Log.d(CommonVariables.TAG, "Error checking internet connection", e);
            CommonVariables.setServerReachable(false);
            return false;
        }

        if (isWifi && tt) {
            CommonVariables.setServerReachable(true);
            return true;
        } else if (tt) {
            CommonVariables.setServerReachable(true);
            return false;
        } else {
            Log.d(CommonVariables.TAG, "No network available!");
            CommonVariables.setServerReachable(false);
            return false;
        }
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
