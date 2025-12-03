package com.lukehemmin.ai_diet_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.data.model.BadgeResponse;

import java.util.ArrayList;
import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private List<BadgeResponse> badges = new ArrayList<>();

    public void setBadges(List<BadgeResponse> badges) {
        this.badges = badges;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        BadgeResponse badge = badges.get(position);
        holder.bind(badge);
    }

    @Override
    public int getItemCount() {
        return badges.size();
    }

    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription;
        ImageView ivIcon, ivLock;

        public BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_badge_name);
            tvDescription = itemView.findViewById(R.id.tv_badge_description);
            ivIcon = itemView.findViewById(R.id.iv_badge_icon);
            ivLock = itemView.findViewById(R.id.iv_lock_status);
        }

        void bind(BadgeResponse badge) {
            tvName.setText(badge.getName());
            tvDescription.setText(badge.getDescription());

            if (badge.isUnlocked()) {
                ivIcon.setAlpha(1.0f);
                ivLock.setVisibility(View.GONE);
                tvName.setTextColor(itemView.getContext().getColor(R.color.gray_text));
            } else {
                ivIcon.setAlpha(0.3f);
                ivLock.setVisibility(View.VISIBLE);
                tvName.setTextColor(itemView.getContext().getColor(R.color.gray_subtext));
            }
            
            // TODO: Set icon dynamically based on badge.getIcon() string if we have resources mapping
            // For now, default to medal icon
        }
    }
}
