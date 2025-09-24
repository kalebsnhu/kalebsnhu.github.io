package com.example.modulethreeassignmentkalebward;

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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ForgottenPasswordFragment extends Fragment {

    private TextView tvSecurityQuestion;
    private CardView cardSecurityQuestion;
    private TextInputEditText etUsername, etSecurityAnswer, etNewPassword, etConfirmPassword;
    private TextInputLayout usernameLayout, securityAnswerLayout, newPasswordLayout, confirmPasswordLayout;
    private Button btnGetQuestion, btnResetPassword, btnBackToLogin;
    private DatabaseHelper databaseHelper;

    private String currentUsername;
    private boolean questionRetrieved = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_forgot_password, container, false);

        databaseHelper = new DatabaseHelper(getContext());

        initializeViews(view);
        setupClickListeners();
        showUsernameStep();

        return view;
    }

    private void initializeViews(View view) {
        tvSecurityQuestion = view.findViewById(R.id.tvSecurityQuestion);
        cardSecurityQuestion = view.findViewById(R.id.cardSecurityQuestion);
        etUsername = view.findViewById(R.id.etUsername);
        etSecurityAnswer = view.findViewById(R.id.etSecurityAnswer);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);

        usernameLayout = view.findViewById(R.id.usernameLayout);
        securityAnswerLayout = view.findViewById(R.id.securityAnswerLayout);
        newPasswordLayout = view.findViewById(R.id.newPasswordLayout);
        confirmPasswordLayout = view.findViewById(R.id.confirmPasswordLayout);

        btnGetQuestion = view.findViewById(R.id.btnGetQuestion);
        btnResetPassword = view.findViewById(R.id.btnResetPassword);
        btnBackToLogin = view.findViewById(R.id.btnBackToLogin);
    }

    private void setupClickListeners() {
        btnGetQuestion.setOnClickListener(v -> getSecurityQuestion());
        btnResetPassword.setOnClickListener(v -> attemptPasswordReset());
        btnBackToLogin.setOnClickListener(v -> navigateBackToLogin());
    }

    private void showUsernameStep() {
        usernameLayout.setVisibility(View.VISIBLE);
        btnGetQuestion.setVisibility(View.VISIBLE);

        cardSecurityQuestion.setVisibility(View.GONE);
        securityAnswerLayout.setVisibility(View.GONE);
        newPasswordLayout.setVisibility(View.GONE);
        confirmPasswordLayout.setVisibility(View.GONE);
        btnResetPassword.setVisibility(View.GONE);
    }

    private void showRecoveryStep() {
        usernameLayout.setVisibility(View.GONE);
        btnGetQuestion.setVisibility(View.GONE);

        cardSecurityQuestion.setVisibility(View.VISIBLE);
        securityAnswerLayout.setVisibility(View.VISIBLE);
        newPasswordLayout.setVisibility(View.VISIBLE);
        confirmPasswordLayout.setVisibility(View.VISIBLE);
        btnResetPassword.setVisibility(View.VISIBLE);
    }

    private void getSecurityQuestion() {
        clearErrors();
        String username = getText(etUsername);

        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError("Please enter your username");
            etUsername.requestFocus();
            return;
        }

        String securityQuestion = databaseHelper.getSecurityQuestion(username);

        if (securityQuestion == null || securityQuestion.isEmpty()) {
            usernameLayout.setError("Username not found or no security question set");
            etUsername.requestFocus();
            Toast.makeText(getContext(), "Username not found or no security question was set for this account",
                    Toast.LENGTH_LONG).show();
            return;
        }

        currentUsername = username;
        questionRetrieved = true;
        tvSecurityQuestion.setText("Security Question: " + securityQuestion);
        showRecoveryStep();
        etSecurityAnswer.requestFocus();
    }

    private void attemptPasswordReset() {
        clearErrors();

        if (!questionRetrieved || currentUsername == null) {
            Toast.makeText(getContext(), "Please get your security question first", Toast.LENGTH_SHORT).show();
            return;
        }

        String securityAnswer = getText(etSecurityAnswer);
        String newPassword = getText(etNewPassword);
        String confirmPassword = getText(etConfirmPassword);

        if (!validateResetInputs(securityAnswer, newPassword, confirmPassword)) {
            return;
        }

        boolean success = databaseHelper.resetPasswordWithSecurity(currentUsername, securityAnswer, newPassword);

        if (success) {
            Toast.makeText(getContext(), "Password reset successfully! Please login with your new password.",
                    Toast.LENGTH_LONG).show();
            navigateBackToLogin();
        } else {
            securityAnswerLayout.setError("Incorrect security answer");
            etSecurityAnswer.requestFocus();
            Toast.makeText(getContext(), "Incorrect security answer. Please try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateResetInputs(String securityAnswer, String newPassword, String confirmPassword) {
        boolean isValid = true;
        View focusView = null;

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            focusView = etConfirmPassword;
            isValid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordLayout.setError("Please confirm your new password");
            focusView = etConfirmPassword;
            isValid = false;
        }

        if (TextUtils.isEmpty(newPassword)) {
            newPasswordLayout.setError("Please enter a new password");
            focusView = etNewPassword;
            isValid = false;
        } else if (newPassword.length() < 4) {
            newPasswordLayout.setError("Password must be at least 4 characters");
            focusView = etNewPassword;
            isValid = false;
        }

        if (TextUtils.isEmpty(securityAnswer)) {
            securityAnswerLayout.setError("Please answer the security question");
            focusView = etSecurityAnswer;
            isValid = false;
        }

        if (focusView != null) {
            focusView.requestFocus();
        }

        return isValid;
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void clearErrors() {
        usernameLayout.setError(null);
        securityAnswerLayout.setError(null);
        newPasswordLayout.setError(null);
        confirmPasswordLayout.setError(null);
    }

    private void navigateBackToLogin() {
        getParentFragmentManager().popBackStack();
    }
}