package com.wings2aspirations.genericleadcreation.activity;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.adapter.Adapter;
import com.wings2aspirations.genericleadcreation.adapter.TrialAdapter;
import com.wings2aspirations.genericleadcreation.models.LeadDetail;
import com.wings2aspirations.genericleadcreation.network.ApiClient;
import com.wings2aspirations.genericleadcreation.network.ApiInterface;
import com.wings2aspirations.genericleadcreation.repository.Utility;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrailActivity extends AppCompatActivity {

    int CHILD_FOLLOW_UP_ID;
    int ID;
    private ApiInterface apiInterface;
    private RelativeLayout progress_bar;
    private RecyclerView trail_list;
    private TrialAdapter adapter;
    private FloatingActionButton fab_trial;
    private String empName;
    private int empId;

    private List<LeadDetail> trailDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trail);

        progress_bar = findViewById(R.id.progress_bar);
        trail_list = findViewById(R.id.trail_list);
        fab_trial = findViewById(R.id.fab_trial);

        Bundle bundle = getIntent().getExtras();


        CHILD_FOLLOW_UP_ID = bundle.getInt("CHILD_FOLLOW_UP_ID");
        ID = bundle.getInt("ID");
        empName = bundle.getString("emp_name");
        empId = bundle.getInt("emp_id");

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        fab_trial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = AddUpdateLeadActivity.getLeadIntent(TrailActivity.this, empName, empId, CHILD_FOLLOW_UP_ID, true);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        getTrailList();
    }

    private void getTrailList() {

        showProgressTrialBar();
        Call<JsonArray> call = apiInterface.getTrialLeadById(CHILD_FOLLOW_UP_ID);

        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {

                hideProgressTrialBar();
                if (!response.isSuccessful()) {
                    if (response.code() == 401) {
                        getTrailList();
                    }
                    return;
                }
                JsonArray jsonArray = response.body();
                Type type = new TypeToken<List<LeadDetail>>() {
                }.getType();
                trailDetails = new Gson().fromJson(jsonArray, type);

                if (trailDetails.size() > 0) {
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TrailActivity.this);

                    adapter = new TrialAdapter(TrailActivity.this, trailDetails);
                    trail_list.setLayoutManager(linearLayoutManager);
                    trail_list.setAdapter(adapter);


                    if (trailDetails.get(trailDetails.size() - 1).getCALL_TYPE().equalsIgnoreCase("Closed")) {
                        fab_trial.setVisibility(View.GONE);
                        Utility.globalMessageDialog(TrailActivity.this, "This Lead is closed");
                    }

                } else
                    fab_trial.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {

            }
        });

    }

    private void showProgressTrialBar() {
        progress_bar.setVisibility(View.VISIBLE);
    }

    private void hideProgressTrialBar() {
        progress_bar.setVisibility(View.GONE);
    }
}
