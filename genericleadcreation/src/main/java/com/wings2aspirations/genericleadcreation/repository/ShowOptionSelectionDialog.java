package com.wings2aspirations.genericleadcreation.repository;

import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.adapter.OptionItemAdapter;
import com.wings2aspirations.genericleadcreation.models.ItemModel;

import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ShowOptionSelectionDialog {
    public static final int TYPE_PRODUCT = 0;
    public static final int TYPE_STATUS = 1;
    public static final int TYPE_CITY = 2;

    public static AlertDialog showOptionItemListDialog;
    public static BottomSheetDialog globalMessageDialog;

    public interface OptionSelectionCallBack {
        void callBack(String optionSelected);
    }

    public static void showDialog(final Context mContext, final int dialogType, List<? extends ItemModel> itemModelList, final OptionSelectionCallBack optionSelectionCallBack) {
        //creating alertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.DialogTheme);
        //creating layoutInflater instance to inflate a layout for the alert dialog
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

        //inflating view
        View optionListView = layoutInflater.inflate(R.layout.add_new_item_layout, null);

        TextView itemTagTV = optionListView.findViewById(R.id.item_tag);

        final TextView no_item_found = optionListView.findViewById(R.id.no_item_found);

        EditText search_option_item = optionListView.findViewById(R.id.search_option_item);

        ImageView close = optionListView.findViewById(R.id.close_dialog);
        FloatingActionButton fab_add_item = optionListView.findViewById(R.id.fab_add_item);

        final TextView add_new_item_tag = optionListView.findViewWithTag(R.id.add_new_item_tag);
        RelativeLayout showListContainer = optionListView.findViewById(R.id.showListContainer);
        LinearLayout addNewItemContainer = optionListView.findViewById(R.id.addNewItemContainer);

        final EditText add_new_item_et = optionListView.findViewById(R.id.add_new_item_et);
        TextView add_new_item_tv = optionListView.findViewById(R.id.add_new_item_tv);

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
        }

        RecyclerView itemListRV = optionListView.findViewById(R.id.item_list_rv);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);

        if (itemModelList.size() > 0) {
            final OptionItemAdapter optionItemAdapter = new OptionItemAdapter(mContext, itemModelList, new OptionItemAdapter.ItemClickCallBack() {
                @Override
                public void callBack(ItemModel itemModel) {
                    optionSelectionCallBack.callBack(itemModel.getITEMNAME());
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
            }

            showListContainer.setVisibility(View.GONE);
            no_item_found.setVisibility(View.VISIBLE);
        }

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptionItemListDialog.dismiss();
            }
        });

        fab_add_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (dialogType) {
                    case TYPE_PRODUCT:
                        add_new_item_tag.setText("Add New Product");
                        break;
                    case TYPE_STATUS:
                        add_new_item_tag.setText("Add New Status");
                        break;
                    case TYPE_CITY:
                        add_new_item_tag.setText("Add New City");
                        break;
                }

            }
        });


        builder.setView(optionListView);
        showOptionItemListDialog = builder.create();
        showOptionItemListDialog.show();
    }


}
