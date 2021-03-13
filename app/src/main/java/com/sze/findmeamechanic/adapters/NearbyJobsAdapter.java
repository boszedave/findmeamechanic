package com.sze.findmeamechanic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.models.NearbyJob;
import java.util.List;

public class NearbyJobsAdapter extends RecyclerView.Adapter<NearbyJobsAdapter.ViewHolder> {
    private List<NearbyJob> jobList;
    private OnJobClickListener onJobClickListener;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name, desc, date;
        OnJobClickListener onJobClickListener;

        public ViewHolder(View view, OnJobClickListener onJobClickListener) {
            super(view);
            name = (TextView) view.findViewById(R.id.text_view_title);
            desc = (TextView) view.findViewById(R.id.text_view_desc);
            date = (TextView) view.findViewById(R.id.text_view_post_date);
            this.onJobClickListener = onJobClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onJobClickListener.onJobClick(getAdapterPosition());
        }
    }

    public NearbyJobsAdapter(List<NearbyJob> jobList, OnJobClickListener onJobClickListener) {
        this.jobList = jobList;
        this.onJobClickListener = onJobClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.job_item, parent,false);

        return new ViewHolder(itemView, onJobClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NearbyJob nearbyJob = jobList.get(position);
        holder.name.setText(nearbyJob.getName());
        holder.desc.setText(nearbyJob.getDesc());
        holder.date.setText(nearbyJob.getDate());
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public interface OnJobClickListener {
        void onJobClick(int position);
    }
}
