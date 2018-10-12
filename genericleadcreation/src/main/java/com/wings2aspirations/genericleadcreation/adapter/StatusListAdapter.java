package com.wings2aspirations.genericleadcreation.adapter;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.models.StatusModel;

import java.util.ArrayList;
import java.util.List;

public class StatusListAdapter extends RecyclerView.Adapter<StatusListAdapter.StatusAdapterViewHolder> implements Filterable{
    private Context context;
    private List<StatusModel> itemModels;
    private List<StatusModel> itemModelsFiltered;
    private StatusClickCallBack statusClickCallBack;

    public void setItemList(List<StatusModel> itemModels) {
        this.itemModels = itemModels;
        this.itemModelsFiltered = itemModels;

        notifyDataSetChanged();
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
                    List<StatusModel> filteredList = new ArrayList<>();
                    for (StatusModel row : itemModels) {
                        if (row.getSTATUSNAMEVC().toLowerCase().contains(charString.toLowerCase())) {
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
                itemModelsFiltered = (List<StatusModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface StatusClickCallBack {
        void statusCallBack(StatusModel statusModel);
    }

    public StatusListAdapter(Context context, List<StatusModel> itemModels, StatusClickCallBack statusClickCallBack) {
        this.context = context;
        this.itemModels = itemModels;
        this.itemModelsFiltered = itemModels;
        this.statusClickCallBack = statusClickCallBack;
    }

    @NonNull
    @Override
    public StatusAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_view_holder_layout, parent, false);
        StatusAdapterViewHolder viewHolder = new StatusAdapterViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull StatusAdapterViewHolder holder, final int position) {

        String itemName = itemModelsFiltered.get(position).getSTATUSNAMEVC();

        int randomAndroidColor = holder.androidColors[position % holder.androidColors.length];
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.hollow_circle);
        drawable.setColorFilter(randomAndroidColor, PorterDuff.Mode.SRC_ATOP);
        holder.itemTag.setBackground(drawable);
        holder.itemTag.setText(String.valueOf(itemName.charAt(0)).toUpperCase());
        holder.item.setText(itemName);

        holder.department.setText(itemModelsFiltered.get(position).getDEPARTMENTVC());

        holder.departmentContainer.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (statusClickCallBack != null)
                    statusClickCallBack.statusCallBack(itemModelsFiltered.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemModelsFiltered.size();
    }

    public class StatusAdapterViewHolder extends RecyclerView.ViewHolder {
        TextView itemTag;
        TextView item;
        TextView department;
        int[] androidColors;
        LinearLayout departmentContainer;

        public StatusAdapterViewHolder(View itemView) {
            super(itemView);

            androidColors = itemView.getResources().getIntArray(R.array.androidcolors);
            //finding views of recyclerView viewHolder
            itemTag = itemView.findViewById(R.id.option_label);
            item = itemView.findViewById(R.id.option_item);

            department = itemView.findViewById(R.id.department);

            departmentContainer = itemView.findViewById(R.id.departmentContainer);


        }
    }
}
