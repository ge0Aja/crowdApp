package com.farah.heavyservice;

import android.util.Log;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Created by Georgi on 9/6/2016.
 */
public class NullHostNameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {
        Log.i(CommonVariables.TAG, "Approving certificate for " + hostname);
        return true;
    }
}
