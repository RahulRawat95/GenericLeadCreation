package com.wings2aspirations.genericleadcreation.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.wings2aspirations.genericleadcreation.fragment.FilterFragment;
import com.wings2aspirations.genericleadcreation.models.ItemModel;

import java.util.ArrayList;
import java.util.HashSet;

public class FilterPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<? extends ItemModel>[] itemModels;
    private HashSet<Integer>[] hashSets;
    private Fragment[] fragments;

    public FilterPagerAdapter(FragmentManager fm, ArrayList<? extends ItemModel>[] itemModels, HashSet<Integer>[] hashSets) {
        super(fm);
        this.itemModels = itemModels;
        this.hashSets = hashSets;
        fragments = new Fragment[3];
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = fragments[position];
        if (fragment == null)
            switch (position) {
                case 0:
                    fragment = fragments[0] = FilterFragment.newInstance(itemModels[0], hashSets[0]);
                    break;
                case 1:
                    fragment = fragments[1] = FilterFragment.newInstance(itemModels[1], hashSets[1]);
                    break;
                case 2:
                    fragment = fragments[2] = FilterFragment.newInstance(itemModels[2], hashSets[2]);
                    break;
            }
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Product";
            case 1:
                return "Status";
            case 2:
                return "City";
            default:
                return "";
        }
    }
}
