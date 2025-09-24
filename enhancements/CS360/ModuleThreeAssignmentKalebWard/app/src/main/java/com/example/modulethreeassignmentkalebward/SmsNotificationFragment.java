package com.example.modulethreeassignmentkalebward;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

/*
 * Class: SmsNotificationFragment:
 * Description: This class is used for the SMS notifications, this has been changed from notifications to settings but
 * it was originally for SMS notifications. You can now swap between themes and chose either SYSTEM, dark, or light mode.
 */

public class SmsNotificationFragment extends Fragment {

    private TextView tvPermissionStatus;
    private Button btnRequestPermission, btnSaveSettings, btnBack;
    private SwitchMaterial switchEnableNotifications;
    private TextInputEditText etPhoneNumber;
    private Slider sliderThreshold;
    private TextView tvThresholdValue;

    
    private RadioGroup radioGroupTheme;
    private RadioButton rbLight, rbDark, rbSystem;

    private DatabaseHelper databaseHelper;
    private NotificationHelper notificationHelper;
    private SharedPreferences preferences;

    private long userId = -1;

    private boolean notificationsEnabled = false;
    private String phoneNumber = "";
    private int threshold = 5;

    private static final int SMS_PERMISSION_REQUEST_CODE = 101;
    private static final String PREF_NAME = "user_session";
    private static final String THEME_PREF = "app_theme";
    private static final String THEME_LIGHT = "light";
    private static final String THEME_DARK = "dark";
    private static final String THEME_SYSTEM = "system";

