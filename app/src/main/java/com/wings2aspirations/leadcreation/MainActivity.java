package com.wings2aspirations.leadcreation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.wings2aspirations.genericleadcreation.activity.ListLeadsActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<String> strings = new ArrayList<>();
        strings.add("rahul");
        strings.add("rawat");
        Intent intent = ListLeadsActivity.getListLeadsIntent(this, "http://192.168.1.14:8080/", "w2aDealerDb", "dbo", BuildConfig.APPLICATION_ID, 1, strings);
        startActivity(intent);
    }
}
