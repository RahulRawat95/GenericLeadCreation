package com.wings2aspirations.genericleadcreation.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.gson.JsonArray;
import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.fragment.ListLeadsFragment;
import com.wings2aspirations.genericleadcreation.models.LeadDetail;
import com.wings2aspirations.genericleadcreation.network.ApiClient;
import com.wings2aspirations.genericleadcreation.network.ApiInterface;
import com.wings2aspirations.genericleadcreation.repository.CalendarHelper;
import com.wings2aspirations.genericleadcreation.repository.ShowToast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;


public class ViewLeadActivity extends AppCompatActivity {
    public static final String EXTRA_ARG_EMPLOYEE_ID = "argsEmployeeId";
    public static final String EXTRA_ARG_EMPLOYEE_NAME = "argsEmployeeName";
    public static final String EXTRA_EMP_NAMES = "employeeNames";
    public static final String EXTRA_BASE_URL = "baseUrlForRetrofit";
    public static final String EXTRA_DB_NAME = "dbName";
    public static final String EXTRA_SCHEMA_NAME = "schemaName";
    public static final String EXTRA_APPLICATION_ID = "applicationId";
    public static final String EXTRA_EMAIL_ID = "emailId";
    private ApiInterface apiInterface;

    private boolean isAdmin;

    private int empId;
    private String empName;
    private ArrayList<String> empNames;
    public static SimpleDateFormat simpleDateFormat;


    public static Intent getListLeadsIntent(Context context, String baseUrl, String dbName, String schemaName, String applicationId, int id, String emailId, ArrayList<String> empNames) {
        empNames.add(0, "Select");
        Intent intent = new Intent(context, ViewLeadActivity.class);
        intent.putExtra(EXTRA_BASE_URL, baseUrl);
        intent.putExtra(EXTRA_ARG_EMPLOYEE_ID, id);
        intent.putExtra(EXTRA_EMP_NAMES, empNames);
        intent.putExtra(EXTRA_DB_NAME, dbName);
        intent.putExtra(EXTRA_SCHEMA_NAME, schemaName);
        intent.putExtra(EXTRA_APPLICATION_ID, applicationId);
        intent.putExtra(EXTRA_EMAIL_ID, emailId);
        return intent;
    }

    public static Intent getListLeadsIntent(Context context, String baseUrl, String dbName, String schemaName, String applicationId, int empId, String emailId, String empName) {
        Intent intent = new Intent(context, ViewLeadActivity.class);
        intent.putExtra(EXTRA_ARG_EMPLOYEE_NAME, empName);
        intent.putExtra(EXTRA_ARG_EMPLOYEE_ID, empId);
        intent.putExtra(EXTRA_BASE_URL, baseUrl);
        intent.putExtra(EXTRA_DB_NAME, dbName);
        intent.putExtra(EXTRA_SCHEMA_NAME, schemaName);
        intent.putExtra(EXTRA_APPLICATION_ID, applicationId);
        intent.putExtra(EXTRA_EMAIL_ID, emailId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_lead);


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

        if (!getIntent().hasExtra(EXTRA_BASE_URL)) {
            ShowToast.showToast(this, R.string.specify_base_url_message);
            finish();
            return;
        }

        ApiClient.setBaseUrl(getIntent().getStringExtra(EXTRA_BASE_URL));

        if (!getIntent().hasExtra(EXTRA_ARG_EMPLOYEE_ID)) {
            ShowToast.showToast(this, "Please Specify ID");
            finish();
            return;
        }

        if (!getIntent().hasExtra(EXTRA_ARG_EMPLOYEE_NAME)) {
            empId = getIntent().getIntExtra(EXTRA_ARG_EMPLOYEE_ID, -1);
            isUserAdmin(true);
        } else {
            empId = getIntent().getIntExtra(EXTRA_ARG_EMPLOYEE_ID, -1);
            empName = getIntent().getStringExtra(EXTRA_ARG_EMPLOYEE_NAME);
            if (empId <= 0) {
                isUserAdmin(true);
            } else if (TextUtils.isEmpty(empName)) {
                isUserAdmin(true);
            } else {
                isUserAdmin(false);
            }
        }


        if (!getIntent().hasExtra(EXTRA_EMAIL_ID)) {
            ShowToast.showToast(this, "Email id is required");
            finish();
        }


        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        if (isAdmin) {
            if (!getIntent().hasExtra(EXTRA_EMP_NAMES)) {
                ShowToast.showToast(this, "Give Emp Names");
                finish();
                return;
            }
            empNames = (ArrayList<String>) getIntent().getExtras().getSerializable(EXTRA_EMP_NAMES);
        }

        Fragment fragment;
        if (isAdmin)
            fragment = ListLeadsFragment.newInstance(ApiClient.BASE_URL, ApiClient.getDbName(), ApiClient.getSchemaName(), ApiClient.applicationId, empId, empNames, false);
        else
            fragment = ListLeadsFragment.newInstance(ApiClient.BASE_URL, ApiClient.getDbName(), ApiClient.getSchemaName(), ApiClient.applicationId, empId, empName, false);

        if (fragment != null)
            addFragmentToBackStack(fragment);

        ApiClient.setId(getIntent().getIntExtra(EXTRA_ARG_EMPLOYEE_ID, -1));
        ApiClient.setBaseUrl(getIntent().getStringExtra(EXTRA_BASE_URL));
        apiInterface = ApiClient.getClient().create(ApiInterface.class);


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.emp_admin_tag_menu, menu);
        if (!isAdmin)
            menu.findItem(R.id.tag).setTitle(empName);
        else
            menu.findItem(R.id.tag).setTitle("Admin");
        return true;
    }


    public void isUserAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            getSupportFragmentManager().getFragments().get(0);
        } catch (Exception e) {
            finish();
        }
    }

    public void addFragmentToBackStack(Fragment fragment) {
        addFragmentToBackStack(fragment, fragment.getClass().getSimpleName());
    }

    public void addFragmentToBackStack(Fragment fragment, String tag) {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_frame);
        if (f != null && f.getClass() == fragment.getClass()) {
            // Pop last fragment if a request was made to add a fragment to back stack
            // that was already being displayed.
            getSupportFragmentManager().popBackStackImmediate();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_frame, fragment, tag)
                .addToBackStack(null)
                .commit();
    }


    public void setActionBarTitle(String pageTitle) {
        try {
            getSupportActionBar().setTitle(pageTitle);
        } catch (Exception e) {
        }
    }
}
