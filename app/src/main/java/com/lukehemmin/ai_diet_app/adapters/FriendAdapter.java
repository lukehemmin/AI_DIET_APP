package com.lukehemmin.ai_diet_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.data.model.FriendResponse;

import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private List<FriendResponse> friends = new ArrayList<>();
    private final OnFriendActionListener listener;
    private final boolean isRequestList;

    public interface OnFriendActionListener {
        void onAccept(String friendshipId);
    }

    public FriendAdapter(boolean isRequestList, OnFriendActionListener listener) {
        this.isRequestList = isRequestList;
        this.listener = listener;
    }

    public void setFriends(List<FriendResponse> friends) {
        this.friends = friends;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        FriendResponse friend = friends.get(position);
        holder.bind(friend);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        Button btnAccept;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_friend_name);
            tvEmail = itemView.findViewById(R.id.tv_friend_email);
            btnAccept = itemView.findViewById(R.id.btn_accept_friend); // Ensure this ID exists in item_friend.xml
        }

        void bind(FriendResponse friend) {
            tvName.setText(friend.getName());
            tvEmail.setText(friend.getEmail());

            if (isRequestList) {
                btnAccept.setVisibility(View.VISIBLE);
                btnAccept.setOnClickListener(v -> {
                    if (listener != null) listener.onAccept(friend.getId());
                });
            } else {
                btnAccept.setVisibility(View.GONE);
            }
        }
    }
}
