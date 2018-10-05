package com.wings2aspirations.genericleadcreation.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.models.City;
import com.wings2aspirations.genericleadcreation.models.ItemModel;
import com.wings2aspirations.genericleadcreation.models.LeadDetail;
import com.wings2aspirations.genericleadcreation.models.ProductListModel;
import com.wings2aspirations.genericleadcreation.network.ApiClient;
import com.wings2aspirations.genericleadcreation.network.ApiInterface;
import com.wings2aspirations.genericleadcreation.reciever.GpsStatusListener;
import com.wings2aspirations.genericleadcreation.reciever.LocationUpdatesBroadcastReceiver;
import com.wings2aspirations.genericleadcreation.repository.CalendarHelper;
import com.wings2aspirations.genericleadcreation.repository.Constants;
import com.wings2aspirations.genericleadcreation.repository.DecimalDigitsInputFilter;
import com.wings2aspirations.genericleadcreation.repository.ShowOptionSelectionDialog;
import com.wings2aspirations.genericleadcreation.repository.ShowToast;
import com.wings2aspirations.genericleadcreation.repository.Utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.wings2aspirations.genericleadcreation.repository.ShowOptionSelectionDialog.TYPE_CITY;
import static com.wings2aspirations.genericleadcreation.repository.ShowOptionSelectionDialog.TYPE_PRODUCT;
import static com.wings2aspirations.genericleadcreation.repository.ShowOptionSelectionDialog.TYPE_STATE;
import static com.wings2aspirations.genericleadcreation.repository.ShowOptionSelectionDialog.TYPE_STATUS;

