package com.example.modulethreeassignmentkalebward;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CreateAccountFragment extends Fragment {

    private TextInputEditText etUsername, etPassword, etConfirmPassword, etDisplayName, etEmail, etSecurityAnswer;
    private TextInputLayout usernameLayout, passwordLayout, confirmPasswordLayout, displayNameLayout, emailLayout, securityAnswerLayout;
    private Spinner spinnerSecurityQuestion;
    private Button btnCreateAccount, btnBackToLogin;
    private DatabaseHelper databaseHelper;
    private SharedPreferences preferences;

    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final String[] securityQuestions = {
            "Select a security question...",
            "What was the name of your first pet?",
            "What city were you born in?",
            "What was your mother's maiden name?",
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_create_account, container, false);

        databaseHelper = new DatabaseHelper(getContext());
        preferences = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        initializeViews(view);
        setupSecurityQuestionSpinner();
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        etDisplayName = view.findViewById(R.id.etDisplayName);
        etEmail = view.findViewById(R.id.etEmail);
        etSecurityAnswer = view.findViewById(R.id.etSecurityAnswer);

        usernameLayout = view.findViewById(R.id.usernameLayout);
        passwordLayout = view.findViewById(R.id.passwordLayout);
        confirmPasswordLayout = view.findViewById(R.id.confirmPasswordLayout);
        displayNameLayout = view.findViewById(R.id.displayNameLayout);
        emailLayout = view.findViewById(R.id.emailLayout);
        securityAnswerLayout = view.findViewById(R.id.securityAnswerLayout);

        spinnerSecurityQuestion = view.findViewById(R.id.spinnerSecurityQuestion);
        btnCreateAccount = view.findViewById(R.id.btnCreateAccount);
        btnBackToLogin = view.findViewById(R.id.btnBackToLogin);
    }

    private void setupSecurityQuestionSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, securityQuestions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSecurityQuestion.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnCreateAccount.setOnClickListener(v -> attemptCreateAccount());
        btnBackToLogin.setOnClickListener(v -> navigateBackToLogin());
    }

    private void attemptCreateAccount() {
        clearErrors();

        String username = getText(etUsername);
        String password = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);
        String displayName = getText(etDisplayName);
        String email = getText(etEmail);
        String securityAnswer = getText(etSecurityAnswer);
        int questionPosition = spinnerSecurityQuestion.getSelectedItemPosition();

        if (!validateInputs(username, password, confirmPassword, displayName, email,
                securityAnswer, questionPosition)) {
            return;
        }

        String securityQuestion = questionPosition > 0 ? securityQuestions[questionPosition] : null;

        long userId = databaseHelper.addUser(username, password, displayName, email,
                securityQuestion, securityAnswer, "light");

        if (userId != -1) {
            saveLoginSession(userId, username);
            Toast.makeText(getContext(), "Account created successfully!", Toast.LENGTH_SHORT).show();
            navigateToDataDisplay(userId, username);
        } else {
            usernameLayout.setError("Username already exists");
            etUsername.requestFocus();

            Toast.makeText(getContext(), "Username already exists. Try forgot password?",
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateInputs(String username, String password, String confirmPassword,
                                   String displayName, String email, String securityAnswer,
                                   int questionPosition) {
        boolean isValid = true;
        View focusView = null;

        if (questionPosition == 0) {
            Toast.makeText(getContext(), "Please select a security question", Toast.LENGTH_SHORT).show();
            spinnerSecurityQuestion.requestFocus();
            isValid = false;
        }

        if (TextUtils.isEmpty(securityAnswer)) {
            securityAnswerLayout.setError("Security answer is required");
            focusView = etSecurityAnswer;
            isValid = false;
        }

        if (!TextUtils.isEmpty(email) && !isValidEmail(email)) {
            emailLayout.setError("Please enter a valid email address");
            focusView = etEmail;
            isValid = false;
        }

        if (TextUtils.isEmpty(displayName)) {
            displayNameLayout.setError("Display name is required");
            focusView = etDisplayName;
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            focusView = etConfirmPassword;
            isValid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordLayout.setError("Please confirm your password");
            focusView = etConfirmPassword;
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            focusView = etPassword;
            isValid = false;
        } else if (password.length() < 4) {
            passwordLayout.setError("Password must be at least 4 characters");
            focusView = etPassword;
            isValid = false;
        }

        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError("Username is required");
            focusView = etUsername;
            isValid = false;
        } else if (username.length() < 3) {
            usernameLayout.setError("Username must be at least 3 characters");
            focusView = etUsername;
            isValid = false;
        }

        if (focusView != null) {
            focusView.requestFocus();
        }

        return isValid;
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void clearErrors() {
        usernameLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);
        displayNameLayout.setError(null);
        emailLayout.setError(null);
        securityAnswerLayout.setError(null);
    }

    private void saveLoginSession(long userId, String username) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    private void navigateBackToLogin() {
        getParentFragmentManager().popBackStack();
    }

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