package com.farah.heavyservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/*
* this activity is used to transfer the notification information to the AlertActivity
* it is used to start the alertactivity only with no further use
* */
public class TransferActivity extends AppCompatActivity {
    private String question;
    private String message;
    private String type;
    private String Qid;
    private String notid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // put the information that are transfered in the intent by the messaging service e
        // in a new intent and start the new activity
        // then close this activity
        Intent intent = getIntent();
        try {
            question = intent.getStringExtra("question");
            message = intent.getStringExtra("message");
            type = intent.getStringExtra("type");
            Qid = intent.getStringExtra("Qid");
            notid = intent.getStringExtra("notid");

            Intent newIntent = new Intent(this, AlertActivity.class);
            newIntent.putExtra("question", question);
            newIntent.putExtra("message", message);
            newIntent.putExtra("type", type);
            newIntent.putExtra("Qid", Qid);
            newIntent.putExtra("notid", notid);
            startActivity(newIntent);
            finish();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
