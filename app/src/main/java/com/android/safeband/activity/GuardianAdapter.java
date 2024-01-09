package com.android.safeband.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import androidx.annotation.NonNull;
import com.android.safebandproject.R;
public class GuardianAdapter extends RecyclerView.Adapter<GuardianAdapter.ViewHolder> {

    private List<Guardian> guardians;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Guardian guardian);
    }

    public GuardianAdapter(List<Guardian> guardians, OnItemClickListener listener) {
        this.guardians = guardians;
        this.onItemClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.guardian_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Guardian guardian = guardians.get(position);
        holder.nameTextView.setText(guardian.getName());
        holder.phoneNumberTextView.setText(guardian.getPhoneNumber());

        // 클릭 리스너 설정
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(guardian);
            }
        });
    }

    @Override
    public int getItemCount() {
        return guardians.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView phoneNumberTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.guardian_name);
            phoneNumberTextView = itemView.findViewById(R.id.guardian_phone);
        }
    }
}