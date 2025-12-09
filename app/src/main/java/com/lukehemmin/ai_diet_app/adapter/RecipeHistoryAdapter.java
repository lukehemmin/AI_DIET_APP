package com.lukehemmin.ai_diet_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.data.model.FridgeRecipeHistoryResponse;

import java.util.ArrayList;
import java.util.List;

public class RecipeHistoryAdapter extends RecyclerView.Adapter<RecipeHistoryAdapter.ViewHolder> {

    private List<FridgeRecipeHistoryResponse> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FridgeRecipeHistoryResponse item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<FridgeRecipeHistoryResponse> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FridgeRecipeHistoryResponse item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtDate;
        private TextView txtIngredients;
        private TextView txtPreview;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txt_date);
            txtIngredients = itemView.findViewById(R.id.txt_ingredients);
            txtPreview = itemView.findViewById(R.id.txt_preview);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(items.get(pos));
                }
            });
        }

        void bind(FridgeRecipeHistoryResponse item) {
            txtDate.setText(item.getFormattedDate());
            txtIngredients.setText(item.getIngredients());
            
            // 레시피 미리보기 (처음 80자)
            String content = item.getContent();
            if (content != null && content.length() > 80) {
                content = content.substring(0, 80) + "...";
            }
            txtPreview.setText(content);
        }
    }
}
