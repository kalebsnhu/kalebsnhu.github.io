package com.example.modulethreeassignmentkalebward;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

/*
 * Class: NotificationHelper
 * Description: This helper allows for various notification settings such as SMS permissions for notifications, notifies user
 * if a phone number is incorrect or invalid and whether or not SMS permissions has been granted.
 */

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context;
    }

    /*
    * hasSmsPermission
    * Description: Verifies whether or not user has granted SMS permission
    */

    public boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /*
    * sendSmsNotification
    * @params()
        *string phoneNumber
        *string message
    *Description: When SMS permission is granted it allows for SMS notifications, granted that the SMS number is valid.
    */

    public boolean sendSmsNotification(String phoneNumber, String message) {
        
        if (!hasSmsPermission()) {
            Log.d(TAG, "SMS permission not granted");
            return false;
        }

        
        if (phoneNumber == null || phoneNumber.isEmpty() || message == null || message.isEmpty()) {
            Log.d(TAG, "Invalid phone number or message");
            return false;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Log.d(TAG, "SMS notification sent to " + phoneNumber);

            
            Toast.makeText(context,
                    context.getString(R.string.sms_notification_sent),
                    Toast.LENGTH_SHORT).show();

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS: " + e.getMessage());

            
            Toast.makeText(context,
                    context.getString(R.string.error_sending_sms),
                    Toast.LENGTH_SHORT).show();

            return false;
        }
    }

    /*
    * formatLowInventoryMessage
    * @params()
        *string items
        *int threshold
    Description: Formats lowered inventory message, verifies it's correct and appends and returns a string depending on the inventory alert.
    */

    public String formatLowInventoryMessage(String[] items, int threshold) {
        if (items == null || items.length == 0) {
            return "";
        }

        StringBuilder message = new StringBuilder();
        message.append(context.getString(R.string.low_inventory_alert, threshold)).append(": ");

        for (int i = 0; i < items.length; i++) {
            message.append(items[i]);
            if (i < items.length - 1) {
                message.append(", ");
            }
        }

        return message.toString();
    }
}