public class AddUpdateLeadActivity extends FragmentActivity implements //OnMapReadyCallback,
        View.OnClickListener,
        ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationUpdatesBroadcastReceiver.ReceiverCallback {
    public static final String EXTRA_ARG_UPDATE_PRIMARY_ID = "argsUpdatePrimaryId";
    public static final String EXTRA_ARG_EMPLOYEE_ID = "argsEmployeeId";
    public static final String EXTRA_ARG_EMPLOYEE_NAME = "argsEmployeeName";
    public static final String EXTRA_ARG_FOLLOW_UP_ID = "argsFollowUpId";

    public static final int REQUEST_CODE_LOCATION_PERMISSION = 2;
    public static final int REQUEST_CODE_STORAGE_CAMERA = 3;
    public static final int REQUEST_CODE_STORAGE = 4;
    private static final int REQUEST_CODE_CALENDAR_WRITE = 123;

    public static final int TAKE_PHOTO_CODE = 1;
    public static final int REQUEST_CHECK_SETTINGS = 4;

    private CalendarHelper calendarHelper;

    private AlertDialog saveAlertDialog;
    private ArrayList<City> cities;
    /*private GoogleMap mMap;
    private CustomMapFragment mSupportMapFragment;*/
    private NestedScrollView nestedScrollView;

    private LocationRequest locationRequest;
    private static RelativeLayout progressLayout;

    private MultipartBody.Part part;

    private Location location;

    private long updateId = -1;
    private long followUpId = -1;

    private ApiInterface apiInterface;

    private TextInputEditText customerNameEt;
    private TextInputEditText contactPersonEt;
    private TextInputEditText emailAddressEt;
    private TextInputEditText mobileNoEt;
    private TextInputEditText addressEt;
    private TextInputEditText pinCodeEt;
    private TextInputEditText customerRemarksEt;
    private TextInputEditText leadRemarksEt;
    private TextInputEditText nextFollowUpDateEt;
    private TextInputEditText nextFollowUpTimeEt, snoozeTimeEt;
    //private TextInputEditText dobDateEt;
    //private TextInputEditText marriageDateEt;
    //private RadioGroup callTypeRg;
    //private RadioButton callTypeHotRb;
    //private RadioButton callTypeColdRb;
    //private RadioButton callTypeWarmRb;
    private TextView productSp, statusSp, unit_sp, cityTv, stateTv;
    private FloatingActionButton cameraBt;
    private TextView fileNameTv;
    private TextInputEditText quantityEt, priceEt;
    private FloatingActionButton saveBt;

    private Spinner timeUnitSp;

    private GoogleApiClient mGoogleApiClient;

    private boolean wasResolutionRequiredCalled;

    private GpsStatusListener gpsStatusListener;

    private String selectedRadioButtonValue;

    private int empId;
    private String empName;

    private boolean isAdmin;

    private File file = null;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

    private LeadDetail leadDetailForDownload;
    private ResponseBody responseBodyForDownload;

    public static List<ProductListModel> itemModelsListProduct;
    public static List<ItemModel> itemModelsListStatus;
    public static List<ItemModel> itemModelsListUnits;

    public static Intent getLeadIntent(Context context, String name, int id) {
        Intent intent = new Intent(context, AddUpdateLeadActivity.class);
        intent.putExtra(EXTRA_ARG_EMPLOYEE_NAME, name);
        intent.putExtra(EXTRA_ARG_EMPLOYEE_ID, id);
        return intent;
    }

    public static Intent getLeadIntent(Context context, String name, int empId, int id, boolean followUp) {
        Intent intent = new Intent(context, AddUpdateLeadActivity.class);
        intent.putExtra(EXTRA_ARG_EMPLOYEE_NAME, name);
        intent.putExtra(EXTRA_ARG_EMPLOYEE_ID, empId);
        intent.putExtra(followUp ? EXTRA_ARG_FOLLOW_UP_ID : EXTRA_ARG_UPDATE_PRIMARY_ID, id);
        return intent;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            removeLocationUpdates();
        }
        if (gpsStatusListener != null) {
            unregisterReceiver(gpsStatusListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient != null) {
            settingsRequest();
        }
        if (gpsStatusListener != null) {
            registerReceiver(gpsStatusListener, new IntentFilter("android.location.PROVIDERS_CHANGED"));
        }
    }

    private void findViews() {
        customerNameEt = (TextInputEditText) findViewById(R.id.customer_name_et);
        contactPersonEt = (TextInputEditText) findViewById(R.id.contact_person_et);
        emailAddressEt = (TextInputEditText) findViewById(R.id.email_address_et);
        mobileNoEt = (TextInputEditText) findViewById(R.id.mobile_no_et);
        addressEt = (TextInputEditText) findViewById(R.id.address_et);
        pinCodeEt = (TextInputEditText) findViewById(R.id.pin_code_et);
        customerRemarksEt = (TextInputEditText) findViewById(R.id.customer_remarks_et);
        leadRemarksEt = (TextInputEditText) findViewById(R.id.lead_remarks_et);
        nextFollowUpDateEt = (TextInputEditText) findViewById(R.id.next_follow_up_date_et);
        nextFollowUpTimeEt = (TextInputEditText) findViewById(R.id.next_follow_up_time_et);
        /*dobDateEt = (TextInputEditText) findViewById(R.id.date_of_birth_et);
        marriageDateEt = (TextInputEditText) findViewById(R.id.marriage_aniv_et);
        callTypeRg = (RadioGroup) findViewById(R.id.call_type_rg);
        callTypeHotRb = (RadioButton) findViewById(R.id.call_type_hot_rb);
        callTypeColdRb = (RadioButton) findViewById(R.id.call_type_cold_rb);
        callTypeWarmRb = (RadioButton) findViewById(R.id.call_type_warm_rb);*/
        cameraBt = (FloatingActionButton) findViewById(R.id.camera_bt);
        fileNameTv = (TextView) findViewById(R.id.file_name_tv);
        saveBt = (FloatingActionButton) findViewById(R.id.save_bt);
        quantityEt = findViewById(R.id.quantity_et);
        priceEt = findViewById(R.id.price_et);

        stateTv = findViewById(R.id.state_tv);
        cityTv = findViewById(R.id.city_tv);
        cityTv.setEnabled(false);

        snoozeTimeEt = findViewById(R.id.snooze_time_et);

        productSp = findViewById(R.id.product_sp);
        statusSp = findViewById(R.id.lead_status_sp);
        unit_sp = findViewById(R.id.unit_sp);
        progressLayout = findViewById(R.id.progress_bar);

        quantityEt.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 3)});
        priceEt.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 2)});

        nextFollowUpDateEt.setHint(simpleDateFormat.format(new Date()));

        Calendar calendar = Calendar.getInstance();
        nextFollowUpTimeEt.setHint(calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));

        timeUnitSp = findViewById(R.id.snooze_time_sp);
        timeUnitSp.setSelection(2);

        /*dobDateEt.setOnClickListener(this);
        marriageDateEt.setOnClickListener(this);
        callTypeHotRb.setOnClickListener(this);
        callTypeColdRb.setOnClickListener(this);
        callTypeWarmRb.setOnClickListener(this);*/
        cameraBt.setOnClickListener(this);
        saveBt.setOnClickListener(this);
        nextFollowUpDateEt.setOnClickListener(this);
        nextFollowUpTimeEt.setOnClickListener(this);

        productSp.setOnClickListener(this);

        statusSp.setOnClickListener(this);

        stateTv.setOnClickListener(this);
        cityTv.setOnClickListener(this);

        leadRemarksEt.setOnTouchListener(touchListener);
        customerRemarksEt.setOnTouchListener(touchListener);
        addressEt.setOnTouchListener(touchListener);

        itemModelsListProduct = new ArrayList<>();
        itemModelsListStatus = new ArrayList<>();
    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_SCROLL:
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onClick(View v) {
        /*if (v == callTypeHotRb) {
            selectedRadioButtonValue = "Hot";
        } else if (v == callTypeColdRb) {
            selectedRadioButtonValue = "Cold";
        } else if (v == callTypeWarmRb) {
            selectedRadioButtonValue = "Warm";
        } else*/
        if (v == cameraBt) {
            checkStorageCameraPermission();
        } else if (v == saveBt) {
            if (!checkValidation())
                return;
            if (!checkDateValidation()) {
                ShowToast.showToast(AddUpdateLeadActivity.this, "Invalid Dob and marriage date");
                return;
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            try {
                Date date = simpleDateFormat.parse(nextFollowUpDateEt.getText().toString() + " " + nextFollowUpTimeEt.getText().toString());
                long millis = 0;
                int snoozeTime = Integer.parseInt(snoozeTimeEt.getText().toString());
                switch (timeUnitSp.getSelectedItemPosition()) {
                    case 0:
                        millis = snoozeTime * 604800000;
                        break;
                    case 1:
                        millis = snoozeTime * 86400000;
                        break;
                    case 2:
                        millis = snoozeTime * 3600000;
                        break;
                    case 3:
                        millis = snoozeTime * 60000;
                        break;
                    case 4:
                        millis = snoozeTime * 1000;
                        break;
                }
                addEvent(new Date(date.getTime() - millis), date, "Follow up with " + customerNameEt.getText().toString(),
                        contactPersonEt.getText().toString() + ", " + mobileNoEt.getText().toString() + " , " + leadRemarksEt.getText().toString(),
                        addressEt.getText().toString(), emailAddressEt.getText().toString(), new CalendarHelper.CalendarCallback() {
                            @Override
                            public void callback(Boolean wasEventInserted, String eventInsertionString) {
                                if (wasEventInserted != null) {
                                    ShowToast.showToast(AddUpdateLeadActivity.this, eventInsertionString);
                                    saveLead();
                                }
                            }
                        });
                calendarHelper.insertEventDirectly(this, REQUEST_CODE_CALENDAR_WRITE);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (v == nextFollowUpDateEt) {
            Utility.showDatePickerDialog(this, nextFollowUpDateEt, null, null, new Date());
        } else if (v == nextFollowUpTimeEt) {
            Utility.showTimePicker(this, nextFollowUpTimeEt);
        } /*else if (v == dobDateEt) {
            Utility.showDatePickerDialog(this, dobDateEt, null, Calendar.getInstance().getTime(), new Date());
        } else if (v == marriageDateEt) {
            Utility.showDatePickerDialog(this, marriageDateEt, null, null, new Date());
        } */ else if (v == productSp) {
            callShowOptionList(TYPE_PRODUCT, itemModelsListProduct, false);
        } else if (v == statusSp) {
            callShowOptionList(TYPE_STATUS, itemModelsListStatus, false);
        } else if (v == cityTv) {
            callShowOptionList(TYPE_CITY, cities, false);
        } else if (v == stateTv) {
            callShowOptionList(TYPE_STATE, Constants.getStates(), false);
        }
    }

    private boolean checkDateValidation() {
        /*Date dobDateEntered = null, marrigeDateEntered = null;
        try {
            dobDateEntered = simpleDateFormat.parse(dobDateEt.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            marrigeDateEntered = simpleDateFormat.parse(marriageDateEt.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (!marrigeDateEntered.after(dobDateEntered))
            return false;*/
        return true;
    }

    public static void showProgress() {
        progressLayout.setVisibility(View.VISIBLE);
    }

    public static void hideProgress() {
        progressLayout.setVisibility(View.GONE);
    }

    public void addEvent(Date fromDate, Date toDate, String title, String description, String location, String emails, CalendarHelper.CalendarCallback calendarCallback) {
        calendarHelper = new CalendarHelper()
                .setContext(this)
                .setDates(fromDate, toDate)
                .setTitleAndDescription(title, description)
                .setLocationAndEmails(location, emails)
                .setCalendarCallback(calendarCallback);
    }

    public void saveLead() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("COMPANY_NAME", customerNameEt.getText().toString());
        jsonObject.addProperty("CONTACT_PERSON", contactPersonEt.getText().toString());
        jsonObject.addProperty("EMAIL", emailAddressEt.getText().toString());
        jsonObject.addProperty("MOBILE_NO", mobileNoEt.getText().toString());
        jsonObject.addProperty("ADDRESS", addressEt.getText().toString());
        jsonObject.addProperty("PIN_CODE", pinCodeEt.getText().toString());
        if (!TextUtils.isEmpty(customerRemarksEt.getText()))
            jsonObject.addProperty("CUSTOMER_REMARKS", customerRemarksEt.getText().toString());
        if (!TextUtils.isEmpty(leadRemarksEt.getText()))
            jsonObject.addProperty("LEAD_REMARKS", leadRemarksEt.getText().toString());
        jsonObject.addProperty("NEXT_FOLLOW_UP_DATE", nextFollowUpDateEt.getText().toString());
        jsonObject.addProperty("NEXT_FOLLOW_UP_TIME", nextFollowUpTimeEt.getText().toString());
        try {
            jsonObject.addProperty("LATITUDE", location.getLatitude());
        } catch (Exception e) {
            jsonObject.addProperty("LATITUDE", 0D);
        }
        try {
            jsonObject.addProperty("LONGITUDE", location.getLongitude());
        } catch (Exception e) {
            jsonObject.addProperty("LONGITUDE", 0D);
        }

        jsonObject.addProperty("CALL_TYPE_ID", (int) statusSp.getTag());
        jsonObject.addProperty("EMP_ID", empId);
        jsonObject.addProperty("EMP_NAME", empName);
        jsonObject.addProperty("DATE_VC", simpleDateFormat.format(new Date()));
        Log.e("AlucarD", productSp.getText().toString());
        jsonObject.addProperty("PRODUCT_ID", (int) productSp.getTag());
        jsonObject.addProperty("PARENT_ID", followUpId);
        /*jsonObject.addProperty("DATE_OF_BIRTH_VC", dobDateEt.getText().toString());
        jsonObject.addProperty("MARRIAGE_DATE_VC", marriageDateEt.getText().toString());*/
        jsonObject.addProperty("CITY_ID", (int) cityTv.getTag());
        jsonObject.addProperty("STATE_ID", (int) stateTv.getTag());
        jsonObject.addProperty("QUANTITY_N", Double.parseDouble(quantityEt.getText().toString()));
        jsonObject.addProperty("PRICE_N", Double.parseDouble(priceEt.getText().toString()));
        if (updateId >= 0) {
            jsonObject.addProperty("ID", updateId);
        } else {
            showProgress();
            Call<JsonObject> call;
            if (part == null)
                call = apiInterface.insertLead(jsonObject);
            else
                call = apiInterface.insertLead(jsonObject, part);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    hideProgress();
                    if (!response.isSuccessful()) {
                        ShowToast.showToast(AddUpdateLeadActivity.this, "Response Insuccessful");
                        return;
                    }
                    ShowToast.showToast(AddUpdateLeadActivity.this, "Success");
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    hideProgress();
                    ShowToast.showToast(AddUpdateLeadActivity.this, t.getMessage());
                }
            });
        }
    }

    public void disableViews() {
        customerNameEt.setEnabled(false);
        contactPersonEt.setEnabled(false);
        emailAddressEt.setEnabled(false);
        mobileNoEt.setEnabled(false);
        addressEt.setEnabled(false);
        pinCodeEt.setEnabled(false);
        customerRemarksEt.setEnabled(false);
        leadRemarksEt.setEnabled(false);
        nextFollowUpDateEt.setEnabled(false);
        nextFollowUpTimeEt.setEnabled(false);
        productSp.setEnabled(false);
        productSp.setOnClickListener(null);
        statusSp.setEnabled(false);
        statusSp.setOnClickListener(null);
        cityTv.setEnabled(false);
        stateTv.setEnabled(false);
        snoozeTimeEt.setEnabled(false);
        timeUnitSp.setEnabled(false);
        quantityEt.setEnabled(false);
        priceEt.setEnabled(false);
      /*  callTypeRg.setEnabled(false);
        callTypeHotRb.setEnabled(false);
        callTypeColdRb.setEnabled(false);
        callTypeWarmRb.setEnabled(false);*/
        cameraBt.setEnabled(false);

        saveBt.setEnabled(false);
        cameraBt.hide();
        saveBt.hide();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_update_lead);

        saveAlertDialog = new AlertDialog.Builder(this)
                .setMessage("Do you want to Exit without saving the Data")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddUpdateLeadActivity.super.onBackPressed();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveBt.callOnClick();
                        dialog.dismiss();
                    }
                })
                .create();

        if (!getIntent().hasExtra(EXTRA_ARG_EMPLOYEE_ID)) {
            ShowToast.showToast(this, "Please Specify Employee Id");
            finish();
            return;
        } else if (!getIntent().hasExtra(EXTRA_ARG_EMPLOYEE_NAME)) {
            ShowToast.showToast(this, "Please Specify Employee Name");
            finish();
            return;
        } else {
            empId = getIntent().getIntExtra(EXTRA_ARG_EMPLOYEE_ID, -1);
            empName = getIntent().getStringExtra(EXTRA_ARG_EMPLOYEE_NAME);
            if (empId <= 0) {
                ShowToast.showToast(this, "Employee Id is <= 0");
                finish();
                return;
            }
            if (TextUtils.isEmpty(empName)) {
                ShowToast.showToast(this, "Employee Name is not given");
                finish();
                return;
            }
        }

        apiInterface = ApiClient.getClient().create(ApiInterface.class);


        gpsStatusListener = new GpsStatusListener(new GpsStatusListener.GpsStatusCallback() {
            @Override
            public void isConnected(boolean isConnected) {
                if (!isAdmin)
                    if (!isConnected && mGoogleApiClient != null) {
                        settingsRequest();
                    }
            }
        });

        findViews();

        if (getIntent().hasExtra(EXTRA_ARG_UPDATE_PRIMARY_ID)) {
            isAdmin = true;
            updateId = getIntent().getIntExtra(EXTRA_ARG_UPDATE_PRIMARY_ID, -1);
            if (updateId <= 0) {
                ShowToast.showToast(this, "Updating id is <= 0");
                finish();
                return;
            }
            if (updateId > 0) {
                showProgress();
                disableViews();
                apiInterface.getLeadById(updateId).enqueue(new Callback<LeadDetail>() {
                    @Override
                    public void onResponse(Call<LeadDetail> call, Response<LeadDetail> response) {
                        hideProgress();
                        if (!response.isSuccessful()) {
                            return;
                        }
                        final LeadDetail leadDetail = response.body();
                        customerNameEt.setText(leadDetail.getCOMPANY_NAME());
                        contactPersonEt.setText(leadDetail.getCONTACT_PERSON());
                        emailAddressEt.setText(leadDetail.getEMAIL());
                        mobileNoEt.setText(leadDetail.getMOBILE_NO());
                        addressEt.setText(leadDetail.getADDRESS());
                        pinCodeEt.setText(leadDetail.getPIN_CODE());
                        customerRemarksEt.setText(leadDetail.getCUSTOMER_REMARKS());
                        leadRemarksEt.setText(leadDetail.getLEAD_REMARKS());
                        nextFollowUpDateEt.setText(leadDetail.getNEXT_FOLLOW_UP_DATE());
                        nextFollowUpTimeEt.setText(leadDetail.getNEXT_FOLLOW_UP_TIME());
                        quantityEt.setText(String.format("%.3f", leadDetail.getQuantity()));
                        priceEt.setText(String.format("%.2f", leadDetail.getPrice()));
                        /*switch (leadDetail.getCALL_TYPE()) {
                         *//* case "Hot":
                                callTypeHotRb.setChecked(true);
                                break;
                            case "Cold":
                                callTypeColdRb.setChecked(true);
                                break;
                            case "Warm":
                                callTypeWarmRb.setChecked(true);
                                break;*//*
                        }*/
                        statusSp.setText(leadDetail.getCALL_TYPE());
                        statusSp.setTag(leadDetail.getCALL_TYPE_ID());

                        productSp.setText(leadDetail.getPRODUCT_NAME_VC());
                        productSp.setTag(leadDetail.getPRODUCT_ID());

                        unit_sp.setText(leadDetail.getUNIT_VC());
                        unit_sp.setTag(leadDetail.getUNIT_ID());

                        cityTv.setText(leadDetail.getCITY_NAME_VC());
                        cityTv.setTag(leadDetail.getCITY_ID());

                        stateTv.setText(leadDetail.getSTATE_NAME_VC());
                        stateTv.setTag(leadDetail.getSTATE_ID());

                        showProgress();
                        apiInterface.getCityList(leadDetail.getSTATE_ID()).enqueue(new Callback<ArrayList<City>>() {
                            @Override
                            public void onResponse(Call<ArrayList<City>> call, Response<ArrayList<City>> response) {
                                hideProgress();
                                if (!response.isSuccessful()) {
                                    return;
                                }
                                cities = response.body();
                            }

                            @Override
                            public void onFailure(Call<ArrayList<City>> call, Throwable t) {
                                hideProgress();
                            }
                        });

                        fileNameTv.setText("Buisness Card.jpg");
                        fileNameTv.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                if (file != null) {
                                    Utility.openFile(AddUpdateLeadActivity.this, file);
                                } else {
                                    showProgress();
                                    apiInterface.getBusinessCard(leadDetail.getID()).enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            hideProgress();
                                            if (!response.isSuccessful()) {
                                                ShowToast.showToast(AddUpdateLeadActivity.this, "No buisness Card Uploaded");
                                                return;
                                            }
                                            checkStoragePermission(leadDetail, response.body());
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            hideProgress();
                                            ShowToast.showToast(AddUpdateLeadActivity.this, "No buisness Card Uploaded");
                                        }
                                    });
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<LeadDetail> call, Throwable t) {
                        hideProgress();
                    }
                });
            }
        }

        if (getIntent().hasExtra(EXTRA_ARG_FOLLOW_UP_ID)) {
            isAdmin = false;
            followUpId = getIntent().getIntExtra(EXTRA_ARG_FOLLOW_UP_ID, -1);
            if (followUpId <= 0) {
                ShowToast.showToast(this, "Follow Up id is <= 0");
                finish();
                return;
            }
            if (followUpId > 0) {
                showProgress();
                apiInterface.getLeadById(followUpId).enqueue(new Callback<LeadDetail>() {
                    @Override
                    public void onResponse(Call<LeadDetail> call, Response<LeadDetail> response) {
                        hideProgress();
                        if (!response.isSuccessful()) {
                            return;
                        }
                        final LeadDetail leadDetail = response.body();
                        customerNameEt.setText(leadDetail.getCOMPANY_NAME());
                        contactPersonEt.setText(leadDetail.getCONTACT_PERSON());
                        emailAddressEt.setText(leadDetail.getEMAIL());
                        mobileNoEt.setText(leadDetail.getMOBILE_NO());
                        addressEt.setText(leadDetail.getADDRESS());
                        pinCodeEt.setText(leadDetail.getPIN_CODE());
                        /*dobDateEt.setText(leadDetail.getDATE_OF_BIRTH_VC());
                        marriageDateEt.setText(leadDetail.getMARRIAGE_DATE_VC());*/
                        cityTv.setText(leadDetail.getCITY_NAME_VC());
                        cityTv.setTag(leadDetail.getCITY_ID());

                        statusSp.setText(leadDetail.getCALL_TYPE());
                        statusSp.setTag(leadDetail.getCALL_TYPE_ID());

                        productSp.setText(leadDetail.getPRODUCT_NAME_VC());
                        productSp.setTag(leadDetail.getPRODUCT_ID());

                        unit_sp.setText(leadDetail.getUNIT_VC());
                        unit_sp.setTag(leadDetail.getUNIT_ID());

                    }

                    @Override
                    public void onFailure(Call<LeadDetail> call, Throwable t) {
                        hideProgress();
                    }
                });
            }
        }

        nestedScrollView = findViewById(R.id.nested_scroll_view_nsv);

        /*mSupportMapFragment = (CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mSupportMapFragment != null)
            mSupportMapFragment.setListener(new CustomMapFragment.OnTouchListener() {
                @Override
                public void onTouch() {
                    nestedScrollView.requestDisallowInterceptTouchEvent(true);
                }
            });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);*/

        if (!isAdmin)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    //Location Permission already granted
                    buildGoogleApiClient();
                } else {
                    //Request Location Permission
                    checkLocationPermission();

                }
            } else {
                buildGoogleApiClient();
            }

        getProductList();
    }

    private void callShowOptionList(final int type, List<? extends ItemModel> itemModelsList, boolean canAdd) {
        ShowOptionSelectionDialog.showDialog(AddUpdateLeadActivity.this, type, canAdd, itemModelsList, new ShowOptionSelectionDialog.OptionSelectionCallBack() {
            @Override
            public void callBack(ItemModel optionSelected) {
                switch (type) {
                    case TYPE_PRODUCT:
                        productSp.setText(optionSelected.getITEMNAME());
                        productSp.setTag(optionSelected.getITEMID());

                        int selectedProductUnitId = 0;

                        for (int i = 0; i < itemModelsListProduct.size(); i++) {
                            if (itemModelsListProduct.get(i).getPRODUCTID() == optionSelected.getITEMID()) {
                                selectedProductUnitId = itemModelsListProduct.get(i).getUNITID();
                                break;
                            }
                        }

                        for (int i = 0; i < itemModelsListUnits.size(); i++) {
                            if (itemModelsListUnits.get(i).getITEMID() == selectedProductUnitId) {
                                unit_sp.setText(itemModelsListUnits.get(i).getITEMNAME());
                                break;
                            }
                        }
                        break;
                    case TYPE_STATUS:
                        statusSp.setText(optionSelected.getITEMNAME());
                        statusSp.setTag(optionSelected.getITEMID());
                        break;
                    case TYPE_CITY:
                        cityTv.setText(optionSelected.getITEMNAME());
                        cityTv.setTag(optionSelected.getITEMID());
                        break;
                    case TYPE_STATE:
                        stateTv.setText(optionSelected.getITEMNAME());
                        stateTv.setTag(optionSelected.getITEMID());
                        cityTv.setText("");
                        showProgress();
                        apiInterface.getCityList(optionSelected.getITEMID()).enqueue(new Callback<ArrayList<City>>() {
                            @Override
                            public void onResponse(Call<ArrayList<City>> call, Response<ArrayList<City>> response) {
                                hideProgress();
                                if (!response.isSuccessful()) {
                                    cityTv.setEnabled(false);
                                    return;
                                }
                                cityTv.setEnabled(true);
                                cities = response.body();
                            }

                            @Override
                            public void onFailure(Call<ArrayList<City>> call, Throwable t) {
                                hideProgress();
                                cityTv.setEnabled(false);
                            }
                        });
                        break;
                }
            }
        });
    }

    private void getProductList() {
        showProgress();
        productSp.setEnabled(false);
        Call<JsonArray> call = apiInterface.getProductList();

        Log.e("AlucarD", call.request().url().toString());
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {

                hideProgress();

                if (response.isSuccessful()) {

                    productSp.setEnabled(true);

                    JsonArray jsonArray = response.body();
                    Type type = new TypeToken<List<ProductListModel>>() {
                    }.getType();

                    itemModelsListProduct = new Gson().fromJson(jsonArray, type);

                } else {
                    ShowToast.showToast(AddUpdateLeadActivity.this, "Failed to get Products");
                }

                getStatusList();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                hideProgress();
                ShowToast.showToast(AddUpdateLeadActivity.this, "In Failure to get Products");
            }
        });
    }

    private void getStatusList() {
        statusSp.setEnabled(false);
        Call<JsonArray> call = apiInterface.getStatusListByDepartment(empId);

        Log.e("AlucarD", call.request().url().toString());
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {

                hideProgress();

                if (response.isSuccessful()) {

                    statusSp.setEnabled(true);

                    JsonArray jsonArray = response.body();
                    Type type = new TypeToken<List<ItemModel>>() {
                    }.getType();

                    itemModelsListStatus = new Gson().fromJson(jsonArray, type);

                    getUnitList();

                } else {
                    ShowToast.showToast(AddUpdateLeadActivity.this, "Failed to get Status");
                }


            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                hideProgress();
                ShowToast.showToast(AddUpdateLeadActivity.this, "In Failure to get Products");
            }
        });
    }

    private void getUnitList() {

        unit_sp.setEnabled(false);
        Call<JsonArray> call = apiInterface.getUnits();

        Log.e("AlucarD", call.request().url().toString());
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {

                hideProgress();

                if (response.isSuccessful()) {

                    unit_sp.setEnabled(true);

                    JsonArray jsonArray = response.body();
                    Type type = new TypeToken<List<ItemModel>>() {
                    }.getType();

                    itemModelsListUnits = new Gson().fromJson(jsonArray, type);


                } else {
                    ShowToast.showToast(AddUpdateLeadActivity.this, "Failed to get Units");
                }


            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                hideProgress();
                ShowToast.showToast(AddUpdateLeadActivity.this, "In Failure to get Products");
            }
        });
    }

    private File downloadFile(LeadDetail leadDetail, ResponseBody response) {
        if (leadDetail == null || response == null)
            return null;
        try {
            file = new File(Environment.getExternalStorageDirectory() + File.separator + "leads_" + leadDetail.getID() + ".jpg");

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                inputStream = response.byteStream();
                outputStream = new FileOutputStream(file);

                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                }

                outputStream.flush();

                Utility.openFile(AddUpdateLeadActivity.this, file);
                return file;
            } catch (IOException e) {
                ShowToast.showToast(AddUpdateLeadActivity.this, "No buisness Card Uploaded");
                return null;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            ShowToast.showToast(AddUpdateLeadActivity.this, "No buisness Card Uploaded");
            return null;
        }
    }

    private void checkStorageCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_STORAGE_CAMERA);
        } else {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                Uri photoURI = Utility.getUriType(this, photoFile);

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
            }
        }
    }

    private void checkStoragePermission(LeadDetail leadDetail, ResponseBody response) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            leadDetailForDownload = leadDetail;
            responseBodyForDownload = response;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_STORAGE);
        } else {
            downloadFile(leadDetail, response);
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION);
        }
    }

    /*@Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK) {
            File file = new File(mCurrentPhotoPath);
            getBody(Utility.getUriType(this, file));
        }
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            wasResolutionRequiredCalled = false;
            switch (resultCode) {
                case Activity.RESULT_OK:
                    requestLocationUpdates();
                    break;
                case Activity.RESULT_CANCELED:
                    try {
                        ShowToast.showToast(this, "You can't use this app without turn ON location.");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            this.finishAffinity();
                        }
                    } catch (Exception e) {

                    }
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mGoogleApiClient == null) {
                        buildGoogleApiClient();
                    }
                } else {
                    ShowToast.showToast(this, "Require location permission to function this App.");
                    openAppSettings();
                }
                break;
            case REQUEST_CODE_STORAGE_CAMERA:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ShowToast.showToast(this, "Require Camera and Storage to function this App.");
                    openAppSettings();
                } else {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                    }
                    if (photoFile != null) {
                        Uri photoURI = Utility.getUriType(this, photoFile);

                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
                    }
                }
                break;
            case REQUEST_CODE_STORAGE:
                downloadFile(leadDetailForDownload, responseBodyForDownload);
                leadDetailForDownload = null;
                responseBodyForDownload = null;
                break;
            case REQUEST_CODE_CALENDAR_WRITE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    calendarHelper.insertEventDirectly(this, REQUEST_CODE_CALENDAR_WRITE);
                else {
                    ShowToast.showToast(this, "This lead will not be added to your Calendar");
                    saveLead();
                }
                break;
        }
    }

    void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /*public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Utility.getUriType(this, inImage);
    }*/

    private String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        File image = new File(Environment.getExternalStorageDirectory() + File.separator + "buisnessCard.jpg");

        if (image.exists()) {
            image.delete();
        }

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void getBody(Uri uri) {
        File file = new File(mCurrentPhotoPath);
        RequestBody requestFile =
                RequestBody.create(MediaType.parse(getContentResolver().getType(uri)), file);

        String fileName = file.getName();
        fileNameTv.setText(fileName);
        part = MultipartBody.Part.createFormData("BusinessCard", UUID.randomUUID().toString(), requestFile);
    }

    public void requestLocationUpdates() {
        try {
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, getPendingIntent());
        } catch (SecurityException e) {
        }
    }

    public void removeLocationUpdates() {
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(getPendingIntent());
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        LocationUpdatesBroadcastReceiver.setCallback(this);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void settingsRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1 * 1000);
        locationRequest.setFastestInterval(2 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        Task<LocationSettingsResponse> task =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    requestLocationUpdates();
                } catch (ApiException exception) {
                    if (wasResolutionRequiredCalled) {
                        return;
                    }
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                wasResolutionRequiredCalled = true;
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                resolvable.startResolutionForResult(
                                        AddUpdateLeadActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                            } catch (ClassCastException e) {
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            }
        });
    }

    private void buildGoogleApiClient() {
        if (mGoogleApiClient != null) {
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API)
                .build();

        settingsRequest();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void hasLocationAvailability(LocationAvailability locationAvailability) {

    }

    @Override
    public void hasLocationResult(LocationResult locationResult) {
        location = locationResult.getLastLocation();

        /*LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map
                .zoom(18)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));*/
    }

    public boolean checkValidation() {
        customerNameEt.setError(null);
        contactPersonEt.setError(null);
        emailAddressEt.setError(null);
        mobileNoEt.setError(null);
        addressEt.setError(null);
        pinCodeEt.setError(null);
        nextFollowUpDateEt.setError(null);
        nextFollowUpTimeEt.setError(null);
        cityTv.setError(null);
        quantityEt.setError(null);
        priceEt.setError(null);

        if (isEmpty(customerNameEt)) {
            customerNameEt.setError("Required");
            return false;
        }
        if (isEmpty(contactPersonEt)) {
            contactPersonEt.setError("Required");
            return false;
        }
        if (isEmpty(emailAddressEt)) {
            emailAddressEt.setError("Required");
            return false;
        }
        if (isEmpty(mobileNoEt)) {
            mobileNoEt.setError("Required");
            return false;
        }
        if (isEmpty(addressEt)) {
            addressEt.setError("Required");
            return false;
        }
        if (isEmpty(pinCodeEt)) {
            pinCodeEt.setError("Required");
            return false;
        }
        if (isEmpty(nextFollowUpDateEt)) {
            nextFollowUpDateEt.setError("Required");
            return false;
        }
        if (isEmpty(nextFollowUpTimeEt)) {
            nextFollowUpTimeEt.setError("Required");
            return false;
        }
        if (isEmpty(quantityEt)) {
            quantityEt.setError("Required");
            return false;
        }
        if (isEmpty(priceEt)) {
            priceEt.setError("Required");
            return false;
        }
        if (TextUtils.isEmpty(stateTv.getText())) {
            stateTv.setError("Required");
            return false;
        }
        if (TextUtils.isEmpty(cityTv.getText())) {
            cityTv.setError("Required");
            return false;
        }
        if (TextUtils.isEmpty(statusSp.getText())) {
            ShowToast.showToast(AddUpdateLeadActivity.this, R.string.select_call_type_error);
            return false;
        }
        if (TextUtils.isEmpty(productSp.getText().toString())) {
            ShowToast.showToast(AddUpdateLeadActivity.this, R.string.select_product_error);
            return false;
        }
        double quantity = Double.parseDouble(quantityEt.getText().toString());
        if (quantity == 0D) {
            quantityEt.setError("Quantity cannot be Zero");
            return false;
        }
        double price = Double.parseDouble(priceEt.getText().toString());
        if (price == 0D) {
            priceEt.setError("Price cannot be Zero");
            return false;
        }
        if (mobileNoEt.getText().length() != 10) {
            mobileNoEt.setError("Enter a Valid Number");
            focusView(mobileNoEt);
            return false;
        }
        if (pinCodeEt.getText().length() != 6) {
            pinCodeEt.setError("Enter a Valid Pin Code");
            focusView(pinCodeEt);
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddressEt.getText()).matches()) {
            emailAddressEt.setError("Not Valid Email ID");
            focusView(emailAddressEt);
            return false;
        }
        return true;
    }

    private boolean isEmpty(TextInputEditText textInputEditText) {
        boolean isEmpty = TextUtils.isEmpty(textInputEditText.getText());
        if (isEmpty) {
            focusView(textInputEditText);
        }
        return isEmpty;
    }

    private void focusView(TextInputEditText textInputEditText) {
        textInputEditText.getParent().requestChildFocus(textInputEditText, textInputEditText);
    }

    private boolean doesNotNeedSaving() {
        return isEmpty(customerNameEt) &&
                isEmpty(contactPersonEt) &&
                isEmpty(emailAddressEt) &&
                isEmpty(mobileNoEt) &&
                isEmpty(addressEt) &&
                isEmpty(pinCodeEt) &&
                isEmpty(nextFollowUpDateEt) &&
                isEmpty(nextFollowUpTimeEt) &&
                TextUtils.isEmpty(statusSp.getText()) &&
                TextUtils.isEmpty(productSp.getText()) &&
                TextUtils.isEmpty(cityTv.getText());
    }

    @Override
    public void onBackPressed() {
        if (doesNotNeedSaving() || isAdmin) {
            super.onBackPressed();
        } else {
            saveAlertDialog.show();
        }
    }
}