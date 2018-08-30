package com.wings2aspirations.genericleadcreation.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.adapter.Adapter;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.wings2aspirations.genericleadcreation.activity.AddUpdateLeadActivity.EXTRA_ARG_EMPLOYEE_ID;
import static com.wings2aspirations.genericleadcreation.activity.AddUpdateLeadActivity.EXTRA_ARG_EMPLOYEE_NAME;

public class ListLeadsActivity extends AppCompatActivity implements Adapter.ProgressCallback {
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

    private Adapter adapter;
    private Spinner spinner;

    private String selectedEmpName = "";

    private ArrayList<String> empNames;

    private Call<JsonArray> call;

    private ApiInterface apiInterface;

    private CalendarHelper calendarHelper;

    public static Intent getListLeadsIntent(Context context, String baseUrl, String dbName, String schemaName, String applicationId, int id, ArrayList<String> empNames) {
        Intent intent = new Intent(context, ListLeadsActivity.class);
        intent.putExtra(ListLeadsActivity.EXTRA_BASE_URL, baseUrl);
        intent.putExtra(EXTRA_ARG_EMPLOYEE_ID, id);
        intent.putExtra(ListLeadsActivity.EXTRA_EMP_NAMES, empNames);
        intent.putExtra(ListLeadsActivity.EXTRA_DB_NAME, dbName);
        intent.putExtra(ListLeadsActivity.EXTRA_SCHEMA_NAME, schemaName);
        intent.putExtra(ListLeadsActivity.EXTRA_APPLICATION_ID, applicationId);
        return intent;
    }

