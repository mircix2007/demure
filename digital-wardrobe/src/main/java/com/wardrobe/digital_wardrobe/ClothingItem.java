package com.wardrobe.digital_wardrobe;

public class ClothingItem {
    private String id;
    private String userId;
    private String name;
    private String category;
    private String imageUrl;

    public ClothingItem() {}

    public ClothingItem(String userId, String name, String category, String imageUrl) {
        this.userId = userId;
        this.name = name;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}