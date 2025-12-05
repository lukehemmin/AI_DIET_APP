package com.lukehemmin.ai_diet_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.data.model.MealAnalysisResult;

import java.util.List;
import java.util.Locale;

public class AnalysisResultAdapter extends RecyclerView.Adapter<AnalysisResultAdapter.ViewHolder> {

    private List<MealAnalysisResult> items;
    private OnItemDeleteListener deleteListener;

    public interface OnItemDeleteListener {
        void onDelete(int position);
    }

    public AnalysisResultAdapter(List<MealAnalysisResult> items, OnItemDeleteListener deleteListener) {
        this.items = items;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_analysis_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealAnalysisResult item = items.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<MealAnalysisResult> getItems() {
        return items;
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, items.size());
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        TextView txtServing;
        TextView txtCalories;
        ImageView btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_food_name);
            txtServing = itemView.findViewById(R.id.txt_serving_size);
            txtCalories = itemView.findViewById(R.id.txt_calories);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        void bind(MealAnalysisResult item, int position) {
            txtName.setText(item.getFoodItem());
            
            // If serving size is provided, show it (e.g. "400g"). Default to "1인분" if null or standard.
            // The mockup showed specific grams like "400g".
            if (item.getServingSize() != null) {
                // Assuming servingSize might be gram or unit. Let's just show the number if available.
                // Or we can format it. For now just showing the value.
                txtServing.setText(String.format(Locale.US, "%.0fg", item.getServingSize())); 
            } else {
                txtServing.setText("1인분");
            }

            if (item.getKcal() != null) {
                txtCalories.setText(String.format(Locale.US, "%.0f kcal", item.getKcal()));
            } else {
                txtCalories.setText("0 kcal");
            }

            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(getAdapterPosition());
                }
            });
        }
    }
}