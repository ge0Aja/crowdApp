package com.farah.heavyservice;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Georgi on 9/3/2016.
 *
 * this class is not used anymore
 */
public class MySingleton {
    private static MySingleton mInstance;
    private static Context mCtx;
    private RequestQueue rQueue;

    private MySingleton(Context context) {
        mCtx = context;
        rQueue = getRequestQueue();
    }

    public static synchronized MySingleton getmInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MySingleton(context);

        }
        return mInstance;
    }
    private RequestQueue getRequestQueue() {

        if (rQueue == null) {
            rQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return rQueue;
    }

    public void addToRequestQueue(Request request){

        getRequestQueue().add(request);
    }
}
