package com.wings2aspirations.genericleadcreation.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.activity.AddUpdateLeadActivity;
import com.wings2aspirations.genericleadcreation.activity.MainActivity;
import com.wings2aspirations.genericleadcreation.adapter.ListLeadsAdapter;
import com.wings2aspirations.genericleadcreation.models.AuthorisationToken;
import com.wings2aspirations.genericleadcreation.models.LeadDetail;
import com.wings2aspirations.genericleadcreation.network.ApiClient;
import com.wings2aspirations.genericleadcreation.network.ApiInterface;
import com.wings2aspirations.genericleadcreation.repository.CalendarHelper;
import com.wings2aspirations.genericleadcreation.repository.ExcelCreator;
import com.wings2aspirations.genericleadcreation.repository.ShowToast;
import com.wings2aspirations.genericleadcreation.repository.Utility;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.wings2aspirations.genericleadcreation.activity.AddUpdateLeadActivity.EXTRA_ARG_EMPLOYEE_ID;
import static com.wings2aspirations.genericleadcreation.activity.AddUpdateLeadActivity.EXTRA_ARG_EMPLOYEE_NAME;

public class ListLeadsFragment extends Fragment implements ListLeadsAdapter.ProgressCallback {
    public static final String EXTRA_BASE_URL = "baseUrlForRetrofit";
    public static final String EXTRA_EMP_NAMES = "employeeNames";
    public static final String EXTRA_DB_NAME = "dbName";
    public static final String EXTRA_SCHEMA_NAME = "schemaName";
    public static final String EXTRA_APPLICATION_ID = "applicationId";


    public AlertDialog leadFileOptionDialog;
    private static final int REQUEST_CODE_CALENDAR_WRITE = 123;
    public static final int SESSION_AUTHORIZATION_TOKEN_OFFSET = 12;

    public interface ListLeadCallback {
        void callback();
    }

    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    private RelativeLayout progressLayout;

    private List<LeadDetail> details;
    private List<LeadDetail> refineDetails;

    private int empId;
    private String empName;

    private boolean isAdmin;
    public static SimpleDateFormat simpleDateFormat;

    private EditText fromDateEt, toDateEt;
    private LinearLayout filterView;
    private Date fromDate, toDate;

    private ListLeadsAdapter adapter;
    private Spinner spinner;

    private String selectedEmpName = "";

    private ArrayList<String> empNames;

    private Call<JsonArray> call;

    private ApiInterface apiInterface;

    private CalendarHelper calendarHelper;

    public static ListLeadsFragment newInstance(String baseUrl, String dbName, String schemaName, String applicationId, int id, ArrayList<String> empNames) {
        Bundle bundle = new Bundle();
        bundle.putString(ListLeadsFragment.EXTRA_BASE_URL, baseUrl);
        bundle.putInt(EXTRA_ARG_EMPLOYEE_ID, id);
        bundle.putSerializable(ListLeadsFragment.EXTRA_EMP_NAMES, empNames);
        bundle.putString(ListLeadsFragment.EXTRA_DB_NAME, dbName);
        bundle.putString(ListLeadsFragment.EXTRA_SCHEMA_NAME, schemaName);
        bundle.putString(ListLeadsFragment.EXTRA_APPLICATION_ID, applicationId);

        ListLeadsFragment fragment = new ListLeadsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static ListLeadsFragment newInstance(String baseUrl, String dbName, String schemaName, String applicationId, int empId, String empName) {
        Bundle bundle = new Bundle();

        bundle.putString(EXTRA_ARG_EMPLOYEE_NAME, empName);
        bundle.putInt(EXTRA_ARG_EMPLOYEE_ID, empId);
        bundle.putString(ListLeadsFragment.EXTRA_BASE_URL, baseUrl);
        bundle.putString(ListLeadsFragment.EXTRA_DB_NAME, dbName);
        bundle.putString(ListLeadsFragment.EXTRA_SCHEMA_NAME, schemaName);
        bundle.putString(ListLeadsFragment.EXTRA_APPLICATION_ID, applicationId);

        ListLeadsFragment fragment = new ListLeadsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded())
            getLeadsList();
    }

    public void getLeadsList() {
        showProgress();
        if (call != null) {
            call.clone().enqueue(new Callback<JsonArray>() {
                @Override
                public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                    hideProgress();
                    if (!response.isSuccessful()) {
                        if (response.code() == 401) {
                            getAuthString(empId, new ListLeadCallback() {
                                @Override
                                public void callback() {
                                    getLeadsList();
                                }
                            });
                        }
                        return;
                    }
                    JsonArray jsonArray = response.body();
                    Type type = new TypeToken<List<LeadDetail>>() {
                    }.getType();
                    details = new Gson().fromJson(jsonArray, type);
                    if (isAdmin) {
                        addEvent(new CalendarHelper.CalendarCallback() {
                            @Override
                            public void callback(Boolean wasEventInserted, String eventInsertionString) {
                                if (wasEventInserted != null) {
                                    ShowToast.showToast(getActivity(), eventInsertionString);
                                }
                            }
                        });
                        calendarHelper.insertEventDirectly(getActivity(), REQUEST_CODE_CALENDAR_WRITE, details);
                    }


                    refineDetalisList();

                }

                @Override
                public void onFailure(Call<JsonArray> call, Throwable t) {
                    hideProgress();
                }
            });
        }
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


