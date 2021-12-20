package com.smittysource.inventorymanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

// This class provides functionality for adding an item to the inventory
public class AddItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // Create local variables and assign UI elements
        Button addCancel = findViewById(R.id.addItemCancel);
        Button addAccept = findViewById(R.id.addItemAccept);
        EditText descriptionEdit = findViewById(R.id.addItemDescription);
        EditText countEdit = findViewById(R.id.addItemCount);

        // Create onclick for cancel button
        addCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Create onclick for accept button
        addAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve data from UI elements
                String description = descriptionEdit.getText().toString();
                String countString =  countEdit.getText().toString();

                // Ensure data is not empty
                if(description.length() == 0 || countString.length() == 0) {
                    // Notify user that all fields are required
                    Toast.makeText(AddItemActivity.this, R.string.fields_required, Toast.LENGTH_SHORT).show();
                } else {
                    // Connect to database handler
                    DBHandler dbHandler = DBHandler.getInstance(AddItemActivity.this);

                    // Create new ItemModel with retrieved data
                    ItemModel item = new ItemModel(description, Integer.parseInt(countString));

                    // Add new item to database
                    int id = (int) dbHandler.addNewItem(item);

                    /*If id returned from item insertion is > -1, it was added successfully
                      Notify user of success or failure
                    */
                    if(id > -1) {
                        Toast.makeText(AddItemActivity.this, R.string.add_item_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AddItemActivity.this, R.string.add_item_fail, Toast.LENGTH_SHORT).show();
                    }

                    // Create new intent and store the added item's data
                    Intent intent = new Intent();
                    intent.putExtra("id", id);
                    intent.putExtra("name", item.getName());
                    intent.putExtra("count", item.getCount());

                    // Send result to calling activity
                    setResult(RESULT_OK, intent);

                    // Close activity
                    finish();
                }
            }
        });
    }
}