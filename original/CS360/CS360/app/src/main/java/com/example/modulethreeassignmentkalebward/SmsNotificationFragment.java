package com.example.modulethreeassignmentkalebward;

import android.Manifest;
import android.content.pm.PackageManager;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Fragment for managing SMS notification settings
 */
public class SmsNotificationFragment extends Fragment {

    private TextView tvPermissionStatus;
    private Button btnRequestPermission, btnSaveSettings, btnBack;
    private SwitchMaterial switchEnableNotifications;
    private TextInputEditText etPhoneNumber;
    private Slider sliderThreshold;
    private TextView tvThresholdValue;

    // Database helper
    private DatabaseHelper databaseHelper;
    private NotificationHelper notificationHelper;

    // User ID from arguments
    private long userId = -1;

    // Notification settings
    private boolean notificationsEnabled = false;
    private String phoneNumber = "";
    private int threshold = 5; // Default threshold value

    // Permission request code
    private static final int SMS_PERMISSION_REQUEST_CODE = 101;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_sms_notification, container, false);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(getContext());

        // Initialize notification helper
        notificationHelper = new NotificationHelper(getContext());

        // Get user ID from arguments
        if (getArguments() != null) {
            userId = getArguments().getLong("userId", -1);
        }

        // Initialize UI components
        tvPermissionStatus = view.findViewById(R.id.tvPermissionStatus);
        btnRequestPermission = view.findViewById(R.id.btnRequestPermission);
        btnSaveSettings = view.findViewById(R.id.btnSaveSettings);
        btnBack = view.findViewById(R.id.btnBack);
        switchEnableNotifications = view.findViewById(R.id.switchEnableNotifications);
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber);

        // Add threshold slider (you need to add this to your layout)
        sliderThreshold = view.findViewById(R.id.sliderThreshold);
        tvThresholdValue = view.findViewById(R.id.tvThresholdValue);

        // Set up threshold slider
        sliderThreshold.addOnChangeListener((slider, value, fromUser) -> {
            threshold = (int) value;
            tvThresholdValue.setText(getString(R.string.threshold_value, threshold));
        });

        // Load saved settings
        loadNotificationSettings();

        // Check if permission is already granted
        updatePermissionStatus();

        // Set click listener for the request permission button
        btnRequestPermission.setOnClickListener(v -> requestSmsPermission());

        // Set click listener for the save settings button
        btnSaveSettings.setOnClickListener(v -> saveSettings());

        // Set click listener for the back button
        btnBack.setOnClickListener(v -> {
            // Navigate back to the data display screen
            getParentFragmentManager().popBackStack();
        });

        // Set listener for notifications switch
        switchEnableNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !notificationHelper.hasSmsPermission()) {
                // Permission required for enabling notifications
                Toast.makeText(getContext(), R.string.permission_required_to_enable, Toast.LENGTH_SHORT).show();
                switchEnableNotifications.setChecked(false);
                btnRequestPermission.requestFocus();
            }
        });

        return view;
    }

    /**
     * Load saved notification settings from database
     */
    private void loadNotificationSettings() {
        if (userId != -1) {
            Object[] settings = databaseHelper.getNotificationSettings(userId);

            if (settings != null) {
                notificationsEnabled = (boolean) settings[0];
                phoneNumber = (String) settings[1] != null ? (String) settings[1] : "";
                threshold = (int) settings[2];

                // Update UI with loaded settings
                switchEnableNotifications.setChecked(notificationsEnabled);
                etPhoneNumber.setText(phoneNumber);
                sliderThreshold.setValue(threshold);
                tvThresholdValue.setText(getString(R.string.threshold_value, threshold));
            }
        }
    }

    // Method to update the permission status display
    private void updatePermissionStatus() {
        if (hasSmsPermission()) {
            tvPermissionStatus.setText(R.string.permission_granted);
            tvPermissionStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_success));
            btnRequestPermission.setEnabled(false);
        } else {
            tvPermissionStatus.setText(R.string.permission_required);
            tvPermissionStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_text));
            btnRequestPermission.setEnabled(true);

            // If notifications are enabled but permission denied, disable them
            if (switchEnableNotifications.isChecked()) {
                switchEnableNotifications.setChecked(false);
                notificationsEnabled = false;
            }
        }
    }

    // Method to check if SMS permission is granted
    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Method to request SMS permission
    private void requestSmsPermission() {
        requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                Toast.makeText(getContext(), R.string.permission_granted, Toast.LENGTH_SHORT).show();
                updatePermissionStatus();
            } else {
                // Permission denied
                Toast.makeText(getContext(), R.string.permission_denied, Toast.LENGTH_SHORT).show();
                updatePermissionStatus();
            }
        }
    }

    // Method to save notification settings
    private void saveSettings() {
        if (userId == -1) {
            Toast.makeText(getContext(), R.string.error_user_not_logged_in, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean notificationsEnabled = switchEnableNotifications.isChecked();
        String phoneNumber = etPhoneNumber.getText() != null ? etPhoneNumber.getText().toString().trim() : "";
        int threshold = (int) sliderThreshold.getValue();

        // Validate phone number if notifications are enabled
        if (notificationsEnabled) {
            if (!hasSmsPermission()) {
                Toast.makeText(getContext(), R.string.permission_required_to_enable, Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(getContext(), R.string.enter_phone_number, Toast.LENGTH_SHORT).show();
                etPhoneNumber.requestFocus();
                return;
            }
        }

        // Save to database
        int result = databaseHelper.saveNotificationSettings(userId, notificationsEnabled, phoneNumber, threshold);

        if (result > 0) {
            // Show confirmation message
            Toast.makeText(getContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show();

            // Send a test notification if enabled
            if (notificationsEnabled && hasSmsPermission() && !TextUtils.isEmpty(phoneNumber)) {
                notificationHelper.sendSmsNotification(
                        phoneNumber,
                        getString(R.string.test_notification_message, threshold));
            }
        } else {
            Toast.makeText(getContext(), R.string.error_saving_settings, Toast.LENGTH_SHORT).show();
        }
    }
}