package com.wings2aspirations.genericleadcreation.fragment;

import android.content.DialogInterface;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.activity.AddUpdateLeadActivity;
import com.wings2aspirations.genericleadcreation.activity.MainActivity;
import com.wings2aspirations.genericleadcreation.activity.ViewLeadActivity;
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

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.wings2aspirations.genericleadcreation.activity.AddUpdateLeadActivity.EXTRA_ARG_EMPLOYEE_ID;
import static com.wings2aspirations.genericleadcreation.activity.AddUpdateLeadActivity.EXTRA_ARG_EMPLOYEE_NAME;

public class ListLeadsFragment extends Fragment implements ListLeadsAdapter.ProgressCallback {
    public static final int REQUEST_CODE_ADD_LEAD_ACTIVITY = 24;

    public static final String EXTRA_BASE_URL = "baseUrlForRetrofit";
    public static final String EXTRA_EMP_NAMES = "employeeNames";
    public static final String EXTRA_DB_NAME = "dbName";
    public static final String EXTRA_SCHEMA_NAME = "schemaName";
    public static final String EXTRA_APPLICATION_ID = "applicationId";
    public static final String EXTRA_SHOW_ADD_BUTTON = "show_button";

    private AlertDialog formatDialog;

    public AlertDialog leadFileOptionDialog;
    private static final int REQUEST_CODE_CALENDAR_WRITE = 123;
    public static final int SESSION_AUTHORIZATION_TOKEN_OFFSET = 12;

    public interface ListLeadCallback {
        void callback();
    }

    private RecyclerView tabRecyclerView, cardRecyclerView;
    private FloatingActionButton floatingActionButton;
    private HorizontalScrollView horizontalScrollView;

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

    private ListLeadsAdapter tabAdapter, cardAdapter;
    private Spinner spinner, demoFilterSpinner, existingCustomerFilterSpinner;

    private String selectedEmpName = "";

    private ArrayList<String> empNames;
    private int selectedResourceId;
    private Call<JsonArray> call;

    private ApiInterface apiInterface;

    private CalendarHelper calendarHelper;

    private boolean showAddButton;

