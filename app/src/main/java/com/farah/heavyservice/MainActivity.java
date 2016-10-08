package com.farah.heavyservice;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;


public class MainActivity extends AppCompatActivity {

    //Button tokenBtn = null;
    //String appServerUrl = "http://192.168.137.76/fcm/fcm_insert.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (!Common.hasPermissions(this, CommonVariables.Permissions)) {
            ActivityCompat.requestPermissions(this, CommonVariables.Permissions, CommonVariables.PERMISSION_ALL);
        } else {
            CommonVariables.startService = true;
        }

        if (CommonVariables.startService) {
            Intent intent = new Intent(this, MyService.class);
            startService(intent);
        } else {
            Toast.makeText(this, "The Service Cannot Start Due to Missing Permissions", Toast.LENGTH_LONG);
            FirebaseCrash.report(new Exception("The Service Cannot Start Due to Missing Permissions"));
        }
    }

    public void startCollecting(View view) {

        if (!Common.hasPermissions(this, CommonVariables.Permissions)) {
            ActivityCompat.requestPermissions(this, CommonVariables.Permissions, CommonVariables.PERMISSION_ALL);
        } else {
            CommonVariables.startService = true;
        }
        if (CommonVariables.startService) {
            Intent intent = new Intent(this, MyService.class);
            startService(intent);
        }
    }

    public void stopCollecting(View view) {
        Intent intent = new Intent(this, MyService.class);
        stopService(intent);
    }

    public void screenstats (View view){
        CommonVariables.setUploadSettings(CommonVariables.ScreenBkup, true, CommonVariables.filetypeScreen);
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
                        Intent intent = new Intent(this, MyService.class);
                        startService(intent);
                    }
                }
                return;
            }

        }
    }
}