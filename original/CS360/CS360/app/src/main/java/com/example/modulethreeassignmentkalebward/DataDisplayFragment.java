package com.example.modulethreeassignmentkalebward;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DataDisplayFragment extends Fragment implements
        DataAdapter.OnDeleteClickListener,
        DataAdapter.OnEditClickListener,
        DataAdapter.OnQuantityChangeListener {

    private RecyclerView recyclerViewData;
    private FloatingActionButton btnAddData;
    private List<DataItem> dataItems;
    private DataAdapter dataAdapter;
    private String username = "Guest"; // Default value
    private long userId = -1; // Default value
    private DatabaseHelper databaseHelper;
    private NotificationHelper notificationHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_data_display, container, false);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(getContext());

        // Initialize notification helper
        notificationHelper = new NotificationHelper(getContext());

        // Get arguments passed from LoginFragment
        if (getArguments() != null) {
            username = getArguments().getString("username", "Guest");
            userId = getArguments().getLong("userId", -1);
        }

        // Initialize UI components
        recyclerViewData = view.findViewById(R.id.recyclerViewData);
        btnAddData = view.findViewById(R.id.btnAddData);
        Button btnSettings = view.findViewById(R.id.btnSettings);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        TextView tvLoggedInUser = view.findViewById(R.id.tvLoggedInUser);

        // Set the logged in username
        tvLoggedInUser.setText(getString(R.string.logged_in_as, username));

        // Initialize data items list and load from database
        loadInventoryData();

        // Set up the RecyclerView
        recyclerViewData.setLayoutManager(new LinearLayoutManager(getContext()));
        dataAdapter = new DataAdapter(dataItems, this, this, this);
        recyclerViewData.setAdapter(dataAdapter);

        // Set content description for RecyclerView to improve accessibility
        recyclerViewData.setContentDescription(getString(R.string.inventory_items_list));

        // Set accessibility announcement when data changes
        dataAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateRecyclerViewAccessibility();
                checkLowInventoryItems();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                updateRecyclerViewAccessibility();
                checkLowInventoryItems();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                updateRecyclerViewAccessibility();
            }
        });

        // Initial setup of accessibility info
        updateRecyclerViewAccessibility();

        // Set click listener for the add button
        btnAddData.setOnClickListener(v -> showAddDataDialog());

        // Set click listener for the settings button
        btnSettings.setOnClickListener(v -> navigateToSmsNotification());

        // Set click listener for the logout button
        btnLogout.setOnClickListener(v -> logout());

        return view;
    }

    @Override
    public void onQuantityChanged(int position, String newQuantity) {
        if (position >= 0 && position < dataItems.size()) {
            DataItem item = dataItems.get(position);

            // Update quantity in the local object
            item.setQuantity(newQuantity);

            // Update in database if this item has a valid ID
            if (item.getId() != -1) {
                databaseHelper.updateInventoryItem(item.getId(), item.getName(), newQuantity, item.getDate());

                // Check for low inventory
                checkLowInventoryItems();
            }
        }
    }

    private void loadInventoryData() {
        if (userId != -1) {
            dataItems = databaseHelper.getAllInventoryItems(userId);

            // If no items were found in database, initialize an empty list
            if (dataItems == null) {
                dataItems = new ArrayList<>();
            }
        } else {
            // No user ID, just create an empty list
            dataItems = new ArrayList<>();
        }
    }

    private void checkLowInventoryItems() {
        if (userId != -1) {
            // Get notification settings
            Object[] settings = databaseHelper.getNotificationSettings(userId);

            if (settings != null) {
                boolean enabled = (boolean) settings[0];
                String phoneNumber = (String) settings[1];
                int threshold = (int) settings[2];

                // If notifications are enabled and we have a phone number
                if (enabled && phoneNumber != null && !phoneNumber.isEmpty()) {
                    // Get items below threshold
                    List<String> lowItems = databaseHelper.getItemsBelowThreshold(userId, threshold);

                    if (!lowItems.isEmpty()) {
                        // Construct message for low inventory items
                        StringBuilder message = new StringBuilder("Low inventory alert: ");
                        for (int i = 0; i < lowItems.size(); i++) {
                            message.append(lowItems.get(i));
                            if (i < lowItems.size() - 1) {
                                message.append(", ");
                            }
                        }

                        // Send notification
                        notificationHelper.sendSmsNotification(phoneNumber, message.toString());
                    }
                }
            }
        }
    }

    private void updateRecyclerViewAccessibility() {
        if (recyclerViewData != null && dataItems != null) {
            if (dataItems.isEmpty()) {
                recyclerViewData.setContentDescription(getString(R.string.empty_inventory_list));
            } else {
                recyclerViewData.setContentDescription(
                        getString(R.string.inventory_items_count, dataItems.size())
                );
            }
            // Announce to screen readers that the content has changed
            recyclerViewData.announceForAccessibility(recyclerViewData.getContentDescription());
        }
    }

    private void logout() {
        // Clear login session
        LoginFragment.clearLoginSession(requireContext());

        // Navigate back to login screen
        LoginFragment loginFragment = new LoginFragment();
        // Get the fragment manager and begin a transaction
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        // Replace the current fragment with the login fragment
        transaction.replace(R.id.fragment_container, loginFragment);
        // Clear the back stack so user can't go back after logout
        transaction.setReorderingAllowed(true);
        transaction.disallowAddToBackStack();
        // Commit the transaction
        transaction.commit();

        // Show a toast message to confirm logout
        Toast.makeText(getContext(), R.string.logout_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(int position) {
        deleteItem(position);
    }

    @Override
    public void onEditClick(int position) {
        editItem(position);
    }

    private void showAddDataDialog() {
        // Create dialog view
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_data, null);
        builder.setView(dialogView);

        // Get references to UI components
        final TextInputEditText etField1 = dialogView.findViewById(R.id.etField1);
        final TextInputEditText etField2 = dialogView.findViewById(R.id.etField2);
        final TextInputEditText etField3 = dialogView.findViewById(R.id.etField3);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Setup date picker for field 3
        etField3.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Format date as m/d/y
                        String date = String.format(Locale.getDefault(),
                                "%d/%d/%d",
                                selectedMonth + 1,
                                selectedDay,
                                selectedYear % 100);
                        etField3.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Create and show dialog
        final AlertDialog dialog = builder.create();
        dialog.show();

        // Set click listeners for buttons
        btnSave.setOnClickListener(v -> {
            String field1 = etField1.getText() != null ? etField1.getText().toString() : "";
            String field2 = etField2.getText() != null ? etField2.getText().toString() : "";
            String field3 = etField3.getText() != null ? etField3.getText().toString() : "";

            if (field1.isEmpty() || field2.isEmpty() || field3.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add item to database
            if (userId != -1) {
                long itemId = databaseHelper.addInventoryItem(field1, field2, field3, userId);

                if (itemId != -1) {
                    // Create DataItem with the new data
                    DataItem newItem = new DataItem(field1, field2, field3);
                    newItem.setId(itemId);

                    // Add to our list and update display
                    dataItems.add(0, newItem);  // Add at the beginning
                    dataAdapter.notifyItemInserted(0);
                    recyclerViewData.scrollToPosition(0);  // Scroll to show new item

                    dialog.dismiss();
                    Toast.makeText(getContext(), R.string.item_added, Toast.LENGTH_SHORT).show();

                    // Announce the addition of a new item for accessibility
                    recyclerViewData.announceForAccessibility(getString(R.string.item_added));
                } else {
                    Toast.makeText(getContext(), R.string.error_adding_item, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), R.string.error_user_not_logged_in, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void editItem(int position) {
        DataItem item = dataItems.get(position);

        // Create dialog view
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_data, null);
        builder.setView(dialogView);

        // Set dialog title
        TextView dialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        dialogTitle.setText(R.string.edit_item);

        // Get references to UI components
        final TextInputEditText etField1 = dialogView.findViewById(R.id.etField1);
        final TextInputEditText etField2 = dialogView.findViewById(R.id.etField2);
        final TextInputEditText etField3 = dialogView.findViewById(R.id.etField3);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Set current values
        etField1.setText(item.getName());
        etField2.setText(item.getQuantity());
        etField3.setText(item.getDate());

        // Change button text
        btnSave.setText(R.string.update);

        // Setup date picker for field 3
        etField3.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Format date as m/d/y
                        String date = String.format(Locale.getDefault(),
                                "%d/%d/%d",
                                selectedMonth + 1,
                                selectedDay,
                                selectedYear % 100);
                        etField3.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Create and show dialog
        final AlertDialog dialog = builder.create();
        dialog.show();

        // Set click listeners for buttons
        btnSave.setOnClickListener(v -> {
            String field1 = etField1.getText() != null ? etField1.getText().toString() : "";
            String field2 = etField2.getText() != null ? etField2.getText().toString() : "";
            String field3 = etField3.getText() != null ? etField3.getText().toString() : "";

            if (field1.isEmpty() || field2.isEmpty() || field3.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update item in database if it has a valid ID
            if (item.getId() != -1) {
                int result = databaseHelper.updateInventoryItem(item.getId(), field1, field2, field3);

                if (result > 0) {
                    // Update item data locally
                    item.setName(field1);
                    item.setQuantity(field2);
                    item.setDate(field3);

                    dataAdapter.notifyItemChanged(position);
                    dialog.dismiss();
                    Toast.makeText(getContext(), R.string.item_updated, Toast.LENGTH_SHORT).show();

                    // Announce the update for accessibility
                    recyclerViewData.announceForAccessibility(getString(R.string.item_updated));
                } else {
                    Toast.makeText(getContext(), R.string.error_updating_item, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), R.string.error_item_not_in_database, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void deleteItem(int position) {
        DataItem item = dataItems.get(position);

        // Confirm deletion with dialog
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_message, item.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    // Delete from database if it has a valid ID
                    if (item.getId() != -1) {
                        int result = databaseHelper.deleteInventoryItem(item.getId());

                        if (result > 0) {
                            // Remove from local list
                            dataItems.remove(position);
                            dataAdapter.notifyItemRemoved(position);
                            dataAdapter.notifyItemRangeChanged(position, dataItems.size());
                            Toast.makeText(getContext(), R.string.item_deleted, Toast.LENGTH_SHORT).show();

                            // Announce the deletion for accessibility
                            recyclerViewData.announceForAccessibility(getString(R.string.item_deleted));
                        } else {
                            Toast.makeText(getContext(), R.string.error_deleting_item, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Item not in database but still in our view - remove it
                        dataItems.remove(position);
                        dataAdapter.notifyItemRemoved(position);
                        dataAdapter.notifyItemRangeChanged(position, dataItems.size());
                        Toast.makeText(getContext(), R.string.item_deleted, Toast.LENGTH_SHORT).show();

                        // Announce the deletion for accessibility
                        recyclerViewData.announceForAccessibility(getString(R.string.item_deleted));
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void navigateToSmsNotification() {
        SmsNotificationFragment smsNotificationFragment = new SmsNotificationFragment();

        // Pass user ID to notification fragment
        Bundle args = new Bundle();
        args.putLong("userId", userId);
        smsNotificationFragment.setArguments(args);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, smsNotificationFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}