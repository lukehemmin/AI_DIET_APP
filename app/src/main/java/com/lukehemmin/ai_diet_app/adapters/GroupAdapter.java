package com.lukehemmin.ai_diet_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.data.model.GroupResponse;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<GroupResponse> groups = new ArrayList<>();

    public void setGroups(List<GroupResponse> groups) {
        this.groups = groups;
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
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        private TextView tvGroupName;
        private TextView tvGroupMembers;
        private TextView tvGroupChallenge;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.txt_group_name);
            tvGroupMembers = itemView.findViewById(R.id.txt_group_members);
            tvGroupChallenge = itemView.findViewById(R.id.txt_group_challenge);
        }

        public void bind(GroupResponse group) {
            if (tvGroupName != null) tvGroupName.setText(group.getName());
            if (tvGroupMembers != null) tvGroupMembers.setText("👥 " + group.getMemberCount() + "명 참여중");
            if (tvGroupChallenge != null) tvGroupChallenge.setText(group.getChallenge());
        }
    }
}
