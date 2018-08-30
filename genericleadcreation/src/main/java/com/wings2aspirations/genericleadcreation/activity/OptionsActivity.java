package com.wings2aspirations.genericleadcreation.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.models.AuthorisationToken;
import com.wings2aspirations.genericleadcreation.models.City;
import com.wings2aspirations.genericleadcreation.network.ApiClient;
import com.wings2aspirations.genericleadcreation.network.ApiInterface;
import com.wings2aspirations.genericleadcreation.repository.Constants;
import com.wings2aspirations.genericleadcreation.repository.ShowToast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.wings2aspirations.genericleadcreation.activity.AddUpdateLeadActivity.EXTRA_ARG_EMPLOYEE_ID;
import static com.wings2aspirations.genericleadcreation.activity.AddUpdateLeadActivity.EXTRA_ARG_EMPLOYEE_NAME;

public class OptionsActivity extends AppCompatActivity {
    public static final String EXTRA_BASE_URL = "baseUrlForRetrofit";
    public static final String EXTRA_EMP_NAMES = "employeeNames";
    public static final String EXTRA_DB_NAME = "dbName";
    public static final String EXTRA_SCHEMA_NAME = "schemaName";
    public static final String EXTRA_APPLICATION_ID = "applicationId";

    public static final int SESSION_AUTHORIZATION_TOKEN_OFFSET = 12;

    private ApiInterface apiInterface;
    private int empId;
    private String empName;

    public static Intent getListLeadsIntent(Context context, String baseUrl, String dbName, String schemaName, String applicationId, int id, ArrayList<String> empNames) {
        Intent intent = new Intent(context, OptionsActivity.class);
        intent.putExtra(EXTRA_BASE_URL, baseUrl);
        intent.putExtra(EXTRA_ARG_EMPLOYEE_ID, id);
        intent.putExtra(EXTRA_EMP_NAMES, empNames);
        intent.putExtra(EXTRA_DB_NAME, dbName);
        intent.putExtra(EXTRA_SCHEMA_NAME, schemaName);
        intent.putExtra(EXTRA_APPLICATION_ID, applicationId);
        return intent;
    }

    public static Intent getListLeadsIntent(Context context, String baseUrl, String dbName, String schemaName, String applicationId, int empId, String empName) {
        Intent intent = new Intent(context, OptionsActivity.class);
        intent.putExtra(EXTRA_ARG_EMPLOYEE_NAME, empName);
        intent.putExtra(EXTRA_ARG_EMPLOYEE_ID, empId);
        intent.putExtra(EXTRA_BASE_URL, baseUrl);
        intent.putExtra(EXTRA_DB_NAME, dbName);
        intent.putExtra(EXTRA_SCHEMA_NAME, schemaName);
        intent.putExtra(EXTRA_APPLICATION_ID, applicationId);
        return intent;
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_leads);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (TextUtils.isEmpty(preferences.getString(getString(R.string.key_remind_at_time), "")))
            preferences.edit().putString(getString(R.string.key_remind_at_time), "18:00").commit();

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

        if (!getIntent().hasExtra(EXTRA_ARG_EMPLOYEE_ID)) {
            ShowToast.showToast(this, "Please Specify ID");
            finish();
            return;
        }

        ApiClient.setId(getIntent().getIntExtra(EXTRA_ARG_EMPLOYEE_ID, -1));
        ApiClient.setBaseUrl(getIntent().getStringExtra(EXTRA_BASE_URL));
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        getAuthString(empId, new ListLeadsActivity.ListLeadCallback() {
            @Override
            public void callback() {
                apiInterface.getCityList().enqueue(new Callback<List<City>>() {
                    @Override
                    public void onResponse(Call<List<City>> call, Response<List<City>> response) {
                        if (!response.isSuccessful()) {
                            return;
                        }
                        Constants.setCities(response.body());
                    }

                    @Override
                    public void onFailure(Call<List<City>> call, Throwable t) {

                    }
                });
            }
        });
    }
}
