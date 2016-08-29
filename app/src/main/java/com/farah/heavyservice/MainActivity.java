package com.farah.heavyservice;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startCollecting(View view) {
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
    }

    public void stopCollecting(View view) {
        Intent intent = new Intent(this, MyService.class);
        stopService(intent);
    }

    public void getarray(View view) {
        EditText txtType = (EditText) findViewById(R.id.editText);

      //  Log.i("TFARRAY", Common.makeJsonArraytf(MyService.TFBkup).toString());
      //  Log.i("CPCARRAY", Common.makeJsonArraycpc(MyService.CPCBkup).toString());
      //  Log.i("CxARRAY", Common.makeJsonArraycxn(MyService.CxBkup).toString());

        String type = txtType.getText().toString();
        switch (type) {
            case "CPC":
                CommonVariables.setUploadSettings(CommonVariables.CPCBkup, true, CommonVariables.filetypeCPC);
                break;
            case "TF":
                CommonVariables.setUploadSettings(CommonVariables.TFBkup, true, CommonVariables.filetypeTf);
                break;
            case "Cx":
                CommonVariables.setUploadSettings(CommonVariables.CxBkup, true, CommonVariables.filetypeCx);
        }
    }
}