        adapter = new ListLeadsAdapter(getActivity(),refineDetails, ListLeadsFragment.this, isAdmin, new ListLeadsAdapter.LeadOnClickCallBack() {
            @Override
            public void callback(LeadDetail leadDetail) {
                TrailFragment fragment = TrailFragment.newInstance(leadDetail.getCHILD_FOLLOW_UP_ID(), leadDetail.getID(), leadDetail.getEMP_NAME(), leadDetail.getEMP_ID());
                ((FragmentActivity) getActivity()).
                        getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_frame, fragment, fragment.getClass().getSimpleName())
                        .addToBackStack(null)
                        .commit();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    public void addEvent(CalendarHelper.CalendarCallback calendarCallback) {
        calendarHelper = new CalendarHelper()
                .setContext(getActivity())
                .setCalendarCallback(calendarCallback);
    }

    @Override
    public void showProgress() {
        progressLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        progressLayout.setVisibility(View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_leads, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        floatingActionButton = view.findViewById(R.id.fab);
        progressLayout = view.findViewById(R.id.progress_bar);
        filterView = view.findViewById(R.id.filter_view);

        fromDateEt = view.findViewById(R.id.from_date_et);
        toDateEt = view.findViewById(R.id.to_date_et);
        spinner = view.findViewById(R.id.spinner);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        try{
            ((MainActivity) getActivity()).setActionBarTitle("Lead List");
        }catch (Exception e){

        }
        getLeadsList();
        adapter = new ListLeadsAdapter(getActivity(),new ArrayList<LeadDetail>(), ListLeadsFragment.this, isAdmin, new ListLeadsAdapter.LeadOnClickCallBack() {
            @Override
            public void callback(LeadDetail leadDetail) {

            }
        });
        recyclerView.setAdapter(adapter);

        if (isAdmin) {
            spinner.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, empNames));

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        selectedEmpName = "";
                    } else {
                        selectedEmpName = (String) parent.getItemAtPosition(position);
                    }
                    adapter.getFilter().filter(selectedEmpName);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            fromDateEt.setOnClickListener(new View.OnClickListener() {
                Date selectedTooDate = null;//creating local variable while click action occurring

                @Override
                public void onClick(View view) {
                    try {
                        //checking for empty field
                        if (!TextUtils.isEmpty(toDateEt.getText().toString())) {
                            //getting starting temp date for setting minDate limit for the toDate datePicker
                            selectedTooDate = simpleDateFormat.parse(toDateEt.getText().toString());
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
                        fromDate = simpleDateFormat.parse(fromDateEt.getText().toString());
                        adapter.setDate(fromDate, toDate, selectedEmpName);
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
                            selectedFromDate = simpleDateFormat.parse(fromDateEt.getText().toString());
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
                        toDate = simpleDateFormat.parse(toDateEt.getText().toString());
                        adapter.setDate(fromDate, toDate, selectedEmpName);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        showProgress();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAdmin) {
                    showCreateOrSendExcelMailOptionDialog();
                } else {
                    Intent intent = AddUpdateLeadActivity.getLeadIntent(getActivity(), empName, empId);
                    startActivity(intent);
                }
            }
        });
        isUserAdmin(isAdmin);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if (!args.containsKey(EXTRA_DB_NAME)) {
            ShowToast.showToast(getActivity(), "No Db name specified");
            getActivity().finish();
            return;
        }

        if (!args.containsKey(EXTRA_SCHEMA_NAME)) {
            ShowToast.showToast(getActivity(), "No Schema name specified");
            getActivity().finish();
            return;
        }

        if (!args.containsKey(EXTRA_APPLICATION_ID)) {
            ShowToast.showToast(getActivity(), "No Application Id specified");
            getActivity().finish();
            return;
        }

        ApiClient.setApplicationId(args.getString(EXTRA_APPLICATION_ID));

        ApiClient.setDbName(args.getString(EXTRA_DB_NAME));
        ApiClient.setSchemaName(args.getString(EXTRA_SCHEMA_NAME));

        if (!args.containsKey(EXTRA_BASE_URL)) {
            ShowToast.showToast(getActivity(), R.string.specify_base_url_message);
            getActivity().finish();
            return;
        }

        if (!args.containsKey(EXTRA_ARG_EMPLOYEE_ID)) {
            ShowToast.showToast(getActivity(), "Please Specify ID");
            getActivity().finish();
            return;
        }

        ApiClient.setId(args.getInt(EXTRA_ARG_EMPLOYEE_ID, -1));
        ApiClient.setBaseUrl(args.getString(EXTRA_BASE_URL));
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        if (!args.containsKey(EXTRA_ARG_EMPLOYEE_NAME)) {
            call = apiInterface.getAllLeads();
            isAdmin = true;
        } else {
            empId = args.getInt(EXTRA_ARG_EMPLOYEE_ID, -1);
            empName = args.getString(EXTRA_ARG_EMPLOYEE_NAME);
            if (empId <= 0) {
                call = apiInterface.getAllLeads();
                isAdmin = true;
            } else if (TextUtils.isEmpty(empName)) {
                call = apiInterface.getAllLeads();
                isAdmin = true;
            } else {
                call = apiInterface.getAllLeads(empId);
                isAdmin = false;
            }
        }

        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");


        if (isAdmin) {
            if (!args.containsKey(EXTRA_EMP_NAMES)) {
                ShowToast.showToast(getActivity(), "Give Emp Names");
                getActivity().finish();
                return;
            }
            empNames = (ArrayList<String>) args.getSerializable(EXTRA_EMP_NAMES);
        }
    }

    boolean isLeadListZeroOrNull = true;

    private void showCreateOrSendExcelMailOptionDialog() {
        //creating builder instance for the alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);

        View optionDialogView = layoutInflater.inflate(R.layout.admin_option_dialog_layout, null);
        LinearLayout createExcel = optionDialogView.findViewById(R.id.create_excel);
        LinearLayout createAndSendExcel = optionDialogView.findViewById(R.id.create_and_send_excel);

        if (details != null && details.size() > 0)
            isLeadListZeroOrNull = false;

        createExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isLeadListZeroOrNull) {
                    callExcelCreator(false);
                    leadFileOptionDialog.dismiss();
                } else
                    ShowToast.showToast(getActivity(), "No Lead List yet");
            }
        });

        createAndSendExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isLeadListZeroOrNull) {
                    callExcelCreator(true);
                    leadFileOptionDialog.dismiss();
                } else
                    ShowToast.showToast(getActivity(), "No Lead List yet");
            }
        });

        builder.setView(optionDialogView);
        leadFileOptionDialog = builder.create();
        leadFileOptionDialog.show();

    }

    private void callExcelCreator(final boolean isSendExcelFileByMail) {


        final String[][] columnRowData = new String[details.size() + 1][];
        columnRowData[0] = new String[]{
                "COMPANY NAME",
                "CONTACT PERSON",
                "EMAIL",
                "MOBILE NO",
                "ADDRESS",
                "PIN CODE",
                "CUSTOMER REMARKS",
                "LEAD REMARKS",
                "NEXT FOLLOW UP DATE",
                "NEXT FOLLOW UP TIME",
                "CALL TYPE",
                "EMP NAME",
                "DATE"};

        for (int i = 0; i < details.size(); i++) {
            columnRowData[i + 1] = details.get(i).getColumnData();
        }

        final String fileName = ApiClient.getSchemaName() + ApiClient.getDbName() + System.currentTimeMillis();

        showProgress();
        ExcelCreator.createExcel(columnRowData, fileName, getActivity(), isSendExcelFileByMail, new ExcelCreator.ExcelCallBack() {
            @Override
            public void excelCreated(boolean hasExcelBeenCreated, String filePath) {
                hideProgress();
                if (hasExcelBeenCreated && !isSendExcelFileByMail)
                    Utility.globalMessageDialog(getActivity(), "File Stored At : \n" + filePath);

                if (hasExcelBeenCreated && isSendExcelFileByMail)
                    ShowToast.showToast(getActivity(), "File Stored At : \n" + filePath);
            }
        });

    }

    public void isUserAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
        if (isAdmin) {
//            floatingActionButton.setVisibility(View.GONE);

            floatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_show_options));
            filterView.setVisibility(View.VISIBLE);
        } else {
//            floatingActionButton.setVisibility(View.VISIBLE);

            floatingActionButton.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_input_add));
            filterView.setVisibility(View.GONE);
        }
    }

    public void getAuthString(int id, final ListLeadCallback listLeadCallback) {
        Call<AuthorisationToken> call = apiInterface.getAuthToken(id);
        call.enqueue(new Callback<AuthorisationToken>() {
            @Override
            public void onResponse(Call<AuthorisationToken> call, Response<AuthorisationToken> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                AuthorisationToken authorisationToken = response.body();
                ApiClient.setAuthString(authorisationToken.getToken());
                setTimerTask(empId, authorisationToken.getExpiresIn());
                if (listLeadCallback != null)
                    listLeadCallback.callback();
            }

            @Override
            public void onFailure(Call<AuthorisationToken> call, Throwable t) {

            }
        });
    }

    public void setTimerTask(final int empId, long time) {
        Timer timer = new Timer();
        long countdown;
        try {
            countdown = time - SESSION_AUTHORIZATION_TOKEN_OFFSET;
        } catch (Exception e) {
            countdown = 0;
        }

        countdown *= 1000;

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getAuthString(empId, null);
            }
        }, countdown);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_CALENDAR_WRITE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    calendarHelper.insertEventDirectly(getActivity(), REQUEST_CODE_CALENDAR_WRITE, details);
                else {
                    ShowToast.showToast(getActivity(), "The Leads will not be added to your Calendar");
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}