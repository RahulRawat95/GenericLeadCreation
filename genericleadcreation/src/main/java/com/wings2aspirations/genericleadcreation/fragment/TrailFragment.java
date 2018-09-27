package com.wings2aspirations.genericleadcreation.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.activity.AddUpdateLeadActivity;
import com.wings2aspirations.genericleadcreation.activity.MainActivity;
import com.wings2aspirations.genericleadcreation.activity.ViewLeadActivity;
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

import static android.app.Activity.RESULT_OK;

public class TrailFragment extends Fragment {
    private static final int REQUEST_CODE_ADD_LEAD_ACTIVITY = 1;

    private static final String ARG_CHILD_FOLLOW_UP_ID = "CHILD_FOLLOW_UP_ID";
    private static final String ARG_ID = "ID";
    private static final String ARG_EMP_NAME = "emp_name";
    private static final String ARG_EMP_ID = "emp_id";
    private static final String ARG_CAN_ADD = "can_add";

    private int CHILD_FOLLOW_UP_ID;
    private int ID;
    private ApiInterface apiInterface;
    private RelativeLayout progress_bar;
    private RecyclerView trail_list;
    private TrialAdapter adapter;
    private FloatingActionButton fab_trial;
    private String empName;
    private int empId;

    private boolean showAddButton;

    private List<LeadDetail> trailDetails;

    public static TrailFragment newInstance(int childFollowUpId, int id, String empName, int empId, boolean canAdd) {

        Bundle args = new Bundle();
        args.putInt(ARG_CHILD_FOLLOW_UP_ID, childFollowUpId);
        args.putInt(ARG_ID, id);
        args.putString(ARG_EMP_NAME, empName);
        args.putInt(ARG_EMP_ID, empId);
        args.putBoolean(ARG_CAN_ADD, canAdd);

        TrailFragment fragment = new TrailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void getValues() {
        Bundle bundle = getArguments();

        CHILD_FOLLOW_UP_ID = bundle.getInt(ARG_CHILD_FOLLOW_UP_ID);
        ID = bundle.getInt(ARG_ID);
        empName = bundle.getString(ARG_EMP_NAME);
        empId = bundle.getInt(ARG_EMP_ID);

        showAddButton = bundle.getBoolean(ARG_CAN_ADD);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getValues();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_trail, container, false);

        progress_bar = view.findViewById(R.id.progress_bar);
        trail_list = view.findViewById(R.id.trail_list);
        fab_trial = view.findViewById(R.id.fab_trial);

        if (showAddButton){
            fab_trial.setVisibility(View.VISIBLE);
            fab_trial.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = AddUpdateLeadActivity.getLeadIntent(getActivity(), empName, empId, CHILD_FOLLOW_UP_ID, true);
                    startActivityForResult(intent, REQUEST_CODE_ADD_LEAD_ACTIVITY);
                }
            });
        }else
            fab_trial.setVisibility(View.GONE);

        if (showAddButton){
            try {
                ((MainActivity) getActivity()).setActionBarTitle("Lead Trail List");
            } catch (Exception e) {

            }
        }else {
            try {
                ((ViewLeadActivity) getActivity()).setActionBarTitle("Lead Trail List");
            } catch (Exception e) {

            }
        }

        return view;
    }

    @Override
    public void onResume() {
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
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

                    adapter = new TrialAdapter(getActivity(), trailDetails);
                    trail_list.setLayoutManager(linearLayoutManager);
                    trail_list.setAdapter(adapter);


                    String trial_call_type = trailDetails.get(trailDetails.size() - 1).getCALL_TYPE();
                    if (trial_call_type.equalsIgnoreCase("Force closed") || trial_call_type.equalsIgnoreCase("Confirm closed")) {
                        fab_trial.setVisibility(View.GONE);
                        Utility.globalMessageDialog(getActivity(), "This Lead is " + trial_call_type);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_ADD_LEAD_ACTIVITY:
                    try {
                        ((MainActivity) getActivity()).updateStatusAndCityFilter();
                    } catch (Exception e) {
                    }
                    return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}