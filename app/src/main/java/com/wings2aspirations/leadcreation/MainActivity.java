package com.wings2aspirations.leadcreation;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    AlertDialog optionAlertDialog;

    @Override
    protected void onResume() {
        super.onResume();
        optionAlertDialog.show();
    }

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
                        "attendanceAppDB", "Lalit3157", BuildConfig.APPLICATION_ID, 1, "lalitkurra@gmail.com", strings, "Company Name");
                startActivity(intent);
                //finish();
            }
        });
        optionAlertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "By Employee", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = com.wings2aspirations.genericleadcreation.activity.MainActivity.getListLeadsIntent(MainActivity.this, "http://13.126.198.143:10004/",
                        "attendanceAppDB", "Lalit3157", BuildConfig.APPLICATION_ID, 2, "laltan309@gmail.com", "Employee 1", "Lalit");
                startActivity(intent);
                //finish();
            }
        });


    }
}
