package com.farah.heavyservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Georgi on 9/3/2016.
 * the class is part of the notification receiving process it is called to obtain and initialize
 * a firebase token from the firebase server
 * and saves the obtained token in the application preferences
 */
public class FcmInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        String recentToken = FirebaseInstanceId.getInstance().getToken();
       // Log.d("Token", "onTokenRefresh: "+recentToken);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.fcm_preference), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.fcm_token),recentToken);
        editor.commit();
    }
}
