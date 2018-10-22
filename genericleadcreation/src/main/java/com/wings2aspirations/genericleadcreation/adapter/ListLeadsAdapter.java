package com.wings2aspirations.genericleadcreation.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.activity.AddUpdateLeadActivity;
import com.wings2aspirations.genericleadcreation.models.LeadDetail;
import com.wings2aspirations.genericleadcreation.network.ApiClient;
import com.wings2aspirations.genericleadcreation.network.ApiInterface;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ListLeadsAdapter extends RecyclerView.Adapter<ListLeadsAdapter.ViewHolder> implements Filterable {

    private List<LeadDetail> leadDetails;
    private List<LeadDetail> filteredLeadDetails;
    private Context context;
    private ProgressCallback progressCallback;
    private ApiInterface apiInterface;
    /*private AlertDialog alertDialog;*/
    private long deleteId = -1;
    private boolean isAdmin;
    private String demoVc, existingVc;

    private Date fromDate, toDate;
    private LeadOnClickCallBack leadOnClickCallBack;
    private int resourceId;

    public interface ProgressCallback {
        void showProgress();

        void hideProgress();
    }

    public interface LeadOnClickCallBack {
        void callback(LeadDetail leadDetail);
    }

    public ListLeadsAdapter(Context context, List<LeadDetail> leadDetails, ProgressCallback progressCallback, boolean isAdmin, LeadOnClickCallBack leadOnClickCallBack, int resourceId) {
        this.leadDetails = leadDetails;
        this.resourceId = resourceId;
        this.filteredLeadDetails = leadDetails;
        this.context = context;
        this.progressCallback = progressCallback;
        this.isAdmin = isAdmin;
        this.leadOnClickCallBack = leadOnClickCallBack;
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
       /* alertDialog = new AlertDialog.Builder(context)
                .setMessage(R.string.delete_lead_message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (deleteId >= 0) {
                            ListLeadsAdapter.this.progressCallback.showProgress();
                            apiInterface.deleteLeadById(deleteId).enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                    ListLeadsAdapter.this.progressCallback.hideProgress();
                                    deleteId = -1;
                                    if (!response.isSuccessful()) {
                                        return;
                                    }
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                    ListLeadsAdapter.this.progressCallback.hideProgress();
                                    deleteId = -1;
                                }
                            });
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();*/
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public void setDate(Date fromDate, Date toDate, String s) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        getFilter().filter(s);
    }

    public void setDemo(String selectedEmp, String demo) {
        this.demoVc = demo;
        getFilter().filter(selectedEmp);
    }

    public void setExistingCust(String selectedEmp, String existing) {
        this.existingVc = existing;
        getFilter().filter(selectedEmp);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new ViewHolder(inflater.inflate(resourceId, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final LeadDetail lead = filteredLeadDetails.get(position);

        if (position == 0) {
            if (holder.distanceLL != null)
                holder.distanceLL.setVisibility(View.INVISIBLE);
        } else {
            if (holder.distanceLL != null)
                holder.distanceLL.setVisibility(View.VISIBLE);
            if (filteredLeadDetails.get(position - 1).getDATE_VC().trim().equalsIgnoreCase(filteredLeadDetails.get(position).getDATE_VC().trim())) {
                LeadDetail lead2 = filteredLeadDetails.get(position - 1);
                float[] results = new float[3];
                try {
                    Location.distanceBetween(Double.parseDouble(lead.getLATITUDE()), Double.parseDouble(lead.getLONGITUDE()),
                            Double.parseDouble(lead2.getLATITUDE()), Double.parseDouble(lead2.getLONGITUDE()), results);
                    if (((int) results[0] / 1000) > 0) {
                        holder.distanceTv.setText(String.format("%.2f", results[0] / 1000) + " Km");
                    } else {
                        holder.distanceTv.setText(String.format("%.2f", results[0]) + " m");
                    }
                } catch (Exception e) {
                    holder.distanceLL.setVisibility(View.INVISIBLE);
                }
            } else {
                if (holder.distanceLL != null)
                    holder.distanceLL.setVisibility(View.INVISIBLE);
            }
        }

        holder.empName.setText(lead.getEMP_NAME());
        holder.date.setText(lead.getDATE_VC());
        holder.compName.setText(lead.getCOMPANY_NAME());
        holder.custName.setText(lead.getCONTACT_PERSON());
        holder.mobNo.setText(lead.getMOBILE_NO());
        holder.emailId.setText(lead.getEMAIL());
        holder.status.setText(lead.getCALL_TYPE());
        holder.followUpDate.setText(lead.getNEXT_FOLLOW_UP_DATE());
        switch (lead.getCALL_TYPE()) {
            case "Force Closed":
                holder.status.setTextColor(Color.parseColor("#c10d0d"));
                break;
            case "Confirm Closed":
                holder.status.setTextColor(Color.parseColor("#1d5e0b"));
                break;
            default:
                holder.status.setTextColor(Color.parseColor("#000000"));
        }
        if (isAdmin)
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = AddUpdateLeadActivity.getLeadIntent(context, lead.getEMP_NAME(), lead.getEMP_ID(), lead.getID(), false);
                    context.startActivity(intent);
                }
            });

        /*holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                deleteId = position;
                alertDialog.show();
                return true;
            }
        });*/
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    List<LeadDetail> filteredList = new ArrayList<>();
                    Date date;
                    if (toDate == null || fromDate == null) {
                        filteredList.addAll(leadDetails);
                    } else
                        for (LeadDetail row : leadDetails) {
                            date = row.getDate();
                            if (date.before(toDate) && date.after(fromDate) || date.equals(toDate) || date.equals(fromDate))
                                filteredList.add(row);
                        }
                    ListLeadsAdapter.this.filteredLeadDetails = filteredList;
                } else {
                    List<LeadDetail> filteredList = new ArrayList<>();
                    Date date;
                    for (LeadDetail row : leadDetails) {
                        date = row.getDate();
                        if (toDate == null || fromDate == null) {
                            if (row.getEMP_NAME().toLowerCase().contains(charString.toLowerCase())) {
                                filteredList.add(row);
                            }
                        } else {
                            if (date.before(toDate) && date.after(fromDate) || date.equals(toDate) || date.equals(fromDate))
                                if (row.getEMP_NAME().toLowerCase().contains(charString.toLowerCase())) {
                                    filteredList.add(row);
                                }
                        }
                    }
                    ListLeadsAdapter.this.filteredLeadDetails = filteredList;
                }

                LeadDetail leadDetail;
                for (int i = 0; i < filteredLeadDetails.size(); ) {
                    leadDetail = filteredLeadDetails.get(i);
                    if (!TextUtils.isEmpty(demoVc) && !leadDetail.getDemoVc().equalsIgnoreCase(demoVc)) {
                        filteredLeadDetails.remove(i);
                    } else if (!TextUtils.isEmpty(existingVc) && !leadDetail.getExistingVc().equalsIgnoreCase(existingVc)) {
                        filteredLeadDetails.remove(i);
                    } else {
                        i++;
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredLeadDetails;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredLeadDetails = (ArrayList<LeadDetail>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getItemCount() {
        return filteredLeadDetails.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView empName;
        private TextView date;
        private TextView compName;
        private TextView custName;
        private TextView mobNo;
        private TextView emailId;
        private TextView status;
        private TextView followUpDate;
        private TextView distanceTv;
        private LinearLayout distanceLL;

        ViewHolder(View itemView) {
            super(itemView);
            empName = (TextView) itemView.findViewById(R.id.emp_name);
            date = (TextView) itemView.findViewById(R.id.date);
            compName = (TextView) itemView.findViewById(R.id.comp_name);
            custName = (TextView) itemView.findViewById(R.id.cust_name);
            mobNo = (TextView) itemView.findViewById(R.id.mob_no);
            emailId = (TextView) itemView.findViewById(R.id.email_id);
            status = (TextView) itemView.findViewById(R.id.status);
            followUpDate = (TextView) itemView.findViewById(R.id.follow_up_date);
            distanceTv = itemView.findViewById(R.id.distance_tv);
            distanceLL = itemView.findViewById(R.id.distance_ll);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            leadOnClickCallBack.callback(filteredLeadDetails.get(getAdapterPosition()));
        }
    }
}