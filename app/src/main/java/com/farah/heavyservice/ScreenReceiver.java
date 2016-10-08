package com.farah.heavyservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

/**
 * Created by Georgi on 10/8/2016.
 */
public class ScreenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String Action = intent.getAction();
        switch (Action){
            case "android.intent.action.SCREEN_OFF":
                Common.writeScreenStatusToFile(CommonVariables.ScreenBkup, "0");
                CommonVariables.screenOn = false;
                Log.i(CommonVariables.TAG, " Screen Logged  Off");
                break;
            case "android.intent.action.SCREEN_ON":
                Common.writeScreenStatusToFile(CommonVariables.ScreenBkup, "1");
                CommonVariables.screenOn = true;
                Log.i(CommonVariables.TAG, "Screen Logged  On");
                break;
            case "android.intent.action.ACTION_BATTERY_LOW":
                CommonVariables.collectInterval *= 2;
                break;
            case "android.intent.action.ACTION_BATTERY_OKAY":
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;
                if (isCharging) {
                    CommonVariables.collectInterval = 10000;
                }
                break;
        }
    }
}
