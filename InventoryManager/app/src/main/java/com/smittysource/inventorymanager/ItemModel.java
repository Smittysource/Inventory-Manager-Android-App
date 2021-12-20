package com.smittysource.inventorymanager;

// This class provides a storage object for items
public class ItemModel {

    // Declare class fields
    private String name;        // Name or description
    private int count;          // Number on hand
    private int id;             // Database id

    // Constructor with all parameters
    public ItemModel(String name, int count, int id) {
        this.name = name;
        this.count = count;
        this.id = id;
    }

    // Constructor without database id
    public ItemModel(String name, int count) {
        this.name = name;
        this.count = count;
    }

    // Get name or description
    public String getName() {
        return name;
    }

    // Set name or description
    public void setName(String name) {
        this.name = name;
    }

    // Get on hand count
    public int getCount() {
        return count;
    }

    // Set on hand count
    public void setCount(int count) {
        this.count = count;
    }

    // Get database id
    public int getId() {
        return id;
    }

    // Set database id
    public void setId(int id) {
        this.id = id;
    }

    // Create custom toString
    @Override
    public String toString() {
        return "ItemModel{" +
                "name='" + name + '\'' +
                ", count=" + count +
                ", id=" + id +
                '}';
    }
}