    public static ListLeadsFragment newInstance(String baseUrl, String dbName, String schemaName, String applicationId, int id, ArrayList<String> empNames, boolean canAdd) {
        Bundle bundle = new Bundle();
        bundle.putString(ListLeadsFragment.EXTRA_BASE_URL, baseUrl);
        bundle.putInt(EXTRA_ARG_EMPLOYEE_ID, id);
        bundle.putSerializable(ListLeadsFragment.EXTRA_EMP_NAMES, empNames);
        bundle.putString(ListLeadsFragment.EXTRA_DB_NAME, dbName);
        bundle.putString(ListLeadsFragment.EXTRA_SCHEMA_NAME, schemaName);
        bundle.putString(ListLeadsFragment.EXTRA_APPLICATION_ID, applicationId);
        bundle.putBoolean(ListLeadsFragment.EXTRA_SHOW_ADD_BUTTON, canAdd);

        ListLeadsFragment fragment = new ListLeadsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static ListLeadsFragment newInstance(String baseUrl, String dbName, String schemaName, String applicationId, int empId, String empName, boolean canAdd) {
        Bundle bundle = new Bundle();

        bundle.putString(EXTRA_ARG_EMPLOYEE_NAME, empName);
        bundle.putInt(EXTRA_ARG_EMPLOYEE_ID, empId);
        bundle.putString(ListLeadsFragment.EXTRA_BASE_URL, baseUrl);
        bundle.putString(ListLeadsFragment.EXTRA_DB_NAME, dbName);
        bundle.putString(ListLeadsFragment.EXTRA_SCHEMA_NAME, schemaName);
        bundle.putString(ListLeadsFragment.EXTRA_APPLICATION_ID, applicationId);
        bundle.putBoolean(ListLeadsFragment.EXTRA_SHOW_ADD_BUTTON, canAdd);

        ListLeadsFragment fragment = new ListLeadsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded() && details == null)
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
        cardAdapter = new ListLeadsAdapter(getActivity(), refineDetails, ListLeadsFragment.this, isAdmin, new ListLeadsAdapter.LeadOnClickCallBack() {
            @Override
            public void callback(LeadDetail leadDetail) {
                TrailFragment fragment = TrailFragment.newInstance(leadDetail.getCHILD_FOLLOW_UP_ID(), leadDetail.getID(), leadDetail.getEMP_NAME(), leadDetail.getEMP_ID(), showAddButton);
                ((FragmentActivity) getActivity()).
                        getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_frame, fragment, fragment.getClass().getSimpleName())
                        .addToBackStack(null)
                        .commit();
            }
        }, R.layout.item_leads_card);
        tabAdapter = new ListLeadsAdapter(getActivity(), refineDetails, ListLeadsFragment.this, isAdmin, new ListLeadsAdapter.LeadOnClickCallBack() {
            @Override
            public void callback(LeadDetail leadDetail) {
                TrailFragment fragment = TrailFragment.newInstance(leadDetail.getCHILD_FOLLOW_UP_ID(), leadDetail.getID(), leadDetail.getEMP_NAME(), leadDetail.getEMP_ID(), showAddButton);
                ((FragmentActivity) getActivity()).
                        getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_frame, fragment, fragment.getClass().getSimpleName())
                        .addToBackStack(null)
                        .commit();
            }
        }, R.layout.item_leads_tab);
        cardRecyclerView.setAdapter(cardAdapter);
        tabRecyclerView.setAdapter(tabAdapter);

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
        cardRecyclerView = view.findViewById(R.id.card_recycler_view);
        tabRecyclerView = view.findViewById(R.id.tab_recycler_view);
        floatingActionButton = view.findViewById(R.id.fab);
        horizontalScrollView = view.findViewById(R.id.horizontal_scroll_view);
        progressLayout = view.findViewById(R.id.progress_bar);
        filterView = view.findViewById(R.id.filter_view);

        fromDateEt = view.findViewById(R.id.from_date_et);
        toDateEt = view.findViewById(R.id.to_date_et);
        spinner = view.findViewById(R.id.spinner);

        demoFilterSpinner = view.findViewById(R.id.demo_filter_spinner);
        existingCustomerFilterSpinner = view.findViewById(R.id.existing_customer_filter_spinner);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        if (showAddButton) {
            try {
                ((MainActivity) getActivity()).setActionBarTitle("Lead List");
            } catch (Exception e) {

            }
        } else {
            try {
                ((ViewLeadActivity) getActivity()).setActionBarTitle("Lead List");
            } catch (Exception e) {

            }
        }

        if (showAddButton)
            //floatingActionButton.setVisibility(View.VISIBLE);
            floatingActionButton.setVisibility(View.INVISIBLE);
        else
            floatingActionButton.setVisibility(View.GONE);
        getLeadsList();
        cardAdapter = new ListLeadsAdapter(getActivity(), new ArrayList<LeadDetail>(), ListLeadsFragment.this, isAdmin, new ListLeadsAdapter.LeadOnClickCallBack() {
            @Override
            public void callback(LeadDetail leadDetail) {

            }
        }, R.layout.item_leads_card);
        cardRecyclerView.setAdapter(cardAdapter);

        tabAdapter = new ListLeadsAdapter(getActivity(), new ArrayList<LeadDetail>(), ListLeadsFragment.this, isAdmin, new ListLeadsAdapter.LeadOnClickCallBack() {
            @Override
            public void callback(LeadDetail leadDetail) {

            }
        }, R.layout.item_leads_tab);
        tabRecyclerView.setAdapter(tabAdapter);

        createFormatDialog();

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
                    if (selectedResourceId == R.layout.item_leads_tab) {
                        tabAdapter.getFilter().filter(selectedEmpName);
                    } else if (selectedResourceId == R.layout.item_leads_card) {
                        cardAdapter.getFilter().filter(selectedEmpName);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            demoFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String demo = "";
                    if (position != 0) {
                        demo = (String) parent.getItemAtPosition(position);
                    }
                    try {
                        if (selectedResourceId == R.layout.item_leads_tab) {
                            tabAdapter.setDemo(selectedEmpName, demo);
                        } else if (selectedResourceId == R.layout.item_leads_card) {
                            cardAdapter.setDemo(selectedEmpName, demo);
                        }
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            existingCustomerFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String existingCust = "";
                    if (position != 0) {
                        existingCust = (String) parent.getItemAtPosition(position);
                    }
                    try {
                        switch (position) {
                            case 1:
                                existingCust = "Yes";
                                break;
                            case 2:
                                existingCust = "No";
                                break;
                        }
                        try {
                            if (selectedResourceId == R.layout.item_leads_tab) {
                                tabAdapter.setExistingCust(selectedEmpName, existingCust);
                            } else if (selectedResourceId == R.layout.item_leads_card) {
                                cardAdapter.setExistingCust(selectedEmpName, existingCust);
                            }
                        } catch (Exception e) {
                        }
                    } catch (Exception e) {
                    }
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
                        try {
                            if (selectedResourceId == R.layout.item_leads_tab) {
                                tabAdapter.setDate(fromDate, toDate, selectedEmpName);
                            } else if (selectedResourceId == R.layout.item_leads_card) {
                                cardAdapter.setDate(fromDate, toDate, selectedEmpName);
                            }
                        } catch (Exception e) {
                        }
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
                            Utility.showDatePickerDialog(getActivity(), toDateEt, selectedFromDate, null, fromDate);
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
                        try {
                            if (selectedResourceId == R.layout.item_leads_tab) {
                                tabAdapter.setDate(fromDate, toDate, selectedEmpName);
                            } else if (selectedResourceId == R.layout.item_leads_card) {
                                cardAdapter.setDate(fromDate, toDate, selectedEmpName);
                            }
                        } catch (Exception e) {
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }

        cardRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        tabRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        showProgress();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAdmin) {
                    showCreateOrSendExcelMailOptionDialog();
                } else {
                    Intent intent = AddUpdateLeadActivity.getLeadIntent(getActivity(), empName, empId);
                    startActivityForResult(intent, REQUEST_CODE_ADD_LEAD_ACTIVITY);
                }
            }
        });
        isUserAdmin(isAdmin);

        if (formatDialog != null) {
            formatDialog.show();
        } else {
            selectedResourceId = R.layout.item_leads_card;
            switchLayout(false);
            getLeadsList();
        }
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

        showAddButton = args.getBoolean(EXTRA_SHOW_ADD_BUTTON);
        if (showAddButton)
            setHasOptionsMenu(true);

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
                "DATE",
                "CUSTOMER TYPE",
                "DEMO"};

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
            if (!showAddButton)
                filterView.setVisibility(View.GONE);
            else
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_ADD_LEAD_ACTIVITY:
                    try {
                        ((MainActivity) getActivity()).updateStatusAndCityFilter();
                        getLeadsList();
                    } catch (Exception e) {
                    }
                    return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_share, menu);
        menu.findItem(R.id.menu_share).setTitle(isAdmin ? "Share" : "Add New Lead");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_share) {
            if (isAdmin) {
                showCreateOrSendExcelMailOptionDialog();
            } else {
                Intent intent = AddUpdateLeadActivity.getLeadIntent(getActivity(), empName, empId);
                startActivityForResult(intent, REQUEST_CODE_ADD_LEAD_ACTIVITY);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchLayout(boolean showHorizontal) {
        horizontalScrollView.setVisibility(showHorizontal ? View.VISIBLE : View.GONE);
        cardRecyclerView.setVisibility(showHorizontal ? View.GONE : View.VISIBLE);
    }

    public void createFormatDialog() {
        try {
            formatDialog = new AlertDialog.Builder(getActivity())
                    .setMessage("Report Format")
                    .setPositiveButton("Tabular", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selectedResourceId = R.layout.item_leads_tab;
                            switchLayout(true);
                            getLeadsList();
                            dialog.dismiss();
                        }
                    })
                    .setNeutralButton("Card", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selectedResourceId = R.layout.item_leads_card;
                            switchLayout(false);
                            getLeadsList();
                            dialog.dismiss();
                        }
                    })
                    .setCancelable(false)
                    .create();
        } catch (Exception e) {
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            formatDialog.dismiss();
        } catch (Exception e) {
        }
    }
}