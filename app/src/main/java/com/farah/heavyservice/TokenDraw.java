package com.farah.heavyservice;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class TokenDraw extends AppCompatActivity {

    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_draw);

      //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        try{
            Intent intent = getIntent();

            TextView not_tok = (TextView) findViewById(R.id.tokenTxtDraw);

            token = intent.getStringExtra("token");
            not_tok.setText(token);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void copyTok(View view){
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("tokCrowdApp", token);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this,"Copied to clipboard",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this,"Couldn't copy to clipboard",Toast.LENGTH_LONG).show();
        }
    }
}
