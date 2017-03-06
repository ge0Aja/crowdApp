package com.farah.heavyservice;

import android.util.Log;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Created by Georgi on 9/6/2016.
 * this is used to accept the self signed certificates used in the experiment
 * this shouldn't take place if the certificate is signed by a well known authority
 */
public class NullHostNameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {
        Log.i(CommonVariables.TAG, "Approving certificate for " + hostname);
        return true;
    }
}
