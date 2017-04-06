package com.farah.heavyservice;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Georgi on 8/19/2016.
 * the Alert activity is triggered when a notifiation is received form the server and the user clicks
 * on that notification.
 * the activity will show the title and message of the notification
 * with a radio button box that allows the user to answer the message sent with the notification
 */

public class AlertActivity extends AppCompatActivity {

    private String type;
    private String Qid;
    private String question;
    private String message;
    private String notId;
    private int More = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        // make sure that the screen layout is portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        try {
            // get the notification information from the intent sent when the notification is received
            // message and question
            Intent intent = getIntent();
            TextView not_message = (TextView) findViewById(R.id.txt_notification);
            TextView not_quest = (TextView) findViewById(R.id.txt_question);

            message = intent.getStringExtra("message");
            not_message.setText(message);

            question = intent.getStringExtra("question");
            not_quest.setText(question);

            // the type of the notification is sent from the server along with the question ID
            // for tracking the answers
            type = intent.getStringExtra("type");
            Qid = intent.getStringExtra("Qid");
            notId = intent.getStringExtra("notid");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // this method shows the text that appears when the user clicks on I don't understand button
    // the messages are saved in the App assets locally
    public void understandClick(View view){
        String dialogMessage = "";
        More = 1;
        if (type != null) {
            switch (type) {
                case "1":
                    dialogMessage = getString(R.string.bandwidthMessagein);
                    break;
                case "2":
                    dialogMessage = getString(R.string.bandwidthMessageout);
                    break;
                case "3":
                    dialogMessage = getString(R.string.cpuMessage);
                    break;
                case "4":
                    dialogMessage = getString(R.string.ramMessage);
                    break;
                case "5":
                    dialogMessage = getString(R.string.connectionAgeMessage);
                    break;
                case "6":
                    dialogMessage = getString(R.string.connectionCountMessage);
                    break;
                default:
                    dialogMessage = "Message";
                    break;
            }
        }
        try {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(dialogMessage)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //when the user clicks on submit we make sure that an answer is selected then we start an async task
    // to submit the answer
    public void submitAnswer(View view){
        try {
            RadioGroup group = (RadioGroup) findViewById(R.id.radioGroup);
            RadioGroup group2 = (RadioGroup) findViewById(R.id.radioGroup2);

            int btnID = group.getCheckedRadioButtonId();
            int btnID2 = group2.getCheckedRadioButtonId();

            if (btnID != -1 && btnID2 != -1) {
                View radioButton = group.findViewById(btnID);
                View radioButton2 = group2.findViewById(btnID2);

                int idx = group.indexOfChild(radioButton);
                int idx2 = group2.indexOfChild(radioButton2);


                String AnswerIdx = String.valueOf(idx+1);
                String AnswerIdx2 = String.valueOf(idx2 + 1);

                Log.d(CommonVariables.TAG, "Answer: " + AnswerIdx + " Answer2: " + AnswerIdx2);

                new SubmitAnswerTask(this, this, notId).execute(CommonVariables.SubmitAnswerURL, Qid, AnswerIdx, String.valueOf(More), AnswerIdx2);
                this.finish();
            }else{
                Toast.makeText(this,"Please Choose an Answer",Toast.LENGTH_SHORT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onDestroy() {
        int idnot = Integer.valueOf(notId);
        super.onDestroy();
    }

}
