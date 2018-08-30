package com.wings2aspirations.genericleadcreation.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.models.ItemModel;

import java.util.ArrayList;
import java.util.List;

public class OptionItemAdapter extends RecyclerView.Adapter<OptionItemAdapter.OptionItemViewHolder> implements Filterable {

    private Context context;
    private List<? extends ItemModel> itemModels;
    private List<? extends ItemModel> itemModelsFiltered;
    private ItemClickCallBack itemClickCallBack;


    public interface ItemClickCallBack {
        public void callBack(ItemModel itemModel);
    }

    public OptionItemAdapter(Context context, List<? extends ItemModel> itemModels, ItemClickCallBack itemClickCallBack) {
        this.context = context;
        this.itemModels = itemModels;
        this.itemModelsFiltered = itemModels;
        this.itemClickCallBack = itemClickCallBack;
    }

    @NonNull
    @Override
    public OptionItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_view_holder_layout, parent, false);
        OptionItemViewHolder viewHolder = new OptionItemViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull OptionItemViewHolder holder, int position) {
        String itemName = itemModelsFiltered.get(position).getITEMNAME();

        int randomAndroidColor = holder.androidColors[position % holder.androidColors.length];
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.hollow_circle);
        drawable.setColorFilter(randomAndroidColor, PorterDuff.Mode.SRC_ATOP);
        holder.itemTag.setBackground(drawable);
        holder.itemTag.setText(String.valueOf(itemName.charAt(0)).toUpperCase());
        holder.item.setText(itemName);
    }

    @Override
    public int getItemCount() {
        return itemModelsFiltered.size();
    }

    public class OptionItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView itemTag;
        TextView item;
        int[] androidColors;

        public OptionItemViewHolder(View itemView) {
            super(itemView);

            androidColors = itemView.getResources().getIntArray(R.array.androidcolors);
            //finding views of recyclerView viewHolder
            itemTag = itemView.findViewById(R.id.option_label);
            item = itemView.findViewById(R.id.option_item);


            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            itemClickCallBack.callBack(itemModelsFiltered.get(getAdapterPosition()));
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    itemModelsFiltered = itemModels;
                } else {
                    List<ItemModel> filteredList = new ArrayList<>();
                    for (ItemModel row : itemModels) {
                        if (row.getITEMNAME().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    itemModelsFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = itemModelsFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                itemModelsFiltered = (List<ItemModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

}
