package com.smittysource.inventorymanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

// This class provides functionality for editing an item in the inventory
public class EditItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        // Retrieve passed in intent
        Intent inIntent = getIntent();

        // Declare and assign local variables
        EditText editName = findViewById(R.id.editTextEditItemDescription);
        EditText editCount = findViewById(R.id.editTextEditItemCount);
        Button editCancel = findViewById(R.id.editItemCancelButton);
        Button editAccept = findViewById(R.id.editItemAcceptButton);
        int mId;
        int mPosition;

        // Retrieve data from inIntent and place in UI controls
        editName.setText(inIntent.getStringExtra("name"));
        editCount.setText(String.format("%d", inIntent.getIntExtra("count", -1)));
        mId = inIntent.getIntExtra("id", -1);
        mPosition = inIntent.getIntExtra("position", -1);

        // Create onclick for cancel button
        editCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Create onclick for accept button
        editAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Retrieve data from UI elements
                String description = editName.getText().toString();
                String countString = editCount.getText().toString();

                // Ensure data is not empty
                if(description.length() == 0 || countString.length() == 0) {
                    // Notify user that all fields are required
                    Toast.makeText(EditItemActivity.this, R.string.fields_required, Toast.LENGTH_SHORT).show();
                } else {
                    // Connect to database handler
                    DBHandler dbHandler = DBHandler.getInstance(EditItemActivity.this);

                    // Create new ItemModel with retrieved data
                    ItemModel item = new ItemModel(description, Integer.parseInt(countString), mId);

                    // Update item in database
                    int success = (int) dbHandler.modifyItem(item);

                    // Failure is noted by a -1 result. Notify user of success or failure
                    if(success > -1) {
                        Toast.makeText(EditItemActivity.this, R.string.edit_item_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditItemActivity.this, R.string.edit_item_fail, Toast.LENGTH_SHORT).show();
                    }

                    // Create new intent and store the updated item's data
                    Intent intent = new Intent();
                    intent.putExtra("id", mId);
                    intent.putExtra("name", editName.getText().toString());
                    intent.putExtra("count", Integer.parseInt(editCount.getText().toString()));
                    intent.putExtra("position", mPosition);

                    // Send result to calling activity
                    setResult(RESULT_OK, intent);

                    // Close activity
                    finish();
                }
            }
        });
    }
}