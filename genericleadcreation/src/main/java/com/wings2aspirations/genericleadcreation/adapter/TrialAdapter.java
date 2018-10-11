package com.wings2aspirations.genericleadcreation.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.models.LeadDetail;

import java.util.List;

public class TrialAdapter extends RecyclerView.Adapter<TrialAdapter.TrialAdapterViewHolder> {

    private Context context;
    private List<LeadDetail> trialDetailsList;
    private List<LeadDetail> trialDetailsListFiltered;



    public TrialAdapter(Context context, List<LeadDetail> trialDetailsList) {
        this.context = context;
        this.trialDetailsList = trialDetailsList;
        this.trialDetailsListFiltered = trialDetailsList;
    }

    @NonNull
    @Override
    public TrialAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new TrialAdapterViewHolder(inflater.inflate(R.layout.trail_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TrialAdapterViewHolder holder, int position) {
        final LeadDetail lead = trialDetailsListFiltered.get(position);
        holder.empName.setText(lead.getEMP_NAME());
        holder.date.setText(lead.getDATE_VC());
        holder.compName.setText(lead.getCOMPANY_NAME());
        holder.custName.setText(lead.getCONTACT_PERSON());
        holder.mobNo.setText(lead.getMOBILE_NO());
        holder.emailId.setText(lead.getEMAIL());
        holder.status.setText(lead.getCALL_TYPE());
        switch (lead.getCALL_TYPE()) {
            case "Warm":
                holder.status.setTextColor(Color.parseColor("#f2c40e"));
                break;
            case "Hot":
                holder.status.setTextColor(Color.parseColor("#c10d0d"));
                break;
            case "Cold":
                holder.status.setTextColor(Color.parseColor("#20b6e8"));
                break;
        }
        holder.followUpDate.setText(lead.getNEXT_FOLLOW_UP_DATE());

        if (position == (trialDetailsListFiltered.size() - 1))
            holder.link_view.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return trialDetailsListFiltered.size();
    }

    public class TrialAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView empName;
        private TextView date;
        private TextView compName;
        private TextView custName;
        private TextView mobNo;
        private TextView emailId;
        private TextView status;
        private TextView followUpDate;
        private LinearLayout link_view;

        public TrialAdapterViewHolder(View itemView) {
            super(itemView);

            empName = (TextView) itemView.findViewById(R.id.emp_name);
            date = (TextView) itemView.findViewById(R.id.date);
            compName = (TextView) itemView.findViewById(R.id.comp_name);
            custName = (TextView) itemView.findViewById(R.id.cust_name);
            mobNo = (TextView) itemView.findViewById(R.id.mob_no);
            emailId = (TextView) itemView.findViewById(R.id.email_id);
            status = (TextView) itemView.findViewById(R.id.status);
            followUpDate = (TextView) itemView.findViewById(R.id.follow_up_date);
            link_view = (LinearLayout) itemView.findViewById(R.id.link_view);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }
    }

}
