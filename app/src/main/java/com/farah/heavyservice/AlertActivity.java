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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        try {
            Intent intent = getIntent();
            TextView not_message = (TextView) findViewById(R.id.txt_notification);
            TextView not_quest = (TextView) findViewById(R.id.txt_question);

            message = intent.getStringExtra("message");
            not_message.setText(message);

            question = intent.getStringExtra("question");
            not_quest.setText(question);

            type = intent.getStringExtra("type");
            Qid = intent.getStringExtra("Qid");
            notId = intent.getStringExtra("notid");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

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

    public void submitAnswer(View view){
        try {
            RadioGroup group = (RadioGroup) findViewById(R.id.radioGroup);
            int btnID = group.getCheckedRadioButtonId();
            if(btnID !=-1) {
                View radioButton = group.findViewById(btnID);
                int idx = group.indexOfChild(radioButton);
                String AnswerIdx = String.valueOf(idx+1);
                Log.d(CommonVariables.TAG,"Answer "+AnswerIdx);
                new SubmitAnswerTask(this, this, notId).execute(CommonVariables.SubmitAnswerURL, Qid, AnswerIdx, String.valueOf(More));
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
