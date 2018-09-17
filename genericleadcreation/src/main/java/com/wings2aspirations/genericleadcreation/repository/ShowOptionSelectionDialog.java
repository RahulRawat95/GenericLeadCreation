package com.wings2aspirations.genericleadcreation.repository;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import com.wings2aspirations.genericleadcreation.adapter.OptionItemAdapter;
import com.wings2aspirations.genericleadcreation.models.ItemModel;
import com.wings2aspirations.genericleadcreation.models.ProductListModel;
import com.wings2aspirations.genericleadcreation.network.ApiClient;
import com.wings2aspirations.genericleadcreation.network.ApiInterface;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.wings2aspirations.genericleadcreation.activity.AddUpdateLeadActivity.hideProgress;
import static com.wings2aspirations.genericleadcreation.activity.AddUpdateLeadActivity.itemModelsListProduct;
import static com.wings2aspirations.genericleadcreation.activity.AddUpdateLeadActivity.itemModelsListStatus;
import static com.wings2aspirations.genericleadcreation.activity.AddUpdateLeadActivity.itemModelsListUnits;
import static com.wings2aspirations.genericleadcreation.activity.AddUpdateLeadActivity.showProgress;

public class ShowOptionSelectionDialog {
    public static final int TYPE_PRODUCT = 0;
    public static final int TYPE_STATUS = 1;
    public static final int TYPE_CITY = 2;
    public static final int TYPE_UNIT = 3;

    public static AlertDialog showOptionItemListDialog;
    public static BottomSheetDialog globalMessageDialog;
    public static RelativeLayout progress_bar_show;

    public static OptionItemAdapter optionItemAdapter;

    public interface OptionSelectionCallBack {
        void callBack(ItemModel optionSelected);
    }

    public static void showDialog(final Context mContext, final int dialogType, boolean canAdd, List<? extends ItemModel> itemModelList, final OptionSelectionCallBack optionSelectionCallBack) {
        //creating alertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.DialogTheme);
        //creating layoutInflater instance to inflate a layout for the alert dialog
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

        //inflating view
        View optionListView = layoutInflater.inflate(R.layout.add_new_item_layout, null);

        progress_bar_show = optionListView.findViewById(R.id.progress_bar);

        TextView itemTagTV = optionListView.findViewById(R.id.item_tag);

        final TextView no_item_found = optionListView.findViewById(R.id.no_item_found);

        EditText search_option_item = optionListView.findViewById(R.id.search_option_item);

        ImageView close = optionListView.findViewById(R.id.close_dialog);
        FloatingActionButton fab_add_item = optionListView.findViewById(R.id.fab_add_item);

        if (!canAdd)
            fab_add_item.setVisibility(View.GONE);

        LinearLayout showListContainer = optionListView.findViewById(R.id.showListContainer);


        switch (dialogType) {
            case TYPE_PRODUCT:
                itemTagTV.setText("Product List");
                break;
            case TYPE_STATUS:
                itemTagTV.setText("Status List");
                break;
            case TYPE_CITY:
                itemTagTV.setText("City List");
                break;
            case TYPE_UNIT:
                itemTagTV.setText("Unit List");
                break;
        }

        RecyclerView itemListRV = optionListView.findViewById(R.id.item_list_rv);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);

