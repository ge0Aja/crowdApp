package com.farah.heavyservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

/**
 * Created by Georgi on 10/8/2016.
 *
 * this broadcast receiver is responsible for logging the screen activity when turning the screen on /off
 * which calls a method to write the screen activity to binary backup files
 *
 * it is also responsible to capture the battery warning broadcast to update the collect interval
 */
public class ScreenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String Action = intent.getAction();
        switch (Action){
            case "android.intent.action.SCREEN_OFF":
                try {
                    Common.writeScreenStatusToFile(CommonVariables.ScreenBkup, "0");
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }
                CommonVariables.screenOn = false;
                Log.d(CommonVariables.TAG, " Screen Logged  Off");
                break;
            case "android.intent.action.SCREEN_ON":
                try {
                    Common.writeScreenStatusToFile(CommonVariables.ScreenBkup, "1");
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }
                CommonVariables.screenOn = true;
                Log.d(CommonVariables.TAG, "Screen Logged  On");
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
