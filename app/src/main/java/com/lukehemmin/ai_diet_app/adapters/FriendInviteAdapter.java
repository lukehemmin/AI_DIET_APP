package com.lukehemmin.ai_diet_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.data.model.FriendResponse;
import com.lukehemmin.ai_diet_app.utils.UrlUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FriendInviteAdapter extends RecyclerView.Adapter<FriendInviteAdapter.FriendInviteViewHolder> {

    private List<FriendResponse> friends = new ArrayList<>();
    private Set<String> selectedFriendIds = new HashSet<>();

    public void setFriends(List<FriendResponse> friends) {
        this.friends = friends != null ? friends : new ArrayList<>();
        selectedFriendIds.clear();
        notifyDataSetChanged();
    }

    public List<String> getSelectedFriendIds() {
        return new ArrayList<>(selectedFriendIds);
    }

    @NonNull
    @Override
    public FriendInviteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_invite, parent, false);
        return new FriendInviteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendInviteViewHolder holder, int position) {
        FriendResponse friend = friends.get(position);
        holder.bind(friend);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    class FriendInviteViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cbInvite;
        private ImageView ivProfile;
        private TextView tvName;

        public FriendInviteViewHolder(@NonNull View itemView) {
            super(itemView);
            cbInvite = itemView.findViewById(R.id.cb_invite);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            tvName = itemView.findViewById(R.id.tv_name);

            itemView.setOnClickListener(v -> {
                cbInvite.setChecked(!cbInvite.isChecked());
            });

            cbInvite.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    String friendId = friends.get(pos).getId();
                    if (isChecked) {
                        selectedFriendIds.add(friendId);
                    } else {
                        selectedFriendIds.remove(friendId);
                    }
                }
            });
        }

        public void bind(FriendResponse friend) {
            tvName.setText(friend.getName());
            
            // 체크 상태 복원
            cbInvite.setChecked(selectedFriendIds.contains(friend.getId()));

            // 프로필 이미지 로드
            if (friend.getProfileImageUrl() != null && !friend.getProfileImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(UrlUtils.ensureHttps(friend.getProfileImageUrl()))
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(ivProfile);
            } else {
                ivProfile.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }
    }
}
