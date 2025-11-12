package com.lukehemmin.ai_diet_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lukehemmin.ai_diet_app.R;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lukehemmin.ai_diet_app.adapters.FriendAdapter;
import com.lukehemmin.ai_diet_app.models.Friend;

import java.util.ArrayList;
import java.util.List;

public class ProfileFriendsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FriendAdapter adapter;
    private List<Friend> friendList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_friends, container, false);

        recyclerView = view.findViewById(R.id.rv_friends);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadFriends();

        adapter = new FriendAdapter(friendList);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void loadFriends() {
        friendList = new ArrayList<>();
        // Mock data, replace with actual data source
        friendList.add(new Friend("안병은", R.drawable.ic_launcher_foreground)); 
        friendList.add(new Friend("김철수", R.drawable.ic_launcher_foreground));
    }
}
