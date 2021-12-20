package com.smittysource.inventorymanager;

import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* This singleton class allows access to the database for storing and retrieving
    users and inventory items
 */
public class DBHandler extends SQLiteOpenHelper {

    // Define class fields
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "inventory.db";
    public static final String USER_TABLE = "USER_TABLE";
    public static final String USER_NAME = "USER_NAME";
    public static final String USER_PHONE = "USER_PHONE";
    public static final String USER_PASSWORD = "USER_PASSWORD";
    public static final String USER_USERNAME = "USER_USERNAME";
    public static final String ITEM_TABLE = "ITEM_TABLE";
    public static final String ITEM_NAME = "ITEM_NAME";
    public static final String ITEM_QUANTITY = "ITEM_QUANTITY";
    public static final String ITEM_ID = "ITEM_ID";

    private static DBHandler myItemDb;

    //  Create as singleton
    public static DBHandler getInstance(Context context) {
        if ( myItemDb == null) {
            myItemDb = new DBHandler(context);
        }
        return myItemDb;
    }

    // Private to avoid new instances
    private DBHandler(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    // Create tables if they don't exist
    @Override
    public void onCreate(SQLiteDatabase db) {

        /* Create USER_TABLE with the following schema
           ID               Integer - Primary Key that auto-increments
           USER_NAME        String  - User's first name
           USER_PHONE       STRING  - User's phone number
           USER_PASSWORD    STRING  - User's password
           USER_USERNAME    STRING  - User's username
        */
        String createUserTableStatement = "CREATE TABLE " + USER_TABLE + " " +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " + USER_NAME + " TEXT, " +
                USER_PHONE + " TEXT, " + USER_PASSWORD + " TEXT, " + USER_USERNAME + " TEXT )";

        db.execSQL(createUserTableStatement);

        /*  Create ITEM_TABLE with the following schema
            ID              Integer - Primary Key that auto-increments
            ITEM_NAME       STRING  - Name or description of item
            ITEM_QUANTITY   Integer - Count of item on hand
         */
        String createItemTableStatement = "CREATE TABLE " + ITEM_TABLE + " " +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " + ITEM_NAME + " TEXT, " +
                ITEM_QUANTITY + " INTEGER)";

        db.execSQL(createItemTableStatement);
    }

    // Add a new user to the database
    public boolean addNewUser(UserModel user) {

        // Create and store content values for command
        ContentValues values = new ContentValues();

        values.put(USER_NAME, user.getName());
        values.put(USER_PASSWORD, user.getPassword());
        values.put(USER_USERNAME, user.getUsername());
        values.put(USER_PHONE, user.getPhone());

        // Open writeable database connection and insert the new user
        SQLiteDatabase db = this.getWritableDatabase();
        long insert = db.insert(USER_TABLE, null, values);
        db.close();
        // Return whether user was added successfully or not
        return insert != -1;
    }

    // Add a new item to the inventory
    public long addNewItem(ItemModel item) {

        // Create and store content values for command
        ContentValues values = new ContentValues();

        values.put(ITEM_NAME, item.getName());
        values.put(ITEM_QUANTITY, item.getCount());

        // Open writeable database connection and insert item
        SQLiteDatabase db = this.getWritableDatabase();
        long insert = db.insert(ITEM_TABLE, null, values);
        db.close();

        // Return id of item inserted
        return insert;
    }

    // Modify an item in the database
    public long modifyItem(ItemModel item) {

        // Create and store content values
        ContentValues values = new ContentValues();

        values.put(ITEM_NAME, item.getName());
        values.put(ITEM_QUANTITY, item.getCount());

        // Open writeable database connection and update item
        SQLiteDatabase db = this.getWritableDatabase();
        long update = db.update(ITEM_TABLE, values,"id=?",
                                new String[]{String.valueOf(item.getId())});
        db.close();

        // Return the row of the item that was updated
        return update;
    }

    // Modify the item count of multiple items
    public void modifyItemCounts(HashMap<Integer, Integer> itemIds) {

        // Open writeable database connection
        SQLiteDatabase db = this.getWritableDatabase();

        // Using the item ids and counts, update the items in the database
        for(Map.Entry<Integer, Integer> entry : itemIds.entrySet()) {
            ContentValues values = new ContentValues();
            values.put(ITEM_QUANTITY, entry.getValue());
            String id = String.valueOf(entry.getKey());

            db.update(ITEM_TABLE, values,"id=?", new String[]{id});
        }
        db.close();
    }

    // Delete multiple items by id
    public void deleteItems(ArrayList<Integer> ids) {
        // Open writeable database connection
        SQLiteDatabase db = this.getWritableDatabase();

        for(Integer id: ids) {
            db.delete(ITEM_TABLE, "id=?", new String[]{id.toString()});
        }
        db.close();
    }

    // Unused method. Retrieves data for all users in the database
    public List<UserModel> getUsers() {
        List<UserModel> returnList = new ArrayList<>();

        String queryString = "SELECT * FROM " + USER_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String phone = cursor.getString(2);
                String username = cursor.getString(3);
                String password = cursor.getString(4);

                UserModel user = new UserModel(id, name, phone, username, password);
                returnList.add(user);
            }while(cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return returnList;
    }

    // Gets all data for all items in the database
    public ArrayList<ItemModel> getItems() {

        // Create storage for items to be returned
        ArrayList<ItemModel> returnList = new ArrayList<>();

        // Create query
        String queryString = "SELECT * FROM " + ITEM_TABLE;

        // Open readable connection to database
        SQLiteDatabase db = this.getReadableDatabase();

        // Run query on database
        Cursor cursor = db.rawQuery(queryString, null);

        // If data exists in the cursor
        if (cursor.moveToFirst()) {
            // Iterate through cursor, retrieving values. Place in an ItemModel
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                int count = cursor.getInt(2);

                ItemModel item = new ItemModel(name, count, id);
                returnList.add(item);
            }while(cursor.moveToNext());
        }

        cursor.close();
        db.close();

        // Return the list of items
        return returnList;
    }

    // Unused method to retrieve a single item
    public ItemModel getItem(int id) {
        ItemModel item = null;
        String queryString = "SELECT * FROM " + ITEM_TABLE + " WHERE ID" + "='" + String.format("%i", id) + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                int mId = cursor.getInt(0);
                String name = cursor.getString(1);
                int count = cursor.getInt(2);
                item = new ItemModel(name, count, mId);
            }
        }
        db.close();
        cursor.close();
        return item;
    }

    // Retrieve a single user from the database
    public UserModel getUser(String mUsername) {

        // Create storage for data to be returned
        UserModel user = null;

        // Create query
        String queryString = "SELECT * FROM " + USER_TABLE + " WHERE " + USER_USERNAME + "='" + mUsername + "'";

        // Get readable connection to database
        SQLiteDatabase db = this.getReadableDatabase();

        // Run query on database
        Cursor cursor = db.rawQuery(queryString, null);

        // If cursor is not empty, retrieve user
        if (cursor.moveToFirst()) {
            // Schema for database uses id, name, phone, password, username
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String phone = cursor.getString(2);
            String password = cursor.getString(3);
            String username = cursor.getString(4);

            // Store data in UserModel
            user = new UserModel(id, name, phone, username, password);
        }
        cursor.close();
        db.close();

        // Return the user data
        return user;
    }

    // Delete a single item in the database
    public void deleteItem(int id) {
        // Open writable connection to database
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete the item based on item id
        db.delete(ITEM_TABLE, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
