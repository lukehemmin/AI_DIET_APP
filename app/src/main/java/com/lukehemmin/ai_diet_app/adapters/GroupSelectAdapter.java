package com.lukehemmin.ai_diet_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.data.model.GroupResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupSelectAdapter extends RecyclerView.Adapter<GroupSelectAdapter.GroupSelectViewHolder> {

    private List<GroupResponse> groups = new ArrayList<>();
    private Set<String> selectedGroupIds = new HashSet<>();

    public void setGroups(List<GroupResponse> groups) {
        this.groups = groups != null ? groups : new ArrayList<>();
        selectedGroupIds.clear();
        notifyDataSetChanged();
    }

    public List<String> getSelectedGroupIds() {
        return new ArrayList<>(selectedGroupIds);
    }

    public boolean hasSelection() {
        return !selectedGroupIds.isEmpty();
    }

    @NonNull
    @Override
    public GroupSelectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_select, parent, false);
        return new GroupSelectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupSelectViewHolder holder, int position) {
        GroupResponse group = groups.get(position);
        holder.bind(group);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    class GroupSelectViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cbSelect;
        private TextView tvGroupName;
        private TextView tvGroupInfo;

        public GroupSelectViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cb_select);
            tvGroupName = itemView.findViewById(R.id.tv_group_name);
            tvGroupInfo = itemView.findViewById(R.id.tv_group_info);

            itemView.setOnClickListener(v -> {
                cbSelect.setChecked(!cbSelect.isChecked());
            });

            cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    String groupId = groups.get(pos).getId();
                    if (isChecked) {
                        selectedGroupIds.add(groupId);
                    } else {
                        selectedGroupIds.remove(groupId);
                    }
                }
            });
        }

        public void bind(GroupResponse group) {
            tvGroupName.setText(group.getName());
            
            String info = "멤버 " + group.getMemberCount() + "명";
            if (group.getChallenge() != null && !group.getChallenge().isEmpty()) {
                info += " · " + group.getChallenge();
            }
            tvGroupInfo.setText(info);

            // 체크 상태 복원
            cbSelect.setChecked(selectedGroupIds.contains(group.getId()));
        }
    }
}
