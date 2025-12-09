package com.lukehemmin.ai_diet_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.data.model.GroupResponse;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<GroupResponse> groups = new ArrayList<>();
    private OnGroupClickListener clickListener;

    public interface OnGroupClickListener {
        void onGroupClick(GroupResponse group);
    }

    public void setOnGroupClickListener(OnGroupClickListener listener) {
        this.clickListener = listener;
    }

    public void setGroups(List<GroupResponse> groups) {
        this.groups = groups != null ? groups : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        GroupResponse group = groups.get(position);
        holder.bind(group);
        
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onGroupClick(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        private TextView tvGroupName;
        private TextView tvGroupMembers;
        private TextView tvGroupChallenge;
        private ProgressBar progressGroup;
        private TextView tvGroupProgress;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.txt_group_name);
            tvGroupMembers = itemView.findViewById(R.id.txt_group_members);
            tvGroupChallenge = itemView.findViewById(R.id.txt_group_challenge);
            progressGroup = itemView.findViewById(R.id.progress_group);
            tvGroupProgress = itemView.findViewById(R.id.txt_group_progress);
        }

        public void bind(GroupResponse group) {
            if (tvGroupName != null) tvGroupName.setText(group.getName());
            if (tvGroupMembers != null) tvGroupMembers.setText("👥 " + group.getMemberCount() + "명 참여중");
            if (tvGroupChallenge != null) tvGroupChallenge.setText(group.getChallenge() != null ? group.getChallenge() : "챌린지 없음");
            
            // TODO: 실제 진행률 데이터로 교체 (현재는 0%로 표시)
            int progress = 0;
            if (progressGroup != null) progressGroup.setProgress(progress);
            if (tvGroupProgress != null) tvGroupProgress.setText(progress + "% 달성");
        }
    }
}
