package com.wings2aspirations.genericleadcreation.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.adapter.FilterOptionsAdapter;
import com.wings2aspirations.genericleadcreation.models.ItemModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FilterFragment extends Fragment {
    private static final String ARG_OPTION_LIST = "argOptionList";
    private static final String ARG_HASH_SET = "argHashSet";

    private ArrayList<? extends ItemModel> list;
    private HashSet<Integer> hashSet;

    private RecyclerView recyclerView;

    public static FilterFragment newInstance(ArrayList<? extends ItemModel> list, HashSet<Integer> hashSet) {

        Bundle args = new Bundle();
        args.putSerializable(ARG_OPTION_LIST, list);
        args.putSerializable(ARG_HASH_SET, hashSet);
        FilterFragment fragment = new FilterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void getValue() {
        Bundle args = getArguments();
        list = (ArrayList<? extends ItemModel>) args.getSerializable(ARG_OPTION_LIST);
        hashSet = (HashSet<Integer>) args.getSerializable(ARG_HASH_SET);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        getValue();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView.setLayoutManager(new FlexboxLayoutManager(getActivity()));
        recyclerView.setAdapter(new FilterOptionsAdapter(getActivity(), list, hashSet));
    }
}
