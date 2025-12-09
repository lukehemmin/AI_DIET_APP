package com.lukehemmin.ai_diet_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.data.model.BadgeResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private List<BadgeResponse> badges = new ArrayList<>();
    
    // Icon mapping for badge types
    private static final Map<String, Integer> ICON_MAP = new HashMap<>();
    static {
        // 기록 관련
        ICON_MAP.put("first_meal", R.drawable.ic_badge_first_step);
        ICON_MAP.put("three_day_streak", R.drawable.ic_badge_streak);
        ICON_MAP.put("seven_day_streak", R.drawable.ic_badge_streak);
        ICON_MAP.put("fourteen_day_streak", R.drawable.ic_badge_streak);
        ICON_MAP.put("thirty_day_streak", R.drawable.ic_badge_streak);
        
        // 식단 마스터
        ICON_MAP.put("meal_10", R.drawable.ic_badge_nutrition);
        ICON_MAP.put("meal_50", R.drawable.ic_badge_nutrition);
        ICON_MAP.put("meal_master", R.drawable.ic_badge_nutrition);
        
        // 챌린지
        ICON_MAP.put("first_challenge", R.drawable.ic_badge_challenge);
        ICON_MAP.put("challenge_5", R.drawable.ic_badge_challenge);
        
        // 수분
        ICON_MAP.put("water_master", R.drawable.ic_badge_water);
        ICON_MAP.put("water_week", R.drawable.ic_badge_water);
        
        // AI
        ICON_MAP.put("ai_first_chat", R.drawable.ic_badge_ai_chat);
        ICON_MAP.put("ai_chat_10", R.drawable.ic_badge_ai_chat);
        
        // 영양 균형
        ICON_MAP.put("nutrition_balance", R.drawable.ic_badge_nutrition);
    }

    public void setBadges(List<BadgeResponse> badges) {
        this.badges = badges;
        notifyDataSetChanged();
    }
    
    public int getUnlockedCount() {
        int count = 0;
        for (BadgeResponse badge : badges) {
            if (badge.isUnlocked()) count++;
        }
        return count;
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_badge_grid, parent, false);
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
        ImageView ivIcon;
        CardView cardView;

        public BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_badge_name);
            tvDescription = itemView.findViewById(R.id.tv_badge_description);
            ivIcon = itemView.findViewById(R.id.iv_badge_icon);
            cardView = (CardView) itemView;
        }

        void bind(BadgeResponse badge) {
            tvName.setText(badge.getName());
            tvDescription.setText(badge.getDescription());

            // Set icon based on badge id
            Integer iconRes = ICON_MAP.get(badge.getId());
            if (iconRes != null) {
                ivIcon.setImageResource(iconRes);
            } else {
                ivIcon.setImageResource(R.drawable.ic_badge_challenge);
            }

            if (badge.isUnlocked()) {
                // Unlocked state - full color
                ivIcon.setAlpha(1.0f);
                tvName.setTextColor(itemView.getContext().getColor(R.color.gray_text));
                tvDescription.setTextColor(itemView.getContext().getColor(R.color.gray_subtext));
                ivIcon.setColorFilter(itemView.getContext().getColor(R.color.primary_blue));
            } else {
                // Locked state - grayed out
                ivIcon.setAlpha(0.4f);
                tvName.setTextColor(itemView.getContext().getColor(R.color.gray_subtext));
                tvDescription.setTextColor(itemView.getContext().getColor(R.color.gray_50));
                ivIcon.setColorFilter(itemView.getContext().getColor(R.color.gray_subtext));
            }
        }
    }
}
