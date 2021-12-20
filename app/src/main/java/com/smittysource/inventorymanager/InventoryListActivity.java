package com.smittysource.inventorymanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/* This class provides the main functionality of the app.
 */
public class InventoryListActivity extends AppCompatActivity implements
             MyRecyclerViewAdapter.ItemClickListener, RecyclerViewClickInterface {

    // Create fields for class
    private MyRecyclerViewAdapter itemsAdapter;     // Adapter for items
    private MyRecyclerViewAdapter headerAdapter;    // Adapter for header
    private final int REQUEST_SMS_CODE = 0;         // Used with PermissionUtil
    private final int MAX_LOGIN_TIME = 10;          // Minutes to stay logged in
    private final int ADD_ITEM_RESPONSE = 1;        // Code for response from AddItemActivity
    private final int EDIT_ITEM_RESPONSE = 2;       // Code for response from EditItemActivity
    SharedPreferences prefs;                        // Local connector for shared preferences
    private HashMap changedCounts = new HashMap();  // Items whose counts changed

    // Items for inventory
    ArrayList<ItemModel> items = new ArrayList<ItemModel>();
    // Items that have been deleted
    private ArrayList<Integer> deletedItemIds = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_list);

        // Connect to shared preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Connect to database
        DBHandler dbHandler = DBHandler.getInstance(InventoryListActivity.this);
        // Retrieve items from database
        items = dbHandler.getItems();

        // Create recycler view
        RecyclerView recyclerView = findViewById(R.id.rvInventoryItems);

        // Limit view to one item per row
        int numberOfColumns = 1;

        // Set the layout manager
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));

        // Create an adapter for the item list recycler
        itemsAdapter = new MyRecyclerViewAdapter(this, items, this);

        // Create an adapter for the header recycler
        headerAdapter = new MyRecyclerViewAdapter(this, null, null);

        // Set click listener for items adapter, ignoring clicks from header
        itemsAdapter.setClickListener(this);

        // Combine the item and header adapters into one concat adapter
        ConcatAdapter concatAdapter = new ConcatAdapter(headerAdapter, itemsAdapter);

        // Set the adapter for the recycler view to contain both adapters
        recyclerView.setAdapter(concatAdapter);
    }

    // Utility to check if user is logged in. If not, force login
    private void checkLogin() {

        // Get shared preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // If user is logged in
        if(prefs.getBoolean("Islogin", false)) {
            // Get current time
            Date time = new Date();
            // Get the time the user logged in
            Date loginTime = new Date();
            long millis = prefs.getLong("Logintime", 0);
            loginTime.setTime(millis);
            // Add MAX_LOGIN_TIME minutes to loginTime
            Date expireTime = Date.from(loginTime.toInstant().plus(MAX_LOGIN_TIME,
                                        ChronoUnit.MINUTES));

            // If the expireTime is before the current time
            if(expireTime.compareTo(time) < 0) {
                // Logout
                prefs.edit().putBoolean("Islogin", false).commit();
            }
        }
        // Force login if not logged in
        if(!prefs.getBoolean("Islogin", false)) {
            // Launch login activity
            Intent myIntent = new Intent(   InventoryListActivity.this,
                                            LoginActivity.class);
            this.startActivity(myIntent);
        }
    }

    // Run when app is no longer visible
    @Override
    public void onStop() {
        super.onStop();
        // Remove deleted items from database and from changedCounts
        DBHandler dbHandler = DBHandler.getInstance(InventoryListActivity.this);

        // Remove deleted items from database
        dbHandler.deleteItems(deletedItemIds);

        // Remove deleted items from map of items that have had their counts changed
        for(int id : deletedItemIds) {
            changedCounts.remove(id);
        }

        // Update changed counts in database
        dbHandler.modifyItemCounts(changedCounts);

        // Empty deletedItemIds and changedCounts to avoid continually processing old ones
        deletedItemIds.clear();
        changedCounts.clear();
    }

    // Run when this activity resumes from another activity, onStop or onPause
    @Override
    public void onResume() {
        super.onResume();

        // Check if a user is logged in, force log in if not
        checkLogin();
    }

    // Show menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.inventory_menu, menu);
        return true;
    }

    // Callback for item selected from menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.action_login:
                // Run login activity
                intent = new Intent(InventoryListActivity.this, LoginActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_add:
                // Run add item activity expecting a response
                intent = new Intent(InventoryListActivity.this, AddItemActivity.class);
                startActivityForResult(intent, ADD_ITEM_RESPONSE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Notify user through SMS when an item reaches a count of 0
    public void notifyZero(String name) {
        // If the app has permission to send an SMS message
        if(PermissionsUtil.hasPermissions(  this, Manifest.permission.SEND_SMS,
                                            R.string.sms_rationale, REQUEST_SMS_CODE)) {
            try {
                // Get shared preferences
                prefs = PreferenceManager.getDefaultSharedPreferences(this);

                // Retrieve phone number and add 1 to beginning for US country code
                String phone = String.format("1%s",prefs.getString("phone", null));

                // Retrieve user's name from shared preferences to personalize text message
                String user_name = prefs.getString("name", null);

                // If phone isn't set, cancel sending message
                if(phone == null) {
                    return;
                } else {
                    // Connect to the smsManager
                    SmsManager smsManager = SmsManager.getDefault();

                    // Send the message
                    smsManager.sendTextMessage( phone,null, String.format(
                                                "%s, this is InventoryManager+ letting you know" +
                                                " that %s is out of stock.", user_name, name),
                                        null,null);

                    // Notify user that message was sent
                    Toast.makeText( getApplicationContext(), "Message Sent",
                                    Toast.LENGTH_LONG).show();
                }
            } catch (Exception ex) {
                // Notify user of error
                Toast.makeText(getApplicationContext(), ex.getMessage().toString(),
                        Toast.LENGTH_LONG).show();
                // Dump logging data
                ex.printStackTrace();
            }
        }
    }

    // Add item to arraylist and notify recyclerview adapter that item has been added
    public void addItem(ItemModel item) {
        items.add(item);
        itemsAdapter.notifyItemInserted(items.size()-1);
    }

    // Update item in arraylist and notify recyclerview adapter that item has changed
    public void updateItem(ItemModel item, int position) {
        items.set(position, item);
        itemsAdapter.notifyItemChanged(position);
    }

    // Enable onclick events
    @Override
    public void onItemClick(View view, int position) {
    }

    // Called when item recyclerview's delete icon is clicked
    @Override
    public void onDeleteClick(int position) {
        // Add the id to the list of deleted items
        deletedItemIds.add(items.get(position).getId());

        // Remove the item from the arraylist
        items.remove(position);

        // Notify the item was removed to the recyclerview
        itemsAdapter.notifyItemRemoved(position);
    }

    // Called when item recyclerview's edit icon is clicked
    @Override
    public void onEditClick(int position) {

        // Get the item to be edited from the arraylist
        ItemModel item = items.get(position);

        // Prepare to start the EditItemActivity
        Intent intent = new Intent(InventoryListActivity.this, EditItemActivity.class);

        // Load data to pass to EditItemActivity into the intent
        intent.putExtra("id", item.getId());
        intent.putExtra("name", item.getName());
        intent.putExtra("count", item.getCount());
        intent.putExtra("position", position);

        // Start EditItemActivity expecting a response
        startActivityForResult(intent, EDIT_ITEM_RESPONSE);

    }

    // Called when item recyclerview's add icon is clicked
    @Override
    public void onIncrementClick(int position) {

        // Get the item from arraylist
        ItemModel item = items.get(position);

        // Increment item's count
        item.setCount(item.getCount() + 1);

        // Replace item in the arraylist
        items.set(position, item);

        // Notify item changed to item recyclerview
        itemsAdapter.notifyItemChanged(position);

        // Store id and count of item for saving to database
        changedCounts.put(item.getId(), item.getCount());
    }


    // Called when item recyclerview's minus icon is clicked
    @Override
    public void onDecrementClick(int position) {

        // Get the item from the arrayList
        ItemModel item = items.get(position);

        // Get the item's count
        int count = item.getCount();

        /* Only allow decrement if item's count is more than 0 (avoids multiple sms when item count
           is already 0.
        */
        if(count > 0) {
            // Decrement count
            count--;

            // Set count back to item
            item.setCount(count);

            // Notify if count has reached zero
            if(count == 0) {
                notifyZero(item.getName());
            }

            // Replace item in the arraylist
            items.set(position, item);

            // Notify item changed to recyclerview adapter
            itemsAdapter.notifyItemChanged(position);
        }

        // Store to write to database
        changedCounts.put(item.getId(), item.getCount());
    }

    // This method is called when an activity started for result sends a response
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check if it is the AddItemActivity with an OK result
        if (requestCode == ADD_ITEM_RESPONSE) {
            if (resultCode == RESULT_OK) {

                // Retrieve added item information from intent
                ItemModel item = new ItemModel( data.getStringExtra("name"),
                                                data.getIntExtra("count", -1),
                                                data.getIntExtra("id", -1));

                // Add item to recyclerview and arraylist
                addItem(item);
            }
        }

        // Check if it is EditItemActivity with an OK result
        else if (requestCode == EDIT_ITEM_RESPONSE) {
            if(resultCode == RESULT_OK) {

                // Get position of edited item
                int position = data.getIntExtra("position", -1);

                // Store response data in new ItemModel
                ItemModel item = new ItemModel( data.getStringExtra("name"),
                                                data.getIntExtra("count", -1),
                                                data.getIntExtra("id", -1));

                // If the item was updated
                if(position > -1) {
                    // Update item in arraylist and notify recyclerview of change
                    updateItem(item, position);
                }
            }
        }
    }
}