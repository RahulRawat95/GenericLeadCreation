package com.wings2aspirations.leadcreation;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;

import com.wings2aspirations.genericleadcreation.activity.ListLeadsActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<String> strings = new ArrayList<>();
        strings.add("Lalit");
        strings.add("Lokesh");
        Intent intent = com.wings2aspirations.genericleadcreation.activity.MainActivity.getListLeadsIntent(this, "http://13.126.198.143:10004/",
                "attendanceAppDB", "Mukul1062", BuildConfig.APPLICATION_ID, 1,"lokeshmudgal06@gmail.com", strings);
       /* Intent intent = com.wings2aspirations.genericleadcreation.activity.ViewLeadActivity.getListLeadsIntent(this, "http://13.126.198.143:10004/",
                "attendanceAppDB", "Mukul1062", BuildConfig.APPLICATION_ID, 2,"lokeshmudgal06@gmail.com", "lokesh");*/

        startActivity(intent);
        finish();
    }
}