        if (itemModelList.size() > 0) {
            optionItemAdapter = new OptionItemAdapter(mContext, itemModelList, new OptionItemAdapter.ItemClickCallBack() {
                @Override
                public void callBack(ItemModel itemModel) {
                    optionSelectionCallBack.callBack(itemModel);
                    optionItemAdapter = null;
                    showOptionItemListDialog.dismiss();
                }
            });
            itemListRV.setLayoutManager(linearLayoutManager);
            itemListRV.setAdapter(optionItemAdapter);

            search_option_item.addTextChangedListener(new TextWatcher() {
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
            });
        } else {
            itemListRV.setVisibility(View.GONE);
            switch (dialogType) {
                case TYPE_PRODUCT:
                    no_item_found.setText("No product found");
                    break;
                case TYPE_STATUS:
                    no_item_found.setText("No status found");
                    break;
                case TYPE_CITY:
                    no_item_found.setText("No City Found");
                    break;
                case TYPE_UNIT:
                    no_item_found.setText("No Unit Found");
                    break;
            }

            showListContainer.setVisibility(View.GONE);
            no_item_found.setVisibility(View.VISIBLE);
        }

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                optionItemAdapter = null;
                showOptionItemListDialog.dismiss();
            }
        });

        fab_add_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (dialogType) {
                    case TYPE_PRODUCT:
                        showOptionItemListDialog.dismiss();
                        showAddProductLayoutDialog(mContext, new AddProductCallBack() {
                            @Override
                            public void callBack(String productName, int unitId, double productPrice) {
                                showProgress();
                                callAddProductApi(mContext, productName, unitId, productPrice);
                            }
                        });
                        break;
                    case TYPE_UNIT:
                    case TYPE_STATUS:
                        showOptionItemListDialog.dismiss();
                        showAddSingleFieldDialog(mContext, dialogType, new AddSingleFieldCallBack() {
                            @Override
                            public void callBack(String NAME_VC, int WHICH_TO_ADD) {
                                showProgress();
                                globalAddSingleFieldDialog.dismiss();
                                callAddDetailsApi(mContext, NAME_VC, WHICH_TO_ADD);
                            }
                        });
                        break;
                }

            }
        });


        builder.setView(optionListView);
        showOptionItemListDialog = builder.create();
        showOptionItemListDialog.show();
    }

    public interface AddSingleFieldCallBack {
        void callBack(String NAME_VC, int WHICH_TO_ADD);
    }

    public static BottomSheetDialog globalAddSingleFieldDialog;

    private static void showAddSingleFieldDialog(Context mContext, final int isUnitAdd, final AddSingleFieldCallBack addSingleFieldCallBack) {


        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.add_name_layout, null);


        TextView add_which_tag = view.findViewById(R.id.add_which_tag);

        switch (isUnitAdd) {
            case 1:
                add_which_tag.setText("Add new Status");
                break;
            case 3:
                add_which_tag.setText("Add new Unit");
                break;
        }


        TextView addDetail = view.findViewById(R.id.addDetail);

        final TextInputEditText name_et = view.findViewById(R.id.name_et);
        final TextInputLayout name_et_til = view.findViewById(R.id.name_et_til);

        name_et.addTextChangedListener(new MyTextWatcher(name_et, name_et_til, mContext));

        addDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(name_et.getText().toString())) {
                    addSingleFieldCallBack.callBack(name_et.getText().toString(), isUnitAdd);
                } else
                    name_et_til.setError("Field Required");

            }
        });

        globalAddSingleFieldDialog = new BottomSheetDialog(mContext);
        globalAddSingleFieldDialog.setContentView(view);

        globalAddSingleFieldDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        globalAddSingleFieldDialog.show();

    }

    private static void callAddDetailsApi(final Context mContext, String NAME_VC, final int WHICH_TO_ADD) {
/*
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<JsonObject> call = apiInterface.insertStatusUnit(NAME_VC, WHICH_TO_ADD);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                hideProgress();
                globalAddSingleFieldDialog.dismiss();
                if (!response.isSuccessful()) {
                    ShowToast.showToast(mContext, "Response Insuccessful");
                    return;
                }

                switch (WHICH_TO_ADD) {
                    case 1:
                        ShowToast.showToast(mContext, "Status Added");
                        getStatusList(mContext);
                        break;
                    case 3:
                        ShowToast.showToast(mContext, "Unit Added");
                        getUnitList(mContext);
                        break;
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                hideProgress();
            }
        });*/

    }

    public interface AddProductCallBack {
        void callBack(String productName, int unitId, double productPrice);
    }

    public static BottomSheetDialog globalAddProductDialog;
    private static List<View> validateViewList;
    private static int selectedUnitId;

    private static void showAddProductLayoutDialog(final Context mContext, final AddProductCallBack addProductCallBack) {

        validateViewList = new ArrayList<View>();
        selectedUnitId = -1;

        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.add_product_layout, null);

        LinearLayout add_product_container = view.findViewById(R.id.add_product_container);

        final TextInputEditText product_name_et = view.findViewById(R.id.product_name_et);
        final TextView unit_tv = view.findViewById(R.id.unit_tv);
        final TextInputEditText product_price_et = view.findViewById(R.id.product_price_et);

        unit_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowOptionSelectionDialog.showDialog(mContext, TYPE_UNIT, true, itemModelsListUnits, new ShowOptionSelectionDialog.OptionSelectionCallBack() {
                    @Override
                    public void callBack(ItemModel optionSelected) {
                        unit_tv.setText(optionSelected.getITEMNAME());
                        selectedUnitId = optionSelected.getITEMID();
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


                currentViewEditText.addTextChangedListener(new MyTextWatcher(currentViewEditText, (TextInputLayout) currentView, mContext));
            }

            validateViewList.add(currentView);
        }

        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (emptyFieldValidation(validateViewList)) {
                    if (!TextUtils.isEmpty(unit_tv.getText().toString()))
                        addProductCallBack.callBack(product_name_et.getText().toString().trim(), selectedUnitId, Double.parseDouble(product_price_et.getText().toString()));
                    else
                        ShowToast.showToast(mContext, "Please select unit");
                }
            }
        });

        globalAddProductDialog = new BottomSheetDialog(mContext);
        globalAddProductDialog.setContentView(view);

        globalAddProductDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        globalAddProductDialog.show();
    }

    private static void callAddProductApi(final Context mContext, String productName, int unitId, double productPrice) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<JsonObject> call = apiInterface.insertProduct(productName, unitId, productPrice);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {


                hideProgress();
                globalAddProductDialog.dismiss();
                if (!response.isSuccessful()) {
                    ShowToast.showToast(mContext, "Response Insuccessful");
                    return;
                }
                ShowToast.showToast(mContext, "Product Added");

                getProductList(mContext);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

                hideProgress();
            }
        });
    }


    public static boolean emptyFieldValidation(List<View> validateViewList) {

        boolean isAllValid = true;

        for (int i = 0; i < validateViewList.size(); i++) {
            View validateView = validateViewList.get(i);

            if (validateView.getVisibility() == View.VISIBLE) {
                if (validateView instanceof TextInputLayout) {
                    if (checkForEmpty(((TextInputLayout) validateView).getEditText())) {
                        isAllValid = false;

                        ((TextInputLayout) validateView).setError("Field Required");
                    }
                }
            }

        }

        return isAllValid;
    }

    private static boolean checkForEmpty(View view) {
        if (view instanceof TextView)
            return TextUtils.isEmpty(((TextView) view).getText().toString());
        return false;
    }


    public static class MyTextWatcher implements TextWatcher {

        private View view;
        private TextInputLayout textInputLayout;
        private Context context;

        public MyTextWatcher(View view, TextInputLayout textInputLayout, Context context) {
            this.view = view;
            this.textInputLayout = textInputLayout;
            this.context = context;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        public void afterTextChanged(Editable editable) {
            singleViewEmptyCheck(view, textInputLayout, context);
        }
    }

    public static void singleViewEmptyCheck(View viewToCheck, TextInputLayout textInputLayout, Context callingContext) {


        if (textInputLayout.getVisibility() == View.VISIBLE) {
            if (checkForEmpty(viewToCheck)) {
                textInputLayout.setErrorEnabled(true);
                textInputLayout.setError("Field Required");
            } else {
                textInputLayout.setErrorEnabled(false);
            }
        }


    }


    private static void getProductList(final Context mContext) {
        showProgress();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<JsonArray> call = apiInterface.getProductList();

        Log.e("AlucarD", call.request().url().toString());
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {

                hideProgress();

                if (response.isSuccessful()) {


                    JsonArray jsonArray = response.body();
                    Type type = new TypeToken<List<ProductListModel>>() {
                    }.getType();

                    itemModelsListProduct = new Gson().fromJson(jsonArray, type);

                    if (optionItemAdapter != null) {
                        optionItemAdapter.setItemList(itemModelsListProduct);
                    }

                } else {
                    ShowToast.showToast(mContext, "Failed to get Products");
                }


            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                hideProgress();
                ShowToast.showToast(mContext, "In Failure to get Products");
            }
        });
    }


    private static void getStatusList(final Context mContext) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<JsonArray> call = apiInterface.getStatusList();

        Log.e("AlucarD", call.request().url().toString());
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {


                if (response.isSuccessful()) {


                    JsonArray jsonArray = response.body();
                    Type type = new TypeToken<List<ItemModel>>() {
                    }.getType();

                    itemModelsListStatus = new Gson().fromJson(jsonArray, type);


                } else {
                    ShowToast.showToast(mContext, "Failed to get Products");
                }


            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {

                ShowToast.showToast(mContext, "In Failure to get Products");
            }
        });
    }


    private static void getUnitList(final Context mContext) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<JsonArray> call = apiInterface.getUnits();

        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {


                if (response.isSuccessful()) {


                    JsonArray jsonArray = response.body();
                    Type type = new TypeToken<List<ItemModel>>() {
                    }.getType();

                    itemModelsListUnits = new Gson().fromJson(jsonArray, type);


                } else {
                    ShowToast.showToast(mContext, "Failed to get Products");
                }


            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {

                ShowToast.showToast(mContext, "In Failure to get Products");
            }
        });
    }


}
