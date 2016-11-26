package com.farah.heavyservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Georgi on 8/21/2016.
 */
public class BootReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String messageLogged = "";
        String packageName = "";
        String action = intent.getAction();


        switch (action) {
            case "android.intent.action.PACKAGE_INSTALL":
            case "android.intent.action.PACKAGE_ADDED":
                Uri uriadd = intent.getData();
                packageName = uriadd != null ? uriadd.getSchemeSpecificPart() : null;
                if (packageName != null)
                    Common.writePackageStatusToFile(CommonVariables.PackagesBkup, "ADD", packageName, String.valueOf(System.currentTimeMillis()), "", context);
                Common.get3rdPartyApps();
                Common.getInstalledPackages(context);
                Log.i(CommonVariables.TAG, "Package Logged  Added");
                break;
            case "android.intent.action.PACKAGE_REMOVED":
                Uri uri = intent.getData();
                packageName = uri != null ? uri.getSchemeSpecificPart() : null;
                String dateAdded = Common.getPackageInstallTime(context, packageName);
                Log.d(CommonVariables.TAG, "Package Date add for " + packageName + " is " + dateAdded);
                if (packageName.equals("com.farah.heavyservice"))
                    break;
                Common.writePackageStatusToFile(CommonVariables.PackagesBkup, "REMOVE", packageName, String.valueOf(System.currentTimeMillis()), dateAdded, context);
                Log.i(CommonVariables.TAG, "Package Logged  Removed");
                Common.get3rdPartyApps();
                Common.getInstalledPackages(context);
                break;

        }
    }
}
