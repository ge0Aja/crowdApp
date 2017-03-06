package com.farah.heavyservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

/**
 * Created by Georgi on 8/21/2016.
 *
 * in the Bootreceiver BroadcastReceiver we catch the following broadcasts:
 * Package Install / Remove
 * we started with this receiver as a boot receiver then we changed its usage
 */
public class BootReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String messageLogged = "";
        String packageName = "";
        String action = intent.getAction();


        switch (action) {
            // in case of package installation get the package info and write a log with the package info
            case "android.intent.action.PACKAGE_INSTALL":
            case "android.intent.action.PACKAGE_ADDED":
                Uri uriadd = intent.getData();
                packageName = uriadd != null ? uriadd.getSchemeSpecificPart() : null;
                if (packageName != null) {
                    try {
                        Common.writePackageStatusToFile(CommonVariables.PackagesBkup, "ADD", packageName, String.valueOf(System.currentTimeMillis()), "", context);
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    }
                }
                Common.get3rdPartyApps();
                Common.getInstalledPackages(context);
                Log.i(CommonVariables.TAG, "Package Logged  Added");
                break;
            // incase or package remove try to get the installation time of the package if available and
            // log the package information
            case "android.intent.action.PACKAGE_REMOVED":
                Uri uri = intent.getData();
                packageName = uri != null ? uri.getSchemeSpecificPart() : null;
                String dateAdded = Common.getPackageInstallTime(CommonVariables.mContext, packageName);
                Log.d(CommonVariables.TAG, "Package Date add for " + packageName + " is " + dateAdded);
                if (packageName.equals("com.farah.heavyservice"))
                    break;
                try {
                    Common.writePackageStatusToFile(CommonVariables.PackagesBkup, "REMOVE", packageName, String.valueOf(System.currentTimeMillis()), dateAdded, context);
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }
                Log.i(CommonVariables.TAG, "Package Logged  Removed");
                Common.get3rdPartyApps();
                Common.getInstalledPackages(context);
                break;

        }
    }
}
