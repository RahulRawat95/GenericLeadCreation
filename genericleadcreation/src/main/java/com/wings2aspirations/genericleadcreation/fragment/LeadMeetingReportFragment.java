package com.wings2aspirations.genericleadcreation.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.activity.ListLeadsActivity;
import com.wings2aspirations.genericleadcreation.activity.MainActivity;
import com.wings2aspirations.genericleadcreation.activity.TrailActivity;
import com.wings2aspirations.genericleadcreation.adapter.Adapter;
import com.wings2aspirations.genericleadcreation.adapter.LeadMeetReportAdapter;
import com.wings2aspirations.genericleadcreation.models.ItemModel;
import com.wings2aspirations.genericleadcreation.models.LeadDetail;
import com.wings2aspirations.genericleadcreation.network.ApiClient;
import com.wings2aspirations.genericleadcreation.network.ApiInterface;
import com.wings2aspirations.genericleadcreation.repository.Constants;
import com.wings2aspirations.genericleadcreation.repository.ShowOptionSelectionDialog;
import com.wings2aspirations.genericleadcreation.repository.ShowToast;
import com.wings2aspirations.genericleadcreation.repository.Utility;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.wings2aspirations.genericleadcreation.repository.ShowOptionSelectionDialog.TYPE_CITY;
import static com.wings2aspirations.genericleadcreation.repository.ShowOptionSelectionDialog.TYPE_PRODUCT;
import static com.wings2aspirations.genericleadcreation.repository.ShowOptionSelectionDialog.TYPE_STATUS;

public class LeadMeetingReportFragment extends Fragment implements LeadMeetReportAdapter.ProgressCallback {
    private static final String ARG_IS_LEAD_REPORT = "argIsLeadReport";
    private static final String ARG_EMP_ID = "argEmployeeId";
    private static final String ARG_IS_ADMIN = "argIsAdmin";

    private boolean isLeadReport;

    private EditText fromDateEt, toDateEt;

    private Date fromDate, toDate;
    private FloatingActionButton leadFilterFab;
    private LeadMeetReportAdapter adapter;
    private RelativeLayout progressLayout;
    private RecyclerView recyclerView;
    private ApiInterface apiInterface;
    private List<LeadDetail> details;
    private List<LeadDetail> refineDetails;

    private HashSet<Integer> productHash, statusHash, cityHash;

    private FilterSheetFragment filterSheetFragment;

    private int empId;
    private boolean isAdmin;

    private HashSet<Integer>[] hashSets;
    private ArrayList<ItemModel>[] itemModels;

