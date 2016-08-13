package com.farah.heavyservice;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.Toast;

/**
 * Created by Georgi on 8/13/2016.
 */
@SuppressLint("ParcelCreator")
public class HttpResultsReceiver extends ResultReceiver {
    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
    public HttpResultsReceiver(Handler handler) {
        super(handler);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case ClientServerService.STATUS_FINISHED:
                String type = (String) resultData.get("Method");
                String results = "";
                if(type == "POST"){
                    results = resultData.getString("POSTResult");
                    returnResults("POST",results);
                }
                else{
                    results = resultData.getString("GETResult");
                    returnResults("GET",results);
                }
                break;
            case ClientServerService.STATUS_ERROR:
                String error = resultData.getString(Intent.EXTRA_TEXT);
                returnResults("Error",error);
                break;
        }
    }

    public String returnResults(String type, String results){
        return (type+"\n"+results);
    }
}
