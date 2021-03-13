package com.sze.findmeamechanic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.models.Repairman;

import java.util.List;

public class ApplicantsAdapter extends RecyclerView.Adapter<ApplicantsAdapter.ViewHolder> {
    private List<Repairman> applicantList;
    private OnApplicantClickListener onApplicantClickListener;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name;
        OnApplicantClickListener onApplicantClickListener;

        public ViewHolder(View view, OnApplicantClickListener onApplicantClickListener) {
            super(view);
            name = (TextView) view.findViewById(R.id.text_view_applicant_name);
            this.onApplicantClickListener = onApplicantClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onApplicantClickListener.onApplicantClick(getAdapterPosition());
        }
    }

    public ApplicantsAdapter(List<Repairman> applicantList, OnApplicantClickListener onApplicantClickListener) {
        this.applicantList = applicantList;
        this.onApplicantClickListener = onApplicantClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.applicant_item, parent, false);

        return new ViewHolder(itemView, onApplicantClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Repairman repman = applicantList.get(position);
        holder.name.setText(repman.getRepName());
    }

    @Override
    public int getItemCount() {
        return applicantList.size();
    }

    public interface OnApplicantClickListener {
        void onApplicantClick(int position);
    }
}
