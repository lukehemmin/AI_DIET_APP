package com.lukehemmin.ai_diet_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProfileMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ProfileMenuItem> items;
    private OnMenuItemClickListener listener;

    public interface OnMenuItemClickListener {
        void onMenuItemClick(String menuId);
    }

    public ProfileMenuAdapter(List<ProfileMenuItem> items, OnMenuItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getViewType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == ProfileMenuItem.TYPE_REGULAR) {
            View view = inflater.inflate(R.layout.item_profile_menu, parent, false);
            return new RegularMenuViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_profile_menu_goal, parent, false);
            return new GoalMenuViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ProfileMenuItem item = items.get(position);

        if (holder instanceof RegularMenuViewHolder) {
            ((RegularMenuViewHolder) holder).bind(item, listener);
        } else if (holder instanceof GoalMenuViewHolder) {
            ((GoalMenuViewHolder) holder).bind(item, listener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder for regular menu items
    static class RegularMenuViewHolder extends RecyclerView.ViewHolder {
        CardView menuCard;
        ImageView menuIcon;
        TextView menuTitle;
        TextView menuDescription;

        public RegularMenuViewHolder(@NonNull View itemView) {
            super(itemView);
            menuCard = itemView.findViewById(R.id.menuCard);
            menuIcon = itemView.findViewById(R.id.menuIcon);
            menuTitle = itemView.findViewById(R.id.menuTitle);
            menuDescription = itemView.findViewById(R.id.menuDescription);
        }

        public void bind(ProfileMenuItem item, OnMenuItemClickListener listener) {
            menuIcon.setImageResource(item.getIconRes());
            menuTitle.setText(item.getTitle());
            menuDescription.setText(item.getDescription());

            menuCard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMenuItemClick(item.getId());
                }
            });
        }
    }

    // ViewHolder for goal menu item
    static class GoalMenuViewHolder extends RecyclerView.ViewHolder {
        CardView menuCard;
        TextView goalTitle;
        TextView goalDescription;
        Button btnStartGoal;

        public GoalMenuViewHolder(@NonNull View itemView) {
            super(itemView);
            menuCard = itemView.findViewById(R.id.menuCard);
            goalTitle = itemView.findViewById(R.id.goalTitle);
            goalDescription = itemView.findViewById(R.id.goalDescription);
            btnStartGoal = itemView.findViewById(R.id.btnStartGoal);
        }

        public void bind(ProfileMenuItem item, OnMenuItemClickListener listener) {
            goalTitle.setText(item.getTitle());
            goalDescription.setText(item.getDescription());

            menuCard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMenuItemClick(item.getId());
                }
            });

            btnStartGoal.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMenuItemClick(item.getId());
                }
            });
        }
    }
}
