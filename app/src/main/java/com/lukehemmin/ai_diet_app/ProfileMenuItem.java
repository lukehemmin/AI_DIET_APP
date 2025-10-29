package com.lukehemmin.ai_diet_app;

public class ProfileMenuItem {
    public static final int TYPE_REGULAR = 0;
    public static final int TYPE_GOAL = 1;

    private int viewType;
    private int iconRes;
    private String title;
    private String description;
    private String id;

    // Constructor for regular menu items
    public ProfileMenuItem(String id, int iconRes, String title, String description) {
        this.viewType = TYPE_REGULAR;
        this.id = id;
        this.iconRes = iconRes;
        this.title = title;
        this.description = description;
    }

    // Constructor for goal item
    public ProfileMenuItem(String id, String title, String description) {
        this.viewType = TYPE_GOAL;
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public int getViewType() {
        return viewType;
    }

    public int getIconRes() {
        return iconRes;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }
}
