package com.wings2aspirations.genericleadcreation.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.activity.MainActivity;
import com.wings2aspirations.genericleadcreation.adapter.OptionItemAdapter;
import com.wings2aspirations.genericleadcreation.adapter.StatusListAdapter;
import com.wings2aspirations.genericleadcreation.models.ItemModel;
import com.wings2aspirations.genericleadcreation.models.ProductListModel;
import com.wings2aspirations.genericleadcreation.models.StatusModel;
import com.wings2aspirations.genericleadcreation.network.ApiClient;
import com.wings2aspirations.genericleadcreation.network.ApiInterface;
import com.wings2aspirations.genericleadcreation.repository.ShowOptionSelectionDialog;
import com.wings2aspirations.genericleadcreation.repository.ShowToast;
import com.wings2aspirations.genericleadcreation.repository.Utility;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.wings2aspirations.genericleadcreation.repository.ShowOptionSelectionDialog.TYPE_DEPARTMENT;
import static com.wings2aspirations.genericleadcreation.repository.ShowOptionSelectionDialog.TYPE_STATUS;
import static com.wings2aspirations.genericleadcreation.repository.ShowOptionSelectionDialog.TYPE_UNIT;
import static com.wings2aspirations.genericleadcreation.repository.ShowOptionSelectionDialog.MyTextWatcher;
import static com.wings2aspirations.genericleadcreation.repository.ShowOptionSelectionDialog.emptyFieldValidation;

public class ProductStatusFragment extends Fragment implements View.OnClickListener {
    public static final String ARG_IS_PRODUCT = "arg_is_prod";

    private LinearLayout showListContainer;
    private EditText search_option_item;
    private RecyclerView item_list_rv;
    private FloatingActionButton fab_add_item;
    private TextView item_tag, no_item_found;
    private RelativeLayout progressContainer;
    private ApiInterface apiInterface;
    private boolean isProduct;
    public List<ProductListModel> itemModelsListProduct;
    public List<StatusModel> itemModelsListStatus;
    public List<ItemModel> itemModelsListDeparment;
    public OptionItemAdapter optionItemAdapter;
    public OptionItemAdapter unitOptionItemAdapter;
    public List<ItemModel> itemModelsListUnits;

    private StatusListAdapter statusListAdapter;

