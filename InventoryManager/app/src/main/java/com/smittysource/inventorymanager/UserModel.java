package com.smittysource.inventorymanager;

// This class provides a storage object for users
public class UserModel {

    // Delcare class fields
    private int id;             // Database id
    private String name;        // Name
    private String phone;       // 10 Digit Phone number
    private String username;    // Username - email address
    private String password;    // Password

    // Constructor with all parameters
    public UserModel(int id, String name, String phone, String username, String password) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.username = username;
        this.password = password;
    }

    // Constructor without database id
    public UserModel(String name, String phone, String username, String password) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.username = username;
        this.password = password;
    }

    // Get database id
    public int getId() {
        return id;
    }

    // Set database id
    public void setId(int id) {
        this.id = id;
    }

    // Get name
    public String getName() {
        return name;
    }

    // Set name
    public void setName(String name) {
        this.name = name;
    }

    // Get phone number
    public String getPhone() {
        return phone;
    }

    // Set phone number
    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Get username
    public String getUsername() {
        return username;
    }

    // Set username
    public void setUsername(String username) {
        this.username = username;
    }

    // Get password
    public String getPassword() {
        return password;
    }

    // Set password
    public void setPassword(String password) {
        this.password = password;
    }

    // Create custom toString
    @Override
    public String toString() {
        return "UserModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
