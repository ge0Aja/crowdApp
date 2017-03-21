package com.farah.heavyservice;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/*
* this is the main activity which is basically an empty activity that has two buttons to start and stop the service
* */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // we make sure that the screen orientation is portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // is the OS is Android OS 6.0 and above we ask for the required permissions
        if (!Common.hasPermissions(this, CommonVariables.Permissions)) {
            ActivityCompat.requestPermissions(this, CommonVariables.Permissions, CommonVariables.PERMISSION_ALL);
        } else {
            CommonVariables.startService = true;
        }

        // if all is checked then we can start the service if it is not started
        if (CommonVariables.startService) {
            if (!Common.isMyServiceRunning(MyService.class, getApplicationContext())) {
                Intent intent = new Intent(this, MyService.class);
                startService(intent);
            }
        } /*else {
            Toast.makeText(this, "The Service Cannot Start Due to Missing Permissions", Toast.LENGTH_LONG);
            FirebaseCrash.report(new Exception("The Service Cannot Start Due to Missing Permissions"));
        }*/
    }

    // on button click check the required permissions before starting the service
    public void startCollecting(View view) {

        if (!Common.hasPermissions(this, CommonVariables.Permissions)) {
            ActivityCompat.requestPermissions(this, CommonVariables.Permissions, CommonVariables.PERMISSION_ALL);
        } else {
            CommonVariables.startService = true;
        }
        if (CommonVariables.startService) {
            if (!Common.isMyServiceRunning(MyService.class, getApplicationContext())) {
                Intent intent = new Intent(this, MyService.class);
                startService(intent);
            }
        }
    }

    // stop the service
    // however the service will not stop because it automatically restarts
    public void stopCollecting(View view) {
        Intent intent = new Intent(this, MyService.class);
        stopService(intent);
    }

    public void screenstats(View view) {
        CommonVariables.setUploadSettings(CommonVariables.ScreenBkup, true, CommonVariables.filetypeScreen);
    }

    public void cxcountstats(View view) {
        CommonVariables.setUploadSettings(CommonVariables.CxCountBkup, true, CommonVariables.filetypeCxCount);
    }

    public void cpcstats(View view) {
        CommonVariables.setUploadSettings(CommonVariables.CPCBkup, true, CommonVariables.filetypeCPC);
    }

    public void cxstats(View view) {
        CommonVariables.setUploadSettings(CommonVariables.CxBkup, true, CommonVariables.filetypeCx);
    }

    public void tfstats(View view) {
        CommonVariables.setUploadSettings(CommonVariables.TFBkup, true, CommonVariables.filetypeTf);
    }

    public void ofstats(View view) {
        CommonVariables.setUploadSettings(CommonVariables.OFBkup, true, CommonVariables.filetypeOF);
    }

    public void utstats(View view) {
        CommonVariables.setUploadSettings(CommonVariables.UTBkup, true, CommonVariables.filetypeUT);
    }

    public void userreg(View view) {
        if (!CommonVariables.userRegistered) {
            Common.regUser(this);
        }
    }

    public void getthresh(View view) {
        if (!CommonVariables.thresholdsAvailable) {
            Common.getThresholds(this);
        }
    }

    public void getIntervals(View view) {
        Common.getIntervals(this);
    }

    public void showRatings(View view) {
        Intent intent = new Intent(this, AppRatingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CommonVariables.PERMISSION_ALL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CommonVariables.startService = true;
                    if (CommonVariables.startService) {
                        if (!Common.isMyServiceRunning(MyService.class, getApplicationContext())) {
                            Intent intent = new Intent(this, MyService.class);
                            startService(intent);
                        }
                    }
                    return;
                } else {
                    finish();
                }
                
            }

        }
    }
}