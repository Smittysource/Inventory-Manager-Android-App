package com.smittysource.inventorymanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.Date;

// This class provides functionality for the login process
public class LoginActivity extends AppCompatActivity {

    // Create fields to store UI elements
    private Button loginButton;
    private Button registerButton;
    private Button cancelButton;
    private Button acceptButton;
    private Button forgotButton;
    private EditText usernameEdit;
    private EditText passwordEdit;
    private EditText nameEdit;
    private EditText phoneEdit;

    // Create fields for process control variables
    private boolean registering;
    private boolean forgot;
    public enum ViewType { LOGIN, REGISTER, FORGOT }

    private DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set UI control variables
        registering = false;    // Used to show different layout based on login in or registering
        forgot = false;         // Used to show different layout based on login in or forgot

        // Assign fields with UI elements
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        cancelButton = findViewById(R.id.registerCancelButton);
        acceptButton = findViewById(R.id.registerAcceptButton);
        forgotButton = findViewById(R.id.forgotButton);
        nameEdit = findViewById(R.id.editName);
        phoneEdit = findViewById(R.id.editTextPhone);
        passwordEdit = findViewById(R.id.editTextPassword);
        usernameEdit = findViewById(R.id.editTextEditUsername);

        // Set UI to login mode
        setView(ViewType.LOGIN);

        // Create onclick for the login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create connection to database
                dbHandler = DBHandler.getInstance(LoginActivity.this);

                // Retrieve data from UI elements
                String username = usernameEdit.getText().toString().toLowerCase();
                String password = passwordEdit.getText().toString();

                // Check if username or password is not empty
                if(isEmailValid(username) && password.length() != 0) {

                    // Retrieve user from database
                    UserModel user = dbHandler.getUser(username);

                    // If user not in database
                    if(user == null) {
                        // Notify that the user does not exist
                        Toast.makeText( LoginActivity.this, R.string.user_does_not_exist,
                                        Toast.LENGTH_SHORT).show();
                    }
                    // Check if the password from the database is the same as the one entered
                    else if (user.getPassword().equals(password)){
                        // Connect to shared preferences
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                        // Add data to shared preferences
                        prefs.edit().putBoolean("Islogin", true).commit();
                        Date time = new Date();
                        long millis = time.getTime();
                        prefs.edit().putLong("Logintime", millis).commit();
                        prefs.edit().putString("username", username).commit();
                        prefs.edit().putString("phone", user.getPhone()).commit();
                        prefs.edit().putString("name", user.getName()).commit();

                        // Notify user that login was successful
                        Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();

                        // End activity
                        finish();
                    }
                    // Notify user that password is incorrect
                    else {
                        Toast.makeText( LoginActivity.this, R.string.invalid_password,
                                        Toast.LENGTH_SHORT).show();
                    }
                }
                // Notify user that all fields are required
                else {
                    Toast.makeText(LoginActivity.this, R.string.fields_required, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Create onclick for register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change control variable to show register UI and activate registering logic
                registering = true;
                setView(ViewType.REGISTER);
            }
        });

        // Create onclick for cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Change view to login and reset logic controls
                registering = false;
                forgot = false;
                setView(ViewType.LOGIN);
            }
        });

        // Create onclick for accept button
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Accept is only visible during register or forgot. Check which is currently active
                // Register user
                if(registering) {
                    // Retrieve data from UI elements
                    String username = usernameEdit.getText().toString();
                    String phone = phoneEdit.getText().toString();
                    String password = passwordEdit.getText().toString();
                    String name = nameEdit.getText().toString();

                    // Check if username and password are valid
                    if(isEmailValid(username) && (phone.length() == 10)
                        && name.length() != 0 && password.length() !=0) {

                        // Create connection to database
                        dbHandler = DBHandler.getInstance(LoginActivity.this);

                        // Store data in a UserModel
                        UserModel user = new UserModel( name, phone, username.toLowerCase(),
                                                        password);

                        // Insert the new user into the database
                        boolean success = dbHandler.addNewUser(user);

                        // Notify user of success or failure
                        if(success) {
                            Toast.makeText( LoginActivity.this, R.string.user_added,
                                            Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText( LoginActivity.this, R.string.user_add_error,
                                            Toast.LENGTH_SHORT).show();
                        }

                        // Reset login form and control variable
                        nameEdit.setText("");
                        passwordEdit.setText("");
                        nameEdit.setText("");
                        phoneEdit.setText("");
                        usernameEdit.setText("");
                        setView(ViewType.LOGIN);
                        registering = false;
                    }
                    else {
                        // Notify user that data entered is invalid
                        Toast.makeText( LoginActivity.this, R.string.fields_required,
                                        Toast.LENGTH_SHORT).show();
                    }
                }
                // Process password recovery
                else if(forgot) {

                    // Retrieve data from UI elements
                    String username = usernameEdit.getText().toString();
                    String phone = phoneEdit.getText().toString();

                    // If username and phone number are valid
                    if(isEmailValid(username) && (phone.length() == 7 || phone.length() == 10)) {

                        // Create connection to database
                        DBHandler dbHandler = DBHandler.getInstance(LoginActivity.this);

                        // Retrieve user from database
                        UserModel user = dbHandler.getUser(username.toLowerCase());

                        if(user == null) {
                            // User does not exist, notify of error
                            Toast.makeText( LoginActivity.this, R.string.user_does_not_exist,
                                            Toast.LENGTH_SHORT).show();
                        } else if (user.getPhone().equals(phone)){
                            // User exists and phone number is correct, toast the password
                            Toast.makeText( LoginActivity.this, String.format(
                                            getString(R.string.password_retrieved),
                                            user.getPassword()), Toast.LENGTH_LONG).show();

                            // Return to login view and reset control variable
                            forgot = false;
                            setView(ViewType.LOGIN);
                        }
                        else {
                            //
                            Toast.makeText( LoginActivity.this, R.string.invalid_phone,
                                            Toast.LENGTH_SHORT).show();
                        }
                    // Field left blank or invalid, notify user
                    } else {
                        Toast.makeText(LoginActivity.this, R.string.fields_required, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Create onclick for forgot button
        forgotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Set UI to forgot layout and set control variable
                setView(ViewType.FORGOT);
                forgot = true;
            }
        });
    }

    // Handle hiding and showing UI elements based on requested user action
    public void setView(ViewType viewType) {
        switch(viewType) {
            case LOGIN:
            default:
                loginButton.setVisibility(View.VISIBLE);
                forgotButton.setVisibility(View.VISIBLE);
                registerButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.GONE);
                acceptButton.setVisibility(View.GONE);
                passwordEdit.setVisibility(View.VISIBLE);
                nameEdit.setVisibility(View.GONE);
                phoneEdit.setVisibility(View.GONE);
                break;
            case REGISTER:
                passwordEdit.setVisibility(View.VISIBLE);
                nameEdit.setVisibility(View.VISIBLE);
                phoneEdit.setVisibility(View.VISIBLE);
                loginButton.setVisibility(View.GONE);
                forgotButton.setVisibility(View.GONE);
                registerButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.VISIBLE);
                acceptButton.setVisibility(View.VISIBLE);
                break;
            case FORGOT:
                loginButton.setVisibility(View.GONE);
                forgotButton.setVisibility(View.GONE);
                registerButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.VISIBLE);
                acceptButton.setVisibility(View.VISIBLE);
                passwordEdit.setVisibility(View.GONE);
                nameEdit.setVisibility(View.GONE);
                phoneEdit.setVisibility(View.VISIBLE);
                break;
        }
    }

    // Check if email matches standard format
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                .matches();
    }
}