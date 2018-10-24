package com.wings2aspirations.genericleadcreation.fragment;

import android.content.DialogInterface;
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
import com.wings2aspirations.genericleadcreation.activity.MainActivity;
import com.wings2aspirations.genericleadcreation.activity.ViewLeadActivity;
import com.wings2aspirations.genericleadcreation.adapter.LeadMeetReportAdapter;
import com.wings2aspirations.genericleadcreation.models.ItemModel;
import com.wings2aspirations.genericleadcreation.models.LeadDetail;
import com.wings2aspirations.genericleadcreation.network.ApiClient;
import com.wings2aspirations.genericleadcreation.network.ApiInterface;
import com.wings2aspirations.genericleadcreation.repository.Constants;
import com.wings2aspirations.genericleadcreation.repository.ExcelCreator;
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

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class LeadMeetingReportFragment extends Fragment implements LeadMeetReportAdapter.ProgressCallback {
    public static final int RESPONSE_CODE_ADD_LEAD_ACTIVITY = 14;

    private static final String ARG_IS_LEAD_REPORT = "argIsLeadReport";
    private static final String ARG_EMP_ID = "argEmployeeId";
    private static final String ARG_IS_ADMIN = "argIsAdmin";
    private static final String ARG_IS_FOR_REPORT = "argIsForReport";
    public static final String ARG_EMP_NAMES = "employeeNames";

    private boolean isLeadReport;
    private AlertDialog formatDialog;
    private EditText fromDateEt, toDateEt;

    private Date fromDate, toDate;
    private FloatingActionButton leadFilterFab;
    private LeadMeetReportAdapter tabAdapter, cardAdapter;
    private RelativeLayout progressLayout;
    private RecyclerView tabRecyclerView, cardRecyclerView;
    private ApiInterface apiInterface;
    private List<LeadDetail> details;
    private ArrayList<String> empNames;
    private List<LeadDetail> refineDetails;

    private HashSet<Integer> productHash, statusHash, cityHash;

    private int selectedResourceId;

    private FilterSheetFragment filterSheetFragment;
    private Spinner spinner, demoFilterSpinner, existingCustomerFilterSpinner;

    private LinearLayout adminEmployeeFilterLl;

    private String selectedEmpName = "";
    private int empId;
    private boolean isAdmin;
    private boolean isOnlyForReports;

    private LinearLayout filterViewReport;
    private HorizontalScrollView horizontalScrollView;

    private HashSet<Integer>[] hashSets;
    private ArrayList<ItemModel>[] itemModels;

    public AlertDialog leadFileOptionDialog;

    public static LeadMeetingReportFragment newInstance(boolean isLeadReport, int empId, boolean isAdmin, ArrayList<String> empNames, boolean isOnlyForReports) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_LEAD_REPORT, isLeadReport);
        args.putInt(ARG_EMP_ID, empId);
        args.putBoolean(ARG_IS_ADMIN, isAdmin);
        args.putBoolean(ARG_IS_FOR_REPORT, isOnlyForReports);
        args.putSerializable(ARG_EMP_NAMES, empNames);

        LeadMeetingReportFragment fragment = new LeadMeetingReportFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void getValues() {
        Bundle args = getArguments();
        isLeadReport = args.getBoolean(ARG_IS_LEAD_REPORT);
        empId = args.getInt(ARG_EMP_ID);
        isAdmin = args.getBoolean(ARG_IS_ADMIN);
        isOnlyForReports = args.getBoolean(ARG_IS_FOR_REPORT);
        empNames = (ArrayList<String>) args.getSerializable(ARG_EMP_NAMES);
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
        setHasOptionsMenu(true);
        createFormatDialog();
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lead_meeting_report, container, false);
        fromDateEt = view.findViewById(R.id.from_date_et);
        toDateEt = view.findViewById(R.id.to_date_et);
        cardRecyclerView = view.findViewById(R.id.card_recycler_view);
        tabRecyclerView = view.findViewById(R.id.tab_recycler_view);
        progressLayout = view.findViewById(R.id.progress_bar);
        leadFilterFab = view.findViewById(R.id.lead_filter_fab);
        spinner = view.findViewById(R.id.spinner);
        adminEmployeeFilterLl = view.findViewById(R.id.admin_employee_filter_ll);
        demoFilterSpinner = view.findViewById(R.id.demo_filter_spinner);
        existingCustomerFilterSpinner = view.findViewById(R.id.existing_customer_filter_spinner);
        filterViewReport = view.findViewById(R.id.filter_view_report);
        horizontalScrollView = view.findViewById(R.id.horizontal_scroll_view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        try {
            ((MainActivity) getActivity()).setActionBarTitle((isLeadReport ? "Lead" : "Meeting") + " Report");
        } catch (Exception e) {

        }

        try {
            ((ViewLeadActivity) getActivity()).setActionBarTitle((isLeadReport ? "Lead" : "Meeting") + " Report");
        } catch (Exception e) {

        }

        if (isOnlyForReports) {
            filterViewReport.setVisibility(View.GONE);
        } else {
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
                        try {
                            if (selectedResourceId == R.layout.item_leads_tab) {
                                tabAdapter.getFilter().filter(selectedEmpName);
                            } else if (selectedResourceId == R.layout.item_leads_card) {
                                cardAdapter.getFilter().filter(selectedEmpName);
                            }
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            } else {
                spinner.setVisibility(View.GONE);
                adminEmployeeFilterLl.setVisibility(View.GONE);
            }

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
                        if (selectedResourceId == R.layout.item_leads_tab) {
                            tabAdapter.setDate(fromDate, toDate, selectedEmpName);
                        } else if (selectedResourceId == R.layout.item_leads_card) {
                            cardAdapter.setDate(fromDate, toDate, selectedEmpName);
                        }
                    } catch (Exception e) {
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
                        toDate = MainActivity.simpleDateFormat.parse(toDateEt.getText().toString());
                        if (selectedResourceId == R.layout.item_leads_tab) {
                            tabAdapter.setDate(fromDate, toDate, selectedEmpName);
                        } else if (selectedResourceId == R.layout.item_leads_card) {
                            cardAdapter.setDate(fromDate, toDate, selectedEmpName);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

        }

        tabRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        cardRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

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
                        if (tabAdapter != null && selectedResourceId == R.layout.item_leads_tab) {
                            tabAdapter.getFilter().filter(selectedEmpName);
                        } else if (cardAdapter != null && selectedResourceId == R.layout.item_leads_card) {
                            cardAdapter.getFilter().filter(selectedEmpName);
                        }
                    }
                });
                filterSheetFragment.show(getActivity().getSupportFragmentManager(), "Filter");
            }
        });

        if (formatDialog != null) {
            formatDialog.show();
        } else {
            selectedResourceId = R.layout.item_leads_card;
            switchLayout(false);
            getLeadsList();
        }

    }

    public void getLeadsList() {
        showProgress();
        Call<JsonArray> call = null;
        if (!isAdmin)
            call = apiInterface.getAllLeads(empId);
        else
            call = apiInterface.getAllLeads();
        call.enqueue(new Callback<JsonArray>() {
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

        tabAdapter = new LeadMeetReportAdapter(refineDetails, getActivity(), hashSets, isAdmin, isLeadReport, this, new LeadMeetReportAdapter.LeadOnClickCallBack() {
            @Override
            public void callback(LeadDetail leadDetail) {
                TrailFragment fragment = TrailFragment.newInstance(leadDetail.getCHILD_FOLLOW_UP_ID(), leadDetail.getID(), leadDetail.getEMP_NAME(), leadDetail.getEMP_ID(), true);
                ((FragmentActivity) getActivity()).
                        getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_frame, fragment, fragment.getClass().getSimpleName())
                        .addToBackStack(null)
                        .commit();

            }
        }, R.layout.item_leads_tab);
        cardAdapter = new LeadMeetReportAdapter(refineDetails, getActivity(), hashSets, isAdmin, isLeadReport, this, new LeadMeetReportAdapter.LeadOnClickCallBack() {
            @Override
            public void callback(LeadDetail leadDetail) {
                TrailFragment fragment = TrailFragment.newInstance(leadDetail.getCHILD_FOLLOW_UP_ID(), leadDetail.getID(), leadDetail.getEMP_NAME(), leadDetail.getEMP_ID(), true);
                ((FragmentActivity) getActivity()).
                        getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_frame, fragment, fragment.getClass().getSimpleName())
                        .addToBackStack(null)
                        .commit();

            }
        }, R.layout.item_leads_card);

        cardRecyclerView.setAdapter(cardAdapter);
        tabRecyclerView.setAdapter(tabAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isAdmin)
            inflater.inflate(R.menu.menu_share, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_share) {

            if (isAdmin) {
                showCreateOrSendExcelMailOptionDialog();
            }
        }
        return true;

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
                "EMP NAME",
                "COMPANY NAME",
                "CONTACT PERSON",
                "DESIGNATION",
                "EMAIL",
                "MOBILE NO",
                "ADDRESS",
                "PIN CODE",
                "CUSTOMER REMARKS",
                "DEMO",
                "LEAD REMARKS",
                "NEXT FOLLOW UP DATE",
                "NEXT FOLLOW UP TIME",
                "LEAD TYPE",
                "PRODUCT",
                "UNIT",
                "QUANTITY",
                "PRICE PER UNIT",
                "TOTAL ORDER VALUE",
                "STATE",
                "CITY",
                "CREATION DATE",
                "CUSTOMER TYPE"};

        for (int i = 0; i < details.size(); i++) {
            columnRowData[i + 1] = details.get(i).getColumnData();
        }

        final String fileName;
        if (Constants.getCompanyName().length() > 5)
            fileName = Constants.getCompanyName().substring(0, 5) +
                    Constants.simpleDateFormat.format(new Date());
        else
            fileName = Constants.getCompanyName() +
                    Constants.simpleDateFormat.format(new Date());

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

    private void switchLayout(boolean showHorizontal) {
        horizontalScrollView.setVisibility(showHorizontal ? View.VISIBLE : View.GONE);
        cardRecyclerView.setVisibility(showHorizontal ? View.GONE : View.VISIBLE);
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