package com.lukehemmin.ai_diet_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lukehemmin.ai_diet_app.R;

public class GroupsFragment extends Fragment {

    private RecyclerView rvGroups;
    private TextView btnCreateGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        
        initializeViews(view);
        setupListeners();
        loadGroups();
        
        return view;
    }

    private void initializeViews(View view) {
        rvGroups = view.findViewById(R.id.rv_groups);
        btnCreateGroup = view.findViewById(R.id.btn_create_group);

        rvGroups.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupListeners() {
        btnCreateGroup.setOnClickListener(v -> openCreateGroupDialog());
    }

    private void loadGroups() {
        // TODO: Load groups data
    }

    private void openCreateGroupDialog() {
        // TODO: Open create group dialog
    }
}