    /*
    * View onCreateView:
    * @params():
        * @LayoutInflater inflater
        * @ViewGroup container
        * @Bundle savedInstanceState
    * Description: This creates the views that initialize the themes and loads the theme settings depending on what
    * is previously selected, it can be changed between SYSTEM, dark, and light themes and it also reads the theme
    * selected so the theme is consistent between settings and what is previously selected.
    */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_sms_notification, container, false);

        databaseHelper = new DatabaseHelper(getContext());
        notificationHelper = new NotificationHelper(getContext());
        preferences = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        if (getArguments() != null) {
            userId = getArguments().getLong("userId", -1);
        }

        initializeViews(view);
        setupThemeListeners();
        loadNotificationSettings();
        loadThemeSettings();
        updatePermissionStatus();

        setupClickListeners();

        return view;
    }

    /*
    * initializeViews:
    * Description: Initalizes the views for themes, SMS settings, and phone number previously added.
    */

    private void initializeViews(View view) {
        
        radioGroupTheme = view.findViewById(R.id.radioGroupTheme);
        rbLight = view.findViewById(R.id.rbLight);
        rbDark = view.findViewById(R.id.rbDark);
        rbSystem = view.findViewById(R.id.rbSystem);

        
        tvPermissionStatus = view.findViewById(R.id.tvPermissionStatus);
        btnRequestPermission = view.findViewById(R.id.btnRequestPermission);
        btnSaveSettings = view.findViewById(R.id.btnSaveSettings);
        btnBack = view.findViewById(R.id.btnBack);
        switchEnableNotifications = view.findViewById(R.id.switchEnableNotifications);
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber);
        sliderThreshold = view.findViewById(R.id.sliderThreshold);
        tvThresholdValue = view.findViewById(R.id.tvThresholdValue);

        
        sliderThreshold.addOnChangeListener((slider, value, fromUser) -> {
            threshold = (int) value;
            tvThresholdValue.setText(getString(R.string.threshold_value, threshold));
        });
    }

    /*
    * setupThemeListeners:
    * Description: Setup theme listeners so we can verify what themes are saved and chosen 
    * previously, it will apply themes when it's saved or chosen.
    */

    private void setupThemeListeners() {
        radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedTheme = getSelectedTheme();
            saveThemePreference(selectedTheme);
            applyThemeChange(selectedTheme);
        });
    }

    /*
    * setupClickListeners:
    * Description: Setup listeners for clicking when changing SMS permissions and saving permissions
    * when changed. It will focus on the chosen selection so the user can preference as needed.
    */

    private void setupClickListeners() {
        btnRequestPermission.setOnClickListener(v -> requestSmsPermission());
        btnSaveSettings.setOnClickListener(v -> saveAllSettings());
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        switchEnableNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !notificationHelper.hasSmsPermission()) {
                Toast.makeText(getContext(), "SMS permission is required to enable notifications", Toast.LENGTH_SHORT).show();
                switchEnableNotifications.setChecked(false);
                btnRequestPermission.requestFocus();
            }
        });
    }

    /*
     * loadThemeSettings:
     * Description: Sets up listeners to verify clicks clicked within the application, it will load the
     * user profile as needed and the theme that is saved previously.
     */

    private void loadThemeSettings() {
        if (userId != -1) {
            DatabaseHelper.UserProfile profile = databaseHelper.getUserProfile(userId);
            if (profile != null && profile.themePreference != null) {
                setThemeRadioButton(profile.themePreference);
                return;
            }
        }

        // Default to saved theme preference
        String savedTheme = preferences.getString(THEME_PREF, THEME_LIGHT);
        setThemeRadioButton(savedTheme);
    }

    /*
    * setThemeRadioButton:
    * Description: This is the settings for the themes, you can chose between DARK, SYSTEM, or LIGHT which is the default.
    */
    
    private void setThemeRadioButton(String theme) {
        switch (theme) {
            case THEME_DARK:
                rbDark.setChecked(true);
                break;
            case THEME_SYSTEM:
                rbSystem.setChecked(true);
                break;
            default:
                rbLight.setChecked(true);
                break;
        }
    }

    /*
    * getSelectedTheme:
    * Description: Gets the selected theme and verifying what theme is selected.
    */

    private String getSelectedTheme() {
        if (rbDark.isChecked()) return THEME_DARK;
        if (rbSystem.isChecked()) return THEME_SYSTEM;
        return THEME_LIGHT;
    }

    /*
    * applyThemeChange:
    * Description: Applies selected theme and applies it to the application.
    */

    private void applyThemeChange(String theme) {
        ThemeManager.saveTheme(getContext(), theme);
    }

    /*
    * saveThemePreference:
    * Description: Saves theme preference, sets the string as app_theme DARK/SYSTEM/LIGHT depending on
    * what is chosen previously, it grabs from the userprofile.
    */

    private void saveThemePreference(String theme) {
        if (userId != -1) {
            databaseHelper.updateUserProfile(userId, null, null, null, null, theme);
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("app_theme", theme);
        editor.apply();
    }

    /*
    * loadNotificationSettings:
    * Description: Loads the notification settings, verifies settings previously set up, it sets the values set
    * to the application previously such as phone number.
    */

    private void loadNotificationSettings() {
        if (userId != -1) {
            Object[] settings = databaseHelper.getNotificationSettings(userId);

            if (settings != null) {
                notificationsEnabled = (boolean) settings[0];
                phoneNumber = (String) settings[1] != null ? (String) settings[1] : "";
                threshold = (int) settings[2];

                switchEnableNotifications.setChecked(notificationsEnabled);
                etPhoneNumber.setText(phoneNumber);
                sliderThreshold.setValue(threshold);
                tvThresholdValue.setText(getString(R.string.threshold_value, threshold));
            }
        }
    }

    /*
    * updatePermissionStatus:
    * Description: Updates permission status depending on whether or not access has been allowed
    */

    private void updatePermissionStatus() {
        if (hasSmsPermission()) {
            tvPermissionStatus.setText("SMS permission granted");
            tvPermissionStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_success));
            btnRequestPermission.setEnabled(false);
        } else {
            tvPermissionStatus.setText("SMS permission is required");
            tvPermissionStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_text));
            btnRequestPermission.setEnabled(true);

            if (switchEnableNotifications.isChecked()) {
                switchEnableNotifications.setChecked(false);
                notificationsEnabled = false;
            }
        }
    }

    /*
    * hasSmsPermission:
    * Description: Provides a boolean on whether or not SMS permission has been granted
    */

    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /*
    * requestSmsPermission:
    * Description: Requests permissions for SMS notifications and verifying with the user if they want to grant.
    */

    private void requestSmsPermission() {
        requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
    }

    /*
    * requestSmsPermission:
    * @params()
        *int requestCode
        *string permissions
        *int grantResults
    * Description: It requests permissions and sends it back to the user for verification, it allows for SMS permission and notifies
    * whether or not the user has SMS permission. 
    */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "SMS permission granted", Toast.LENGTH_SHORT).show();
                updatePermissionStatus();
            } else {
                Toast.makeText(getContext(), "SMS permission denied", Toast.LENGTH_SHORT).show();
                updatePermissionStatus();
            }
        }
    }

    /*
    * saveAllSettings:
    * Description: Saves all settings such as notification settings and theme perferences.
    */

    private void saveAllSettings() {
        
        String selectedTheme = getSelectedTheme();
        saveThemePreference(selectedTheme);

        
        saveNotificationSettings();
    }

    /*
    * saveNotificationSettings:
    * Description: Saves notification settings and provides whether or not the user has provided access.
    */

    private void saveNotificationSettings() {
        if (userId == -1) {
            Toast.makeText(getContext(), "Error: User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean notificationsEnabled = switchEnableNotifications.isChecked();
        String phoneNumber = etPhoneNumber.getText() != null ? etPhoneNumber.getText().toString().trim() : "";
        int threshold = (int) sliderThreshold.getValue();

        if (notificationsEnabled) {
            if (!hasSmsPermission()) {
                Toast.makeText(getContext(), "SMS permission is required to enable notifications", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(getContext(), "Please enter a phone number", Toast.LENGTH_SHORT).show();
                etPhoneNumber.requestFocus();
                return;
            }
        }

        int result = databaseHelper.saveNotificationSettings(userId, notificationsEnabled, phoneNumber, threshold);

        if (result > 0) {
            Toast.makeText(getContext(), "Settings saved successfully!", Toast.LENGTH_SHORT).show();

            if (notificationsEnabled && hasSmsPermission() && !TextUtils.isEmpty(phoneNumber)) {
                String testMessage = "This is a test notification. You will receive alerts when inventory items drop to " + threshold + " or below.";
                notificationHelper.sendSmsNotification(phoneNumber, testMessage);
            }
        } else {
            Toast.makeText(getContext(), "Error saving settings", Toast.LENGTH_SHORT).show();
        }
    }
}
