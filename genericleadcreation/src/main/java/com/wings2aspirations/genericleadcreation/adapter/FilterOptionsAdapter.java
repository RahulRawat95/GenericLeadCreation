package com.wings2aspirations.genericleadcreation.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.models.ItemModel;

import java.util.ArrayList;
import java.util.HashSet;

public class FilterOptionsAdapter extends RecyclerView.Adapter<FilterOptionsAdapter.ViewHolder> {
    private ArrayList<? extends ItemModel> list;
    private HashSet<Integer> hashSet;
    private int colors[];

    public FilterOptionsAdapter(Context context, ArrayList<? extends ItemModel> list, HashSet<Integer> hashSet) {
        this.list = list;
        this.hashSet = hashSet;
        colors = context.getResources().getIntArray(R.array.filter_item_text_color);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.single_chip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final ItemModel itemModel = list.get(position);
        if (hashSet.contains(itemModel.getITEMID())) {
            holder.label.setBackgroundResource(R.drawable.chip_selected);
            holder.label.setTextColor(colors[1]);
        } else {
            holder.label.setBackgroundResource(R.drawable.chip_unselected);
            holder.label.setTextColor(colors[0]);
        }
        holder.label.setText(itemModel.getITEMNAME());
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (hashSet.contains(itemModel.getITEMID())) {
                    holder.label.setBackgroundResource(R.drawable.chip_unselected);
                    hashSet.remove(itemModel.getITEMID());
                } else {
                    holder.label.setBackgroundResource(R.drawable.chip_selected);
                    hashSet.add(itemModel.getITEMID());
                }
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView label;

        public ViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.txt_title);
        }
    }
}