    public static Intent getListLeadsIntent(Context context, String baseUrl, String dbName, String schemaName, String applicationId, int empId, String empName) {
        Intent intent = new Intent(context, ListLeadsActivity.class);
        intent.putExtra(EXTRA_ARG_EMPLOYEE_NAME, empName);
        intent.putExtra(EXTRA_ARG_EMPLOYEE_ID, empId);
        intent.putExtra(ListLeadsActivity.EXTRA_BASE_URL, baseUrl);
        intent.putExtra(ListLeadsActivity.EXTRA_DB_NAME, dbName);
        intent.putExtra(ListLeadsActivity.EXTRA_SCHEMA_NAME, schemaName);
        intent.putExtra(ListLeadsActivity.EXTRA_APPLICATION_ID, applicationId);
        return intent;
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                                    ShowToast.showToast(ListLeadsActivity.this, eventInsertionString);
                                }
                            }
                        });
                        calendarHelper.insertEventDirectly(ListLeadsActivity.this, REQUEST_CODE_CALENDAR_WRITE, details);
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
        int isSameFound;
        for (int i = 0; i < details.size(); i++) {
            if (refineDetails.size() == 0) {
                refineDetails.add(details.get(i));
            } else {
                isSameFound = -1;
                for (int j = 0; j < refineDetails.size(); j++) {
                    if (refineDetails.get(j).getCHILD_FOLLOW_UP_ID() == details.get(i).getCHILD_FOLLOW_UP_ID())
                        isSameFound = j;
                }
                if (isSameFound != -1)
                    refineDetails.set(isSameFound, details.get(i));
                else
                    refineDetails.add(details.get(i));
            }
        }


        adapter = new Adapter(refineDetails, ListLeadsActivity.this, isAdmin, new Adapter.LeadOnClickCallBack() {
            @Override
            public void callback(LeadDetail leadDetail) {
                Intent trialIntent = new Intent(ListLeadsActivity.this, TrailActivity.class);
                trialIntent.putExtra("CHILD_FOLLOW_UP_ID", leadDetail.getCHILD_FOLLOW_UP_ID());
                trialIntent.putExtra("ID", leadDetail.getID());
                trialIntent.putExtra("emp_name", leadDetail.getEMP_NAME());
                trialIntent.putExtra("emp_id", leadDetail.getEMP_ID());
                startActivity(trialIntent);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    public void addEvent(CalendarHelper.CalendarCallback calendarCallback) {
        calendarHelper = new CalendarHelper()
                .setContext(this)
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_leads);

        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(getString(R.string.key_remind_at_time), "18:00").commit();

        if (!getIntent().hasExtra(EXTRA_DB_NAME)) {
            ShowToast.showToast(this, "No Db name specified");
            finish();
            return;
        }

        if (!getIntent().hasExtra(EXTRA_SCHEMA_NAME)) {
            ShowToast.showToast(this, "No Schema name specified");
            finish();
            return;
        }

        if (!getIntent().hasExtra(EXTRA_APPLICATION_ID)) {
            ShowToast.showToast(this, "No Application Id specified");
            finish();
            return;
        }

        ApiClient.setApplicationId(getIntent().getStringExtra(EXTRA_APPLICATION_ID));

        ApiClient.setDbName(getIntent().getStringExtra(EXTRA_DB_NAME));
        ApiClient.setSchemaName(getIntent().getStringExtra(EXTRA_SCHEMA_NAME));

        recyclerView = findViewById(R.id.recycler_view);
        floatingActionButton = findViewById(R.id.fab);
        progressLayout = findViewById(R.id.progress_bar);
        filterView = findViewById(R.id.filter_view);

        if (!getIntent().hasExtra(EXTRA_BASE_URL)) {
            ShowToast.showToast(this, R.string.specify_base_url_message);
            finish();
            return;
        }

        if (!getIntent().hasExtra(EXTRA_ARG_EMPLOYEE_ID)) {
            ShowToast.showToast(this, "Please Specify ID");
            finish();
            return;
        }

        ApiClient.setId(getIntent().getIntExtra(EXTRA_ARG_EMPLOYEE_ID, -1));
        ApiClient.setBaseUrl(getIntent().getStringExtra(EXTRA_BASE_URL));
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        if (!getIntent().hasExtra(EXTRA_ARG_EMPLOYEE_NAME)) {
            call = apiInterface.getAllLeads();
            isUserAdmin(true);
        } else {
            empId = getIntent().getIntExtra(EXTRA_ARG_EMPLOYEE_ID, -1);
            empName = getIntent().getStringExtra(EXTRA_ARG_EMPLOYEE_NAME);
            if (empId <= 0) {
                call = apiInterface.getAllLeads();
                isUserAdmin(true);
            } else if (TextUtils.isEmpty(empName)) {
                call = apiInterface.getAllLeads();
                isUserAdmin(true);
            } else {
                call = apiInterface.getAllLeads(empId);
                isUserAdmin(false);
            }
        }

        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        fromDateEt = findViewById(R.id.from_date_et);
        toDateEt = findViewById(R.id.to_date_et);

        adapter = new Adapter(new ArrayList<LeadDetail>(), ListLeadsActivity.this, isAdmin, new Adapter.LeadOnClickCallBack() {
            @Override
            public void callback(LeadDetail leadDetail) {

            }
        });
        recyclerView.setAdapter(adapter);

        spinner = findViewById(R.id.spinner);

        if (isAdmin) {
            if (!getIntent().hasExtra(EXTRA_EMP_NAMES)) {
                ShowToast.showToast(this, "Give Emp Names");
                finish();
                return;
            }
            ArrayList<String> strings = (ArrayList<String>) getIntent().getSerializableExtra(EXTRA_EMP_NAMES);
            strings.add(0, "Select");
            spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, strings));

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
                            Utility.showDatePickerDialog(ListLeadsActivity.this, fromDateEt, null, selectedTooDate, fromDate);
                        } else {
                            Utility.showDatePickerDialog(ListLeadsActivity.this, fromDateEt, null, null, fromDate);
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
                            Utility.showDatePickerDialog(ListLeadsActivity.this, toDateEt, selectedFromDate, null, toDate);
                        } else {
                            Utility.showDatePickerDialog(ListLeadsActivity.this, toDateEt, null, null, toDate);
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

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        showProgress();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAdmin) {
                    showCreateOrSendExcelMailOptionDialog();
                } else {
                    Intent intent = AddUpdateLeadActivity.getLeadIntent(ListLeadsActivity.this, empName, empId);
                    startActivity(intent);
                }
            }
        });
    }

    boolean isLeadListZeroOrNull = true;

    private void showCreateOrSendExcelMailOptionDialog() {
        //creating builder instance for the alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(ListLeadsActivity.this);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

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
                    ShowToast.showToast(ListLeadsActivity.this, "No Lead List yet");
            }
        });

        createAndSendExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isLeadListZeroOrNull) {
                    callExcelCreator(true);
                    leadFileOptionDialog.dismiss();
                } else
                    ShowToast.showToast(ListLeadsActivity.this, "No Lead List yet");
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
        ExcelCreator.createExcel(columnRowData, fileName, ListLeadsActivity.this, isSendExcelFileByMail, new ExcelCreator.ExcelCallBack() {
            @Override
            public void excelCreated(boolean hasExcelBeenCreated, String filePath) {
                hideProgress();
                if (hasExcelBeenCreated && !isSendExcelFileByMail)
                    Utility.globalMessageDialog(ListLeadsActivity.this, "File Stored At : \n" + filePath);

                if (hasExcelBeenCreated && isSendExcelFileByMail)
                    ShowToast.showToast(ListLeadsActivity.this, "File Stored At : \n" + filePath);
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
                    calendarHelper.insertEventDirectly(ListLeadsActivity.this, REQUEST_CODE_CALENDAR_WRITE, details);
                else {
                    ShowToast.showToast(this, "The Leads will not be added to your Calendar");
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return isAdmin;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}