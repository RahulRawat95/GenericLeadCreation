package com.wings2aspirations.genericleadcreation.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.fragment.LeadMeetingReportFragment;
import com.wings2aspirations.genericleadcreation.fragment.ListLeadsFragment;
import com.wings2aspirations.genericleadcreation.models.AuthorisationToken;
import com.wings2aspirations.genericleadcreation.models.City;
import com.wings2aspirations.genericleadcreation.models.ItemModel;
import com.wings2aspirations.genericleadcreation.models.ProductListModel;
import com.wings2aspirations.genericleadcreation.models.State;
import com.wings2aspirations.genericleadcreation.network.ApiClient;
import com.wings2aspirations.genericleadcreation.network.ApiInterface;
import com.wings2aspirations.genericleadcreation.repository.Constants;
import com.wings2aspirations.genericleadcreation.repository.ShowToast;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ViewLeadActivity extends AppCompatActivity {
    public static final String EXTRA_ARG_EMPLOYEE_ID = "argsEmployeeId";
    public static final String EXTRA_ARG_EMPLOYEE_NAME = "argsEmployeeName";
    public static final String EXTRA_EMP_NAMES = "employeeNames";
    public static final String EXTRA_BASE_URL = "baseUrlForRetrofit";
    public static final String EXTRA_DB_NAME = "dbName";
    public static final String EXTRA_SCHEMA_NAME = "schemaName";
    public static final String EXTRA_APPLICATION_ID = "applicationId";
    public static final String EXTRA_EMAIL_ID = "emailId";
    public static final String EXTRA_IS_FOR_REPORT = "forReport";
    private ApiInterface apiInterface;

    public static final int SESSION_AUTHORIZATION_TOKEN_OFFSET = 12;
    private boolean isAdmin;

    private int empId;
    private String empName;
    private ArrayList<String> empNames;
    public static SimpleDateFormat simpleDateFormat;

    private boolean isForReport;
    private Fragment fragment;

    public static Intent getListLeadsIntent(Context context, String baseUrl, String dbName, String schemaName, String applicationId, int id, String emailId,
                                            ArrayList<String> empNames, boolean isForReport) {
        empNames.add(0, "Select");
        Intent intent = new Intent(context, ViewLeadActivity.class);
        intent.putExtra(EXTRA_BASE_URL, baseUrl);
        intent.putExtra(EXTRA_ARG_EMPLOYEE_ID, id);
        intent.putExtra(EXTRA_EMP_NAMES, empNames);
        intent.putExtra(EXTRA_DB_NAME, dbName);
        intent.putExtra(EXTRA_SCHEMA_NAME, schemaName);
        intent.putExtra(EXTRA_APPLICATION_ID, applicationId);
        intent.putExtra(EXTRA_EMAIL_ID, emailId);
        intent.putExtra(EXTRA_IS_FOR_REPORT, isForReport);
        return intent;
    }

    public static Intent getListLeadsIntent(Context context, String baseUrl, String dbName, String schemaName, String applicationId, int empId,
                                            String emailId, String empName, boolean isForReport) {
        Intent intent = new Intent(context, ViewLeadActivity.class);
        intent.putExtra(EXTRA_ARG_EMPLOYEE_NAME, empName);
        intent.putExtra(EXTRA_ARG_EMPLOYEE_ID, empId);
        intent.putExtra(EXTRA_BASE_URL, baseUrl);
        intent.putExtra(EXTRA_DB_NAME, dbName);
        intent.putExtra(EXTRA_SCHEMA_NAME, schemaName);
        intent.putExtra(EXTRA_APPLICATION_ID, applicationId);
        intent.putExtra(EXTRA_EMAIL_ID, emailId);
        intent.putExtra(EXTRA_IS_FOR_REPORT, isForReport);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_lead);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_view_lead);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.view_lead_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.syncState();

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


        ApiClient.setId(getIntent().getIntExtra(EXTRA_ARG_EMPLOYEE_ID, -1));
        ApiClient.setBaseUrl(getIntent().getStringExtra(EXTRA_BASE_URL));
        apiInterface = ApiClient.getClient().create(ApiInterface.class);


        isForReport = getIntent().getBooleanExtra(EXTRA_IS_FOR_REPORT, false);
        if (isForReport) {
            fragment = LeadMeetingReportFragment.newInstance(true, empId, true, empNames, true);


            getAuthString(empId, new ListLeadsActivity.ListLeadCallback() {
                @Override
                public void callback() {
                    if (fragment instanceof LeadMeetingReportFragment) {
                        ((LeadMeetingReportFragment) fragment).getLeadsList();
                    }
                    getProductList();
                    getCityList();
                    apiInterface.getStates().enqueue(new Callback<ArrayList<State>>() {
                        @Override
                        public void onResponse(Call<ArrayList<State>> call, Response<ArrayList<State>> response) {
                            if (!response.isSuccessful()) {
                                return;
                            }
                            Constants.setStates(response.body());
                        }

                        @Override
                        public void onFailure(Call<ArrayList<State>> call, Throwable t) {

                        }
                    });
                }
            });

        } else {
            if (isAdmin)
                fragment = ListLeadsFragment.newInstance(ApiClient.BASE_URL, ApiClient.getDbName(), ApiClient.getSchemaName(), ApiClient.applicationId, empId, empNames, false);
            else
                fragment = ListLeadsFragment.newInstance(ApiClient.BASE_URL, ApiClient.getDbName(), ApiClient.getSchemaName(), ApiClient.applicationId, empId, empName, false);
        }


        if (fragment != null)
            addFragmentToBackStack(fragment);


    }

    public void getAuthString(int id, final ListLeadsActivity.ListLeadCallback listLeadCallback) {
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


    private ArrayList<ProductListModel> itemModelsListProduct;
    private ArrayList<ItemModel> itemModelsListStatus;

    private void getStatusList() {
        int localId = isAdmin ? 0 : empId;
        Call<ArrayList<ItemModel>> call = apiInterface.getStatusForFilter(localId);
        call.enqueue(new Callback<ArrayList<ItemModel>>() {

            @Override
            public void onResponse(Call<ArrayList<ItemModel>> call, Response<ArrayList<ItemModel>> response) {
                if (response.isSuccessful()) {
                    itemModelsListStatus = response.body();
                } else {
                    itemModelsListStatus = new ArrayList<>();
                }
                Constants.setStatuses(itemModelsListStatus);
            }

            @Override
            public void onFailure(Call<ArrayList<ItemModel>> call, Throwable t) {
                itemModelsListProduct = new ArrayList<>();
            }
        });
    }

    private void getProductList() {
        Call<JsonArray> call = apiInterface.getProductList();
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful()) {
                    JsonArray jsonArray = response.body();
                    Type type = new TypeToken<ArrayList<ProductListModel>>() {
                    }.getType();
                    itemModelsListProduct = new Gson().fromJson(jsonArray, type);
                    Constants.setProducts(itemModelsListProduct);
                    getStatusList();
                } else {
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                itemModelsListProduct = new ArrayList<>();
            }
        });
    }

    private void getCityList() {
        int localId = isAdmin ? 0 : empId;
        apiInterface.getCityListForFilter(localId).enqueue(new Callback<ArrayList<City>>() {
            @Override
            public void onResponse(Call<ArrayList<City>> call, Response<ArrayList<City>> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                Constants.setCities(response.body());
            }

            @Override
            public void onFailure(Call<ArrayList<City>> call, Throwable t) {

            }
        });
    }
}
