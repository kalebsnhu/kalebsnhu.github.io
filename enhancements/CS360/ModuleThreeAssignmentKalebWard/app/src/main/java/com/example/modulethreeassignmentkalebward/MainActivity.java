package com.example.modulethreeassignmentkalebward;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/*
 * Class: MainActivity
 * Description: This class verifies SMS permission, creates the action bars, and requests SMS permission if needed
*/

public class MainActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_REQUEST_CODE = 101;

    /*
    * onCreate
    * @params()
        *bundle savedInstanceState
    * Description: Applies theme depending on theme chosen, verifies sliders and other action bars that are needed throughout.
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        if (savedInstanceState == null) {
            LoginFragment loginFragment = new LoginFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, loginFragment)
                    .commit();
        }
    }

    /*
    * checkSmsPermission
    * Description: Verfies whether or not the phone has granted SMS permission and whether it's valid
    */

    public boolean checkSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /*
    * requestSmsPermission
    * Description: Requests SMS permission for the inventory application
    */

    public void requestSmsPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.SEND_SMS},
                SMS_PERMISSION_REQUEST_CODE);
    }

    /*
    * onRequestPermissionsResult
    * @params()
        *int requestCode
        *string permissions
        *int grantResults
    * Description: Verifies permission results and verifies whether it's granted or denied.
    */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
