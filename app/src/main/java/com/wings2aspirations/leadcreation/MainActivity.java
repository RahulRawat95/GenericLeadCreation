package com.wings2aspirations.leadcreation;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
AlertDialog optionAlertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ArrayList<String> strings = new ArrayList<>();
        strings.add("Lalit");
        strings.add("Lokesh");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lead User");
        optionAlertDialog = builder.create();

        optionAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "By Admin", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = com.wings2aspirations.genericleadcreation.activity.MainActivity.getListLeadsIntent(MainActivity.this, "http://13.126.198.143:10004/",
                        "attendanceAppDB", "Loadw3118", BuildConfig.APPLICATION_ID, 1, "lokeshmudgal06@gmail.com", strings);

       /* Intent intent = com.wings2aspirations.genericleadcreation.activity.ViewLeadActivity.getListLeadsIntent(this, "http://13.126.198.143:10004/",
                "attendanceAppDB", "Mukul1062", BuildConfig.APPLICATION_ID, 2,"lokeshmudgal06@gmail.com", "lokesh");*/

                startActivity(intent);
                finish();
            }
        });
        optionAlertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "By Employee", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = com.wings2aspirations.genericleadcreation.activity.MainActivity.getListLeadsIntent(MainActivity.this, "http://13.126.198.143:10004/",
                        "attendanceAppDB", "Loadw3118", BuildConfig.APPLICATION_ID, 4, "lokeshmudgal06@gmail.com", "Lokesh");
       /* Intent intent = com.wings2aspirations.genericleadcreation.activity.ViewLeadActivity.getListLeadsIntent(this, "http://13.126.198.143:10004/",
                "attendanceAppDB", "Mukul1062", BuildConfig.APPLICATION_ID, 2,"lokeshmudgal06@gmail.com", "lokesh");*/

                startActivity(intent);
                finish();
            }
        });

        optionAlertDialog.show();

    }
}
