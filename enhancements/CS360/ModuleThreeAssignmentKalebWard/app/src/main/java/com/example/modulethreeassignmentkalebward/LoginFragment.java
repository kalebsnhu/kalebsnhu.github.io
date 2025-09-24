package com.example.modulethreeassignmentkalebward;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/*
 * Class: LoginFragment
 * Description: This class allows for the logging in of the user, providing information related to who is logged in,
 * the username of the user, the user's id. 
 */

public class LoginFragment extends Fragment {

    private TextInputEditText etUsername, etPassword;
    private TextInputLayout usernameLayout, passwordLayout;
    private Button btnLogin, btnCreateAccount;
    private TextView tvForgotPassword;
    private DatabaseHelper databaseHelper;
    private SharedPreferences preferences;

    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    /*
    * onCreateView
    * @params()
        *layoutinflater inflater
        *viewgroup container
        *bundle savedInstanceState
    Description: Creates the view for the login fragment, initializes the various views provided
    */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_login, container, false);

        databaseHelper = new DatabaseHelper(getContext());
        preferences = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        if (isLoggedIn()) {
            navigateToDataDisplay(getLoggedInUserId(), getLoggedInUsername());
            return view;
        }

        initializeViews(view);
        setupClickListeners();

        return view;
    }

    /*
    * initializeViews
    * @params()
        *view view
    Description: Actually initializes the views with username, password, and create account and forgotten password views.
    */

    private void initializeViews(View view) {
        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        usernameLayout = view.findViewById(R.id.usernameLayout);
        passwordLayout = view.findViewById(R.id.passwordLayout);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnCreateAccount = view.findViewById(R.id.btnCreateAccount);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
    }

    /*
    * setupClickListeners
    Description: Sets up click listeners for create account and forgotten password.
    */

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        btnCreateAccount.setOnClickListener(v -> navigateToCreateAccount());
        tvForgotPassword.setOnClickListener(v -> navigateToPasswordRecovery());
    }

    /*
    * isLoggedIn
    Description: Returns whether a user is logged in
    */

    private boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /*
    * getLoggedInUserId
    Description: Returns the logged in user's id
    */

    private long getLoggedInUserId() {
        return preferences.getLong(KEY_USER_ID, -1);
    }

    /*
    * getLoggedInUsername
    Description: Returns the logged in user's username
    */

    private String getLoggedInUsername() {
        return preferences.getString(KEY_USERNAME, "");
    }

    /*
    * saveLoginSession
    * @params()
        *long userid
        *string username
    Description: Saves logged in user, with username, id, and setting is_logged in to true
    */

    private void saveLoginSession(long userId, String username) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /*
    * clearLoginSession
    * @params()
        *context context
    Description: Clears current logged in session
    */

    public static void clearLoginSession(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
    
    /*
    * attemptLogin
    Description: Attempts to log in with the provided username and password. Verifies username and password and notifies if incorrect.
    */

    private void attemptLogin() {
        usernameLayout.setError(null);
        passwordLayout.setError(null);

        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            focusView = etPassword;
            cancel = true;
        }

        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError("Username is required");
            focusView = etUsername;
            cancel = true;
        }

        if (cancel) {
            if (focusView != null) {
                focusView.requestFocus();
            }
        } else {
            long userId = databaseHelper.authenticateUser(username, password);

            if (userId != -1) {
                saveLoginSession(userId, username);
                Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                navigateToDataDisplay(userId, username);
            } else {
                Toast.makeText(getContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
                passwordLayout.setError("Invalid credentials");
                etPassword.requestFocus();

                
                tvForgotPassword.setVisibility(View.VISIBLE);
            }
        }
    }

    /*
    * navigateToCreateAccount
    Description: Navigates to the create account location in the UI
    */

    private void navigateToCreateAccount() {
        CreateAccountFragment createAccountFragment = new CreateAccountFragment();

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, createAccountFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /*
    * navigateToPasswordRecovery
    Description: Navigates to the password recovery in the UI
    */

    private void navigateToPasswordRecovery() {
        ForgottenPasswordFragment ForgottenPasswordFragment = new ForgottenPasswordFragment();

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, ForgottenPasswordFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /*
    * navigateToDataDisplay
    * @params()
        *long userid
        *string username
    Description: Navigates to the data display with the username and userid
    */

    private void navigateToDataDisplay(long userId, String username) {
        DataDisplayFragment dataDisplayFragment = new DataDisplayFragment();

        Bundle args = new Bundle();
        args.putString("username", username);
        args.putLong("userId", userId);
        dataDisplayFragment.setArguments(args);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, dataDisplayFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
