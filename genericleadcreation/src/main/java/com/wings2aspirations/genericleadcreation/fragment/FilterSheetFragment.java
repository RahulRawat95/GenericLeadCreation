package com.wings2aspirations.genericleadcreation.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.adapter.FilterPagerAdapter;
import com.wings2aspirations.genericleadcreation.models.ItemModel;

import java.util.ArrayList;
import java.util.HashSet;

public class FilterSheetFragment extends BottomSheetDialogFragment {
    private static final String ARG_OPTION_LIST = "argOptionList";
    private static final String ARG_HASH_SET = "argHashSet";

    private ArrayList<? extends ItemModel>[] itemModels;
    private HashSet<Integer>[] hashSets;

    private ImageButton checkButton, refreshButton;

    public interface Callback {
        void callback();
    }

    private Callback callback;

    public static FilterSheetFragment newInstance(ArrayList<? extends ItemModel>[] itemModels, HashSet<Integer>[] hashSets) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_OPTION_LIST, itemModels);
        args.putSerializable(ARG_HASH_SET, hashSets);

        FilterSheetFragment fragment = new FilterSheetFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void getValues() {
        Bundle args = getArguments();
        itemModels = (ArrayList<? extends ItemModel>[]) args.getSerializable(ARG_OPTION_LIST);
        hashSets = (HashSet<Integer>[]) args.getSerializable(ARG_HASH_SET);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        getValues();
        return super.onCreateDialog(savedInstanceState);
    }

    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_filter, container, false);
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        checkButton = view.findViewById(R.id.imgbtn_apply);
        refreshButton = view.findViewById(R.id.imgbtn_refresh);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewPager.setAdapter(new FilterPagerAdapter(getChildFragmentManager(), itemModels, hashSets));
        tabLayout.setupWithViewPager(viewPager);
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < hashSets.length; i++)
                    hashSets[i].clear();
                dismiss();
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (callback != null)
            callback.callback();
        super.onDestroyView();
    }
}