    public static LeadMeetingReportFragment newInstance(boolean isLeadReport, int empId, boolean isAdmin) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_LEAD_REPORT, isLeadReport);
        args.putInt(ARG_EMP_ID, empId);
        args.putBoolean(ARG_IS_ADMIN, isAdmin);

        LeadMeetingReportFragment fragment = new LeadMeetingReportFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void getValues() {
        Bundle args = getArguments();
        isLeadReport = args.getBoolean(ARG_IS_LEAD_REPORT);
        empId = args.getInt(ARG_EMP_ID);
        isAdmin = args.getBoolean(ARG_IS_ADMIN);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        productHash = new HashSet<>();
        statusHash = new HashSet<>();
        cityHash = new HashSet<>();
        hashSets = new HashSet[]{productHash, statusHash, cityHash};
        getValues();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lead_meeting_report, container, false);
        fromDateEt = view.findViewById(R.id.from_date_et);
        toDateEt = view.findViewById(R.id.to_date_et);
        recyclerView = view.findViewById(R.id.recycler_view);
        progressLayout = view.findViewById(R.id.progress_bar);
        leadFilterFab = view.findViewById(R.id.lead_filter_fab);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        try{
            ((MainActivity) getActivity()).setActionBarTitle("Lead Meeting");
        }catch (Exception e){

        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        fromDateEt.setOnClickListener(new View.OnClickListener() {
            Date selectedTooDate = null;//creating local variable while click action occurring

            @Override
            public void onClick(View view) {
                try {
                    //checking for empty field
                    if (!TextUtils.isEmpty(toDateEt.getText().toString())) {
                        //getting starting temp date for setting minDate limit for the toDate datePicker
                        selectedTooDate = MainActivity.simpleDateFormat.parse(toDateEt.getText().toString());
                        //calling global datePicker
                        Utility.showDatePickerDialog(getActivity(), fromDateEt, null, selectedTooDate, fromDate);
                    } else {
                        Utility.showDatePickerDialog(getActivity(), fromDateEt, null, null, fromDate);
                    }

                } catch (Exception e) {
                }
            }
        });


        fromDateEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    //creating fromDate variable for executing query as date set on the editText
                    fromDate = MainActivity.simpleDateFormat.parse(fromDateEt.getText().toString());
                    adapter.setDate(fromDate, toDate, "");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //setting on ClickListener on the toDate EitText
        toDateEt.setOnClickListener(new View.OnClickListener() {
            //creating onClick toDate variable
            Date selectedFromDate = null;

            @Override
            public void onClick(View view) {
                try {
                    //checking for empty field
                    if (!TextUtils.isEmpty(fromDateEt.getText().toString())) {
                        //getting from temp date for setting maxDate limit for the fromDate datePicker
                        selectedFromDate = MainActivity.simpleDateFormat.parse(fromDateEt.getText().toString());
                        Utility.showDatePickerDialog(getActivity(), toDateEt, selectedFromDate, null, toDate);
                    } else {
                        Utility.showDatePickerDialog(getActivity(), toDateEt, null, null, toDate);
                    }

                } catch (Exception e) {
                }
            }
        });

        toDateEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    //creating toDate variable for executing query as date set on the editText
                    toDate = MainActivity.simpleDateFormat.parse(toDateEt.getText().toString());
                    adapter.setDate(fromDate, toDate, "");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        leadFilterFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterSheetFragment == null) {
                    itemModels = new ArrayList[]{Constants.getProducts(), Constants.getStatuses(), Constants.getCities()};
                    filterSheetFragment = filterSheetFragment.newInstance(itemModels, hashSets);
                }
                filterSheetFragment.setCallback(new FilterSheetFragment.Callback() {
                    @Override
                    public void callback() {
                        if (adapter != null)
                            adapter.getFilter().filter("");
                    }
                });
                filterSheetFragment.show(getActivity().getSupportFragmentManager(), "Filter");
            }
        });

        getLeadsList();
    }

    public void getLeadsList() {
        showProgress();
        apiInterface.getAllLeads(empId).clone().enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                hideProgress();
                if (!response.isSuccessful()) {
                    return;
                }
                JsonArray jsonArray = response.body();
                Type type = new TypeToken<List<LeadDetail>>() {
                }.getType();
                details = new Gson().fromJson(jsonArray, type);
                refineDetalisList();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                hideProgress();
            }
        });
    }

    @Override
    public void showProgress() {
        progressLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        progressLayout.setVisibility(View.GONE);
    }

    private void refineDetalisList() {
        refineDetails = new ArrayList<>();
        SparseArray<LeadDetail> sparseArray = new SparseArray<>();
        LeadDetail leadDetail;
        for (int i = 0; i < details.size(); i++) {
            leadDetail = details.get(i);
            sparseArray.put(leadDetail.getCHILD_FOLLOW_UP_ID(), leadDetail);
        }
        for (int i = 0; i < sparseArray.size(); i++) {
            refineDetails.add(sparseArray.get(sparseArray.keyAt(i)));
        }

        itemModels = new ArrayList[]{Constants.getProducts(), Constants.getStatuses(), Constants.getCities()};

        adapter = new LeadMeetReportAdapter(refineDetails, getActivity(), hashSets, isAdmin, isLeadReport, this, new LeadMeetReportAdapter.LeadOnClickCallBack() {
            @Override
            public void callback(LeadDetail leadDetail) {
                Intent trialIntent = new Intent(getActivity(), TrailActivity.class);
                trialIntent.putExtra("CHILD_FOLLOW_UP_ID", leadDetail.getCHILD_FOLLOW_UP_ID());
                trialIntent.putExtra("ID", leadDetail.getID());
                trialIntent.putExtra("emp_name", leadDetail.getEMP_NAME());
                trialIntent.putExtra("emp_id", leadDetail.getEMP_ID());
                startActivity(trialIntent);
            }
        });
        recyclerView.setAdapter(adapter);
    }
}