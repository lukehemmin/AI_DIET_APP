package com.lukehemmin.ai_diet_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.data.model.ChallengeResponse;

import java.util.ArrayList;
import java.util.List;

public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder> {

    private List<ChallengeResponse> challenges = new ArrayList<>();
    private OnChallengeActionListener listener;

    public interface OnChallengeActionListener {
        void onJoin(String challengeId);
    }

    public void setOnChallengeActionListener(OnChallengeActionListener listener) {
        this.listener = listener;
    }

    public void setChallenges(List<ChallengeResponse> challenges) {
        this.challenges = challenges;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_challenge, parent, false);
        return new ChallengeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        holder.bind(challenges.get(position));
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    class ChallengeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvGoal;
        ImageView imgIcon;
        Button btnJoin;

        public ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_challenge_title);
            tvDescription = itemView.findViewById(R.id.tv_challenge_description);
            tvGoal = itemView.findViewById(R.id.tv_challenge_goal);
            imgIcon = itemView.findViewById(R.id.img_challenge_icon);
            btnJoin = itemView.findViewById(R.id.btn_join_challenge);
        }

        void bind(ChallengeResponse challenge) {
            tvTitle.setText(challenge.getTitle());
            tvDescription.setText(challenge.getDescription());
            
            String goalText = "목표: " + challenge.getGoal() + " " + challenge.getGoalUnit();
            tvGoal.setText(goalText);

            // TODO: Set icon based on challenge.getIcon() if needed.
            // For now, we use default.

            btnJoin.setOnClickListener(v -> {
                if (listener != null) listener.onJoin(challenge.getId());
            });
        }
    }
}
