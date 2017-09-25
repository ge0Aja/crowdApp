package com.farah.heavyservice;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.Toast;

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

        //  SharedPreferences editor = getSharedPreferences(getString(R.string.know_q), Context.MODE_PRIVATE);
        final SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.know_q), Context.MODE_PRIVATE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View lay = inflater.inflate(R.layout.popupquestion, null, false);
        final PopupWindow pw = new PopupWindow(lay, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
        final RatingBar knowBar = (RatingBar) lay.findViewById(R.id.ratingBarKnowledge);
        LayerDrawable stars = (LayerDrawable) knowBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP);
        Button btn_sub_k  = (Button) lay.findViewById(R.id.btn_itknow);

        btn_sub_k.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                float rating = knowBar.getRating();

                if(rating == 0.0){
                    Toast.makeText(getApplicationContext(), "Please select rating", Toast.LENGTH_LONG).show();
                }else{
                    sharedPref.edit().putString(getApplicationContext().getString(R.string.know_q), String.valueOf(rating)).apply();
                    pw.dismiss();
                }
            }
        });


        boolean ck = Common.checkKnowledge(getApplicationContext(),sharedPref);
        if(ck) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    pw.showAtLocation(findViewById(R.id.mainLayout), Gravity.CENTER, 0, 0);
                }
            }, 1000);
        }
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

    public void showPopup(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        PopupWindow pw = new PopupWindow(inflater.inflate(R.layout.popupquestion, null, false), 500, 700, true);
        //pw.setFocusable(true);

        pw.showAtLocation(findViewById(R.id.mainLayout), Gravity.CENTER, 0, 0);

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