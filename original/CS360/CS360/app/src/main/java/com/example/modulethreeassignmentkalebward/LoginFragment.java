package com.example.modulethreeassignmentkalebward;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment {

    private TextInputEditText etUsername, etPassword;
    private TextInputLayout usernameLayout, passwordLayout;
    private Button btnLogin, btnCreateAccount;
    private DatabaseHelper databaseHelper;
    private SharedPreferences preferences;

    // Constants for saving user session
    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_login, container, false);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(getContext());

        // Get shared preferences for saving login session
        preferences = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Check if user is already logged in
        if (isLoggedIn()) {
            navigateToDataDisplay(getLoggedInUserId(), getLoggedInUsername());
            return view;
        }

        // Initialize UI components
        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        usernameLayout = view.findViewById(R.id.usernameLayout);
        passwordLayout = view.findViewById(R.id.passwordLayout);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnCreateAccount = view.findViewById(R.id.btnCreateAccount);

        // Set click listeners
        btnLogin.setOnClickListener(v -> attemptLogin());
        btnCreateAccount.setOnClickListener(v -> createNewAccount());

        return view;
    }

    private boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    private long getLoggedInUserId() {
        return preferences.getLong(KEY_USER_ID, -1);
    }
    private String getLoggedInUsername() {
        return preferences.getString(KEY_USERNAME, "");
    }

    private void saveLoginSession(long userId, String username) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public static void clearLoginSession(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    private void attemptLogin() {
        // Reset any previous errors
        usernameLayout.setError(null);
        passwordLayout.setError(null);

        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Validate inputs
        boolean cancel = false;
        View focusView = null;

        // Check for valid password
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.error_field_required));
            focusView = etPassword;
            cancel = true;
        }

        // Check for valid username
        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError(getString(R.string.error_field_required));
            focusView = etUsername;
            cancel = true;
        }

        if (cancel) {
            // There was an error; focus the first form field with an error
            if (focusView != null) {
                focusView.requestFocus();
            }
        } else {
            // Attempt authentication with database
            long userId = databaseHelper.authenticateUser(username, password);

            if (userId != -1) {
                // Login successful
                saveLoginSession(userId, username);
                Toast.makeText(getContext(), R.string.login_success, Toast.LENGTH_SHORT).show();
                navigateToDataDisplay(userId, username);
            } else {
                // Login failed
                Toast.makeText(getContext(), R.string.error_invalid_credentials, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createNewAccount() {
        // Reset any previous errors
        usernameLayout.setError(null);
        passwordLayout.setError(null);

        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Validate inputs
        boolean cancel = false;
        View focusView = null;

        // Check for valid password
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.error_field_required));
            focusView = etPassword;
            cancel = true;
        } else if (password.length() < 4) {
            passwordLayout.setError(getString(R.string.error_password_too_short));
            focusView = etPassword;
            cancel = true;
        }

        // Check for valid username
        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError(getString(R.string.error_field_required));
            focusView = etUsername;
            cancel = true;
        } else if (username.length() < 3) {
            usernameLayout.setError(getString(R.string.error_username_too_short));
            focusView = etUsername;
            cancel = true;
        }

        if (cancel) {
            // There was an error; focus the first form field with an error
            if (focusView != null) {
                focusView.requestFocus();
            }
        } else {
            // Attempt to create account in database
            long userId = databaseHelper.addUser(username, password);

            if (userId != -1) {
                // Account creation successful
                saveLoginSession(userId, username);
                Toast.makeText(getContext(), R.string.account_created, Toast.LENGTH_SHORT).show();
                navigateToDataDisplay(userId, username);
            } else {
                // Account creation failed - likely username already exists
                usernameLayout.setError(getString(R.string.error_username_exists));
                etUsername.requestFocus();
            }
        }
    }

    private void navigateToDataDisplay(long userId, String username) {
        // Create an instance of the DataDisplayFragment
        DataDisplayFragment dataDisplayFragment = new DataDisplayFragment();

        // Pass data to the fragment
        Bundle args = new Bundle();
        args.putString("username", username);
        args.putLong("userId", userId);
        dataDisplayFragment.setArguments(args);

        // Get the fragment manager and begin a transaction
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

        // Replace the current fragment with the data display fragment
        transaction.replace(R.id.fragment_container, dataDisplayFragment);

        // Add the transaction to the back stack
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }
}