    public static ProductStatusFragment newInstance(boolean isProduct) {

        Bundle args = new Bundle();

        args.putBoolean(ARG_IS_PRODUCT, isProduct);

        ProductStatusFragment fragment = new ProductStatusFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void getValue() {

        Bundle args = getArguments();

        isProduct = args.getBoolean(ARG_IS_PRODUCT);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getValue();
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.product_status_fragment_layout, container, false);
        search_option_item = view.findViewById(R.id.search_option_item);
        item_list_rv = view.findViewById(R.id.item_list_rv);
        fab_add_item = view.findViewById(R.id.fab_add_item);
        showListContainer = view.findViewById(R.id.showListContainer);
        no_item_found = view.findViewById(R.id.no_item_found);
        progressContainer = view.findViewById(R.id.progress_bar);

        fab_add_item.setOnClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        item_list_rv.setLayoutManager(linearLayoutManager);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        try {
            ((MainActivity) getActivity()).setActionBarTitle("Available " + (isProduct ? "Products" : "Status") + " List");
        } catch (Exception e) {

        }

        if (isProduct) {

            no_item_found.setText("No Product found");
            getUnitList(false);
        } else {

            no_item_found.setText("No Status found");
            getDepartmentList();
        }


        search_option_item.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (optionItemAdapter != null)
                    optionItemAdapter.getFilter().filter(charSequence);
                else if (!isProduct && statusListAdapter != null)
                    statusListAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void getDepartmentList() {
        showProgress();
        Call<JsonArray> call = apiInterface.getDepartmentList();

        Log.e("AlucarD", call.request().url().toString());
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {

                hideProgress();

                if (response.isSuccessful()) {


                    JsonArray jsonArray = response.body();
                    if (jsonArray != null || jsonArray.size() > 0) {
                        Type type = new TypeToken<List<ItemModel>>() {
                        }.getType();

                        itemModelsListDeparment = new Gson().fromJson(jsonArray, type);


                        getStatusList(false);

                    }

                } else {
                    ShowToast.showToast(getActivity(), "Failed to get Department");
                }


            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                hideProgress();
                ShowToast.showToast(getActivity(), "In Failure to get Department");
            }
        });
    }


    @Override
    public void onClick(View view) {


        int i = view.getId();
        if (i == R.id.fab_add_item) {
            if (isProduct) {
                showAddProductLayoutDialog(new AddProductCallBack() {
                    @Override
                    public void callBack(String productName, int unitId, double productPrice) {
                        showProgress();
                        callAddProductApi(productName, unitId, productPrice);
                    }
                });
            } else {
                showAddSingleFieldDialog(TYPE_STATUS, new AddSingleFieldCallBack() {
                    @Override
                    public void callBack(String NAME_VC, int WHICH_TO_ADD) {
                        showProgress();
                        callAddDetailsApi(NAME_VC, WHICH_TO_ADD);
                    }
                });
            }

        }
    }


    private void getUnitList(final boolean isCallAfterAdd) {

        showProgress();
        Call<JsonArray> call = apiInterface.getUnits();

        Log.e("AlucarD", call.request().url().toString());
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {

                hideProgress();

                if (response.isSuccessful()) {


                    JsonArray jsonArray = response.body();

                    if (jsonArray != null && jsonArray.size() > 0) {
                        Type type = new TypeToken<List<ItemModel>>() {
                        }.getType();

                        itemModelsListUnits = new Gson().fromJson(jsonArray, type);
                        if (!isCallAfterAdd) {


                            getProductList(false);
                        } else {
                            if (unitOptionItemAdapter != null)
                                unitOptionItemAdapter.setItemList(itemModelsListUnits);
                        }


                    }
                } else {
                    ShowToast.showToast(getActivity(), "Failed to get Products");
                }


            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                hideProgress();
                ShowToast.showToast(getActivity(), "In Failure to get Products");
            }
        });
    }

    private void getProductList(final boolean isCallAfterAdd) {
        showProgress();

        Call<JsonArray> call = apiInterface.getProductList();

        Log.e("AlucarD", call.request().url().toString());
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {

                hideProgress();

                if (response.isSuccessful()) {


                    JsonArray jsonArray = response.body();
                    if (jsonArray != null || jsonArray.size() > 0) {
                        Type type = new TypeToken<List<ProductListModel>>() {
                        }.getType();

                        itemModelsListProduct = new Gson().fromJson(jsonArray, type);

                        if (!isCallAfterAdd)
                            setAdapterFunction(itemModelsListProduct);
                        else
                            optionItemAdapter.setItemList(itemModelsListProduct);
                    } else
                        no_item_found.setVisibility(View.VISIBLE);

                } else {
                    ShowToast.showToast(getActivity(), "Failed to get Products");
                }


            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                hideProgress();
                ShowToast.showToast(getActivity(), "In Failure to get Products");
            }
        });
    }

    private void getStatusList(final boolean isCallAfterAdd) {

        showProgress();
        Call<JsonArray> call = apiInterface.getStatusList();

        Log.e("AlucarD", call.request().url().toString());
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {

                hideProgress();

                if (response.isSuccessful()) {


                    JsonArray jsonArray = response.body();
                    if (jsonArray != null || jsonArray.size() > 0) {
                        Type type = new TypeToken<List<StatusModel>>() {
                        }.getType();

                        itemModelsListStatus = new Gson().fromJson(jsonArray, type);


                        if (!isCallAfterAdd) {
                            statusListAdapter = new StatusListAdapter(getActivity(), itemModelsListStatus, null);
                            item_list_rv.setAdapter(statusListAdapter);
                        } else
                            statusListAdapter.setItemList(itemModelsListStatus);
                    } else
                        no_item_found.setVisibility(View.VISIBLE);

                } else {
                    ShowToast.showToast(getActivity(), "Failed to get Products");
                }


            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                hideProgress();
                ShowToast.showToast(getActivity(), "In Failure to get Products");
            }
        });
    }

    private void setAdapterFunction(List<? extends ItemModel> itemModelsList) {

        optionItemAdapter = new OptionItemAdapter(getActivity(), itemModelsList, null);
        item_list_rv.setAdapter(optionItemAdapter);
    }

    private void showProgress() {
        progressContainer.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progressContainer.setVisibility(View.GONE);
    }


    private List<View> validateViewList;
    private int selectedUnitId;
    private int selectedDepartmentId;

    public BottomSheetDialog globalAddProductDialog;

    public interface AddProductCallBack {
        void callBack(String productName, int unitId, double productPrice);
    }

    public interface OptionSelectionCallBack {
        void callBack(ItemModel optionSelected);
    }


    private void showAddProductLayoutDialog(final AddProductCallBack addProductCallBack) {

        validateViewList = new ArrayList<View>();
        selectedUnitId = -1;

        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.add_product_layout, null);

        LinearLayout add_product_container = view.findViewById(R.id.add_product_container);

        final TextInputEditText product_name_et = view.findViewById(R.id.product_name_et);
        final TextView unit_tv = view.findViewById(R.id.unit_tv);
        final TextInputEditText product_price_et = view.findViewById(R.id.product_price_et);

        unit_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUnitListDialog(TYPE_UNIT, new UnitSelectionCallBack() {
                    @Override
                    public void callBack(ItemModel itemModel) {

                        selectedUnitId = itemModel.getITEMID();
                        unit_tv.setText(itemModel.getITEMNAME());
                    }
                });

            }
        });

        TextView addProduct = view.findViewById(R.id.addProduct);

        for (int i = 0; i < add_product_container.getChildCount(); i++) {
            View currentView = add_product_container.getChildAt(i);

            //if view is TextInputLayout then adding addTextChangeListener on its editText
            if (currentView instanceof TextInputLayout) {
                EditText currentViewEditText = ((TextInputLayout) currentView).getEditText();


                currentViewEditText.addTextChangedListener(new MyTextWatcher(currentViewEditText, (TextInputLayout) currentView, getActivity()));
            }

            validateViewList.add(currentView);
        }

        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (emptyFieldValidation(validateViewList)) {
                    if (!TextUtils.isEmpty(unit_tv.getText().toString())) {
                        globalAddProductDialog.dismiss();
                        addProductCallBack.callBack(product_name_et.getText().toString().trim(), selectedUnitId, Double.parseDouble(product_price_et.getText().toString()));
                    } else
                        ShowToast.showToast(getActivity(), "Please select unit");
                }
            }
        });

        globalAddProductDialog = new BottomSheetDialog(getActivity());
        globalAddProductDialog.setContentView(view);

        globalAddProductDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        globalAddProductDialog.show();
    }

    private interface UnitSelectionCallBack {
        void callBack(ItemModel itemModel);
    }

    public AlertDialog showOptionUniListDialog;

    private void showUnitListDialog(final int isUnit, final UnitSelectionCallBack unitSelectionCallBack) {

        //creating alertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
        //creating layoutInflater instance to inflate a layout for the alert dialog
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);

        //inflating view
        View optionListView = layoutInflater.inflate(R.layout.add_new_item_layout, null);

        RelativeLayout progress_bar_show = optionListView.findViewById(R.id.progress_bar);

        TextView itemTagTV = optionListView.findViewById(R.id.item_tag);


        final TextView no_item_found = optionListView.findViewById(R.id.no_item_found);


        ImageView close = optionListView.findViewById(R.id.close_dialog);
        FloatingActionButton fab_add_item = optionListView.findViewById(R.id.fab_add_item);

        RecyclerView itemListRV = optionListView.findViewById(R.id.item_list_rv);

        LinearLayout showListContainer = optionListView.findViewById(R.id.showListContainer);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        itemListRV.setLayoutManager(linearLayoutManager);

        if (isUnit == TYPE_UNIT) {
            itemTagTV.setText("Unit List");
            if (itemModelsListUnits != null && itemModelsListUnits.size() > 0) {

                setListToShow(itemModelsListUnits, unitSelectionCallBack);
                itemListRV.setAdapter(unitOptionItemAdapter);
            } else {
                showListContainer.setVisibility(View.GONE);
                no_item_found.setText("No Unit found");
                no_item_found.setVisibility(View.VISIBLE);
            }
        } else {
            itemTagTV.setText("Department List");


            if (itemModelsListDeparment != null && itemModelsListDeparment.size() > 0) {

                setListToShow(itemModelsListDeparment, unitSelectionCallBack);
                itemListRV.setAdapter(unitOptionItemAdapter);
            } else {
                showListContainer.setVisibility(View.GONE);
                no_item_found.setText("No Department found");
                no_item_found.setVisibility(View.VISIBLE);
            }
        }

        EditText search_option_item = optionListView.findViewById(R.id.search_option_item);
        search_option_item.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (unitOptionItemAdapter != null)
                    unitOptionItemAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unitOptionItemAdapter = null;
                showOptionUniListDialog.dismiss();
            }
        });

        switch (isUnit) {
            case TYPE_UNIT:
                fab_add_item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAddSingleFieldDialog(TYPE_UNIT, new AddSingleFieldCallBack() {
                            @Override
                            public void callBack(String NAME_VC, int WHICH_TO_ADD) {
                                showProgress();
                                callAddDetailsApi(NAME_VC, WHICH_TO_ADD);
                            }
                        });

                    }
                });
                break;
            case TYPE_DEPARTMENT:
                fab_add_item.setVisibility(View.GONE);
                break;
        }
        builder.setView(optionListView);
        showOptionUniListDialog = builder.create();
        showOptionUniListDialog.show();
    }

    private void setListToShow(List<ItemModel> itemModelsListUnits, final UnitSelectionCallBack unitSelectionCallBack) {

        showListContainer.setVisibility(View.VISIBLE);
        no_item_found.setVisibility(View.GONE);
        unitOptionItemAdapter = new OptionItemAdapter(getActivity(), itemModelsListUnits, new OptionItemAdapter.ItemClickCallBack() {
            @Override
            public void callBack(ItemModel itemModel) {
                unitOptionItemAdapter = null;
                unitSelectionCallBack.callBack(itemModel);
                showOptionUniListDialog.dismiss();
            }
        });


        /*search_option_item.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                optionItemAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });*/
    }

    private void callAddProductApi(String productName, int unitId, double productPrice) {


        Call<JsonObject> call = apiInterface.insertProduct(productName, unitId, productPrice);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {


                hideProgress();
                if (!response.isSuccessful()) {
                    Utility.globalMessageDialog(getActivity(), "Product already exists");
                    return;
                }
                ShowToast.showToast(getActivity(), "Product Added");

                getProductList(true);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

                hideProgress();
            }
        });
    }


    public BottomSheetDialog globalAddSingleFieldDialog;

    public interface AddSingleFieldCallBack {
        void callBack(String NAME_VC, int WHICH_TO_ADD);
    }

    private void showAddSingleFieldDialog(final int isUnitAdd, final AddSingleFieldCallBack addSingleFieldCallBack) {


        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.add_name_layout, null);


        LinearLayout department_container = view.findViewById(R.id.department_container);
        final TextView department_tv = view.findViewById(R.id.department_tv);

        TextView add_which_tag = view.findViewById(R.id.add_which_tag);

        TextView addDetail = view.findViewById(R.id.addDetail);

        final TextInputEditText name_et = view.findViewById(R.id.name_et);
        final TextInputLayout name_et_til = view.findViewById(R.id.name_et_til);


        switch (isUnitAdd) {
            case TYPE_STATUS:
                name_et.setHint("Enter Status");
                selectedDepartmentId = -1;
                department_container.setVisibility(View.VISIBLE);
                department_tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showUnitListDialog(TYPE_DEPARTMENT, new UnitSelectionCallBack() {
                            @Override
                            public void callBack(ItemModel itemModel) {
                                department_tv.setText(itemModel.getITEMNAME());
                                selectedDepartmentId = itemModel.getITEMID();

                                Log.e("AlucarD dep", String.valueOf(selectedDepartmentId));
                            }
                        });
                    }
                });

                add_which_tag.setText("Add new Status");
                break;
            case TYPE_UNIT:
                name_et.setHint("Enter Unit");
                add_which_tag.setText("Add new Unit");
                department_container.setVisibility(View.GONE);
                break;
        }


        name_et.addTextChangedListener(new MyTextWatcher(name_et, name_et_til, getActivity()));

        addDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(name_et.getText().toString())) {
                    if (isUnitAdd == TYPE_STATUS) {

                        if (selectedDepartmentId == -1) {
                            ShowToast.showToast(getActivity(), "Select a department");
                            return;
                        }

                        if (name_et.getText().toString().trim().equalsIgnoreCase("Force Closed") ||
                                name_et.getText().toString().trim().equalsIgnoreCase("Confirm Closed")) {
                            ShowToast.showToast(getActivity(), "Status Already exists");
                            return;
                        }
                    }

                    addSingleFieldCallBack.callBack(name_et.getText().toString(), isUnitAdd);


                    globalAddSingleFieldDialog.dismiss();
                } else
                    name_et_til.setError("Field Required");

            }
        });

        globalAddSingleFieldDialog = new BottomSheetDialog(getActivity());
        globalAddSingleFieldDialog.setContentView(view);

        globalAddSingleFieldDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        globalAddSingleFieldDialog.show();

    }

    private void callAddDetailsApi(String NAME_VC, final int WHICH_TO_ADD) {

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<JsonObject> call = apiInterface.insertStatusUnit(NAME_VC, selectedDepartmentId, WHICH_TO_ADD);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                hideProgress();
                if (!response.isSuccessful()) {
                    return;
                }

                JsonObject result = response.body();
                String message = result.get("message").getAsString();
                if (message.equalsIgnoreCase("error")) {
                    Utility.globalMessageDialog(getActivity(), "Value already exists");
                    return;
                }
                if (message.equalsIgnoreCase("success")) {
                    switch (WHICH_TO_ADD) {
                        case 1:
                            ShowToast.showToast(getActivity(), "Status Added");
                            getStatusList(true);
                            break;
                        case 3:
                            ShowToast.showToast(getActivity(), "Unit Added");
                            getUnitList(true);
                            break;
                    }
                } else if (message.equalsIgnoreCase("failure")) {
                    ShowToast.showToast(getActivity(), "Response Insuccessful");
                } else {
                    Utility.globalMessageDialog(getActivity(), message);
                }


            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                hideProgress();
                Log.e("AlucarD", "e", t);
            }
        });

    }
}
