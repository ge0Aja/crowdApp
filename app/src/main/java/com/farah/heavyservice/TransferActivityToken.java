package com.farah.heavyservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/*
* this activity is used to transfer the notification information to the AlertActivity
* it is used to start the alertactivity only with no further use
* */
public class TransferActivityToken extends AppCompatActivity {
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // put the information that are transfered in the intent by the messaging service e
        // in a new intent and start the new activity
        // then close this activity
        Intent intent = getIntent();
        try {
            token = intent.getStringExtra("token");

            //TODO change activity
            Intent newIntent = new Intent(this, TokenDraw.class);
            newIntent.putExtra("token", token);

            startActivity(newIntent);
            finish();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
