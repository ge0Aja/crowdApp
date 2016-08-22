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
                MyService.setUploadSettings(MyService.CPCBkup, true, MyService.filetypeCPC);
                break;
            case "TF":
                MyService.setUploadSettings(MyService.TFBkup, true, MyService.filetypeTf);
                break;
            case "Cx":
                MyService.setUploadSettings(MyService.CxBkup, true, MyService.filetypeCx);
        }
    }
}